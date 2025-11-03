import enums.VisitingMode.Normal
import enums.{PathType, TransportServicePermission, VisitingMode}

import scala.collection.mutable

object Tester extends App{
  def runTest(): Unit = {
    val nodes = mutable.ListBuffer[TopoNode]()
    val edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("SW_corner", Map.empty)
    val node2 = TopoNode("SE_corner", Map.empty)

    val node3 = TopoNode("SouthExitOutside", Map.empty)
    val node4 = TopoNode("SouthExitInside", Map.empty)
    val node5 = TopoNode("WestExitOutside", Map.empty)
    val node6 = TopoNode("WestExitInside", Map.empty)
    val node7 = TopoNode("EastExitOutside", Map.empty)
    val node8 = TopoNode("EastExitInside", Map.empty) // == OS_hall_outside
    val node9 = TopoNode("hankyu_exit_outside", Map.empty)
    val node10 = TopoNode("hankyu_exit_inside", Map.empty)

    val node11 = TopoNode("OP1_hall", Map.empty)
    val node12 = TopoNode("OP1_hall_outside", Map.empty)
    val node13 = TopoNode("OP2_hall", Map.empty)
    val node14 = TopoNode("OP2_hall_outside", Map.empty)
    val node15 = TopoNode("OEX_hall_low", Map.empty)
    val node16 = TopoNode("OEX_hall_upp", Map.empty)
    val node17 = TopoNode("OEX_hall_upp_outside", Map.empty)
    val node18 = TopoNode("OPS_hall_1", Map.empty)
    val node19 = TopoNode("OPS_hall_1M", Map.empty)
    val node20 = TopoNode("OS_hall", Map.empty)

    val node21 = TopoNode("escalator_high", Map.empty)
    val node22 = TopoNode("staircase_A_1", Map.empty)
    val node23 = TopoNode("staircase_A_1M", Map.empty)
    val node24 = TopoNode("staircase_B", Map.empty)
    val node25 = TopoNode("staircase_C", Map.empty)
    val node26 = TopoNode("staircase_D", Map.empty)
    val node27 = TopoNode("staircase_E", Map.empty)
    val node28 = TopoNode("staircase_E_outside", Map.empty)

    val node29 = TopoNode("west_intersact_1", Map.empty)
    val node30 = TopoNode("west_intersact_2", Map.empty)
    val node31 = TopoNode("east_intersact_1", Map.empty)

    // Add all nodes to the list
    nodes ++= List(
      node1, node2, node3, node4, node5, node6, node7, node8, node9, node10,
      node11, node12, node13, node14, node15, node16, node17, node18, node19,
      node20, node21, node22, node23, node24, node25, node26, node27, node28,
      node29, node30, node31
    )

    println(s"Created ${nodes.size} TopoNodes:")
    nodes.foreach(node => println(s"  - ${node}"))

    // Normal paths on the south
    edges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 5.0), PathType.General) // SouthExitOutside <-> SouthExitInside
    edges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node1, node4, Map(VisitingMode.Normal -> 8.0), PathType.General) // SW_corner <-> SouthExitInside
    edges += AtomicPath(node4, node1, Map(VisitingMode.Normal -> 8.0), PathType.General)

    edges += AtomicPath(node2, node4, Map(VisitingMode.Normal -> 8.0), PathType.General) // SE_corner <-> SouthExitInside
    edges += AtomicPath(node4, node2, Map(VisitingMode.Normal -> 8.0), PathType.General)

    // Normal paths on the west
    edges += AtomicPath(node5, node6, Map(VisitingMode.Normal -> 5.0), PathType.General) // WestExitOutside <-> WestExitInside
    edges += AtomicPath(node6, node5, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node1, node12, Map(VisitingMode.Normal -> 5.0), PathType.General) // SW_corner <-> OP1_hall_outside
    edges += AtomicPath(node12, node1, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node21, node12, Map(VisitingMode.Normal -> 15.0), PathType.General) // escalator_high <-> OP1_hall_outside
    edges += AtomicPath(node12, node21, Map(VisitingMode.Normal -> 15.0), PathType.General)

    edges += AtomicPath(node29, node18, Map(VisitingMode.Normal -> 3.0), PathType.General) // west_intersact_1 <-> OPS_hall_1
    edges += AtomicPath(node18, node29, Map(VisitingMode.Normal -> 3.0), PathType.General)

    edges += AtomicPath(node29, node6, Map(VisitingMode.Normal -> 3.0), PathType.General) // west_intersact_1 <-> WestExitInside
    edges += AtomicPath(node6, node29, Map(VisitingMode.Normal -> 3.0), PathType.General)

    edges += AtomicPath(node12, node6, Map(VisitingMode.Normal -> 3.0), PathType.General) // OP1_hall_outside <-> WestExitInside
    edges += AtomicPath(node6, node12, Map(VisitingMode.Normal -> 3.0), PathType.General)

    edges += AtomicPath(node12, node30, Map(VisitingMode.Normal -> 3.0), PathType.General) // OP1_hall_outside <-> west_intersact_2
    edges += AtomicPath(node30, node12, Map(VisitingMode.Normal -> 3.0), PathType.General)

    // Normal paths on the east
    edges += AtomicPath(node7, node8, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitOutside <-> EastExitInside
    edges += AtomicPath(node8, node7, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node7, node14, Map(VisitingMode.Normal -> 6.0), PathType.General) // EastExitOutside <-> OP2_hall_outside
    edges += AtomicPath(node14, node7, Map(VisitingMode.Normal -> 6.0), PathType.General)

    edges += AtomicPath(node2, node14, Map(VisitingMode.Normal -> 5.0), PathType.General) // SE_corner <-> OP2_hall_outside
    edges += AtomicPath(node14, node2, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node8, node14, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitInside <-> OP2_hall_outside
    edges += AtomicPath(node14, node8, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node8, node31, Map(VisitingMode.Normal -> 7.0), PathType.General) // EastExitInside <-> east_intersact_1
    edges += AtomicPath(node31, node8, Map(VisitingMode.Normal -> 7.0), PathType.General)

    edges += AtomicPath(node10, node31, Map(VisitingMode.Normal -> 4.0), PathType.General) // hankyu_exit_inside <-> east_intersact_1
    edges += AtomicPath(node31, node10, Map(VisitingMode.Normal -> 4.0), PathType.General)

    edges += AtomicPath(node9, node10, Map(VisitingMode.Normal -> 3.0), PathType.General) // hankyu_exit_inside <-> hankyu_exit_outside
    edges += AtomicPath(node10, node9, Map(VisitingMode.Normal -> 3.0), PathType.General)

    edges += AtomicPath(node7, node28, Map(VisitingMode.Normal -> 12.0), PathType.General) // EastExitOutside <-> staircase_E_outside
    edges += AtomicPath(node28, node7, Map(VisitingMode.Normal -> 12.0), PathType.General)

    // Strange stuff
    edges += AtomicPath(node16, node23, Map(VisitingMode.Normal -> 8.0), PathType.General) // OEX_hall_upp <-> staircase_A_1M
    edges += AtomicPath(node23, node16, Map(VisitingMode.Normal -> 8.0), PathType.General)

    edges += AtomicPath(node20, node22, Map(VisitingMode.Normal -> 5.0), PathType.General) // OS_hall <-> staircase_A_1
    edges += AtomicPath(node22, node20, Map(VisitingMode.Normal -> 5.0), PathType.General)

    // Restricted paths (all as General for testing)
    edges += AtomicPath(node12, node11, Map(VisitingMode.Normal -> 5.0), PathType.General) // OP1_hall_outside <-> OP1_hall
    edges += AtomicPath(node11, node12, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node14, node13, Map(VisitingMode.Normal -> 5.0), PathType.General) // OP2_hall_outside <-> OP2_hall
    edges += AtomicPath(node13, node14, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node8, node18, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitInside -> OPS_hall_1

    edges += AtomicPath(node20, node8, Map(VisitingMode.Normal -> 4.0), PathType.General) // OS_hall -> EastExitInside

    edges += AtomicPath(node15, node29, Map(VisitingMode.Normal -> 5.0), PathType.General) // OEX_hall_low <-> west_intersact_1
    edges += AtomicPath(node29, node15, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node16, node17, Map(VisitingMode.Normal -> 5.0), PathType.General) // OEX_hall_upp <-> OEX_hall_upp_outside
    edges += AtomicPath(node17, node16, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node30, node17, Map(VisitingMode.Normal -> 6.0), PathType.General) // west_intersact_2 <-> OEX_hall_upp_outside
    edges += AtomicPath(node17, node30, Map(VisitingMode.Normal -> 6.0), PathType.General)

    edges += AtomicPath(node30, node26, Map(VisitingMode.Normal -> 10.0), PathType.General) // west_intersact_2 -> staircase_D

    edges += AtomicPath(node31, node24, Map(VisitingMode.Normal -> 7.0), PathType.General) // east_intersact_1 -> staircase_B

    edges += AtomicPath(node24, node31, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_B -> east_intersact_1

    edges += AtomicPath(node25, node4, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_C -> SouthExitInside
    edges += AtomicPath(node4, node25, Map(VisitingMode.Normal -> 6.0), PathType.General) // SouthExitInside -> staircase_C

    edges += AtomicPath(node26, node30, Map(VisitingMode.Normal -> 8.0), PathType.General) // staircase_D -> west_intersact_2

    edges += AtomicPath(node28, node27, Map(VisitingMode.Normal -> 3.0), PathType.General) // staircase_E_outside -> staircase_E

    val graph = NavigationGraph("testMap", nodes.toList, edges.toList)

    println(s"Created ${edges.size} AtomicPaths")
    edges.foreach(edge => println(s"  - ${edge}"))


    // Test the graph
    val start = node1 // NW_corner
    val goal = node27 //

    println(s"Testing path from ${start.identifier} to ${goal.identifier}")
    val path = graph.findPath(start, goal, Normal)

    path match {
      case Some(foundPath) =>
        println(s"✅ Path found: ${foundPath.routeNodes.map(_.identifier).mkString(" -> ")}")
      case None =>
        println("❌ No path found")
    }
  }

  // Create simple NavigationGraphs for testing
  def createSimpleGraph(identifier: String): NavigationGraph = {
    NavigationGraph(identifier)
  }

  runTest()
}


object TransportTester extends App {

  // Create a simple test graph
  def createTestGraph(): TransportGraph = {
    val naviGraph1M = NavigationGraph.createSimpleGraph("Floor1M")
    val naviGraph1 = NavigationGraph.createSimpleGraph("Floor1")
    val naviGraphB1M = NavigationGraph.createSimpleGraph("FloorB1M")
    val naviGraphB1 = NavigationGraph.createSimpleGraph("FloorB1")
    val naviGraphB2 = NavigationGraph.createSimpleGraph("FloorB2")
    val naviGraphB3 = NavigationGraph.createSimpleGraph("FloorB3")

    val lobbyNode = TopoNode("1_hall", Map.empty)

    // Create mock elevator bank
    val OPSBank = ElevatorBank(
      identifier = "OPS",
      // Map with some entries
      stationNodes = Map(
        naviGraph1M -> TopoNode("OPS_1M_hall", Map.empty),
        naviGraph1 -> TopoNode("OPS_1_hall", Map.empty),
        naviGraphB2 -> TopoNode("OPS_B2_hall", Map.empty),
        naviGraphB3 -> TopoNode("OPS_B3_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph1M -> 15.5,
        naviGraph1 -> 12.0,
        naviGraphB2 -> 3.5,
        naviGraphB3 -> 0.0
      ),
      stationPermissions = Map(
        naviGraph1M -> TransportServicePermission.FullyGranted,
        naviGraph1 -> TransportServicePermission.DepartOnly, // TODO: Fix permission limit
        naviGraphB2 -> TransportServicePermission.FullyGranted,
        naviGraphB3 -> TransportServicePermission.FullyGranted
      ),
      maxVelocity = 2.0,
      acceleration = 1.0
    )

    // Create mock elevator bank
    val BBFFBank = ElevatorBank(
      identifier = "BBFF",
      // Map with some entries
      stationNodes = Map(
        naviGraph1 -> TopoNode("BBFF_1_hall", Map.empty),
        naviGraphB1 -> TopoNode("BBFF_B1_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph1 -> 4.5,
        naviGraphB1 -> 0.0
      ),
      stationPermissions = Map(
        naviGraph1 -> TransportServicePermission.FullyGranted,
        naviGraphB1 -> TransportServicePermission.FullyGranted
      ),
      maxVelocity = 1.0,
      acceleration = 1.0
    )

    // Create mock elevator bank
    val PHFFBank = ElevatorBank(
      identifier = "PHFF",
      // Map with some entries
      stationNodes = Map(
        naviGraph1 -> TopoNode("PHFF_1_hall", Map.empty),
        naviGraphB1M -> TopoNode("PHFF_B1M_hall", Map.empty),
        naviGraphB1 -> TopoNode("PHFF_B1_hall", Map.empty),
        naviGraphB2 -> TopoNode("PHFF_B2_hall", Map.empty),
        naviGraphB3 -> TopoNode("PHFF_B3_hall", Map.empty),
      ),
      stationLocations = Map(
        naviGraph1 -> 12.0,
        naviGraphB1M -> 9.5,
        naviGraphB1 -> 7.5,
        naviGraphB2 -> 3.5,
        naviGraphB3 -> 0.0
      ),
      stationPermissions = Map(
        naviGraph1 -> TransportServicePermission.FullyGranted,
        naviGraphB1M -> TransportServicePermission.DepartOnly,
        naviGraphB1 -> TransportServicePermission.FullyGranted,
        naviGraphB2 -> TransportServicePermission.FullyGranted,
        naviGraphB3 -> TransportServicePermission.FullyGranted
      ),
      maxVelocity = 2.0,
      acceleration = 1.0
    )

    TransportGraph(List(OPSBank, BBFFBank, PHFFBank))
  }
  
  def runTest(): Unit = {
    println("=== Creating Test Transport Graph ===")

    // Create the test graph
    val graph = createTestGraph()

    println(s"Graph created with ${graph.nodes.size} nodes")
    graph.nodes.foreach(node => println(s"  - Node: ${node.identifier}"))

    // Test finding a path
    if (graph.nodes.size >= 2) {
      val start = graph.nodes.head
      val goal = graph.nodes(1)

      println(s"\n=== Testing Path Finding ===")
      println(s"Start: ${start.identifier}")
      println(s"Goal: ${goal.identifier}")

      // Find path (3rd param for testing only, to-be-connected to other subsystems)
      val result = graph.findPath(start, goal, List("Floor1M", "Floor1", "FloorB1M", "FloorB1", "FloorB2", "FloorB3"))

      result match {
        case Some(path) =>
          println(s"Path found with ${path.routeNodes.size} nodes:")
          path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        case None =>
          println("No path found")
      }
    } else {
      println("Not enough nodes to test path finding")
    }

    // Test adjacency list
    println(s"\n=== Testing Adjacency List ===")
    graph.adjacencyList.foreach { case (node, neighbors) =>
      println(s"${node.identifier} -> ${neighbors.map(_.identifier).mkString(", ")}")
    }
  }

  // Run the test
  runTest()
}