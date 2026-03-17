package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import navigation.RoutePlanner
import enums.NavigationError.{ConstraintFailure, InvalidData, NoRouteFound}
import com.e611.toponavi.web.cache.CompilationCacheService
import java.util.{Map => JMap}
import scala.jdk.CollectionConverters._


object TopoNaviService {
  private val compiler = new TopoScriptCompiler()

  // Lazy initialization of cache service (will be injected by Spring at runtime)
  private var cacheService: Option[CompilationCacheService] = None

  /**
   * Initializes the cache service for use by Scala object.
   * This is called from Java to register the cache service.
   */
  def setCacheService(service: CompilationCacheService): Unit = {
    cacheService = Some(service)
  }

  // 1. Compilation Check (without cache)
  // Returns "Success" or throws Exception with error message
  def validateCode(files: JMap[String, String]): String = {
    try {
      compiler.compileProject(files)
      "Compilation Successful"
    } catch {
      case e: Exception => throw new RuntimeException(e.getMessage)
    }
  }

  /**
   * Compilation Check with automatic caching.
   * Uses cache if available and valid (matches current file hash).
   *
   * @param files Map of filename -> file content
   * @return "Compilation Successful" or throws Exception
   */
  def validateCodeWithCache(
    files: JMap[String, String]
  ): String = {
    val scalaFiles = files.asScala.toMap

    // Try to load from cache
    cacheService match {
      case Some(cache) =>
        cache.load(scalaFiles) match {
          case Some(cached) =>
            return "Compilation Successful (from cache)"
          case None =>
            // Cache miss - compile normally
            val result = compiler.compileProject(scalaFiles)
            cache.save(scalaFiles, result)
            return "Compilation Successful"
        }
      case None =>
        // Cache not initialized - compile without caching
        compiler.compileProject(scalaFiles)
        return "Compilation Successful"
    }
  }

  // 2. Compilation with manual cache control
  //
  // @param files Map of filename -> file content
  // @param forceRecompile If "true", bypass cache and force recompilation
  // @return CompilationResult
  def compileProjectCached(
    files: JMap[String, String],
    forceRecompile: String = "false"
  ): CompilationResult = {
    val scalaFiles = files.asScala.toMap

    cacheService match {
      case Some(cache) =>
        // If force recompile, don't check cache
        if ("true".equals(forceRecompile)) {
          val result = compiler.compileProject(scalaFiles)
          cache.save(scalaFiles, result)
          return result
        }

        // Try to load from cache
        cache.load(scalaFiles) match {
          case Some(cached) =>
            return cached.getResult()
          case None =>
            // Cache miss - compile and save
            val result = compiler.compileProject(scalaFiles)
            cache.save(scalaFiles, result)
            return result
        }

      case None =>
        // Cache not initialized - compile directly
        compiler.compileProject(scalaFiles)
    }
  }

  // 3. Navigation Request (unchanged - uses cached compilation internally)
  // Takes Code AND start/end points. Compiles on-the-fly, finds path, returns String.
  def findPath(
    files: JMap[String, String],
    startNodeName: String,  // graph::node
    endNodeName: String    // graph::node
  ): String = {
    // Step A: Compile (uses cache if available)
    val result: CompilationResult = compileProjectCached(files, "false")
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
