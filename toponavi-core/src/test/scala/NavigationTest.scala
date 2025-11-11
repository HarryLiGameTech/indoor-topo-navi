import data.{AtomicPath, ElevatorBank, NavigationGraph, TopoNode, TransportGraph}
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import enums.{AttributeValue, PathType, TransportServicePermission, VisitingMode}
import navigation.RoutePlanner

import scala.collection.mutable



object RoutePlanningTester extends App{
  // TODO: Add the following elevators (along with their respective NavigationGraphs. Only <elevator_bank>_hall nodes are needed in each graph, leave the edge list EMPTY):
  // floor52 is 200 height, and each floor above adds 4 height

  // FS4 (Modify): B1, 1, 28, 52, 72, 89
  // LS9: 52-65
  // S2: 1, 52


/*  val floor100: NavigationGraph = generateFloor100Map()
  val floor96: NavigationGraph = generateFloor96Map()
  val floor94: NavigationGraph = generateFloor94Map()
  val floor1: NavigationGraph = generateFloor1Map()
  val floorB1: NavigationGraph = generateFloorB1Map()
  val floor89: NavigationGraph = generateFloor89Map()

  val floor28: NavigationGraph = generateFloorMapWithElevatorOnly("floor28", "FS4_hall")
  val floor72: NavigationGraph = generateFloorMapWithElevatorOnly("floor72", "FS4_hall")
  for(i <- 52 to 65){
//    val floorX: NavigationGraph = generateFloorMapWithElevatorOnly(s"floor$i", "LS9_hall")
    // TODO: 不能更改变量名称，用Map来存
  }*/

  val floorMap: Map[String, NavigationGraph] = Map(
    "floor100" -> generateFloor100Map(),
    "floor96" -> generateFloor96Map(),
    "floor94" -> generateFloor94Map(),
    "floor1" -> generateFloor1Map(),
    "floorB1" -> generateFloorB1Map(),
    "floor89" -> generateFloor89Map(),
    "floor52" -> generateFloor52Map(),
    "floor28" -> generateFloorMapWithElevatorOnly("floor28", "FS4_hall"),
    "floor72" -> generateFloorMapWithElevatorOnly("floor72", "FS4_hall")
  ) ++ (53 to 65).map { i =>
    s"floor$i" -> generateFloorMapWithElevatorOnly(s"floor$i", "LS9_hall")
  }.toMap



  val FS10 = ElevatorBank(
    identifier = "FS10",
    stationNodes = Map(
      floorMap("floor100") -> floorMap("floor100").nodes.find(n => n.identifier == "FS10_hall").get,
      floorMap("floor96") -> floorMap("floor96").nodes.find(n => n.identifier == "FS10_hall").get
    ),
    stationLocations = Map(floorMap("floor100") -> 475, floorMap("floor96") -> 435),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 1.75,
    acceleration = 1.0
  )

  val FS8 = ElevatorBank(
    identifier = "FS8",
    stationNodes = Map(
      floorMap("floor96") -> floorMap("floor96").nodes.find(n => n.identifier == "FS8_hall").get,
      floorMap("floor94") -> floorMap("floor94").nodes.find(n => n.identifier == "FS8_hall").get,
      floorMap("floor89") -> floorMap("floor89").nodes.find(n => n.identifier == "FS8_hall").get
    ),
    stationLocations = Map(floorMap("floor96") -> 435, floorMap("floor94") -> 425, floorMap("floor89") -> 390),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 1.75,
    acceleration = 1.0
  )

  val GE = ElevatorBank(
    identifier = "GE",
    stationNodes = Map(
      floorMap("floor94") -> floorMap("floor94").nodes.find(n => n.identifier == "GE_hall").get,
      floorMap("floorB1") -> floorMap("floorB1").nodes.find(n => n.identifier == "GE_hall").get
    ),
    stationLocations = Map(floorMap("floor94") -> 425, floorMap("floorB1") -> -10),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 8.0,
    acceleration = 1.0
  )

