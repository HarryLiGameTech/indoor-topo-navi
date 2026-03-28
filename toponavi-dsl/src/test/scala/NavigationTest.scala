import compiler.CompilationResult
import enums.RoutePlanningPreferences
import enums.VisitingMode.Normal
import navigation.RoutePlanner

import java.io.ObjectInputStream
import java.nio.file.{Files, Paths}


object RoutePlanningTester extends App {

  private val cachePath = Paths.get(System.getProperty("user.home"), ".toponavi", "tester_swfc")

  if (!Files.exists(cachePath)) {
    println(s"Cache not found at ${cachePath.toAbsolutePath}. Run TesterCacheGenerator first.")
    sys.exit(1)
  }

  val result: CompilationResult = {
    val ois = new ObjectInputStream(Files.newInputStream(cachePath))
    try ois.readObject().asInstanceOf[CompilationResult]
    finally ois.close()
  }

  println(s"Loaded cache: ${result.graphs.size} graphs, sequence: ${result.graphSequence.mkString(", ")}")

  val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)

  // TEST NAVIGATE BEGIN //
  routePlanner.navigate("LowerLobby", "Floor50", "CP3_4_hall", "toilet_A", Normal, RoutePlanningPreferences.MinimizeTransfers) match {
    case Right(navigationPath) =>
      println(s"\n=== Testing RoutePlanner Navigate ===")
      println(s"Path found with ${navigationPath.routeNodes.size} nodes:")
      navigationPath.routeNodes.foreach(node => println(s"   → ${node.toString}"))
      println(s"With ${navigationPath.routeEdges.size} AtomicPaths, taking ${navigationPath.totalCost} seconds:")
      navigationPath.routeEdges.foreach(edge => println(s"   ${edge.source.toString} => ${edge.target.toString} takes ${edge.cost} seconds"))
    case Left(error) =>
      println(s"Navigation error: ${error}")
  }
}