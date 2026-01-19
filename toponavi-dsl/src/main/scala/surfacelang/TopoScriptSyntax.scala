package surfacelang

import corelang.Value.BoolVal
import corelang.{Env, Environment, Expr, Identifier, Interpreter, Term, Type, Value}

trait SurfaceSyntax

// AST
type Params = List[(String, Type)]
type Data = Expr.Record

trait SyntaxNameSpace {
  def types: List[(String, Type)]
  def defns: List[(String, Expr)]

  final def lookup(name: String): Expr = {
    defns.toMap.getOrElse(name, throw new RuntimeException(s"Term '$name' not found"))
  }
  
  lazy val synthesisEnv: Env = {
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
  def elaborate(using topoEnv: TopoEnvironment): T
}

trait ConstrainedElaborateable[T] extends Elaborateable[Option[T]] {
  def constraints: Iterable[Expr]
  def constrainedElaborate(using topoEnv: TopoEnvironment): T
  override def elaborate(using topoEnv: TopoEnvironment): Option[T] = {
    if constraints.forall { expr =>
      given Env = topoEnv.env
      expr.toTerm(topoEnv.env).eval match {
        case BoolVal(b) => b
        case _ => throw RuntimeException("Constraint must be a boolean value")
      }
    } then Some(this.constrainedElaborate) else None
  } 
}

// Root TopoMap definition
case class RootExpr(
  name: String,
  params: List[(String, Type)], // paramName -> type
  types: List[(String, Type)], // type aliases like enums
  defns: List[(String, Expr)],
  data: List[Data],
  submaps: List[SubTopoMapExpr],
  submapReferences: List[String]
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoRootValue] {
  
  override def elaborate(using topoEnv: TopoEnvironment): TopoRootValue = {
    val env = this.synthesisEnv
    TopoRootValue(
      name = name,
      params = params,
      submaps = submaps.map(_.elaborate(using topoEnv.merge(env))).toSet,
      transportations = Set.empty, // TODO: Add transportation elaboration
      context = env
    )
    
  }
}

// Submap definition
case class SubTopoMapExpr(
  name: String,
  params: Params,
  types: List[(String, Type)],
  defns: List[(String, Expr)], // aliases
  data: List[Data],
  nodes: List[TopoNodeExpr],
  paths: List[AtomicPathExpr]
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue] {
  
  override def elaborate(using topoEnv: TopoEnvironment): TopoMapValue = {
    given newTopoEnv: TopoEnvironment = topoEnv.merge(this.synthesisEnv)
    TopoMapValue(
      name = name,
      nodes = nodes.map(_.elaborate).toSet,
      paths = paths.flatMap(_.elaborate).toSet,
      context = newTopoEnv.env,
    )
  }
}

// Node definition
case class TopoNodeExpr(
  name: String,
  data: Data
) extends SurfaceSyntax with Elaborateable[TopoNodeValue] {

  override def elaborate(using topoEnv: TopoEnvironment): TopoNodeValue = {
    Interpreter.eval(data.toTerm(topoEnv.env)) match {
      case rv: Value.RecordVal => TopoNodeValue(
        name = name,
        context = topoEnv.env,
        data = rv
      )
      case other => throw new RuntimeException(s"Node data must evaluate to RecordVal, got: $other")
    }
  }
}

// Path definition
case class AtomicPathExpr(
  from: String,
  to: String,
  bidirectional: Boolean,
  data: Data,
  override val constraints: List[Expr]
) extends SurfaceSyntax with ConstrainedElaborateable[AtomicPathValue] {
  override def constrainedElaborate(using topoEnv: TopoEnvironment): AtomicPathValue = {
    AtomicPathValue(
      from = topoEnv.nodes.getOrElse(from, throw RuntimeException(s"Fuck, no such node: $from")),
      to = topoEnv.nodes.getOrElse(to, throw RuntimeException(s"Fuck, no such node: $to")),
      bidirectional = bidirectional,
      context = topoEnv.env,
    )
  }
}


case class TopoMapRef(
  name: String
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue]{
  override def elaborate(using topoEnv: TopoEnvironment): Any // TODO
}

case class VehicleRef(
  name: String
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue]{
  override def elaborate(using topoEnv: TopoEnvironment): Any // TODO
}