  val FS4 = ElevatorBank(
    identifier = "FS4",
    stationNodes = Map(
      floorMap("floor89") -> floorMap("floor89").nodes.find(n => n.identifier == "FS4_hall").get,
      floorMap("floor72") -> floorMap("floor72").nodes.find(n => n.identifier == "FS4_hall").get,
      floorMap("floor52") -> floorMap("floor52").nodes.find(n => n.identifier == "FS4_hall").get,
      floorMap("floor28") -> floorMap("floor28").nodes.find(n => n.identifier == "FS4_hall").get,
      floorMap("floor1") -> floorMap("floor1").nodes.find(n => n.identifier == "FS4_hall").get,
      floorMap("floorB1") -> floorMap("floorB1").nodes.find(n => n.identifier == "FS4_hall").get,

    ),
    stationLocations = Map(floorMap("floor89") -> 390, floorMap("floor72") -> 280, floorMap("floor52") -> 200, floorMap("floor28") -> 100, floorMap("floor1") -> 0, floorMap("floorB1") -> -10),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 6.0,
    acceleration = 1.0
  )

  val S2 = ElevatorBank(
    identifier = "S2",
    stationNodes = Map(
      floorMap("floor52") -> floorMap("floor52").nodes.find(n => n.identifier == "S2_hall").get,
      floorMap("floor1") -> floorMap("floor1").nodes.find(n => n.identifier == "S2_hall").get,
    ),
    stationLocations = Map(floorMap("floor1") -> 0, floorMap("floor52") -> 200),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 6.0,
    acceleration = 1.0
  )

