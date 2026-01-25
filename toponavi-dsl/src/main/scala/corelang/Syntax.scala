package corelang

import corelang.Identifier.Index
import corelang.Value
import corelang.{Environment, Identified, TypeEnvironment, TypeOnlyEnvironment}

enum Identifier {
  case Index(index: Int)
  case Symbol(name: String)
}

object Identifier {
  given Conversion[String, Identifier] with {
    def apply(s: String): Identifier = Identifier(s)
  }

  def apply(s: String): Identifier = s.toIntOption match {
    case Some(i) => Identifier.Index(i)
    case None => Identifier.Symbol(s)
  }
}

type TypeOnlyEnv = TypeOnlyEnvironment[Identifier, Type]
type TypeEnv = TypeEnvironment[Identifier, Type]
type Env = Environment[Identifier, Type, Value]

enum Type extends Identified[Identifier] {
  case IntType
  case FloatType
  case BoolType
  case StringType
  case PropositionType // used for constraints
  case ListType(elemType: Type)
  case Arrow(from: Type, to: Type) // a.k.a. function
  case RecordType(fields: Map[String, Type])
  case EnumType(name: String, variants: Set[String]) // Plain enum with named variants
}

enum OpKind {
  case Add, Sub, Mul, Eq, Lt, Gt, Concat

  override def toString: String = this match {
    case Add => "+"
    case Concat => "++"
    case Sub => "-"
    case Mul => "*"
    case Eq  => "=="
    case Lt  => "<"
    case Gt  => ">"
  }
}

enum Expr extends Identified[Identifier] {
  case Var(name: String)
  case Lam(param: String, tpe: Type, body: Expr)
  case App(fn: Expr, arg: Expr)
  case IntLit(value: Long)
  case FloatLit(value: Double)
  case BoolLit(value: Boolean)
  case StringLit(value: String)
  case Proposition(predicates: Set[Expr])
  case ListLit(tpe: Option[Type], elements: List[Expr])
  case BinOp(kind: OpKind, lhs: Expr, rhs: Expr)
  case If(cond: Expr, thenBr: Expr, elseBr: Expr)
  case Fix(name: String, tpe: Type, body: Expr)
  case Record(fields: Map[String, Expr])
  case Proj(record: Expr, field: String)
  case Let(name: String, value: Expr, body: Expr)
  case LetRec(name: String, tpe: Type, value: Expr, body: Expr)
  case EnumLit(enumType: String, variant: String) // Enum literal: MyEnum.Variant
  case Path(ids: List[String]) // Path identifier: root::sub::leaf
  case Match(scrutinee: Expr, cases: List[(String, Expr)]) // Pattern match on enum

