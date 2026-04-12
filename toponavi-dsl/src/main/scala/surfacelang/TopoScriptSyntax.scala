package surfacelang

import corelang.Value.BoolVal
import corelang.{Env, Environment, Expr, Identifier, Interpreter, Term, Type, TypeOnlyEnvironment, Value}
import cp.util.Graph

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
    val localTerms = currentEnv.values.map { (id, expr) => (id, expr.toTerm(typeEnvWithLocals)) }

    // Collect symbols to determining the evaluation order (topological sort)
    val localSymbolNames = localTerms.keys.collect { case Identifier.Symbol(name) => name }.toSet

    var graph = Graph.directed[String].addVertices(localSymbolNames)

    localTerms.foreach {
      case (Identifier.Symbol(name), term) =>
        term.collectSymbols.foreach { dep =>
           if (localSymbolNames.contains(dep)) {
             // dependency 'dep' must be evaluated before 'name'
             graph = graph.addEdge(dep, name)
           }
        }
      case _ => // Should not happen for definitions in Surface syntax usually
    }

    val sortedNames = graph.topologicalSort.getOrElse(
      throw new RuntimeException("Cycle detected or sorting failed in definitions: " + graph.toString)
    )

    // Evaluate in topological order
    // Accessing `topoEnv.env` gives the base environment (globals/imports)
    // We accumulate local values
    val localValues = sortedNames.foldLeft(Map.empty[Identifier, Value]) { (accValues, name) =>
      val id = Identifier.Symbol(name)
      val term = localTerms(id)

      // Evaluation environment includes base env + currently evaluated local values
      val evalEnv = Environment(
        types = typeEnvWithLocals.types,
        values = topoEnv.env.values ++ accValues
      )

      val value = term.eval(using evalEnv)
      accValues + (id -> value)
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
  // Each expr is evaluated against topoEnv.env as a Bool.
  // Named constraints (e.g. `requires TenantsOnly`) are simply Expr.Var references
  // that resolve to a BoolVal already registered in topoEnv.env by the enclosing surface body.
  def constraints: Iterable[Expr]
  def constrainedElaborate(using topoEnv: TopoEnvironment): T
  override def elaborate(using topoEnv: TopoEnvironment): Option[T] = {
    val allPass = constraints.forall { expr =>
      Interpreter.eval(expr.toTerm(topoEnv.env))(using topoEnv.env) match {
        case BoolVal(b) => b
        case other => throw new RuntimeException(
          s"Constraint expression must evaluate to Bool, got: $other"
        )
      }
    }
    if allPass then Some(this.constrainedElaborate) else None
  }
}

