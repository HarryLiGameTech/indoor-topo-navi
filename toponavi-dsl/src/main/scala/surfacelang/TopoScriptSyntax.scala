package surfacelang

import corelang.{Env, Environment, Expr, Identifier, Interpreter, Term, Type, Value}

// AST
type Params = List[(String, Type)]
type Data = Expr.Record

trait SyntaxNameSpace {
  def types: List[(String, Type)]
  def defns: List[(String, Expr)]

  final def lookup(name: String): Expr = {
    defns.toMap.getOrElse(name, throw new RuntimeException(s"Term '$name' not found"))
  }
  
  def synthesisEnv: Env = {
    val typeEnv = types.foldLeft(Environment.empty[Identifier, Type, Value]) { 
      case (env, (name, tpe)) => env.addTypeVar(Identifier.Symbol(name), tpe)
    }
    val defnTerms = defns.map { case (n, e) => (n, e.toTerm(typeEnv)) }.toMap
    defnTerms.foldLeft(typeEnv) { case (env, (name, term)) =>
      val value = Interpreter.eval(term)(using env)
      env.addValueVar(Identifier.Symbol(name), value)
    }
  }
}

// Root TopoMap definition
case class RootSyntax(
  name: String,
  params: List[(String, Type)],
  types: List[(String, Type)],
  defns: List[(String, Expr)],
  data: List[Data],
  submaps: List[String],
  constraints: List[ConstraintSyntax]
) extends SyntaxNameSpace{
  
  def elaborate: TopoRootTerm = {
    ???
  }
}

// Submap definition
case class SubTopoMapSyntax(
  name: String,
  params: Params,
  types: List[(String, Type)],
  defns: List[(String, Expr)],
  data: List[Data],
  nodes: List[TopoNode],
  paths: List[AtomicPath]
) extends SyntaxNameSpace

// Constraint definition
// TODO: Modify
case class ConstraintSyntax(
  name: String,
  params: List[(String, Type)],
  conditions: List[Expr]
) {

}

// Node definition
case class TopoNode(
  name: String,
  data: Data
) {

}

// Path definition
case class AtomicPath(
  from: String,
  to: String,
  bidirectional: Boolean,
  data: Data,
  constraints: List[String] // TODO: List[Constraint]???
) {

}
