import data.{AtomicPath, ElevatorBank, NavigationGraph, TopoNode, TransportGraph}
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import enums.{AttributeValue, PathType, TransportServicePermission, VisitingMode}

import scala.collection.mutable




object RoutePlanningTester extends App{
  val floor100: NavigationGraph = generateFloor100Map()
  val floor96: NavigationGraph = generateFloor96Map()
  val floor94: NavigationGraph = generateFloor94Map()
  val floor1: NavigationGraph = generateFloor1Map()
  val floorB1: NavigationGraph = generateFloorB1Map()
  val floor89: NavigationGraph = generateFloor89Map()

  val FS10 = ElevatorBank(
    identifier = "FS10",
    stationNodes = Map(
      floor100 -> floor100.nodes.find(n => n.identifier == "FS10_hall").get,
      floor96 -> floor96.nodes.find(n => n.identifier == "FS10_hall").get
    ),
    stationLocations = Map(floor100 -> 475, floor96 -> 435),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 1.75,
    acceleration = 1.0
  )

  val FS8 = ElevatorBank(
    identifier = "FS8",
    stationNodes = Map(
      floor96 -> floor96.nodes.find(n => n.identifier == "FS8_hall").get,
      floor94 -> floor94.nodes.find(n => n.identifier == "FS8_hall").get,
      floor89 -> floor89.nodes.find(n => n.identifier == "FS8_hall").get
    ),
    stationLocations = Map(floor96 -> 435, floor94 -> 425, floor89 -> 390),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 1.75,
    acceleration = 1.0
  )

  val GE = ElevatorBank(
    identifier = "GE",
    stationNodes = Map(
      floor94 -> floor94.nodes.find(n => n.identifier == "GE_hall").get,
      floorB1 -> floorB1.nodes.find(n => n.identifier == "GE_hall").get
    ),
    stationLocations = Map(floor94 -> 425, floorB1 -> -10),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 8.0,
    acceleration = 1.0
  )

  val FS4 = ElevatorBank( 
    identifier = "FS4",
    stationNodes = Map(
      floor89 -> floor89.nodes.find(n => n.identifier == "FS4_hall").get,
      floor1 -> floor1.nodes.find(n => n.identifier == "FS4_hall").get,
      floorB1 -> floorB1.nodes.find(n => n.identifier == "FS4_hall").get,

    ),
    stationLocations = Map(floor89 -> 390, floor1 -> 0, floorB1 -> -10),
    stationPermissions = Map.empty.withDefaultValue(TransportServicePermission.FullyGranted), // DONT GIVE A SHIT
    maxVelocity = 6.0,
    acceleration = 1.0
  )

  val transportGraph = TransportGraph(List(FS10, FS8, GE, FS4))

  // Test precise pathfinding for transport
  if (transportGraph.nodes.size >= 2) {
    val start = transportGraph.nodes.find(node => node.identifier == "FS10@floor100").getOrElse(throw RuntimeException(""))
    val goal = transportGraph.nodes.find(node => node.identifier == "FS4@floor89").getOrElse(throw RuntimeException(""))

    println(s"\n=== Testing Path Finding ===")
    println(s"Start: ${start.identifier}")
    println(s"Goal: ${goal.identifier}")

    // Find path (3rd param for testing only)
    val result = transportGraph.findPath(start, goal, List("floor100", "floor96", "floor94", "floor89", "floor1", "floorB1"))
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
    val start = floor100
    val target = floor89

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

    val start1 = floor100
    val target1 = floor89

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

    val start2 = floor100
    val target2 = floor89

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

  def generateFloor1Map(): NavigationGraph = {
    val floor1Nodes = mutable.ListBuffer[TopoNode]()
    val floor1Edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("FS4_hall", Map.empty)
    floor1Nodes ++= List(
      node1
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
}
