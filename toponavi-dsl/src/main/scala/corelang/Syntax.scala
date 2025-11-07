package corelang

enum Type {
  case IntType
  case FloatType
  case BoolType
  case StringType
  case ListType(elemType: Type)
  case Arrow(from: Type, to: Type) // a.k.a. function
  case RecordType(fields: Map[String, Type])
}

enum OpKind {
  case Add, Sub, Mul, Eq, Lt, Gt, Concat

//  def resultType: Type = this match {
//    case Add | Sub | Mul => Type.IntType
//    case Eq | Lt | Gt => Type.BoolType
//    case Concat => Type.StringType
//  }

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

enum Expr {
  case Var(name: String)
  case Lam(param: String, tpe: Type, body: Expr)
  case App(fn: Expr, arg: Expr)
  case IntLit(value: Long)
  case FloatLit(value: Double)
  case BoolLit(value: Boolean)
  case StringLit(value: String)
  case ListLit(tpe: Option[Type], elements: List[Expr])
  case BinOp(kind: OpKind, lhs: Expr, rhs: Expr)
  case If(cond: Expr, thenBr: Expr, elseBr: Expr)
  case Fix(name: String, tpe: Type, body: Expr)
  case Record(fields: Map[String, Expr])
  case Proj(record: Expr, field: String)
  case Let(name: String, value: Expr, body: Expr)
  case LetRec(name: String, tpe: Type, value: Expr, body: Expr)

  /**
   * Convert this expression (with names) to a Term (with De Bruijn indices).
   * @param stack binding stack; head = most-recent binding
   */
  def toTerm(stack: List[String] = Nil): Term = {

    def lookup(name: String, ctx: List[String]): Int = {
      ctx.indexOf(name) match {
        case -1 => throw new RuntimeException(s"Unbound variable: $name")
        case idx => idx
      }
    }

    this match {
      case Expr.Var(name) => Term.Var(lookup(name, stack))

      case Expr.Lam(param, tpe, body) =>
        val extended = param :: stack
        Term.Lam(tpe, body.toTerm(extended))

      case Expr.App(fnExpr, argExpr) =>
        val fnTerm = fnExpr.toTerm(stack)
        val argTerm = argExpr.toTerm(stack)
        Term.App(fnTerm, argTerm)

      case Expr.IntLit(value) =>
        Term.IntLit(value)

      case Expr.FloatLit(value) =>
        Term.FloatLit(value)

      case Expr.BoolLit(value) =>
        Term.BoolLit(value)

      case Expr.StringLit(value) =>
        Term.StringLit(value)

      case Expr.ListLit(tpe, elements) =>
        Term.ListLit(tpe, elements.map(_.toTerm(stack)))

      case Expr.BinOp(kind, lhs, rhs) =>
        val leftTerm = lhs.toTerm(stack)
        val rightTerm = rhs.toTerm(stack)
        Term.BinOp(kind, leftTerm, rightTerm)

      case Expr.If(cond, thenBr, elseBr) =>
        val condTerm = cond.toTerm(stack)
        val thenTerm = thenBr.toTerm(stack)
        val elseTerm = elseBr.toTerm(stack)
        Term.If(condTerm, thenTerm, elseTerm)

      case Expr.Fix(name, tpe, body) =>
        val extended = name :: stack
        val bodyTerm = body.toTerm(extended)
        Term.Fix(tpe, bodyTerm)

      case Expr.Record(fields) =>
        val termFields = fields.map { case (name, expr) => (name, expr.toTerm(stack)) }
        Term.Record(termFields)

      case Expr.Proj(record, field) =>
        val recordTerm = record.toTerm(stack)
        Term.Proj(recordTerm, field)

      case Expr.Let(name, value, body) =>
        // let x = e1 in e2  ~~>  (\x. e2) e1
        val valueTerm = value.toTerm(stack)
        val extended = name :: stack
        val bodyTerm = body.toTerm(extended)
        Term.App(Term.Lam(valueTerm.infer(), bodyTerm), valueTerm)

      case Expr.LetRec(name, tpe, value, body) =>
        // let rec f: T = e1 in e2  ~~>  (\f. e2) (fix f: T. e1)
        val extended = name :: stack
        val valueTerm = value.toTerm(extended)
        val fixTerm = Term.Fix(tpe, valueTerm)
        val bodyTerm = body.toTerm(extended)
        Term.App(Term.Lam(tpe, bodyTerm), fixTerm)
    }
  }
}

enum Term {
  case Var(index: Int)
  case Lam(parameterType: Type, body: Term)
  case App(leftTerm: Term, rightTerm: Term)
  case IntLit(n: Long)
  case FloatLit(v: Double)
  case BoolLit(b: Boolean)
  case StringLit(s: String)
  case ListLit(tpe: Option[Type], elements: List[Term])
  case BinOp(kind: OpKind, leftTerm: Term, rightTerm: Term)
  case If(cond: Term, thenBranch: Term, elseBranch: Term)
  case Fix(annotatedType: Type, body: Term)
  case Record(fields: Map[String, Term])
  case Proj(record: Term, field: String)

