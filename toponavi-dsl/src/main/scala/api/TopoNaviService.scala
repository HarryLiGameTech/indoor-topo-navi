package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
import data.NavigationOutputPath
import enums.{NavigationError, RoutePlanningPreferences}
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import navigation.RoutePlanner
import enums.NavigationError.{ConstraintFailure, InvalidData, NoRouteFound}

import java.util.Map as JMap


object TopoNaviService {
  private val compiler = new TopoScriptCompiler()

  // 1. Compile and return result (for use by web layer with caching)
  def compile(files: JMap[String, String]): CompilationResult =
    compiler.compileProject(files)

  // 2. Compilation Check (stateless, no cache)
  def validateCode(files: JMap[String, String]): String = {
    try {
      compiler.compileProject(files)
      "Compilation Successful"
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage)
    }
  }

  // 3. Navigate from a pre-compiled result; returns NavigationOutputPath for structured access.
  //    Throws RuntimeException on navigation error (Java-friendly).
  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): NavigationOutputPath = {
    val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)
    val (startGraphName, startNode) = resolveNode(startNodeName, result)
    val (endGraphName, endNode) = resolveNode(endNodeName, result)
    routePlanner.navigate(startGraphName, endGraphName, startNode, endNode, Normal, preference) match {
      case Left(error) => throw new RuntimeException(formatError(error))
      case Right(plan) => plan
    }
  }

  // 4. Text-only convenience wrapper (backward-compatible).
  def findPathFromResult(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): String =
    try { findRoutePlan(result, startNodeName, endNodeName, preference).prettyPrint }
    catch { case e: RuntimeException => e.getMessage }

  // 5. Navigation Request (stateless - compiles on-the-fly, no cache)
  def findPath(
    files: JMap[String, String],
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): String = {
    val result: CompilationResult = compiler.compileProject(files)
    findPathFromResult(result, startNodeName, endNodeName, preference)
  }

  // Java-friendly overloads: accept preference as a String to avoid Scala enum interop issues
  def findPath(
    files: JMap[String, String],
    startNodeName: String,
    endNodeName: String,
    preference: String
  ): String = findPath(files, startNodeName, endNodeName, parsePreference(preference))

  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: String
  ): NavigationOutputPath = findRoutePlan(result, startNodeName, endNodeName, parsePreference(preference))

  private def parsePreference(s: String): RoutePlanningPreferences = s match {
    case "MinimizeTransfers"       => RoutePlanningPreferences.MinimizeTransfers
    case "MinimizePhysicalDemands" => RoutePlanningPreferences.MinimizePhysicalDemands
    case _                         => RoutePlanningPreferences.MinimizeTime
  }


  private def resolveNode(input: String, result: CompilationResult): (String, String) = {
    if (input.contains("::")) {
      val Array(graphName, nodeName) = input.split("::", 2)
      (graphName, nodeName)
    } else {
      val matches = result.graphs.values.flatMap { graph =>
        graph.nodes.collect {
          case node if node.identifier == input => (graph.identifier, node.identifier)
        }
      }.toSeq
      matches.size match {
        case 1 => matches.head
        case 0 => throw new RuntimeException(s"Node '$input' not found in any graph")
        case _ =>
          val locations = matches.map { case (g, n) => s"$g::$n" }.mkString(", ")
          throw new RuntimeException(s"Ambiguous node '$input'. Found in multiple locations: $locations")
      }
    }
  }

  private def formatError(error: NavigationError): String = error match {
    case NoRouteFound(msg)     => msg
    case InvalidData(msg)      => msg
    case ConstraintFailure(msg) => msg
    case _                     => "Unknown error occurred during navigation"
  }
}
