import compiler.CompilationResult
import enums.RoutePlanningPreferences
import enums.VisitingMode.Normal
import navigation.RoutePlanner

import java.io.ObjectInputStream
import java.nio.file.{Files, Paths}


object SwfcRoutePlanningTester extends App {

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

  println(s"Loaded swfc cache: ${result.graphs.size} graphs, sequence: ${result.graphSequence.mkString(", ")}")

  val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)

  // TEST NAVIGATE BEGIN //
  routePlanner.navigate("LowerLobby", "Floor50", "CP3_4_hall", "toilet_A", Normal, RoutePlanningPreferences.MinimizeTransfers) match {
    case Right(navigationPath) =>
      println(s"\n=== Testing SwfcRoutePlanner Navigate ===")
      println(s"Path found with ${navigationPath.routeNodes.size} nodes:")
      navigationPath.routeNodes.foreach(node => println(s"   → ${node.toString}"))
      println(s"With ${navigationPath.routeEdges.size} AtomicPaths, taking ${navigationPath.totalCost} seconds:")
      navigationPath.routeEdges.foreach(edge => println(s"   ${edge.source.toString} => ${edge.target.toString} takes ${edge.cost} seconds"))
    case Left(error) =>
      println(s"Navigation error: ${error}")
  }
}


object TrentRoutePlanningTester extends App {

  private val cachePath = Paths.get(System.getProperty("user.home"), ".toponavi", "tester_trent")

  if (!Files.exists(cachePath)) {
    println(s"Cache not found at ${cachePath.toAbsolutePath}. Run TesterCacheGenerator first.")
    sys.exit(1)
  }

  val result: CompilationResult = {
    val ois = new ObjectInputStream(Files.newInputStream(cachePath))
    try ois.readObject().asInstanceOf[CompilationResult]
    finally ois.close()
  }

  val stairCases = result.transportGraph.nodes
    .map(_.ownerLine)
    .distinct
    .collect { case sc: data.StairCase => sc }

  if (stairCases.isEmpty) {
    println("ERROR: No StairCase found in the transport graph. Check that Stairs transports compiled correctly.")
    sys.exit(1)
  } else {
    println(s"StairCase objects found (${stairCases.size}):")
    stairCases.foreach(sc => pprint.pprintln(sc))
  }

  println(s"Loaded trent cache: ${result.graphs.size} graphs, sequence: ${result.graphSequence.mkString(", ")}")

  val routePlanner = RoutePlanner(result.graphs, result.transportGraph, result.graphSequence, true)

  // ── DIAGNOSTIC: Why does Floor1→Floor2 route via the elevator? ─────────────

  val floor1Graph = result.graphs("Floor1")
  val floor2Graph = result.graphs("Floor2")

  // 1. Show every StationNode on Floor1 and Floor2 in the TransportGraph,
  //    together with which line owns it and its permission.
  println("\n=== [DIAG 1] TransportGraph station nodes on Floor1 ===")
  val floor1Stations = result.transportGraph.nodes.filter(_.ownerGraph == floor1Graph)
  floor1Stations.foreach { sn =>
    println(s"  ${sn.identifier}  ownerLine=${sn.ownerLine.identifier}  permission=${sn.permission}  localNode=${sn.localNode.identifier}")
  }

  println("\n=== [DIAG 1] TransportGraph station nodes on Floor2 ===")
  val floor2Stations = result.transportGraph.nodes.filter(_.ownerGraph == floor2Graph)
  floor2Stations.foreach { sn =>
    println(s"  ${sn.identifier}  ownerLine=${sn.ownerLine.identifier}  permission=${sn.permission}  localNode=${sn.localNode.identifier}")
  }

