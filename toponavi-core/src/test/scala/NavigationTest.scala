import data.{AtomicPath, ElevatorBank, NavigationGraph, TopoNode, TransportGraph}
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import enums.{AttributeValue, PathType, RoutePlanningPreferences, TransportServicePermission, VisitingMode}
import navigation.RoutePlanner

import scala.collection.mutable



object RoutePlanningTester extends App{

  val floorMap: Map[String, NavigationGraph] = Map(
    "floor100" -> generateFloor100Map(),
    "floor96" -> generateFloor96Map(),
    "floor94" -> generateFloor94Map(),
    "floor3" -> generateFloor3Map(),
    "floor1" -> generateFloor1Map(),
    "floorB1" -> generateFloorB1Map(),
    "floor89" -> generateFloor89Map(),
    "floor52" -> generateFloor52Map(),
    "floor28" -> generateFloor28Map(),
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
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
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
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), 
    maxVelocity = 1.75,
    acceleration = 1.0
  )

  val GE = ElevatorBank(
    identifier = "GE",
    stationNodes = Map(
      floorMap("floor94") -> floorMap("floor94").nodes.find(n => n.identifier == "GE_hall").get,
      floorMap("floor3") -> floorMap("floor3").nodes.find(n => n.identifier == "GE_hall").get,
      floorMap("floorB1") -> floorMap("floorB1").nodes.find(n => n.identifier == "GE_hall").get
    ),
    stationLocations = Map(floorMap("floor94") -> 425, floorMap("floor3") -> 10, floorMap("floorB1") -> -5),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
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
    stationLocations = Map(floorMap("floor89") -> 390, floorMap("floor72") -> 280, floorMap("floor52") -> 200, floorMap("floor28") -> 100, floorMap("floor1") -> 0, floorMap("floorB1") -> -5),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
    maxVelocity = 6.0,
    acceleration = 1.0
  )

  val S1 = ElevatorBank(
    identifier = "S1",
    stationNodes = Map(
      floorMap("floor28") -> floorMap("floor28").nodes.find(n => n.identifier == "S1_hall").get,
      floorMap("floor1") -> floorMap("floor1").nodes.find(n => n.identifier == "S1_hall").get,
    ),
    stationLocations = Map(floorMap("floor1") -> 0, floorMap("floor28") -> 100),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
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
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
    maxVelocity = 10.0,
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
    maxVelocity = 1.25,
    acceleration = 0.7
  )

  val CP = ElevatorBank(
    identifier = "CP",
    stationNodes = Map(
      floorMap("floor3") -> floorMap("floor3").nodes.find(n => n.identifier == "CP_hall").get,
      floorMap("floor1") -> floorMap("floor1").nodes.find(n => n.identifier == "CP_hall").get,
      floorMap("floorB1") -> floorMap("floorB1").nodes.find(n => n.identifier == "CP_hall").get
    ),
    stationLocations = Map(floorMap("floor3") -> 10, floorMap("floor1") -> 0, floorMap("floorB1") -> -5),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted),
    maxVelocity = 1.5,
    acceleration = 0.8
  )

  val transportGraph = TransportGraph(List(FS10, FS8, GE, FS4, S1, S2, LS9, CP))

  // Test precise pathfinding for transport
  if (transportGraph.nodes.size >= 2) {
    val start = transportGraph.nodes.find(node => node.identifier == "FS10@floor100").getOrElse(throw RuntimeException(""))
    val goal = transportGraph.nodes.find(node => node.identifier == "CP@floor1").getOrElse(throw RuntimeException(""))

    println(s"\n=== Testing Path Finding ===")
    println(s"Start: ${start.identifier}")
    println(s"Goal: ${goal.identifier}")

    // Find path (3rd param for testing only)
    val result = transportGraph.findPath(start, goal, List(
      "floor100", "floor96", "floor94", "floor89",
      "floor72", "floor65", "floor64", "floor63", "floor62", "floor61", "floor60",
      "floor59", "floor58", "floor57", "floor56", "floor55", "floor54", "floor53",
      "floor52", "floor28", "floor3", "floor1", "floorB1"
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
    val target = floorMap("floor1")

    val result = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor3", "floor1", "floorB1"), MinimizeTime, 0)
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
    val target1 = floorMap("floor1")

    val result1 = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor3", "floor1", "floorB1"), MinimizeTime, 1)
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
    val target2 = floorMap("floor1")

    val result2 = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor3", "floor1", "floorB1"), MinimizeTime, 2)
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

    val start3 = floorMap("floor100")
    val target3 = floorMap("floor1")

    val result3 = transportGraph.findPathFuzzy(start, target, List("floor100", "floor96", "floor94", "floor89", "floor3", "floor1", "floorB1"), MinimizeTime, 3)
    result3 match {
      case Some(path) =>
        println(s"\n=== Testing Fuzzy Path Finding with Solution 3 ===")
        println(s"Start: ${start.identifier}")
        println(s"Goal: ${target.identifier}")
        println(s"Path found with ${path.routeNodes.size} nodes:")
        path.routeNodes.foreach(node => println(s"   → ${node.identifier}"))
        println(s"With ${path.routeEdges.size} AtomicPaths, taking ${path.totalCost} seconds:")
        path.routeEdges.foreach(edge => println(s"   ${edge.source.identifier} => ${edge.target.identifier} takes ${edge.cost} seconds"))
      case None =>
        println("No fuzzy path found for solution 3")
    }
  }







  // TEST NAVIGATE BEGIN //
  val routePlanner = RoutePlanner(floorMap, transportGraph, List(
    "floor100", "floor96", "floor94", "floor89",
    "floor72", "floor65", "floor64", "floor63", "floor62", "floor61", "floor60",
    "floor59", "floor58", "floor57", "floor56", "floor55", "floor54", "floor53",
    "floor52", "floor28", "floor3", "floor1", "floorB1"
  ), true)

  routePlanner.navigate("floor100", "floor3", "FS10_hall", "CP_hall", Normal, RoutePlanningPreferences.MinimizeTime) match {
    case Right(navigationPath) =>
      println(s"\n=== Testing RoutePlanner Navigate ===")
//      println(s"Start: floor100@FS10_hall")
//      println(s"Goal: floor64@LS9_hall")
      println(s"Path found with ${navigationPath.routeNodes.size} nodes:")
      navigationPath.routeNodes.foreach(node => println(s"   → ${node.toString}"))
      println(s"With ${navigationPath.routeEdges.size} AtomicPaths, taking ${navigationPath.totalCost} seconds:")
      navigationPath.routeEdges.foreach(edge => println(s"   ${edge.source.toString} => ${edge.target.toString} takes ${edge.cost} seconds"))
    case Left(error) =>
      println(s"Navigation error: ${error}")
  }

  routePlanner.navigate("floor3", "floor1", "CP_hall", "CP_hall", Normal, RoutePlanningPreferences.MinimizeTime) match {
    case Right(navigationPath) =>
      println(s"\n=== Testing RoutePlanner Navigate ===")
      //      println(s"Start: floor100@FS10_hall")
      //      println(s"Goal: floor64@LS9_hall")
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
    val node2 = TopoNode("maintenance_hall", Map.empty)
    val node3 = TopoNode("FS8_hall", Map.empty)
    floor96Nodes ++= List(
      node1, node2, node3
    )

    floor96Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS10_hall <-> maintenance_hall
    floor96Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 1.0), PathType.General)

    floor96Edges += AtomicPath(node2, node3, Map(VisitingMode.Normal -> 1.0), PathType.General) // maintenance_hall <-> FS8_hall
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

    floor94Edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 12.0), PathType.General) // FS8_hall <-> shops
    floor94Edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 12.0), PathType.General)

    floor94Edges += AtomicPath(node2, node3, Map(VisitingMode.Normal -> 12.0), PathType.General) // shops <-> toilet
    floor94Edges += AtomicPath(node3, node2, Map(VisitingMode.Normal -> 12.0), PathType.General)

    floor94Edges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 8.0), PathType.General) // toilet <-> GE_hall
    floor94Edges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 8.0), PathType.General)

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

  def generateFloor28Map(): NavigationGraph = {
    val floor28Nodes = mutable.ListBuffer[TopoNode]()
    val floor28Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node_fs4 = TopoNode("FS4_hall", Map.empty)
    val node_s1 = TopoNode("S1_hall", Map.empty)
    val node_amenities = TopoNode("amenities", Map.empty)
    floor28Edges += AtomicPath(node_fs4, node_amenities, Map(VisitingMode.Normal -> 1.0), PathType.General) // FS4_hall  -> amenities

    floor28Edges += AtomicPath(node_amenities, node_s1, Map(VisitingMode.Normal -> 1.0), PathType.General) // amenities <-> S1_hall
    floor28Edges += AtomicPath(node_s1, node_amenities, Map(VisitingMode.Normal -> 1.0), PathType.General)
    floor28Nodes ++= List(
      node_fs4, node_s1, node_amenities
    )

    NavigationGraph("floor28", floor28Nodes.toList, floor28Edges.toList)
  }

  def generateFloor3Map(): NavigationGraph = {
    val floor3Nodes = mutable.ListBuffer[TopoNode]()
    val floor3Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node_ge = TopoNode("GE_hall", Map.empty)
    val node_shops = TopoNode("shops", Map.empty)
    val node_cp = TopoNode("CP_hall", Map.empty)
    floor3Nodes ++= List(
      node_ge, node_shops, node_cp
    )

    floor3Edges += AtomicPath(node_ge, node_shops, Map(VisitingMode.Normal -> 10.0), PathType.General) // GE_hall <-> shops
    floor3Edges += AtomicPath(node_shops, node_ge, Map(VisitingMode.Normal -> 10.0), PathType.General)
    floor3Edges += AtomicPath(node_shops, node_cp, Map(VisitingMode.Normal -> 15.0), PathType.General) // shops <-> CP_hall
    floor3Edges += AtomicPath(node_cp, node_shops, Map(VisitingMode.Normal -> 15.0), PathType.General)

    NavigationGraph("floor3", floor3Nodes.toList, floor3Edges.toList)
  }

  def generateFloor1Map(): NavigationGraph = {
    val floor1Nodes = mutable.ListBuffer[TopoNode]()
    val floor1Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node_fs4 = TopoNode("FS4_hall", Map.empty)
    val node_s2 = TopoNode("S2_hall", Map.empty)
    val node_s1 = TopoNode("S1_hall", Map.empty)
    val node_cp = TopoNode("CP_hall", Map.empty)
    floor1Edges += AtomicPath(node_fs4, node_s1, Map(VisitingMode.Normal -> 12.0), PathType.General) // S1_hall <-  FS4_hall
    floor1Edges += AtomicPath(node_fs4, node_cp, Map(VisitingMode.Normal -> 15.0), PathType.General) // CP_hall <-  FS4_hall

    floor1Edges += AtomicPath(node_s2, node_cp, Map(VisitingMode.Normal -> 5.0), PathType.General) // CP_hall <->  S2_hall
    floor1Edges += AtomicPath(node_cp, node_s2, Map(VisitingMode.Normal -> 5.0), PathType.General)

    floor1Edges += AtomicPath(node_s1, node_cp, Map(VisitingMode.Normal -> 35.0), PathType.General) // CP_hall <->  S1_hall
    floor1Edges += AtomicPath(node_cp, node_s1, Map(VisitingMode.Normal -> 35.0), PathType.General)
    floor1Nodes ++= List(
      node_fs4, node_s1, node_s2, node_cp
    )


    NavigationGraph("floor1", floor1Nodes.toList, floor1Edges.toList)
  }

  def generateFloorB1Map(): NavigationGraph = {
    val floorB1Nodes = mutable.ListBuffer[TopoNode]()
    val floorB1Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node_ge = TopoNode("GE_hall", Map.empty)
    val node_entrance = TopoNode("entrance", Map.empty)
    val node_fs4 = TopoNode("FS4_hall", Map.empty)
    val node_cp = TopoNode("CP_hall", Map.empty)
    floorB1Nodes ++= List(
      node_ge, node_entrance, node_fs4, node_cp
    )

    floorB1Edges += AtomicPath(node_ge, node_entrance, Map(VisitingMode.Normal -> 15.0), PathType.General) // GE_hall <-> entrance
    floorB1Edges += AtomicPath(node_entrance, node_ge, Map(VisitingMode.Normal -> 15.0), PathType.General)

    floorB1Edges += AtomicPath(node_entrance, node_cp, Map(VisitingMode.Normal -> 20.0), PathType.General) // entrance <-> FS4_hall
    floorB1Edges += AtomicPath(node_cp, node_entrance, Map(VisitingMode.Normal -> 20.0), PathType.General)

    floorB1Edges += AtomicPath(node_fs4, node_cp, Map(VisitingMode.Normal -> 40.0), PathType.General) // FS4_hall <-> CP_hall
    floorB1Edges += AtomicPath(node_cp, node_fs4, Map(VisitingMode.Normal -> 40.0), PathType.General)

    NavigationGraph("floorB1", floorB1Nodes.toList, floorB1Edges.toList)
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

    NavigationGraph("floor89", floor89Nodes.toList, floor89Edges.toList)
  }

  def generateFloorMapWithElevatorOnly(floorName: String, elevatorName: String): NavigationGraph = {
    val floorNodes = mutable.ListBuffer[TopoNode]()
    val floorEdges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode(s"${elevatorName}", Map.empty)
    floorNodes ++= List(
      node1
    )

    NavigationGraph(s"${floorName}", floorNodes.toList, floorEdges.toList)
  }
}
