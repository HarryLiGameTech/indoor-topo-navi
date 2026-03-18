package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import navigation.RoutePlanner
import enums.NavigationError.{ConstraintFailure, InvalidData, NoRouteFound}

import java.util.{Map => JMap}


object TopoNaviService {
  private val compiler = new TopoScriptCompiler()

  // 1. Compile and return result (for use by web layer with caching)
  def compile(files: JMap[String, String]): CompilationResult =
    compiler.compileProject(files)

  // 2. Compilation Check (stateless, no cache)
  // Returns "Compilation Successful" or throws Exception with error message
  def validateCode(files: JMap[String, String]): String = {
    try {
      compiler.compileProject(files)
      "Compilation Successful"
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage)
    }
  }

  // 3. Navigate from a pre-compiled result (used by web layer after cache lookup)
  def findPathFromResult(
    result: CompilationResult,
    startNodeName: String,  // graph::node
    endNodeName: String     // graph::node
  ): String = {
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

    // Resolve start and end nodes
    val (startGraphName, startNode) = resolveNode(startNodeName)
    val (endGraphName, endNode) = resolveNode(endNodeName)

    // Execute Pathfinding
    val navigationPlan = routePlanner.navigate(startGraphName, endGraphName, startNode, endNode, Normal, MinimizeTime)

    // Pretty Print
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

  // 4. Navigation Request (stateless - compiles on-the-fly, no cache)
  // Takes files AND start/end points. Compiles fresh, finds path, returns String.
  def findPath(
    files: JMap[String, String],
    startNodeName: String,  // graph::node
    endNodeName: String     // graph::node
  ): String = {
    val result: CompilationResult = compiler.compileProject(files)
    findPathFromResult(result, startNodeName, endNodeName)
  }
}