  val LS9 = ElevatorBank(
    identifier = "LS9",
    stationNodes = Map(
      floorMap("floor52") -> floorMap("floor52").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor53") -> floorMap("floor53").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor54") -> floorMap("floor54").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor55") -> floorMap("floor55").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor56") -> floorMap("floor56").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor57") -> floorMap("floor57").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor58") -> floorMap("floor58").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor59") -> floorMap("floor59").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor60") -> floorMap("floor60").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor61") -> floorMap("floor61").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor62") -> floorMap("floor62").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor63") -> floorMap("floor63").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor64") -> floorMap("floor64").nodes.find(n => n.identifier == "LS9_hall").get,
      floorMap("floor65") -> floorMap("floor65").nodes.find(n => n.identifier == "LS9_hall").get
    ),
    stationLocations = Map(
      floorMap("floor52") -> 200,
      floorMap("floor53") -> 204,
      floorMap("floor54") -> 208,
      floorMap("floor55") -> 212,
      floorMap("floor56") -> 216,
      floorMap("floor57") -> 220,
      floorMap("floor58") -> 224,
      floorMap("floor59") -> 228,
      floorMap("floor60") -> 232,
      floorMap("floor61") -> 236,
      floorMap("floor62") -> 240,
      floorMap("floor63") -> 244,
      floorMap("floor64") -> 248,
      floorMap("floor65") -> 252
    ),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 6.0,
    acceleration = 1.0
  )

  val transportGraph = TransportGraph(List(FS10, FS8, GE, FS4, S2, LS9))

  // Test precise pathfinding for transport
  if (transportGraph.nodes.size >= 2) {
    val start = transportGraph.nodes.find(node => node.identifier == "FS10@floor100").getOrElse(throw RuntimeException(""))
    val goal = transportGraph.nodes.find(node => node.identifier == "LS9@floor64").getOrElse(throw RuntimeException(""))

    println(s"\n=== Testing Path Finding ===")
    println(s"Start: ${start.identifier}")
    println(s"Goal: ${goal.identifier}")

    // Find path (3rd param for testing only)
    val result = transportGraph.findPath(start, goal, List(
      "floor100", "floor96", "floor94", "floor89",
      "floor72", "floor65", "floor64", "floor63", "floor62", "floor61", "floor60",
      "floor59", "floor58", "floor57", "floor56", "floor55", "floor54", "floor53",
      "floor52", "floor28", "floor1", "floorB1"
    ))
    result match {
      case Some(path) =>
        println(s"Path found with ${path.routeNodes.size} nodes:")
        path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost} seconds:")
        path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.cost} seconds"))
      case None =>
        println("No path found")
    }
  } else {
    println("Not enough nodes to test path finding")
  }

  // Test fuzzy pathfinding for transport
  if (transportGraph.nodes.size >= 2) {
    val start = floorMap("floor100")
    val target = floorMap("floor89")

    val result = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor1", "floorB1"), MinimizeTime, 0)
    result match {
      case Some(path) =>
        println(s"\n=== Testing Fuzzy Path Finding with Solution 0 ===")
        println(s"Start: ${start.identifier}")
        println(s"Goal: ${target.identifier}")
        println(s"Path found with ${path.routeNodes.size} nodes:")
        path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost} seconds:")
        path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.cost} seconds"))
      case None =>
        println("No fuzzy path found")
    }

    val start1 = floorMap("floor100")
    val target1 = floorMap("floor89")

    val result1 = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor1", "floorB1"), MinimizeTime, 1)
    result1 match {
      case Some(path) =>
        println(s"\n=== Testing Fuzzy Path Finding with Solution 1 ===")
        println(s"Start: ${start.identifier}")
        println(s"Goal: ${target.identifier}")
        println(s"Path found with ${path.routeNodes.size} nodes:")
        path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost} seconds:")
        path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.cost} seconds"))
      case None =>
        println("No fuzzy path found")
    }

    val start2 = floorMap("floor100")
    val target2 = floorMap("floor89")

    val result2 = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor1", "floorB1"), MinimizeTime, 2)
    result2 match {
      case Some(path) =>
        println(s"\n=== Testing Fuzzy Path Finding with Solution 2 ===")
        println(s"Start: ${start.identifier}")
        println(s"Goal: ${target.identifier}")
        println(s"Path found with ${path.routeNodes.size} nodes:")
        path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost} seconds:")
        path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.cost} seconds"))
      case None =>
        println("No fuzzy path found for solution 2")
    }
  }

  // TEST NAVIGATE BEGIN //
  val routePlanner = RoutePlanner(floorMap, transportGraph, List(
    "floor100", "floor96", "floor94", "floor89",
    "floor72", "floor65", "floor64", "floor63", "floor62", "floor61", "floor60",
    "floor59", "floor58", "floor57", "floor56", "floor55", "floor54", "floor53",
    "floor52", "floor28", "floor1", "floorB1"
  ), true)

  routePlanner.navigate("floor100", "floor64", "FS10_hall", "LS9_hall", Normal) match {
    case Right(navigationPath) =>
      println(s"\n=== Testing RoutePlanner Navigate ===")
      println(s"Start: floor100@FS10_hall")
      println(s"Goal: floor64@LS9_hall")
      println(s"Path found with ${navigationPath.routeNodes.size} nodes:")
      navigationPath.routeNodes.foreach(node => println(s"   → ${node.toString}"))
      println(s"With ${navigationPath.routeEdges.size} AtomicPaths, taking ${navigationPath.totalCost} seconds:")
      navigationPath.routeEdges.foreach(edge => println(s"   ${edge.source.toString} => ${edge.target.toString} takes ${edge.cost} seconds"))
    case Left(error) =>
      println(s"Navigation error: ${error}")
  }
  // TEST NAVIGATE END //




















  def generateFloor100Map(): NavigationGraph = {
    val floor100Nodes = mutable.ListBuffer[TopoNode]()
    val floor100Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS10_hall", Map.empty)
    val node2 = TopoNode("observation_deck", Map.empty)
    // Add all nodes to the list
    floor100Nodes ++= List(
      node1, node2
    )
//    val node5 = TopoNode("inside_room_01", Map("description" -> AttributeValue.StringValue("inside the door of room 01")))

    floor100Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS10_hall <-> observation_deck
    floor100Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    NavigationGraph("floor100", floor100Nodes.toList, floor100Edges.toList) // TODO: Modify this
  }

  def generateFloor96Map(): NavigationGraph = {
    val floor96Nodes = mutable.ListBuffer[TopoNode]()
    val floor96Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS10_hall", Map.empty)
    val node2 = TopoNode("gallery", Map.empty)
    val node3 = TopoNode("FS8_hall", Map.empty)
    floor96Nodes ++= List(
      node1, node2, node3
    )

    floor96Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS10_hall <-> gallery
    floor96Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor96Edges += AtomicPath(node2, node3, Map(VisitingMode.Normal -> 1.0), PathType.General) // gallery <-> FS8_hall
    floor96Edges += AtomicPath(node3, node2, Map(VisitingMode.Normal -> 1.0), PathType.General)

    NavigationGraph("floor96", floor96Nodes.toList, floor96Edges.toList) // TODO: Modify this
  }

  def generateFloor94Map(): NavigationGraph = {
    val floor94Nodes = mutable.ListBuffer[TopoNode]()
    val floor94Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS8_hall", Map.empty)
    val node2 = TopoNode("shops", Map.empty)
    val node3 = TopoNode("toilet", Map.empty)
    val node4 = TopoNode("GE_hall", Map.empty)
    floor94Nodes ++= List(
      node1, node2, node3, node4
    )

    floor94Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS8_hall <-> shops
    floor94Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor94Edges += AtomicPath(node2, node3, Map(VisitingMode.Normal -> 1.0), PathType.General) // shops <-> toilet
    floor94Edges += AtomicPath(node3, node2, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor94Edges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 1.0), PathType.General) // toilet <-> GE_hall
    floor94Edges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 1.0), PathType.General)

    NavigationGraph("floor94", floor94Nodes.toList, floor94Edges.toList) // TODO: Modify this
  }

  def generateFloor52Map(): NavigationGraph = {
    val floor52Nodes = mutable.ListBuffer[TopoNode]()
    val floor52Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS4_hall", Map.empty)
    val node2 = TopoNode("S2_hall", Map.empty) // Standalone, no path to any other node
    val node3 = TopoNode("LS9_hall", Map.empty)
    floor52Nodes ++= List(
      node1, node2, node3
    )

    // FS4 and LS9 can interchange
    floor52Edges += AtomicPath(node1, node3, Map(VisitingMode.Normal -> 5.0), PathType.General)
    floor52Edges += AtomicPath(node3, node1, Map(VisitingMode.Normal -> 5.0), PathType.General)


    NavigationGraph("floor52", floor52Nodes.toList, floor52Edges.toList)
  }

  def generateFloor1Map(): NavigationGraph = {
    val floor1Nodes = mutable.ListBuffer[TopoNode]()
    val floor1Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS4_hall", Map.empty)
    val node2 = TopoNode("S2_hall", Map.empty)
    floor1Nodes ++= List(
      node1, node2
    )

    NavigationGraph("floor1", floor1Nodes.toList, floor1Edges.toList) // TODO: Modify this
  }

  def generateFloorB1Map(): NavigationGraph = {
    val floorB1Nodes = mutable.ListBuffer[TopoNode]()
    val floorB1Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("GE_hall", Map.empty)
    val node2 = TopoNode("entrance", Map.empty)
    val node3 = TopoNode("FS4_hall", Map.empty)
    floorB1Nodes ++= List(
      node1, node2, node3
    )

    floorB1Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // GE_hall <-> entrance
    floorB1Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floorB1Edges += AtomicPath(node2, node3, Map(VisitingMode.Normal -> 1.0), PathType.General) // entrance <-> FS4_hall
    floorB1Edges += AtomicPath(node3, node2, Map(VisitingMode.Normal -> 1.0), PathType.General)

    NavigationGraph("floorB1", floorB1Nodes.toList, floorB1Edges.toList) // TODO: Modify this
  }

  def generateFloor89Map(): NavigationGraph = {
    val floor89Nodes = mutable.ListBuffer[TopoNode]()
    val floor89Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS8_hall", Map.empty)
    val node2 = TopoNode("refuge_A", Map.empty)
    val node3 = TopoNode("FS4_hall", Map.empty)
    val node4 = TopoNode("refuge_B", Map.empty)
    floor89Nodes ++= List(
      node1, node2, node3, node4
    )

    floor89Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS8_hall <-> refuge_A
    floor89Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor89Edges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS4_hall <-> refuge_B
    floor89Edges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 1.0), PathType.General)

    NavigationGraph("floor89", floor89Nodes.toList, floor89Edges.toList) // TODO: Modify this
  }

  def generateFloorMapWithElevatorOnly(floorName: String, elevatorName: String): NavigationGraph = {
    val floorNodes = mutable.ListBuffer[TopoNode]()
    val floorEdges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode(s"${elevatorName}", Map.empty)
    floorNodes ++= List(
      node1
    )

    NavigationGraph(s"${floorName}", floorNodes.toList, floorEdges.toList) // TODO: Modify this
  }
}
