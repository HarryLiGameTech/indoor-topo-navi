package api

import compiler.TopoScriptCompiler
import compiler.CompilationResult
// Import your pathfinding logic here (e.g. from toponavi-core)

// TODO: Complete this file

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

  // TODO: Check correctness
  // 2. Navigation Request
  // Takes the Code AND the start/end points. Compiles on the fly, finds path, returns String.
  def findPath(
    files: java.util.Map[String, String],
    startNode: String,
    endNode: String
  ): String = {

    // Step A: Compile
    val result: CompilationResult = compiler.compileProject(files)

    // Step B: Locate Nodes in the compiled graphs
    // (You'll need logic to find which graph contains the start/end nodes)
    // For example:
    val startGraph = result.graphs.values.find(_.nodes.exists(_.identifier == startNode))
    val endGraph   = result.graphs.values.find(_.nodes.exists(_.identifier == endNode))

    if (startGraph.isEmpty || endGraph.isEmpty) {
      return s"Error: Nodes $startNode or $endNode not found in any map."
    }

    // Step C: Execute Pathfinding (Mocking your core logic here)
    // val path = YourPathFinder.findPath(result.transportGraph, startNode, endNode)

    // Step D: Pretty Print
    // return path.toString
    s"Path found from $startNode to $endNode via [NodeA, NodeB, Elevator1...]"
  }
}