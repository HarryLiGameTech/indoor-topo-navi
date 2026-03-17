package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import navigation.RoutePlanner
import enums.NavigationError.{ConstraintFailure, InvalidData, NoRouteFound}


object TopoNaviService {
  private val compiler = new TopoScriptCompiler()

  // 1. Compilation Check
  // Returns "Success" or throws Exception with error message
  def validateCode(files: java.util.Map[String, String]): String = {
    // import scala.jdk.CollectionConverters._
    try {
      compiler.compileProject(files)
      "Compilation Successful"
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage)
    }
  }


  // 2. Navigation Request
  // Takes the Code AND the start/end points. Compiles on-the-fly, finds path, returns String.
  def findPath(
    files: java.util.Map[String, String],
    startNodeName: String,  // graph::node
    endNodeName: String    // graph::node
  ): String = {

    // Step A: Compile
    val result: CompilationResult = compiler.compileProject(files)
    val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)

    // Helper function for fuzzy src-dst specification
    def resolveNode(input: String): (String, String) = {
      if (input.contains("::")) {
        val Array(graphName, nodeName) = input.split("::", 2)
        (graphName, nodeName)
      } else {
        // Fuzzy search across all graphs
        val matches = result.graphs.values.flatMap { graph =>
          graph.nodes.collect {
            case node if node.identifier == input =>
              (graph.identifier, node.identifier)
          }
        }.toSeq

        matches.size match {
          case 1 => matches.head
          case 0 =>
            throw new RuntimeException(s"Node '$input' not found in any graph")
          case _ =>
            val locations = matches.map { case (g, n) => s"$g::$n" }.mkString(", ")
            throw new RuntimeException(
              s"Ambiguous node '$input'. Found in multiple locations: $locations"
            )
        }
      }
    }

    // Step B: Resolve start and end nodes
    val (startGraphName, startNode) = resolveNode(startNodeName)
    val (endGraphName, endNode) = resolveNode(endNodeName)

    // Step C: Execute Pathfinding
    val navigationPlan = routePlanner.navigate(startGraphName, endGraphName, startNode, endNode, Normal, MinimizeTime)

    // Step D: Pretty Print
    navigationPlan match {
      case Left(error) => error match {
        case NoRouteFound(msg) => s"${msg}"
        case InvalidData(msg) => s"${msg}"
        case ConstraintFailure(msg) => s"${msg}"
        case _ => "Unknown error occurred during navigation"
      }
      case Right(plan) => s"${plan.prettyPrint}"
    }
  }
}
