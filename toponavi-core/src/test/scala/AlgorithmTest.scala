import enums.VisitingMode.Normal
import enums.{AttributeValue, PathType, TransportServicePermission, VisitingMode}

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
    val start = nodes.find(n => n.identifier == "OPS_hall_1").get // NW_corner
    val goal = nodes.find(n => n.identifier == "OEX_hall_low").get //

    println(s"Testing path from ${start.identifier} to ${goal.identifier}")
    val path = graph.findPath(start, goal, Normal)

    path match {
      case Some(foundPath) =>
        println(s"✅ Path found: ${foundPath.routeNodes.map(_.identifier).mkString(" -> ")}")
        print(s"Costing ${foundPath.totalCost(VisitingMode.Normal)} seconds")
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

    // Create additional floors for expanded test cases
    val naviGraph2 = NavigationGraph.createSimpleGraph("Floor2")
    val naviGraph3 = NavigationGraph.createSimpleGraph("Floor3")
    val naviGraph4 = NavigationGraph.createSimpleGraph("Floor4")
    val naviGraph5 = NavigationGraph.createSimpleGraph("Floor5")
    val naviGraph6 = NavigationGraph.createSimpleGraph("Floor6")
    val naviGraph7 = NavigationGraph.createSimpleGraph("Floor7")
    val naviGraph8 = NavigationGraph.createSimpleGraph("Floor8")
    val naviGraph9 = NavigationGraph.createSimpleGraph("Floor9")
    val naviGraph10 = NavigationGraph.createSimpleGraph("Floor10")
    val naviGraph11 = NavigationGraph.createSimpleGraph("Floor11")
    val naviGraph12 = NavigationGraph.createSimpleGraph("Floor12")
    val naviGraph13 = NavigationGraph.createSimpleGraph("Floor13")
    val naviGraph14 = NavigationGraph.createSimpleGraph("Floor14")
    val naviGraph15 = NavigationGraph.createSimpleGraph("Floor15")
    val naviGraph16 = NavigationGraph.createSimpleGraph("Floor16")
    val naviGraph17 = NavigationGraph.createSimpleGraph("Floor17")
    val naviGraph18 = NavigationGraph.createSimpleGraph("Floor18")
    val naviGraph19 = NavigationGraph.createSimpleGraph("Floor19")
    val naviGraph20 = NavigationGraph.createSimpleGraph("Floor20")
    val naviGraph21 = NavigationGraph.createSimpleGraph("Floor21")
    val naviGraph22 = NavigationGraph.createSimpleGraph("Floor22")
    val naviGraph23 = NavigationGraph.createSimpleGraph("Floor23")
    val naviGraph24 = NavigationGraph.createSimpleGraph("Floor24")
    val naviGraph25 = NavigationGraph.createSimpleGraph("Floor25")
    val naviGraph26 = NavigationGraph.createSimpleGraph("Floor26")
    val naviGraph27 = NavigationGraph.createSimpleGraph("Floor27")
    val naviGraph28 = NavigationGraph.createSimpleGraph("Floor28")
    val naviGraph29 = NavigationGraph.createSimpleGraph("Floor29")
    val naviGraph30 = NavigationGraph.createSimpleGraph("Floor30")
    val naviGraph31 = NavigationGraph.createSimpleGraph("Floor31")
    val naviGraph32 = NavigationGraph.createSimpleGraph("Floor32")
    val naviGraph33 = NavigationGraph.createSimpleGraph("Floor33")
    val naviGraph34 = NavigationGraph.createSimpleGraph("Floor34")
    val naviGraph35 = NavigationGraph.createSimpleGraph("Floor35")
    val naviGraph36 = NavigationGraph.createSimpleGraph("Floor36")
    val naviGraph37 = NavigationGraph.createSimpleGraph("Floor37")
    val naviGraph38 = NavigationGraph.createSimpleGraph("Floor38")
    val naviGraph39 = NavigationGraph.createSimpleGraph("Floor39")
    val naviGraph40 = NavigationGraph.createSimpleGraph("Floor40")
    val naviGraph41 = NavigationGraph.createSimpleGraph("Floor41")
    val naviGraph42 = NavigationGraph.createSimpleGraph("Floor42")
    val naviGraph43 = NavigationGraph.createSimpleGraph("Floor43")
    val naviGraph44 = NavigationGraph.createSimpleGraph("Floor44")
    val naviGraph45 = NavigationGraph.createSimpleGraph("Floor45")
    val naviGraph46 = NavigationGraph.createSimpleGraph("Floor46")
    val naviGraph47 = NavigationGraph.createSimpleGraph("Floor47")
    val naviGraph48 = NavigationGraph.createSimpleGraph("Floor48")
    val naviGraph49 = NavigationGraph.createSimpleGraph("Floor49")
    val naviGraph50 = NavigationGraph.createSimpleGraph("Floor50")
    val naviGraph51 = NavigationGraph.createSimpleGraph("Floor51")
    val naviGraph52 = NavigationGraph.createSimpleGraph("Floor52")
    val naviGraph53 = NavigationGraph.createSimpleGraph("Floor53")
    val naviGraph54 = NavigationGraph.createSimpleGraph("Floor54")
    val naviGraph55 = NavigationGraph.createSimpleGraph("Floor55")
    val naviGraph56 = NavigationGraph.createSimpleGraph("Floor56")
    val naviGraph57 = NavigationGraph.createSimpleGraph("Floor57")
    val naviGraph58 = NavigationGraph.createSimpleGraph("Floor58")
    val naviGraph59 = NavigationGraph.createSimpleGraph("Floor59")

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

    // PHS-1/2: B1, 2~4
    val PHSBank = ElevatorBank(
      identifier = "PHS",
      stationNodes = Map(
        naviGraphB1 -> TopoNode("PHS_B1_hall", Map.empty),
        naviGraph2 -> TopoNode("PHS_2_hall", Map.empty),
        naviGraph3 -> TopoNode("PHS_3_hall", Map.empty),
        naviGraph4 -> TopoNode("PHS_4_hall", Map.empty)
      ),
      stationLocations = Map(naviGraphB1 -> 0.0, naviGraph2 -> 9.0, naviGraph3 -> 13.0, naviGraph4 -> 18.0),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 1.0,
      acceleration = 1.0
    )

    // OP1: 1, 3~10, 12~17
    val OP1Bank = ElevatorBank(
      identifier = "OP1",
      stationNodes = Map(
        naviGraph1 -> TopoNode("OP1_1_hall", Map.empty),
        naviGraph3 -> TopoNode("OP1_3_hall", Map.empty),
        naviGraph4 -> TopoNode("OP1_4_hall", Map.empty),
        naviGraph5 -> TopoNode("OP1_5_hall", Map.empty),
        naviGraph6 -> TopoNode("OP1_6_hall", Map.empty),
        naviGraph7 -> TopoNode("OP1_7_hall", Map.empty),
        naviGraph8 -> TopoNode("OP1_8_hall", Map.empty),
        naviGraph9 -> TopoNode("OP1_9_hall", Map.empty),
        naviGraph10 -> TopoNode("OP1_10_hall", Map.empty),
        naviGraph12 -> TopoNode("OP1_12_hall", Map.empty),
        naviGraph13 -> TopoNode("OP1_13_hall", Map.empty),
        naviGraph14 -> TopoNode("OP1_14_hall", Map.empty),
        naviGraph15 -> TopoNode("OP1_15_hall", Map.empty),
        naviGraph16 -> TopoNode("OP1_16_hall", Map.empty),
        naviGraph17 -> TopoNode("OP1_17_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph1 -> calculateHeight(1),
        naviGraph3 -> calculateHeight(3),
        naviGraph4 -> calculateHeight(4),
        naviGraph5 -> calculateHeight(5),
        naviGraph6 -> calculateHeight(6),
        naviGraph7 -> calculateHeight(7),
        naviGraph8 -> calculateHeight(8),
        naviGraph9 -> calculateHeight(9),
        naviGraph10 -> calculateHeight(10),
        naviGraph12 -> calculateHeight(12),
        naviGraph13 -> calculateHeight(13),
        naviGraph14 -> calculateHeight(14),
        naviGraph15 -> calculateHeight(15),
        naviGraph16 -> calculateHeight(16),
        naviGraph17 -> calculateHeight(17)
      ),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 2.5,
      acceleration = 1.0
    )

    // OP2: 1, 19~27
    val OP2Bank = ElevatorBank(
      identifier = "OP2",
      stationNodes = Map(
        naviGraph1 -> TopoNode("OP2_1_hall", Map.empty),
        naviGraph19 -> TopoNode("OP2_19_hall", Map.empty),
        naviGraph20 -> TopoNode("OP2_20_hall", Map.empty),
        naviGraph21 -> TopoNode("OP2_21_hall", Map.empty),
        naviGraph22 -> TopoNode("OP2_22_hall", Map.empty),
        naviGraph23 -> TopoNode("OP2_23_hall", Map.empty),
        naviGraph24 -> TopoNode("OP2_24_hall", Map.empty),
        naviGraph25 -> TopoNode("OP2_25_hall", Map.empty),
        naviGraph26 -> TopoNode("OP2_26_hall", Map.empty),
        naviGraph27 -> TopoNode("OP2_27_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph1 -> calculateHeight(1),
        naviGraph19 -> calculateHeight(19),
        naviGraph20 -> calculateHeight(20),
        naviGraph21 -> calculateHeight(21),
        naviGraph22 -> calculateHeight(22),
        naviGraph23 -> calculateHeight(23),
        naviGraph24 -> calculateHeight(24),
        naviGraph25 -> calculateHeight(25),
        naviGraph26 -> calculateHeight(26),
        naviGraph27 -> calculateHeight(27)
      ),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 4.0,
      acceleration = 1.0
    )

    // OP3: 30, 31~37, 40~46
    val OP3Bank = ElevatorBank(
      identifier = "OP3",
      stationNodes = Map(
        naviGraph30 -> TopoNode("OP3_30_hall", Map.empty),
        naviGraph31 -> TopoNode("OP3_31_hall", Map.empty),
        naviGraph32 -> TopoNode("OP3_32_hall", Map.empty),
        naviGraph33 -> TopoNode("OP3_33_hall", Map.empty),
        naviGraph34 -> TopoNode("OP3_34_hall", Map.empty),
        naviGraph35 -> TopoNode("OP3_35_hall", Map.empty),
        naviGraph36 -> TopoNode("OP3_36_hall", Map.empty),
        naviGraph37 -> TopoNode("OP3_37_hall", Map.empty),
        naviGraph40 -> TopoNode("OP3_40_hall", Map.empty),
        naviGraph41 -> TopoNode("OP3_41_hall", Map.empty),
        naviGraph42 -> TopoNode("OP3_42_hall", Map.empty),
        naviGraph43 -> TopoNode("OP3_43_hall", Map.empty),
        naviGraph44 -> TopoNode("OP3_44_hall", Map.empty),
        naviGraph45 -> TopoNode("OP3_45_hall", Map.empty),
        naviGraph46 -> TopoNode("OP3_46_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph30 -> calculateHeight(30),
        naviGraph31 -> calculateHeight(31),
        naviGraph32 -> calculateHeight(32),
        naviGraph33 -> calculateHeight(33),
        naviGraph34 -> calculateHeight(34),
        naviGraph35 -> calculateHeight(35),
        naviGraph36 -> calculateHeight(36),
        naviGraph37 -> calculateHeight(37),
        naviGraph40 -> calculateHeight(40),
        naviGraph41 -> calculateHeight(41),
        naviGraph42 -> calculateHeight(42),
        naviGraph43 -> calculateHeight(43),
        naviGraph44 -> calculateHeight(44),
        naviGraph45 -> calculateHeight(45),
        naviGraph46 -> calculateHeight(46)
      ),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 3.5,
      acceleration = 1.0
    )

    // OP4: 29, 47~48, 50~59
    val OP4Bank = ElevatorBank(
      identifier = "OP4",
      stationNodes = Map(
        naviGraph29 -> TopoNode("OP4_29_hall", Map.empty),
        naviGraph47 -> TopoNode("OP4_47_hall", Map.empty),
        naviGraph48 -> TopoNode("OP4_48_hall", Map.empty),
        naviGraph50 -> TopoNode("OP4_50_hall", Map.empty),
        naviGraph51 -> TopoNode("OP4_51_hall", Map.empty),
        naviGraph52 -> TopoNode("OP4_52_hall", Map.empty),
        naviGraph53 -> TopoNode("OP4_53_hall", Map.empty),
        naviGraph54 -> TopoNode("OP4_54_hall", Map.empty),
        naviGraph55 -> TopoNode("OP4_55_hall", Map.empty),
        naviGraph56 -> TopoNode("OP4_56_hall", Map.empty),
        naviGraph57 -> TopoNode("OP4_57_hall", Map.empty),
        naviGraph58 -> TopoNode("OP4_58_hall", Map.empty),
        naviGraph59 -> TopoNode("OP4_59_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph29 -> calculateHeight(29),
        naviGraph47 -> calculateHeight(47),
        naviGraph48 -> calculateHeight(48),
        naviGraph50 -> calculateHeight(50),
        naviGraph51 -> calculateHeight(51),
        naviGraph52 -> calculateHeight(52),
        naviGraph53 -> calculateHeight(53),
        naviGraph54 -> calculateHeight(54),
        naviGraph55 -> calculateHeight(55),
        naviGraph56 -> calculateHeight(56),
        naviGraph57 -> calculateHeight(57),
        naviGraph58 -> calculateHeight(58),
        naviGraph59 -> calculateHeight(59)
      ),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 5.0,
      acceleration = 1.0
    )

    // OEX_low: 1, 29
    val OEXLowBank = ElevatorBank(
      identifier = "OEX_low",
      stationNodes = Map(
        naviGraph1 -> TopoNode("OEX_low_1_hall", Map.empty),
        naviGraph29 -> TopoNode("OEX_low_29_hall", Map.empty)
      ),
      stationLocations = Map(naviGraph1 -> 0, naviGraph29 -> 140),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 6.0,
      acceleration = 1.0
    )

    // OEX_upp: 1M, 30
    val OEXUppBank = ElevatorBank(
      identifier = "OEX_upp",
      stationNodes = Map(
        naviGraph1M -> TopoNode("OEX_upp_1M_hall", Map.empty),
        naviGraph30 -> TopoNode("OEX_upp_30_hall", Map.empty)
      ),
      stationLocations = Map(naviGraph1M -> 0, naviGraph30 -> 140),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 6.0,
      acceleration = 1.0
    )

    // OEX6: 29~30
    val OEX6Bank = ElevatorBank(
      identifier = "OEX6",
      stationNodes = Map(
        naviGraph29 -> TopoNode("OEX6_29_hall", Map.empty),
        naviGraph30 -> TopoNode("OEX6_30_hall", Map.empty)
      ),
      stationLocations = Map(naviGraph29 -> 0.0, naviGraph30 -> 4.5),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 1.0,
      acceleration = 0.8
    )

    TransportGraph(List(
      OPSBank, BBFFBank, PHFFBank,
      OP1Bank, OP2Bank, OP3Bank, OP4Bank,
      OEXLowBank, OEXUppBank, OEX6Bank,
      PHSBank
    ))
  }

  // Now define station locations using the existing graphs
  def calculateHeight(floor: Int): Double = {
    if (floor <= 0) floor * 4.0 else (floor - 1) * 4.0 + 12.0
  }
  
  def runTest(): Unit = {
    println("=== Creating Test Transport Graph ===")

    // Create the test graph
    val graph = createTestGraph()

    println(s"Graph created with ${graph.nodes.size} nodes")
    graph.nodes.foreach(node => println(s"  - Node: ${node.identifier}"))

    // Test finding a path
    if (graph.nodes.size >= 2) {
      val start = graph.nodes.find(node => node.identifier == "OP1_Floor4").getOrElse(throw RuntimeException(""))
      val goal = graph.nodes.find(node => node.identifier == "OP4_Floor59").getOrElse(throw RuntimeException(""))

      println(s"\n=== Testing Path Finding ===")
      println(s"Start: ${start.identifier}")
      println(s"Goal: ${goal.identifier}")

      // Find path (3rd param for testing only, TODO: to-be-connected to other subsystems)
      val result = graph.findPath(start, goal, List(
        "Floor59", "Floor58", "Floor57", "Floor56", "Floor55", "Floor54", "Floor53", "Floor52", "Floor51", "Floor50",
        "Floor49", "Floor48", "Floor47", "Floor46", "Floor45", "Floor44", "Floor43", "Floor42", "Floor41", "Floor40",
        "Floor39", "Floor38", "Floor37", "Floor36", "Floor35", "Floor34", "Floor33", "Floor32", "Floor31", "Floor30",
        "Floor29", "Floor28", "Floor27", "Floor26", "Floor25", "Floor24", "Floor23", "Floor22", "Floor21", "Floor20",
        "Floor19", "Floor18", "Floor17", "Floor16", "Floor15", "Floor14", "Floor13", "Floor12", "Floor11", "Floor10",
        "Floor9", "Floor8", "Floor7", "Floor6", "Floor5", "Floor4", "Floor3", "Floor2", "Floor1M", "Floor1",
        "FloorB1M", "FloorB1", "FloorB2", "FloorB3"
      ))

      result match {
        case Some(path) =>
          println(s"Path found with ${path.routeNodes.size} nodes:")
          path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
          println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost(VisitingMode.Normal)} seconds:")
          path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier}"))
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



object HybridMapTest extends App {

  def runTest(): Unit = {
    val lobby: NavigationGraph = generateLobbyMap()
    val carpark: NavigationGraph = generateCarparkMap()
    val floor29: NavigationGraph = generateFloor29Map()

    // OEX_low: 1, 29
    val OEXLowBank = ElevatorBank(
      identifier = "OEX_low",
      stationNodes = Map(
        lobby -> lobby.nodes.find(n => n.identifier == "OEX_low").get,
        floor29 -> floor29.nodes.find(n => n.identifier == "OEX_low").get
      ),
      stationLocations = Map(lobby -> 0, floor29 -> 140),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 6.0,
      acceleration = 1.0
    )

    val OPSBank = ElevatorBank(
      identifier = "OPS",
      stationNodes = Map(
        lobby -> lobby.nodes.find(n => n.identifier == "OPS").get,
        carpark -> carpark.nodes.find(n => n.identifier == "OPS").get
      ),
      stationLocations = Map(lobby -> 10.0, carpark -> 0.0),
      stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
      maxVelocity = 1.5,
      acceleration = 0.8
    )

    println(s"OEX: ${OEXLowBank.stationNodes}")
    println(s"OPS: ${OPSBank.stationNodes}")
    println(s"Carpark: ${carpark.nodes}")
    println(s"Lobby: ${lobby.nodes}")
    println(s"Floor30: ${floor29.nodes}")

    val transportGraph = TransportGraph(List(OEXLowBank, OPSBank))
    // Test finding a path
    if (transportGraph.nodes.size >= 2) {
      val start = transportGraph.nodes.find(node => node.identifier == "OPS@Carpark").getOrElse(throw RuntimeException(""))
      val goal = transportGraph.nodes.find(node => node.identifier == "OEX_low@Floor29").getOrElse(throw RuntimeException(""))

      println(s"\n=== Testing Path Finding ===")
      println(s"Start: ${start.identifier}")
      println(s"Goal: ${goal.identifier}")

      // Find path (3rd param for testing only, TODO: to-be-connected to other subsystems)
      val result = transportGraph.findPath(start, goal, List("Floor29", "Lobby", "Carpark"))

      result match {
        case Some(path) =>
          println(s"Path found with ${path.routeNodes.size} nodes:")
          path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
          println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost(VisitingMode.Normal)} seconds:")
          path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.costs(VisitingMode.Normal)} seconds"))
        case None =>
          println("No path found")
      }
    } else {
      println("Not enough nodes to test path finding")
    }
  }

  private def generateLobbyMap(): NavigationGraph = {
    val lobbyNodes = mutable.ListBuffer[TopoNode]()
    val lobbyEdges = mutable.ListBuffer[AtomicPath]()

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
    val node15 = TopoNode("OEX_low", Map.empty)
    val node16 = TopoNode("OEX_hall_upp", Map.empty)
    val node17 = TopoNode("OEX_hall_upp_outside", Map.empty)
    val node18 = TopoNode("OPS", Map.empty)
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
    lobbyNodes ++= List(
      node1, node2, node3, node4, node5, node6, node7, node8, node9, node10,
      node11, node12, node13, node14, node15, node16, node17, node18, node19,
      node20, node21, node22, node23, node24, node25, node26, node27, node28,
      node29, node30, node31
    )


    // Normal paths on the south
    lobbyEdges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 5.0), PathType.General) // SouthExitOutside <-> SouthExitInside
    lobbyEdges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node1, node4, Map(VisitingMode.Normal -> 8.0), PathType.General) // SW_corner <-> SouthExitInside
    lobbyEdges += AtomicPath(node4, node1, Map(VisitingMode.Normal -> 8.0), PathType.General)

    lobbyEdges += AtomicPath(node2, node4, Map(VisitingMode.Normal -> 8.0), PathType.General) // SE_corner <-> SouthExitInside
    lobbyEdges += AtomicPath(node4, node2, Map(VisitingMode.Normal -> 8.0), PathType.General)

    // Normal paths on the west
    lobbyEdges += AtomicPath(node5, node6, Map(VisitingMode.Normal -> 5.0), PathType.General) // WestExitOutside <-> WestExitInside
    lobbyEdges += AtomicPath(node6, node5, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node1, node12, Map(VisitingMode.Normal -> 5.0), PathType.General) // SW_corner <-> OP1_hall_outside
    lobbyEdges += AtomicPath(node12, node1, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node21, node12, Map(VisitingMode.Normal -> 15.0), PathType.General) // escalator_high <-> OP1_hall_outside
    lobbyEdges += AtomicPath(node12, node21, Map(VisitingMode.Normal -> 15.0), PathType.General)

    lobbyEdges += AtomicPath(node29, node18, Map(VisitingMode.Normal -> 3.0), PathType.General) // west_intersact_1 <-> OPS_hall_1
    lobbyEdges += AtomicPath(node18, node29, Map(VisitingMode.Normal -> 3.0), PathType.General)

    lobbyEdges += AtomicPath(node29, node6, Map(VisitingMode.Normal -> 3.0), PathType.General) // west_intersact_1 <-> WestExitInside
    lobbyEdges += AtomicPath(node6, node29, Map(VisitingMode.Normal -> 3.0), PathType.General)

    lobbyEdges += AtomicPath(node12, node6, Map(VisitingMode.Normal -> 3.0), PathType.General) // OP1_hall_outside <-> WestExitInside
    lobbyEdges += AtomicPath(node6, node12, Map(VisitingMode.Normal -> 3.0), PathType.General)

    lobbyEdges += AtomicPath(node12, node30, Map(VisitingMode.Normal -> 3.0), PathType.General) // OP1_hall_outside <-> west_intersact_2
    lobbyEdges += AtomicPath(node30, node12, Map(VisitingMode.Normal -> 3.0), PathType.General)

    // Normal paths on the east
    lobbyEdges += AtomicPath(node7, node8, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitOutside <-> EastExitInside
    lobbyEdges += AtomicPath(node8, node7, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node7, node14, Map(VisitingMode.Normal -> 6.0), PathType.General) // EastExitOutside <-> OP2_hall_outside
    lobbyEdges += AtomicPath(node14, node7, Map(VisitingMode.Normal -> 6.0), PathType.General)

    lobbyEdges += AtomicPath(node2, node14, Map(VisitingMode.Normal -> 5.0), PathType.General) // SE_corner <-> OP2_hall_outside
    lobbyEdges += AtomicPath(node14, node2, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node8, node14, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitInside <-> OP2_hall_outside
    lobbyEdges += AtomicPath(node14, node8, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node8, node31, Map(VisitingMode.Normal -> 7.0), PathType.General) // EastExitInside <-> east_intersact_1
    lobbyEdges += AtomicPath(node31, node8, Map(VisitingMode.Normal -> 7.0), PathType.General)

    lobbyEdges += AtomicPath(node10, node31, Map(VisitingMode.Normal -> 4.0), PathType.General) // hankyu_exit_inside <-> east_intersact_1
    lobbyEdges += AtomicPath(node31, node10, Map(VisitingMode.Normal -> 4.0), PathType.General)

    lobbyEdges += AtomicPath(node9, node10, Map(VisitingMode.Normal -> 3.0), PathType.General) // hankyu_exit_inside <-> hankyu_exit_outside
    lobbyEdges += AtomicPath(node10, node9, Map(VisitingMode.Normal -> 3.0), PathType.General)

    lobbyEdges += AtomicPath(node7, node28, Map(VisitingMode.Normal -> 12.0), PathType.General) // EastExitOutside <-> staircase_E_outside
    lobbyEdges += AtomicPath(node28, node7, Map(VisitingMode.Normal -> 12.0), PathType.General)

    // Strange stuff
    lobbyEdges += AtomicPath(node16, node23, Map(VisitingMode.Normal -> 8.0), PathType.General) // OEX_hall_upp <-> staircase_A_1M
    lobbyEdges += AtomicPath(node23, node16, Map(VisitingMode.Normal -> 8.0), PathType.General)

    lobbyEdges += AtomicPath(node20, node22, Map(VisitingMode.Normal -> 5.0), PathType.General) // OS_hall <-> staircase_A_1
    lobbyEdges += AtomicPath(node22, node20, Map(VisitingMode.Normal -> 5.0), PathType.General)

    // Restricted paths (all as General for testing)
    lobbyEdges += AtomicPath(node12, node11, Map(VisitingMode.Normal -> 5.0), PathType.General) // OP1_hall_outside <-> OP1_hall
    lobbyEdges += AtomicPath(node11, node12, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node14, node13, Map(VisitingMode.Normal -> 5.0), PathType.General) // OP2_hall_outside <-> OP2_hall
    lobbyEdges += AtomicPath(node13, node14, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node8, node18, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitInside -> OPS_hall_1

    lobbyEdges += AtomicPath(node20, node8, Map(VisitingMode.Normal -> 4.0), PathType.General) // OS_hall -> EastExitInside

    lobbyEdges += AtomicPath(node15, node29, Map(VisitingMode.Normal -> 5.0), PathType.General) // OEX_hall_low <-> west_intersact_1
    lobbyEdges += AtomicPath(node29, node15, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node16, node17, Map(VisitingMode.Normal -> 5.0), PathType.General) // OEX_hall_upp <-> OEX_hall_upp_outside
    lobbyEdges += AtomicPath(node17, node16, Map(VisitingMode.Normal -> 5.0), PathType.General)

    lobbyEdges += AtomicPath(node30, node17, Map(VisitingMode.Normal -> 6.0), PathType.General) // west_intersact_2 <-> OEX_hall_upp_outside
    lobbyEdges += AtomicPath(node17, node30, Map(VisitingMode.Normal -> 6.0), PathType.General)

    lobbyEdges += AtomicPath(node30, node26, Map(VisitingMode.Normal -> 10.0), PathType.General) // west_intersact_2 -> staircase_D

    lobbyEdges += AtomicPath(node31, node24, Map(VisitingMode.Normal -> 7.0), PathType.General) // east_intersact_1 -> staircase_B

    lobbyEdges += AtomicPath(node24, node31, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_B -> east_intersact_1

    lobbyEdges += AtomicPath(node25, node4, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_C -> SouthExitInside
    lobbyEdges += AtomicPath(node4, node25, Map(VisitingMode.Normal -> 6.0), PathType.General) // SouthExitInside -> staircase_C

    lobbyEdges += AtomicPath(node26, node30, Map(VisitingMode.Normal -> 8.0), PathType.General) // staircase_D -> west_intersact_2

    lobbyEdges += AtomicPath(node28, node27, Map(VisitingMode.Normal -> 3.0), PathType.General) // staircase_E_outside -> staircase_E

    NavigationGraph("Lobby", lobbyNodes.toList, lobbyEdges.toList)
  }

  private def generateFloor29Map(): NavigationGraph = {
    val floor30Nodes = mutable.ListBuffer[TopoNode]()
    val floor30Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("NW_corner", Map.empty)
    val node2 = TopoNode("NE_corner", Map.empty)
    val node3 = TopoNode("SW_corner", Map.empty)
    val node4 = TopoNode("SE_corner", Map.empty)

    val node5 = TopoNode("inside_room_01", Map("description" -> AttributeValue.StringValue("inside the door of room 01")))
    val node6 = TopoNode("inside_room_02", Map("description" -> AttributeValue.StringValue("inside the door of room 02")))
    val node7 = TopoNode("inside_room_03", Map("description" -> AttributeValue.StringValue("inside the door of room 03")))
    val node8 = TopoNode("outside_room_01", Map("description" -> AttributeValue.StringValue("outside the door of room 01")))
    val node9 = TopoNode("outside_room_02", Map("description" -> AttributeValue.StringValue("outside the door of room 02")))

    val node10 = TopoNode("OP3_hall", Map("description" -> AttributeValue.StringValue("zone-3 elevator hall")))
    val node11 = TopoNode("OP3_hall_outside", Map.empty)
    val node12 = TopoNode("OS_hall", Map("description" -> AttributeValue.StringValue("service elevator hall")))
    val node13 = TopoNode("OS_hall_outside", Map.empty)
    val node14 = TopoNode("OEX_low", Map("description" -> AttributeValue.StringValue("express elevator hall")))
    val node15 = TopoNode("OEX_hall_outside", Map.empty)
    val node16 = TopoNode("OEX6_hall", Map("description" -> AttributeValue.StringValue("just beside the door of the ferry elevator")))

    val node17 = TopoNode("staircase_A_outside", Map.empty)
    val node18 = TopoNode("staircase_B", Map.empty)
    val node19 = TopoNode("staircase_B_outside", Map.empty)
    val node20 = TopoNode("staircase_C", Map.empty)
    val node21 = TopoNode("staircase_C_outside", Map.empty)
    val node22 = TopoNode("staircase_D", Map.empty)
    val node23 = TopoNode("staircase_D_outside", Map.empty)

    val node24 = TopoNode("intersact0", Map.empty)
    val node25 = TopoNode("intersact1", Map.empty)
    val node26 = TopoNode("intersact2", Map.empty)
    val node27 = TopoNode("intersact3_north", Map.empty)
    val node28 = TopoNode("intersact3_inside", Map.empty)
    val node29 = TopoNode("intersact3_south", Map.empty)

    val node30 = TopoNode("toilet", Map.empty)
    val node31 = TopoNode("toilet_outside", Map.empty)
    val node32 = TopoNode("model", Map.empty)
    val node33 = TopoNode("Bar", Map.empty)

    // Add all nodes to the list
    floor30Nodes ++= List(
      node1, node2, node3, node4, node5, node6, node7, node8, node9, node10,
      node11, node12, node13, node14, node15, node16, node17, node18, node19, node20,
      node21, node22, node23, node24, node25, node26, node27, node28, node29, node30,
      node31, node32, node33
    )

    // In/Out rooms
    floor30Edges += AtomicPath(node8, node5, Map(VisitingMode.Normal -> 1.0), PathType.General) // outside_room_01 <-> inside_room_01
    floor30Edges += AtomicPath(node5, node8, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor30Edges += AtomicPath(node9, node6, Map(VisitingMode.Normal -> 1.0), PathType.General) // outside_room_02 <-> inside_room_02
    floor30Edges += AtomicPath(node6, node9, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor30Edges += AtomicPath(node29, node7, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact3_south <-> inside_room_03
    floor30Edges += AtomicPath(node7, node29, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node29, node3, Map(VisitingMode.Normal -> 4.0), PathType.General) // intersact3_south <-> SW_corner
    floor30Edges += AtomicPath(node3, node29, Map(VisitingMode.Normal -> 4.0), PathType.General)

    floor30Edges += AtomicPath(node7, node3, Map(VisitingMode.Normal -> 3.0), PathType.General) // inside_room_03 <-> SW_corner
    floor30Edges += AtomicPath(node3, node7, Map(VisitingMode.Normal -> 3.0), PathType.General)

    floor30Edges += AtomicPath(node24, node1, Map(VisitingMode.Normal -> 5.0), PathType.General) // intersact0 <-> NW_corner
    floor30Edges += AtomicPath(node1, node24, Map(VisitingMode.Normal -> 5.0), PathType.General)

    floor30Edges += AtomicPath(node26, node2, Map(VisitingMode.Normal -> 5.0), PathType.General) // intersact2 <-> NE_corner
    floor30Edges += AtomicPath(node2, node26, Map(VisitingMode.Normal -> 5.0), PathType.General)

    // The ring-like corridor around the core
    floor30Edges += AtomicPath(node24, node31, Map(VisitingMode.Normal -> 7.0), PathType.General) // intersact0 <-> toilet_outside
    floor30Edges += AtomicPath(node31, node24, Map(VisitingMode.Normal -> 7.0), PathType.General)

    floor30Edges += AtomicPath(node19, node31, Map(VisitingMode.Normal -> 4.0), PathType.General) // staircase_B_outside <-> toilet_outside
    floor30Edges += AtomicPath(node31, node19, Map(VisitingMode.Normal -> 4.0), PathType.General)

    floor30Edges += AtomicPath(node26, node31, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact2 <-> toilet_outside
    floor30Edges += AtomicPath(node31, node26, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node26, node13, Map(VisitingMode.Normal -> 5.0), PathType.General) // intersact2 <-> OS_hall_outside
    floor30Edges += AtomicPath(node13, node26, Map(VisitingMode.Normal -> 5.0), PathType.General)

    floor30Edges += AtomicPath(node11, node13, Map(VisitingMode.Normal -> 6.0), PathType.General) // OP3_hall_outside <-> OS_hall_outside
    floor30Edges += AtomicPath(node13, node11, Map(VisitingMode.Normal -> 6.0), PathType.General)

    floor30Edges += AtomicPath(node11, node27, Map(VisitingMode.Normal -> 2.0), PathType.General) // OP3_hall_outside <-> intersact3_north
    floor30Edges += AtomicPath(node27, node11, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node28, node27, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact3_inside -> intersact3_north
    floor30Edges += AtomicPath(node28, node29, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact3_inside -> intersact3_south

    floor30Edges += AtomicPath(node27, node28, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact3_north -> intersact3_inside
    floor30Edges += AtomicPath(node29, node28, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact3_south -> intersact3_inside

    floor30Edges += AtomicPath(node28, node21, Map(VisitingMode.Normal -> 7.0), PathType.General) // intersact3_inside <-> staircase_C_outside
    floor30Edges += AtomicPath(node21, node28, Map(VisitingMode.Normal -> 7.0), PathType.General)

    floor30Edges += AtomicPath(node25, node21, Map(VisitingMode.Normal -> 6.0), PathType.General) // intersact1 <-> staircase_C_outside
    floor30Edges += AtomicPath(node21, node25, Map(VisitingMode.Normal -> 6.0), PathType.General)

    floor30Edges += AtomicPath(node25, node23, Map(VisitingMode.Normal -> 6.0), PathType.General) // intersact1 <-> staircase_D_outside
    floor30Edges += AtomicPath(node23, node25, Map(VisitingMode.Normal -> 6.0), PathType.General)

    floor30Edges += AtomicPath(node15, node23, Map(VisitingMode.Normal -> 4.0), PathType.General) // OEX_hall_outside <-> staircase_D_outside
    floor30Edges += AtomicPath(node23, node15, Map(VisitingMode.Normal -> 4.0), PathType.General)

    floor30Edges += AtomicPath(node15, node24, Map(VisitingMode.Normal -> 3.0), PathType.General) // OEX_hall_outside <-> intersact0
    floor30Edges += AtomicPath(node24, node15, Map(VisitingMode.Normal -> 3.0), PathType.General)

    // Bar Area
    floor30Edges += AtomicPath(node25, node32, Map(VisitingMode.Normal -> 2.0), PathType.General) // intersact1 <-> model
    floor30Edges += AtomicPath(node32, node25, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node3, node32, Map(VisitingMode.Normal -> 3.0), PathType.General) // SW_corner <-> model
    floor30Edges += AtomicPath(node32, node3, Map(VisitingMode.Normal -> 3.0), PathType.General)

    floor30Edges += AtomicPath(node33, node32, Map(VisitingMode.Normal -> 6.0), PathType.General) // bar <-> model
    floor30Edges += AtomicPath(node32, node33, Map(VisitingMode.Normal -> 6.0), PathType.General)

    floor30Edges += AtomicPath(node33, node29, Map(VisitingMode.Normal -> 5.0), PathType.General) // bar <-> intersact3_south
    floor30Edges += AtomicPath(node29, node33, Map(VisitingMode.Normal -> 5.0), PathType.General)

    // In/Out staircases or elevators
    floor30Edges += AtomicPath(node19, node18, Map(VisitingMode.Normal -> 3.0), PathType.General) // staircase_B_outside -> staircase_B
    floor30Edges += AtomicPath(node18, node19, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_B -> staircase_B_outside

    floor30Edges += AtomicPath(node21, node20, Map(VisitingMode.Normal -> 3.0), PathType.General) // staircase_C_outside -> staircase_C
    floor30Edges += AtomicPath(node20, node21, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_C -> staircase_C_outside

    floor30Edges += AtomicPath(node23, node22, Map(VisitingMode.Normal -> 10.0), PathType.General) // staircase_D_outside -> staircase_D
    floor30Edges += AtomicPath(node22, node23, Map(VisitingMode.Normal -> 8.0), PathType.General) // staircase_D -> staircase_D_outside

    floor30Edges += AtomicPath(node12, node17, Map(VisitingMode.Normal -> 3.0), PathType.General) // OS_hall -> staircase_A
    floor30Edges += AtomicPath(node17, node12, Map(VisitingMode.Normal -> 5.0), PathType.General) // staircase_A -> OS_hall

    floor30Edges += AtomicPath(node12, node13, Map(VisitingMode.Normal -> 2.0), PathType.General) // OS_hall <-> OS_hall_outside
    floor30Edges += AtomicPath(node13, node12, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node10, node11, Map(VisitingMode.Normal -> 2.0), PathType.General) // OP3_hall <-> OP3_hall_outside
    floor30Edges += AtomicPath(node11, node10, Map(VisitingMode.Normal -> 2.0), PathType.General)

    floor30Edges += AtomicPath(node14, node15, Map(VisitingMode.Normal -> 2.0), PathType.General) // OEX_hall <-> OEX_hall_outside
    floor30Edges += AtomicPath(node15, node14, Map(VisitingMode.Normal -> 2.0), PathType.General)

    NavigationGraph("Floor29", floor30Nodes.toList, floor30Edges.toList)
  }

  private def generateCarparkMap(): NavigationGraph = {
    NavigationGraph("Carpark", List(TopoNode("OPS", Map.empty)), List.empty)
  }

  runTest()
}