  // 2. Show every TransportEdge that crosses Floor1↔Floor2
  //    (i.e. source on Floor1, target on Floor2, or vice-versa).
  println("\n=== [DIAG 2] TransportGraph edges crossing Floor1↔Floor2 ===")
  val crossFloorEdges = result.transportGraph.nodes.flatMap { sn =>
    result.transportGraph.adjacencyList
      .getOrElse(sn, Map.empty)
      .collect {
        case (target, cost)
          if (sn.ownerGraph == floor1Graph && target.ownerGraph == floor2Graph) ||
             (sn.ownerGraph == floor2Graph && target.ownerGraph == floor1Graph) =>
          (sn, target, cost)
      }
  }
  crossFloorEdges.foreach { case (from, to, cost) =>
    println(s"  ${from.identifier} (${from.ownerGraph.identifier}) → ${to.identifier} (${to.ownerGraph.identifier})  cost=$cost  line=${from.ownerLine.identifier}")
  }

  // 3. Show what findPathFuzzy resolves to for Floor1→Floor2
  //    (index 0 = best, 1 = second-best, etc.)
  println("\n=== [DIAG 3] findPathFuzzy results Floor1→Floor2 (top 3) ===")
  for (idx <- 0 until 3) {
    result.transportGraph.findPathFuzzy(floor1Graph, floor2Graph, result.graphSequence, RoutePlanningPreferences.MinimizeTransfers, idx) match {
      case Some(tp) =>
        println(s"  [index=$idx] routeNodes: ${tp.routeNodes.map(_.identifier).mkString(" → ")}")
        println(s"            routeEdges: ${tp.routeEdges.map(e => s"${e.source.identifier}→${e.target.identifier}(${e.cost})").mkString(", ")}")
        println(s"            totalCost : ${tp.totalCost}")
      case None =>
        println(s"  [index=$idx] No path found")
    }
  }

  // 4. Verify whether the staircase's Floor1 local node is actually reachable
  //    from the source node (stair_LLT) — checks if the capitalisation mismatch
  //    (Floor1 has "stair_LLT" but ST_LLT references "Stair_LLT") matters.
  println("\n=== [DIAG 4] ST_LLT Floor1 station localNode reachability ===")
  val stLLT = stairCases.find(_.identifier == "ST_LLT")
  stLLT match {
    case None => println("  ST_LLT not found among compiled StairCases!")
    case Some(sc) =>
      val stLLTFloor1Station = floor1Stations.find(_.ownerLine.identifier == "ST_LLT")
      stLLTFloor1Station match {
        case None => println("  ST_LLT has NO station node on Floor1 in the TransportGraph!")
        case Some(sn) =>
          println(s"  ST_LLT Floor1 station: ${sn.identifier}  localNode=${sn.localNode.identifier}")
          val sourceNode = floor1Graph.nodes.find(_.identifier == "stair_LLT")
          sourceNode match {
            case None => println("  'stair_LLT' node not found in Floor1 graph!")
            case Some(src) =>
              floor1Graph.findPath(src, sn.localNode, enums.VisitingMode.Normal) match {
                case Some(p) =>
                  println(s"  Walk from stair_LLT → ${sn.localNode.identifier}: cost=${p.totalCost(enums.VisitingMode.Normal)}, ${p.routeNodes.size} nodes")
                case None =>
                  println(s"  No intra-map path from stair_LLT → ${sn.localNode.identifier}!")
              }
          }
      }
  }

  // ── END DIAGNOSTICS ────────────────────────────────────────────────────────

  // TEST NAVIGATE BEGIN //
  routePlanner.navigate("Floor1", "Floor2", "stair_LLT", "room_201_out_1", Normal, RoutePlanningPreferences.MinimizePhysicalDemands) match { // This task only need 1-floor of stairclimbing. But the system actually "forced" the user to the main_elevator, which is definitely farther (not expected)
    case Right(navigationPath) =>
      println(s"\n=== Testing TrentRoutePlanner Navigate ===")
      println(s"Path found with ${navigationPath.routeNodes.size} nodes:")
      navigationPath.routeNodes.foreach(node => println(s"   → ${node.toString}"))
      println(s"With ${navigationPath.routeEdges.size} AtomicPaths, taking ${navigationPath.totalCost} seconds:")
      navigationPath.routeEdges.foreach(edge => println(s"   ${edge.source.toString} => ${edge.target.toString} takes ${edge.cost} seconds"))
    case Left(error) =>
      println(s"Navigation error: ${error}")
  }
}