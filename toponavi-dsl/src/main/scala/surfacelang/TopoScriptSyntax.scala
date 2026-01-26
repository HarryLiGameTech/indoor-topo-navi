package surfacelang

import corelang.Value.BoolVal
import corelang.{Env, Environment, Expr, Identifier, Interpreter, Term, Type, TypeOnlyEnvironment, Value}

trait SurfaceSyntax

// AST
type Params = List[(String, Type)]
type Data = Expr.Record

// TODO: Refactor
trait SyntaxNameSpace {
  def env: Environment[Identifier, Type, Expr]

  final def lookup(name: String): Expr = {
    env.getValue(Identifier.Symbol(name))
      .getOrElse(throw new RuntimeException(s"Term '$name' not found in env"))
  }

  final def synthesisEnv(using topoEnv: TopoEnvironment): Environment[Identifier, Type, Value] = {
    val currentEnv = this.env
    
    // 1. Start with the incoming environment's type context + local types
    val typeEnvWithLocals = TypeOnlyEnvironment(topoEnv.env.typeEnv.types ++ currentEnv.typeEnv.types)
    
    // 2. Evaluate each definition in the local environment
    val localValues = currentEnv.values.map { case (id, expr) =>
       val term = expr.toTerm(typeEnvWithLocals)
       val value = Interpreter.eval(term)(using topoEnv.env)
       (id, value)
    }

    // 3. Create a new Env with these values
    Environment[Identifier, Type, Value](
      types = currentEnv.types, 
      values = localValues
    )
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
  params: Params = List.empty, // paramName -> type
  env: Environment[Identifier, Type, Expr] = Environment.empty,
  data: List[Data] = List.empty
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[RootValue] {
  
  override def elaborate(using topoEnv: TopoEnvironment): RootValue = {
    val evaluatedEnv = this.synthesisEnv

    RootValue(
      name = name,
      params = params,
      context = evaluatedEnv 
    )
  }
}

// Submap definition
case class SubTopoMapExpr(
  name: String,
  params: Params = List.empty,
  env: Environment[Identifier, Type, Expr] = Environment.empty,
  data: List[Data] = List.empty,
  nodes: List[TopoNodeExpr] = List.empty,
  paths: List[AtomicPathExpr] = List.empty
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue] {
  
  override def elaborate(using topoEnv: TopoEnvironment): TopoMapValue = {
    val newEnvValues = this.synthesisEnv

    given newTopoEnv: TopoEnvironment = topoEnv.merge(newEnvValues)
    TopoMapValue(
      name = name,
      nodes = nodes.map(_.elaborate).toSet,
      paths = paths.flatMap(_.elaborate).toSet,
      context = newTopoEnv.env,
    )
  }
}

case class TransportExpr(
  name: String,
  stationNodes: Map[TopoMapRef, TopoNodeExpr],
  stationLocations: Map[TopoMapRef, Double],
  data: Data
) extends SurfaceSyntax with Elaborateable[TransportValue] {
  override def elaborate(using topoEnv: TopoEnvironment): TransportValue = {
    TransportValue(
      name = name,
      stations = stationNodes.map { case (mapExpr, nodeExpr) =>
        val mapValue = TopoMapRefValue(mapExpr.name)
        val nodeValue = nodeExpr.elaborate
        (mapValue, nodeValue)
      },
      stationLocations = stationLocations.map { case (mapExpr, location) =>
        val mapValue = TopoMapRefValue(mapExpr.name)
        (mapValue, location)
      },
      context = topoEnv.env,
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
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapRefValue]{
  val env: Environment[Identifier, Type, Expr] = Environment.empty
  override def elaborate(using topoEnv: TopoEnvironment): TopoMapRefValue = TopoMapRefValue(name)
}

case class VehicleRef(
  name: String
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue]{
  val env: Environment[Identifier, Type, Expr] = Environment.empty
  override def elaborate(using topoEnv: TopoEnvironment): TopoMapValue = ??? // TODO
}

case class GlobalConfigExpr(
  submaps: List[TopoMapRef],
  vehicles: List[VehicleRef]
) extends SurfaceSyntax