  /**
   * Infer the type of this term.
   * @param typeEnv type environment mapping De Bruijn indices to types
   */
  def infer(typeEnv: List[Type] = Nil): Type = this match {
    case Term.Var(index) =>
      if (index >= 0 && index < typeEnv.length) typeEnv(index)
      else throw new RuntimeException(s"Variable index $index out of bounds in type environment")

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
    case Term.ListLit(Some(tpe), _)     => Type.ListType(tpe)
    case Term.ListLit(None, hd::tl)     => Type.ListType(hd.infer(typeEnv))
    case Term.ListLit(None, Nil)        => throw new RuntimeException("Cannot infer type of empty list without annotation")
    case Term.BinOp(kind, lhs, rhs)     => (kind, lhs.infer(typeEnv), rhs.infer(typeEnv)) match {
      case (OpKind.Add, Type.IntType, Type.IntType) => Type.IntType
      case (OpKind.Sub, Type.IntType, Type.IntType) => Type.IntType
      case (OpKind.Mul, Type.IntType, Type.IntType) => Type.IntType
      case (OpKind.Add, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Sub, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Mul, Type.FloatType, Type.FloatType) => Type.FloatType
      case (OpKind.Eq, Type.FloatType, Type.FloatType) => Type.BoolType
      case (OpKind.Lt, Type.FloatType, Type.FloatType) => Type.BoolType
      case (OpKind.Gt, Type.FloatType, Type.FloatType) => Type.BoolType
      case (OpKind.Eq, Type.BoolType, Type.BoolType) => Type.BoolType
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
  }
}

enum Value {
  case IntVal(n: Long)
  case FloatVal(v: Double)
  case BoolVal(b: Boolean)
  case StringVal(s: String)
  case ListVal(tpe: Type, elements: List[Value]) // type is now inferred, no need to Option[] anymore
  case Closure(env: Map[Int, Value], body: Term)
  case FixThunk(annotatedType: Type, body: Term, env: Map[Int, Value] = Map.empty)
  case RecordVal(fields: Map[String, Value])

  def toTerm: Term = this match {
    case IntVal(n) => Term.IntLit(n)
    case FloatVal(v) => Term.FloatLit(v)
    case BoolVal(b) => Term.BoolLit(b)
    case StringVal(s) => Term.StringLit(s)
    case ListVal(tpe, elements) => Term.ListLit(Some(tpe), elements.map(_.toTerm))
    case Closure(_, _) | FixThunk(_, _, _) => throw new RuntimeException("Cannot convert closure or fixpoint to term")
    case RecordVal(fields) => Term.Record(fields.map { case (k, v) => (k, v.toTerm) })
  }

  override def toString: String = this match {
    case IntVal(n) => n.toString
    case FloatVal(v) => v.toString
    case BoolVal(b) => b.toString
    case StringVal(s) => s"\"$s\""
    case ListVal(tpe, elements) => elements.mkString("[", ", ", "]")
    case Closure(_, body) => s"<closure>"
    case FixThunk(annotatedType, _, _) => s"<fixpoint: $annotatedType>"
    case RecordVal(fields) =>
      val fieldStr = fields.map { case (name, value) => s"$name: $value" }.mkString(", ")
      s"{ $fieldStr }"
  }
}