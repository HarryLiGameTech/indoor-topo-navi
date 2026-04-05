package corelang

enum TypeCheckResult {
  case Ok(tpe: Type)
  case Err(errors: List[TypeCheckError])

  def isOk: Boolean = this match { case Ok(_) => true; case _ => false }
  def isErr: Boolean = !isOk

  def get: Type = this match {
    case Ok(t)  => t
    case Err(es) => throw new TypeCheckException(es)
  }

  def and(other: => TypeCheckResult): TypeCheckResult = (this, other) match {
    case (Ok(_), Ok(_))     => other
    case (Ok(_), e: Err)    => e
    case (e: Err, Ok(_))    => e
    case (Err(e1), Err(e2)) => Err(e1 ++ e2)
  }

  def map(f: Type => Type): TypeCheckResult = this match {
    case Ok(t)  => Ok(f(t))
    case e: Err => e
  }
}

case class TypeCheckError(message: String, context: Option[String] = None) {
  override def toString: String = context match {
    case Some(ctx) => s"[TypeCheckError] $message (in $ctx)"
    case None      => s"[TypeCheckError] $message"
  }
}

class TypeCheckException(val errors: List[TypeCheckError])
    extends RuntimeException(errors.map(_.toString).mkString("\n"))

object TypeChecker {

  def check(
      term: Term,
      env: TypeOnlyEnv = TypeEnvironment.empty[Identifier, Type],
  ): TypeCheckResult = infer(term, env)

