package corelang

enum Value extends Identified[Identifier] {
  case IntVal(n: Long)
  case FloatVal(v: Double)
  case BoolVal(b: Boolean)
  case StringVal(s: String)
  case ListVal(tpe: Type, elements: List[Value]) // type is now inferred, no need to Option[] anymore
  case Closure(env: Env, body: Term)
  case FixThunk(annotatedType: Type, body: Term, env: Env)
  case RecordVal(fields: Map[String, Value])
  case EnumVal(enumType: String, variant: String)

  def toTerm: Term = this match {
    case IntVal(n) => Term.IntLit(n)
    case FloatVal(v) => Term.FloatLit(v)
    case BoolVal(b) => Term.BoolLit(b)
    case StringVal(s) => Term.StringLit(s)
    case ListVal(tpe, elements) => Term.ListLit(Some(tpe), elements.map(_.toTerm))
    case Closure(_, _) | FixThunk(_, _, _) => throw new RuntimeException("Cannot convert closure or fixpoint to term")
    case RecordVal(fields) => Term.Record(fields.map { case (k, v) => (k, v.toTerm) })
    case EnumVal(enumType, variant) => Term.EnumLit(enumType, variant)
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
    case EnumVal(enumType, variant) => s"$enumType.$variant"
  }
}