  /**
   * Convert this expression (with names) to a Term (with De Bruijn indices).
   * @param stack binding stack; head = most-recent binding
   */
  def toTerm(typeEnv: TypeEnv, stack: List[String] = Nil): Term = {

    def lookup(ident: String, ctx: List[String]): Term = {
      ctx.indexOf(ident) match {
        case -1    => Term.Sym(ident)
        case index => Term.Var(index)
      }
    }

    this match {
      case Expr.Path(ids) =>
        ids match {
          case Nil => throw new RuntimeException("Path cannot be empty")
          case head :: tail =>
             // Try to resolve as Enum first (if length is 2)
             val enumTry = if (tail.size == 1) {
               typeEnv.getType(Identifier.Symbol(head)) match {
                 case Some(et: Type.EnumType) if et.variants.contains(tail.head) =>
                   Some(Term.EnumLit(et, tail.head))
                 case _ => None
               }
             } else None

             enumTry.getOrElse {
               // Otherwise, treat as variable/symbol access + projections
               val startTerm = lookup(head, stack)
               tail.foldLeft(startTerm) { (acc, field) =>
                 Term.Proj(acc, field)
               }
             }
        }
      case Expr.Var(name) => lookup(name, stack)

      case Expr.Lam(param, tpe, body) =>
        val extended = param :: stack
        Term.Lam(tpe, body.toTerm(typeEnv, extended))

      case Expr.App(fnExpr, argExpr) =>
        val fnTerm = fnExpr.toTerm(typeEnv, stack)
        val argTerm = argExpr.toTerm(typeEnv, stack)
        Term.App(fnTerm, argTerm)

      case Expr.IntLit(value) =>
        Term.IntLit(value)

      case Expr.FloatLit(value) =>
        Term.FloatLit(value)

      case Expr.BoolLit(value) =>
        Term.BoolLit(value)

      case Expr.StringLit(value) =>
        Term.StringLit(value)

      case Expr.Proposition(predicates) =>
        Term.Proposition(predicates.map(_.toTerm(typeEnv, stack)))

      case Expr.ListLit(tpe, elements) =>
        Term.ListLit(tpe, elements.map(_.toTerm(typeEnv, stack)))

      case Expr.BinOp(kind, lhs, rhs) =>
        val leftTerm = lhs.toTerm(typeEnv, stack)
        val rightTerm = rhs.toTerm(typeEnv, stack)
        Term.BinOp(kind, leftTerm, rightTerm)

      case Expr.If(cond, thenBr, elseBr) =>
        val condTerm = cond.toTerm(typeEnv, stack)
        val thenTerm = thenBr.toTerm(typeEnv, stack)
        val elseTerm = elseBr.toTerm(typeEnv, stack)
        Term.If(condTerm, thenTerm, elseTerm)

      case Expr.Fix(name, tpe, body) =>
        val extended = name :: stack
        val bodyTerm = body.toTerm(typeEnv, extended)
        Term.Fix(tpe, bodyTerm)

      case Expr.Record(fields) =>
        val termFields = fields.map { case (name, expr) => (name, expr.toTerm(typeEnv, stack)) }
        Term.Record(termFields)

      case Expr.Proj(record, field) =>
        val recordTerm = record.toTerm(typeEnv, stack)
        Term.Proj(recordTerm, field)

      case Expr.Let(name, value, body) =>
        // let x = e1 in e2  ~~>  (\x. e2) e1
        val valueTerm = value.toTerm(typeEnv, stack)
        val extended = name :: stack
        val bodyTerm = body.toTerm(typeEnv, extended)
        Term.App(Term.Lam(valueTerm.infer(), bodyTerm), valueTerm)

      case Expr.LetRec(name, tpe, value, body) =>
        // let rec f: T = e1 in e2  ~~>  (\f. e2) (fix f: T. e1)
        val extended = name :: stack
        val valueTerm = value.toTerm(typeEnv, extended)
        val fixTerm = Term.Fix(tpe, valueTerm)
        val bodyTerm = body.toTerm(typeEnv, extended)
        Term.App(Term.Lam(tpe, bodyTerm), fixTerm)

      case Expr.EnumLit(enumType, variant) =>
        // Lookup the enum type from the type environment
        typeEnv.getType(Identifier.Symbol(enumType)) match {
          case Some(et: Type.EnumType) =>
            if (et.variants.contains(variant)) {
              Term.EnumLit(et, variant)
            } else {
              throw new RuntimeException(s"Variant $variant not found in enum $enumType")
            }
          case Some(_) =>
            throw new RuntimeException(s"$enumType is not an enum type")
          case None =>
            throw new RuntimeException(s"Enum type $enumType not found in environment")
        }

      case Expr.Match(scrutinee, cases) =>
        val scrutineeTerm = scrutinee.toTerm(typeEnv, stack)
        val casesTerms = cases.map { case (variant, expr) => (variant, expr.toTerm(typeEnv, stack)) }
        Term.Match(scrutineeTerm, casesTerms)
    }
  }
}

enum Term {
  case Var(index: Int)
  case Sym(ident: String)
  case Lam(parameterType: Type, body: Term)
  case App(leftTerm: Term, rightTerm: Term)
  case IntLit(n: Long)
  case FloatLit(v: Double)
  case BoolLit(b: Boolean)
  case StringLit(s: String)
  case Proposition(predicates: Set[Term])
  case ListLit(tpe: Option[Type], elements: List[Term])
  case BinOp(kind: OpKind, leftTerm: Term, rightTerm: Term)
  case If(cond: Term, thenBranch: Term, elseBranch: Term)
  case Fix(annotatedType: Type, body: Term)
  case Record(fields: Map[String, Term])
  case Proj(record: Term, field: String)
  case EnumLit(enumType: Type.EnumType, variant: String)
  case Match(scrutinee: Term, cases: List[(String, Term)])

