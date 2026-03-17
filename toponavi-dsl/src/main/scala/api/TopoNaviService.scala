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

    // Step B: Strip graph name and node name from "graph::node" format
    val Array(startGraphName, startNode) = startNodeName.split("::", 2)
    val Array(endGraphName, endNode)     = endNodeName.split("::", 2)

    val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)

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