  private def infer(term: Term, env: TypeOnlyEnv): TypeCheckResult = term match {

    case Term.Var(index) =>
      env.getType(Identifier.Index(index)) match {
        case Some(tpe) => TypeCheckResult.Ok(tpe)
        case None =>
          TypeCheckResult.Err(List(TypeCheckError(
            s"Variable index $index is unbound (environment has ${env.types.size} bindings)"
          )))
      }

    case Term.Sym(ident) =>
      env.getType(Identifier.Symbol(ident)) match {
        case Some(tpe) => TypeCheckResult.Ok(tpe)
        case None      => TypeCheckResult.Err(List(TypeCheckError(s"Unbound symbol: '$ident'")))
      }

    case Term.Lam(paramType, body) =>
      val extEnv = paramType :: env
      infer(body, extEnv).map(retType => Type.Arrow(paramType, retType))

    case Term.App(fn, arg) =>
      infer(fn, env) match {
        case TypeCheckResult.Err(es) => TypeCheckResult.Err(es)
        case TypeCheckResult.Ok(fnTpe) =>
          fnTpe match {
            case Type.Arrow(paramTpe, retTpe) =>
              infer(arg, env) match {
                case TypeCheckResult.Err(es) => TypeCheckResult.Err(es)
                case TypeCheckResult.Ok(argTpe) =>
                  if (argTpe == paramTpe)
                    TypeCheckResult.Ok(retTpe)
                  else
                    TypeCheckResult.Err(List(TypeCheckError(
                      s"Function application type mismatch: expected argument of type $paramTpe, got $argTpe"
                    )))
              }
            case other =>
              TypeCheckResult.Err(List(TypeCheckError(
                s"Cannot apply a non-function: expression has type $other"
              )))
          }
      }

    case Term.IntLit(_)    => TypeCheckResult.Ok(Type.IntType)
    case Term.FloatLit(_)  => TypeCheckResult.Ok(Type.FloatType)
    case Term.BoolLit(_)   => TypeCheckResult.Ok(Type.BoolType)
    case Term.StringLit(_) => TypeCheckResult.Ok(Type.StringType)

    case Term.Proposition(predicates) =>
      val errors = predicates.toList.flatMap { pred =>
        infer(pred, env) match {
          case TypeCheckResult.Err(es) => es
          case TypeCheckResult.Ok(_)   => Nil
        }
      }
      if (errors.isEmpty) TypeCheckResult.Ok(Type.PropositionType)
      else TypeCheckResult.Err(errors)

    case Term.ListLit(annotatedTpe, elements) =>
      elements match {
        case Nil =>
          annotatedTpe match {
            case Some(tpe) => TypeCheckResult.Ok(Type.ListType(tpe))
            case None =>
              TypeCheckResult.Err(List(TypeCheckError(
                "Cannot infer element type of an empty list without a type annotation"
              )))
          }
        case _ =>
          val inferredResults = elements.map(e => infer(e, env))
          val errors = inferredResults.collect { case TypeCheckResult.Err(es) => es }.flatten
          if (errors.nonEmpty) return TypeCheckResult.Err(errors)

          val inferredTypes = inferredResults.collect { case TypeCheckResult.Ok(t) => t }
          val distinct = inferredTypes.distinct

          if (distinct.size > 1)
            return TypeCheckResult.Err(List(TypeCheckError(
              s"List elements have inconsistent types: ${distinct.mkString(", ")}"
            )))

          val elemType = distinct.head
          annotatedTpe match {
            case Some(ann) if ann != elemType =>
              TypeCheckResult.Err(List(TypeCheckError(
                s"List annotation $ann does not match inferred element type $elemType"
              )))
            case _ =>
              TypeCheckResult.Ok(Type.ListType(elemType))
          }
      }

    case Term.BinOp(kind, lhs, rhs) =>
      (infer(lhs, env), infer(rhs, env)) match {
        case (TypeCheckResult.Err(e1), TypeCheckResult.Err(e2)) => TypeCheckResult.Err(e1 ++ e2)
        case (TypeCheckResult.Err(es), _)                       => TypeCheckResult.Err(es)
        case (_, TypeCheckResult.Err(es))                       => TypeCheckResult.Err(es)
        case (TypeCheckResult.Ok(lt), TypeCheckResult.Ok(rt))   => checkBinOp(kind, lt, rt)
      }

    case Term.If(cond, thenBr, elseBr) =>
      val condResult = infer(cond, env)
      val thenResult = infer(thenBr, env)
      val elseResult = infer(elseBr, env)

      val subErrors = List(condResult, thenResult, elseResult).collect {
        case TypeCheckResult.Err(es) => es
      }.flatten
      if (subErrors.nonEmpty) return TypeCheckResult.Err(subErrors)

      val condType = condResult.get
      val thenType = thenResult.get
      val elseType = elseResult.get

      val condError =
        if (condType != Type.BoolType)
          List(TypeCheckError(s"If-condition must be Bool, got $condType"))
        else Nil

      val branchError =
        if (thenType != elseType)
          List(TypeCheckError(s"If-branches have different types: then is $thenType, else is $elseType"))
        else Nil

      val allErrors = condError ++ branchError
      if (allErrors.nonEmpty) TypeCheckResult.Err(allErrors) else TypeCheckResult.Ok(thenType)

    case Term.Fix(annotatedType, body) =>
      // Fix introduces a self-reference at De Bruijn index 0 of type `annotatedType`.
      // The body must therefore be a function `annotatedType -> annotatedType`.
      val extEnv = annotatedType :: env
      infer(body, extEnv) match {
        case TypeCheckResult.Err(es) => TypeCheckResult.Err(es)
        case TypeCheckResult.Ok(bodyType) =>
          val expected = Type.Arrow(annotatedType, annotatedType)
          if (bodyType != expected)
            TypeCheckResult.Err(List(TypeCheckError(
              s"Fix body must have type $expected, but got $bodyType"
            )))
          else
            TypeCheckResult.Ok(annotatedType)
      }

    case Term.Record(fields) =>
      val (fieldErrors, fieldTypes) = fields.toList.map { case (name, term) =>
        infer(term, env) match {
          case TypeCheckResult.Ok(t)   => (Nil, Some(name -> t))
          case TypeCheckResult.Err(es) =>
            (es.map(e => TypeCheckError(e.message, Some(s"field '$name'"))), None)
        }
      }.unzip

      val allErrors = fieldErrors.flatten
      if (allErrors.nonEmpty)
        TypeCheckResult.Err(allErrors)
      else
        TypeCheckResult.Ok(Type.RecordType(fieldTypes.flatten.toMap))

    case Term.Proj(record, field) =>
      infer(record, env) match {
        case TypeCheckResult.Err(es) => TypeCheckResult.Err(es)
        case TypeCheckResult.Ok(tpe) =>
          tpe match {
            case Type.RecordType(fields) =>
              fields.get(field) match {
                case Some(fieldType) => TypeCheckResult.Ok(fieldType)
                case None =>
                  TypeCheckResult.Err(List(TypeCheckError(
                    s"Record has no field '$field'. Available fields: ${fields.keys.mkString(", ")}"
                  )))
              }
            case other =>
              TypeCheckResult.Err(List(TypeCheckError(
                s"Cannot project field '$field' from non-record type $other"
              )))
          }
      }

    case Term.EnumLit(enumType, variant) =>
      if (enumType.variants.contains(variant))
        TypeCheckResult.Ok(enumType)
      else
        TypeCheckResult.Err(List(TypeCheckError(
          s"Variant '$variant' is not a member of enum '${enumType.name}'. " +
          s"Known variants: ${enumType.variants.mkString(", ")}"
        )))

    case Term.Match(scrutinee, cases) =>
      infer(scrutinee, env) match {
        case TypeCheckResult.Err(es) => TypeCheckResult.Err(es)
        case TypeCheckResult.Ok(scrutineeTpe) =>
          scrutineeTpe match {
            case Type.EnumType(enumName, variants) =>
              if (cases.isEmpty)
                return TypeCheckResult.Err(List(TypeCheckError(
                  "Match expression must have at least one case"
                )))

              val caseVariants = cases.map(_._1).toSet
              val unknown = caseVariants -- variants
              val unknownErrors =
                if (unknown.nonEmpty)
                  List(TypeCheckError(
                    s"Match on '$enumName' has unknown variants: ${unknown.mkString(", ")}. " +
                    s"Known: ${variants.mkString(", ")}"
                  ))
                else Nil

              val duplicates = cases.map(_._1).groupBy(identity).collect {
                case (v, vs) if vs.size > 1 => v
              }.toSet
              val dupErrors =
                if (duplicates.nonEmpty)
                  List(TypeCheckError(
                    s"Match on '$enumName' has duplicate cases: ${duplicates.mkString(", ")}"
                  ))
                else Nil

              val branchResults = cases.map { case (variant, expr) => variant -> infer(expr, env) }
              val branchErrors = branchResults.collect {
                case (v, TypeCheckResult.Err(es)) =>
                  es.map(e => TypeCheckError(e.message, Some(s"case '$v'")))
              }.flatten

              val allErrors = unknownErrors ++ dupErrors ++ branchErrors
              if (allErrors.nonEmpty) return TypeCheckResult.Err(allErrors)

              val branchTypes = branchResults.map(_._2.get)
              val distinctTypes = branchTypes.distinct
              if (distinctTypes.size > 1)
                TypeCheckResult.Err(List(TypeCheckError(
                  s"Match branches have inconsistent types: ${distinctTypes.mkString(", ")}"
                )))
              else
                TypeCheckResult.Ok(distinctTypes.head)

            case other =>
              TypeCheckResult.Err(List(TypeCheckError(
                s"Cannot match on non-enum type: $other"
              )))
          }
      }
  }