// Root TopoMap definition
case class RootExpr(
  name: String,
  params: Params = List.empty, // paramName -> type
  env: Environment[Identifier, Type, Expr] = Environment.empty,
  data: List[Data] = List.empty,
  constraints: List[ConstraintExpr] = List.empty
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[RootValue] {
  
  override def elaborate(using topoEnv: TopoEnvironment): RootValue = {
    // Evaluate each named constraint and bind its BoolVal result into the core env by name,
    // so that `requires <Name>` in child elements resolves as a plain Expr.Var lookup.
    val envWithConstraints = constraints.foldLeft(topoEnv) { (acc, c) =>
      acc.copy(env = acc.env.addValueVar(Identifier.Symbol(c.name), c.elaborate(using acc)))
    }
    // Merge local definitions on top; also carry constraint BoolVals into the
    // returned context so that submaps elaborated from this root can resolve them.
    val evaluatedEnv = this.synthesisEnv(using envWithConstraints)
    val contextWithConstraints = envWithConstraints.env.merge(evaluatedEnv)

    RootValue(
      name = name,
      params = params,
      context = contextWithConstraints
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
  paths: List[AtomicPathExpr] = List.empty,
  constraints: List[ConstraintExpr] = List.empty
) extends SurfaceSyntax with SyntaxNameSpace with Elaborateable[TopoMapValue] {

  override def elaborate(using topoEnv: TopoEnvironment): TopoMapValue = {
    val newEnvValues = this.synthesisEnv

    // Merge local definitions into the topological environment so that nodes and paths
    // can access variables defined in this scope.
    val envWithCore = topoEnv.merge(newEnvValues)

    // Evaluate each named constraint and bind its BoolVal result into the core env by name,
    // so that `requires <Name>` in atomic-paths resolves as a plain Expr.Var lookup.
    val envWithConstraints = constraints.foldLeft(envWithCore) { (acc, c) =>
      acc.copy(env = acc.env.addValueVar(Identifier.Symbol(c.name), c.elaborate(using acc)))
    }

    val elaboratedNodes = nodes.map(_.elaborate(using envWithConstraints))

    // Add nodes to environment so paths can find them
    val envWithNodes = envWithConstraints.copy(
      nodes = envWithConstraints.nodes ++ elaboratedNodes.map(n => n.name -> n)
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
  data: Data,
  constraints: List[Expr] = List.empty
)

case class TransportExpr(
  name: String,
  surfaceType: String = "Elevator", // e.g., "Elevator", "Escalator", "Stairs"
  stations: List[StationDef],
  env: Environment[Identifier, Type, Expr] = Environment.empty,
  data: Data,
  constraints: List[ConstraintExpr] = List.empty
) extends SyntaxNameSpace with SurfaceSyntax with Elaborateable[TransportValue] {
  override def elaborate(using topoEnv: TopoEnvironment): TransportValue = {
    // Evaluate local definitions (e.g., let bindings inside the transport block)
    val localCtx = this.synthesisEnv
    // Create an environment that includes both global values and local definitions
    val envWithLocals = topoEnv.merge(localCtx)

    // Evaluate each named constraint and bind its BoolVal into the env by name,
    // so that `requires <Name>` on stations resolves as a plain Expr.Var lookup.
    val envWithConstraints = constraints.foldLeft(envWithLocals) { (acc, c) =>
      acc.copy(env = acc.env.addValueVar(Identifier.Symbol(c.name), c.elaborate(using acc)))
    }

    TransportValue(
      name = name,
      surfaceType = this.surfaceType,
      stations = stations.map { station =>
        // Evaluate the station's requires-clause; if any constraint fails, mark with NoAccess.
        val passes = station.constraints.forall { expr =>
          Interpreter.eval(expr.toTerm(envWithConstraints.env))(using envWithConstraints.env) match {
            case Value.BoolVal(b) => b
            case other => throw new RuntimeException(s"Station constraint must evaluate to Bool, got: $other")
          }
        }

        // Strict Validation: Ensure the referenced node exists
        if (topoEnv.resolveNode(station.node.fromMapName, station.node.nodeName).isEmpty) {
          throw new RuntimeException(s"Station refers to unknown node: ${station.node.fromMapName}::${station.node.nodeName}")
        }

        val nodeValue = station.node.elaborate
        val stationDataVal = Interpreter.eval(station.data.toTerm(envWithConstraints.env))(using envWithConstraints.env) match {
          case rv: Value.RecordVal => rv
          case other => throw new RuntimeException(s"Station data must evaluate to RecordVal, got: $other")
        }

        // If constraint failed, inject "_permission" -> "NoAccess" into the station RecordVal
        val finalDataVal: Value.RecordVal = if passes then stationDataVal
                           else Value.RecordVal(stationDataVal.fields + ("_permission" -> Value.StringVal("NoAccess")))
        (nodeValue, finalDataVal)
      },
      // Use envWithConstraints to allow transport data to reference local variables
      data = Interpreter.eval(data.toTerm(envWithConstraints.env))(using envWithConstraints.env) match {
        case rv: Value.RecordVal => rv
        case other => throw new RuntimeException(s"Transport data must evaluate to RecordVal, got: $other")
      },
      context = localCtx
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
        from = topoEnv.nodes.getOrElse(from, throw RuntimeException(s"No such node in topoEnv: $from")),
        to = topoEnv.nodes.getOrElse(to, throw RuntimeException(s"No such node in topoEnv: $to")),
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
  vehicles: List[VehicleRef],
  submapUsages: Map[TopoMapRef, List[String]] = Map.empty,
  orderedSubmapNames: List[String] = List.empty
) extends SurfaceSyntax

// Named compile-time constraint: a set of boolean predicates evaluated against invocation params.
// elaborate() returns a BoolVal — true iff ALL predicates hold — so it can be bound
// directly into the core env by name and resolved via a plain Expr.Var lookup.
case class ConstraintExpr(
  name: String,
  predicates: List[Expr]
) extends SurfaceSyntax with Elaborateable[Value.BoolVal] {
  override def elaborate(using topoEnv: TopoEnvironment): Value.BoolVal = {
    val result = predicates.forall { predExpr =>
      Interpreter.eval(predExpr.toTerm(topoEnv.env))(using topoEnv.env) match {
        case Value.BoolVal(b) => b
        case other => throw new RuntimeException(
          s"Constraint '$name': predicate must evaluate to Bool, got: $other"
        )
      }
    }
    Value.BoolVal(result)
  }
}