  /**
   * Infer the type of this term.
   * @param typeEnv type environment mapping De Bruijn indices to types
   */
  def infer(typeEnv: TypeOnlyEnv = TypeEnvironment.empty[Identifier, Type]): Type = this match {
    case Term.Var(index) =>
      if (index >= 0 && index < typeEnv.types.size) typeEnv.types(Index(index))
      else throw new RuntimeException(s"Variable index $index out of bounds in type environment")

    case Term.Sym(ident) =>
      if (typeEnv.getType(Identifier.Symbol(ident)).isDefined)
        typeEnv.types(Identifier.Symbol(ident))
      else
        throw new RuntimeException(s"Unbound symbol: $ident")

    case Term.Lam(parameterType, body) =>
      val extendedEnv = parameterType :: typeEnv
      Type.Arrow(parameterType, body.infer(extendedEnv))

    case Term.App(leftTerm, rightTerm) =>
      leftTerm.infer(typeEnv) match {
        case Type.Arrow(_, to) => to
        case other => throw new RuntimeException(s"Cannot apply non-function type: $other")
      }

    case Term.IntLit(_)                 => Type.IntType
    case Term.FloatLit(_)               => Type.FloatType
    case Term.BoolLit(_)                => Type.BoolType
    case Term.StringLit(_)              => Type.StringType
    case Term.Proposition(_)            => Type.PropositionType
    case Term.ListLit(Some(tpe), _)     => Type.ListType(tpe)
    case Term.ListLit(None, hd::tl)     => Type.ListType(hd.infer(typeEnv))
    case Term.ListLit(None, Nil)        => throw new RuntimeException("Cannot infer type of empty list without annotation")
    case Term.BinOp(kind, lhs, rhs)     => (kind, lhs.infer(typeEnv), rhs.infer(typeEnv)) match {
      case (OpKind.Add, Type.IntType, Type.IntType)     => Type.IntType
      case (OpKind.Sub, Type.IntType, Type.IntType)     => Type.IntType
      case (OpKind.Mul, Type.IntType, Type.IntType)     => Type.IntType
      case (OpKind.Add, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Sub, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Mul, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Eq, Type.FloatType, Type.FloatType)  => Type.BoolType
      case (OpKind.Lt, Type.FloatType, Type.FloatType)  => Type.BoolType
      case (OpKind.Gt, Type.FloatType, Type.FloatType)  => Type.BoolType
      case (OpKind.Eq, Type.BoolType, Type.BoolType)    => Type.BoolType
      case (OpKind.Concat, Type.StringType, Type.StringType) => Type.StringType
      case (OpKind.Concat, Type.ListType(lhsType), Type.ListType(rhsType)) if lhsType == rhsType => Type.ListType(lhsType)
      case _ => throw RuntimeException(s"Illegal type for binary operator: ${lhs.infer(typeEnv)} ${kind} ${rhs.infer(typeEnv)}")
    }

    case Term.If(_, thenBranch, _) => thenBranch.infer(typeEnv)

    case Term.Fix(annotatedType, _) => annotatedType

    case Term.Record(fields) =>
      Type.RecordType(fields.map { case (name, term) => (name, term.infer(typeEnv)) })

    case Term.Proj(record, field) =>
      record.infer(typeEnv) match {
        case Type.RecordType(fields) => fields.getOrElse(field,
          throw new RuntimeException(s"Field $field not found in record"))
        case _ => throw new RuntimeException("Cannot project from non-record type")
      }

    case Term.EnumLit(enumType, variant) =>
      // The enumType is already a Type.EnumType, just validate the variant and return it
      if (enumType.variants.contains(variant)) {
        enumType
      } else {
        throw new RuntimeException(s"Variant $variant not found in enum ${enumType.name}")
      }

    case Term.Match(scrutinee, cases) =>
      scrutinee.infer(typeEnv) match {
        case Type.EnumType(name, variants) =>
          // Verify all cases are valid variants
          val caseVariants = cases.map(_._1).toSet
          if (!caseVariants.subsetOf(variants)) {
            val invalid = caseVariants -- variants
            throw new RuntimeException(s"Invalid variants in match: ${invalid.mkString(", ")}")
          }
          // All case branches should have the same type, return the type of the first branch
          if (cases.isEmpty) throw new RuntimeException("Match expression must have at least one case")
          cases.head._2.infer(typeEnv)
        case other =>
          throw new RuntimeException(s"Cannot match on non-enum type: $other")
      }
  }
  
  def eval(using env: Env): Value = Interpreter.eval(this)(using env)
}