  private def checkBinOp(kind: OpKind, lt: Type, rt: Type): TypeCheckResult =
    (kind, lt, rt) match {
      case (OpKind.Neg, Type.IntType, _)   => TypeCheckResult.Ok(Type.IntType)
      case (OpKind.Neg, Type.FloatType, _) => TypeCheckResult.Ok(Type.FloatType)
      case (OpKind.Neg, other, _) =>
        TypeCheckResult.Err(List(TypeCheckError(s"Operator '-' requires Int or Float, got $other")))

      case (OpKind.Add, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.IntType)
      case (OpKind.Sub, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.IntType)
      case (OpKind.Mul, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.IntType)
      case (OpKind.Add, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.FloatType)
      case (OpKind.Sub, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.FloatType)
      case (OpKind.Mul, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.FloatType)

      case (OpKind.Eq, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Lt, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Gt, Type.IntType, Type.IntType)     => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Eq, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Lt, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Gt, Type.FloatType, Type.FloatType) => TypeCheckResult.Ok(Type.BoolType)
      case (OpKind.Eq, Type.BoolType, Type.BoolType)   => TypeCheckResult.Ok(Type.BoolType)

      case (OpKind.Concat, Type.StringType, Type.StringType) => TypeCheckResult.Ok(Type.StringType)

      case (OpKind.Concat, Type.ListType(l), Type.ListType(r)) if l == r =>
        TypeCheckResult.Ok(Type.ListType(l))
      case (OpKind.Concat, Type.ListType(l), Type.ListType(r)) =>
        TypeCheckResult.Err(List(TypeCheckError(
          s"List concatenation requires matching element types, got List[$l] ++ List[$r]"
        )))

      case (op, l, r) =>
        TypeCheckResult.Err(List(TypeCheckError(
          s"Operator '$op' cannot be applied to types $l and $r"
        )))
    }
}
