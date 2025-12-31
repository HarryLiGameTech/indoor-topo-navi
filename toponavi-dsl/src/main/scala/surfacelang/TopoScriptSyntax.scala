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

trait Elaborateable[T] {
  def elaborate(env: Env): T
}

// Root TopoMap definition
case class RootSyntax(
  name: String,
  params: List[(String, Type)], // paramName -> type
  types: List[(String, Type)], // type aliases like enums
  defns: List[(String, Expr)],
  data: List[Data],
  submaps: List[SubTopoMapSyntax],
  constraints: List[ConstraintSyntax]
) extends SyntaxNameSpace with Elaborateable[TopoRootValue] {
  
  def elaborate(env: Env): TopoRootValue = {
    val ctx = this.synthesisEnv
    
    TopoRootValue(
      name = name,
      params = params,
      submaps = submaps.map(_.elaborate(ctx)).toSet,
      transportations = Set.empty, // TODO: Add transportation elaboration
      context = ctx
    )
    
  }
}

// Submap definition
case class SubTopoMapSyntax(
  name: String,
  params: Params,
  types: List[(String, Type)],
  defns: List[(String, Expr)], // aliases
  data: List[Data],
  nodes: List[TopoNode],
  paths: List[AtomicPath]
) extends SyntaxNameSpace with Elaborateable[TopoMapValue] {
  
  def elaborate(env: Env): TopoMapValue = {
    val ctx = this.synthesisEnv
    
    TopoMapValue(
      name = name,
      context = ctx
    )
  }
}

// Constraint definition
// TODO: Modify
case class ConstraintSyntax(
  name: String,
  params: List[(String, Type)],
  conditions: List[Expr]
) extends Elaborateable[ConstraintValue] {

  def elaborate(env: Env): ConstraintValue = {
    val ctx = env // TODO: Extend env with params?

    ConstraintValue(
      name = name,
      context = ctx
    )
  }

}

// Node definition
case class TopoNode(
  name: String,
  data: Data
) extends Elaborateable[TopoNodeValue] {

  def elaborate(env: Env): TopoNodeValue = {
    Interpreter.eval(data.toTerm(env)) match {
      case rv: Value.RecordVal => TopoNodeValue(
        name = name,
        context = env,
        data = rv
      )
      case other => throw new RuntimeException(s"Node data must evaluate to RecordVal, got: $other")
    }
  }
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
