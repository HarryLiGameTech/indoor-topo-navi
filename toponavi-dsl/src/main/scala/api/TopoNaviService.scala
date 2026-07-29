package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
import data.{LinearSegment, NavigationOutputPath, TurnHint}
import enums.{NavigationError, RoutePlanningPreferences}
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import navigation.{RoutePlanner, TraversalTagPolicy}
import enums.NavigationError.{ConstraintFailure, DestinationHasBannedTags, InvalidData, NoRouteFound}

import java.util.List as JList
import java.util.Map as JMap
import scala.jdk.CollectionConverters.*


object TopoNaviService {
  private val compiler = new TopoScriptCompiler()

  // 1. Compile and return result (for use by web layer with caching)
  def compile(files: JMap[String, String], params: JMap[String, AnyRef]): CompilationResult =
    compiler.compileProject(files, params)

  // Backward-compatible overload with no params
  def compile(files: JMap[String, String]): CompilationResult =
    compiler.compileProject(files)

  // 2. Compilation Check (stateless, no cache)
  def validateCode(files: JMap[String, String], params: JMap[String, AnyRef]): String = {
    try {
      compiler.compileProject(files, params)
      "Compilation Successful"
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage)
    }
  }

  def validateCode(files: JMap[String, String]): String =
    validateCode(files, java.util.Collections.emptyMap())

  // 3. Navigate from a pre-compiled result; returns NavigationOutputPath for structured access.
  //    Throws RuntimeException on navigation error (Java-friendly).
  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): NavigationOutputPath =
    findRoutePlan(result, startNodeName, endNodeName, preference, Set.empty)

  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences,
    banTags: Set[String]
  ): NavigationOutputPath = {
    val isHighRise = result.metadata.get("coordEstimated") match {
      case Some(enums.AttributeValue.BoolValue(true)) => false // If coordinates were estimated, we assume it's a standard building.
      case _ => true
    }
    // TODO (production): Consider refactoring to hold a Map[BuildingKey, RoutePlanner] and only rebuild on cache miss.
    val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, isHighRise)
    val (startGraphName, startNode) = resolveNode(startNodeName, result)
    val (endGraphName, endNode) = resolveNode(endNodeName, result)
    val tagPolicy = TraversalTagPolicy(banTags)
    routePlanner.navigate(startGraphName, endGraphName, startNode, endNode, Normal, preference, tagPolicy) match {
      case Left(DestinationHasBannedTags(nodeIdentifier, conflicts)) =>
        throw NavigationRequestException(
          "DESTINATION_HAS_BANNED_TAG",
          s"Destination '$nodeIdentifier' has banned tags: ${conflicts.mkString(", ")}",
          Map[String, Object](
            "nodeIdentifier" -> nodeIdentifier,
            "conflictingTags" -> conflicts.asJava
          ).asJava
        )
      case Left(NoRouteFound(message)) if banTags.nonEmpty =>
        throw NavigationRequestException(
          "NO_ROUTE_WITH_BAN_TAGS",
          message,
          Map[String, Object]("banTags" -> banTags.toList.sorted.asJava).asJava
        )
      case Left(NoRouteFound(message)) =>
        throw NavigationRequestException(
          "NO_ROUTE_FOUND",
          message,
          Map.empty[String, Object].asJava
        )
      case Left(error) => throw new RuntimeException(formatError(error))
      case Right(plan) =>
        // Convert DSL-level spatial annotations into slim core-level types and attach to the plan
        val segments: List[LinearSegment] = result.linearPaths.flatMap { case (graphId, lpSet) =>
          lpSet.map(lp => LinearSegment(graphId, lp.nodes.map(_.name)))
        }.toList

        val hints: List[TurnHint] = result.directionalArrows.flatMap { case (graphId, arrowSet) =>
          arrowSet.map { da =>
            TurnHint(
              graphId       = graphId,
              fromNode      = da.anchor.name,
              referenceNode = da.reference.name,
              invertFacing  = da.invertFacing,
              toNode        = da.target.name,
              direction     = da.direction
            )
          }
        }.toList

        plan.copy(linearSegments = segments, turnHints = hints)
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
    params: JMap[String, AnyRef],
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): String = {
    val result: CompilationResult = compiler.compileProject(files, params)
    findPathFromResult(result, startNodeName, endNodeName, preference)
  }

  // Backward-compatible overload with no params
  def findPath(
    files: JMap[String, String],
    startNodeName: String,
    endNodeName: String,
    preference: RoutePlanningPreferences
  ): String = findPath(files, java.util.Collections.emptyMap(), startNodeName, endNodeName, preference)

  // Java-friendly overloads: accept preference as a String to avoid Scala enum interop issues
  def findPath(
    files: JMap[String, String],
    params: JMap[String, AnyRef],
    startNodeName: String,
    endNodeName: String,
    preference: String
  ): String = findPath(files, params, startNodeName, endNodeName, parsePreference(preference))

  def findPath(
    files: JMap[String, String],
    startNodeName: String,
    endNodeName: String,
    preference: String
  ): String = findPath(files, java.util.Collections.emptyMap(), startNodeName, endNodeName, parsePreference(preference))

  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: String
  ): NavigationOutputPath = findRoutePlan(result, startNodeName, endNodeName, parsePreference(preference))

  def findRoutePlan(
    result: CompilationResult,
    startNodeName: String,
    endNodeName: String,
    preference: String,
    banTags: JList[String]
  ): NavigationOutputPath = {
    val tags = if banTags == null then Set.empty else banTags.asScala.toSet
    findRoutePlan(result, startNodeName, endNodeName, parsePreference(preference), tags)
  }

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
    case DestinationHasBannedTags(nodeIdentifier, tags) =>
      s"Destination '$nodeIdentifier' has banned tags: ${tags.mkString(", ")}"
  }
}
