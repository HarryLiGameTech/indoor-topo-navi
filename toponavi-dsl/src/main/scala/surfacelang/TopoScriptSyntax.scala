package surfacelang

import corelang.Value.BoolVal
import corelang.{Env, Environment, Expr, Identifier, Interpreter, Term, Type, TypeOnlyEnvironment, Value}

trait SurfaceSyntax

// AST
type Params = List[(String, Type)]
type Data = Expr.Record

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

  // TODO: Verify correctness of context
  override def elaborate(using topoEnv: TopoEnvironment): TopoMapValue = {
    val newEnvValues = this.synthesisEnv

    // We merge the new environment values into the current topological environment
    // to ensure that nodes and paths can access the variables defined in this scope.
    val envWithCore = topoEnv.merge(newEnvValues)

    val elaboratedNodes = nodes.map(_.elaborate(using envWithCore))

    // Add nodes to environment so paths can find them
    val envWithNodes = envWithCore.copy(
      nodes = envWithCore.nodes ++ elaboratedNodes.map(n => n.name -> n)
    )

    TopoMapValue(
      name = name,
      nodes = elaboratedNodes.toSet,
      paths = paths.flatMap(_.elaborate(using envWithNodes)).toSet,
      context = newEnvValues,
    )
  }
}

case class StationDef(
  node: TopoNodeRef,
  data: Data
)

case class TransportExpr(
  name: String,
  stations: List[StationDef],
  data: Data
) extends SurfaceSyntax with Elaborateable[TransportValue] {
  override def elaborate(using topoEnv: TopoEnvironment): TransportValue = {
    TransportValue(
      name = name,
      stations = stations.map { station =>
        // Strict Validation: Ensure the referenced node exists
        if (topoEnv.resolveNode(station.node.fromMapName, station.node.nodeName).isEmpty) {
          throw new RuntimeException(s"Station refers to unknown node: ${station.node.fromMapName}::${station.node.nodeName}")
        }
        
        val nodeValue = station.node.elaborate
        val stationDataVal = Interpreter.eval(station.data.toTerm(topoEnv.env))(using topoEnv.env) match {
          case rv: Value.RecordVal => rv
          case other => throw new RuntimeException(s"Station data must evaluate to RecordVal, got: $other")
        }
        (nodeValue, stationDataVal)
      },
      data = Interpreter.eval(data.toTerm(topoEnv.env))(using topoEnv.env) match {
        case rv: Value.RecordVal => rv
        case other => throw new RuntimeException(s"Transport data must evaluate to RecordVal, got: $other")
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
    Interpreter.eval(data.toTerm(topoEnv.env))(using topoEnv.env) match {
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
    Interpreter.eval(data.toTerm(topoEnv.env))(using topoEnv.env) match {
      case rv: Value.RecordVal => AtomicPathValue(
        from = topoEnv.nodes.getOrElse(from, throw RuntimeException(s"Fuck, no such node: $from")),
        to = topoEnv.nodes.getOrElse(to, throw RuntimeException(s"Fuck, no such node: $to")),
        bidirectional = bidirectional,
        data = rv,
        context = topoEnv.env,
      )
      case other => throw new RuntimeException(s"Path data must evaluate to RecordVal, got: $other")
    }
  }
}

case class TopoNodeRef(
  fromMapName: String,
  nodeName: String
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoNodeRefValue]{
  val env: Environment[Identifier, Type, Expr] = Environment.empty
  override def elaborate(using topoEnv: TopoEnvironment): TopoNodeRefValue =
    TopoNodeRefValue(fromMapName, nodeName)
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
