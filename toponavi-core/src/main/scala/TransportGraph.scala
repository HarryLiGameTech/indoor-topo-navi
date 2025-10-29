import cats.effect.IO
import enums.TransportServicePermission

import scala.collection.mutable

// The RailNetwork shall be generated after all the modifiers are applied. i.e. locked and ineligible floor/elevator pair shall not appear here
private class TransportGraph private(
  val nodes: List[StationNode],
  val adjacencyList: Map[StationNode, Set[StationNode]]
){
  def findPath(
    start: StationNode,
    goal: StationNode,
    floorNameList: List[String]
  ): Option[Path] = {
    // Priority queue for open set (min-heap based on fScore)
    implicit val nodeOrdering: Ordering[(StationNode, Double)] =
      Ordering.by[(StationNode, Double), Double](_._2).reverse

    val openSet = mutable.PriorityQueue.empty[(StationNode, Double)]
    val gScore = mutable.Map[StationNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val fScore = mutable.Map[StationNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val cameFrom = mutable.Map[StationNode, StationNode]()

    // Initialize starting node
    gScore(start) = 0.0
    fScore(start) = heuristic(start, goal, floorNameList)
    openSet.enqueue((start, fScore(start)))

    while (openSet.nonEmpty) {
      val (current, currentFScore) = openSet.dequeue()

      // If we reached the goal, reconstruct the path
      if (current == goal) {
        return Some(reconstructPath(cameFrom, current))
      }

      // If the current fScore is outdated, skip
      if (currentFScore > fScore(current)) {
        // This can happen because priority queue doesn't support decrease-key
        // So we allow duplicate nodes with different scores
        ()
      } else {
        // Explore neighbors
        for (neighbor <- adjacencyList.getOrElse(current, Nil)) {
          // Tentative gScore is distance from start to neighbor through current
          val tentativeGScore = gScore(current) + distanceBetween(current, neighbor)

          // If we found a better path to neighbor
          if (tentativeGScore < gScore(neighbor)) {
            // This path to neighbor is better than any previous one
            cameFrom(neighbor) = current
            gScore(neighbor) = tentativeGScore
            fScore(neighbor) = tentativeGScore + heuristic(neighbor, goal, floorNameList)

            // Add to open set (we don't have decrease-key, so we allow duplicates)
            openSet.enqueue((neighbor, fScore(neighbor)))
          }
        }
      }
    }

    // No path found
    None
  }

  private def heuristic(from: StationNode, to: StationNode, stationNameList: List[String]): Double = {
    // Get the floor identifiers
    val fromFloor = from.ownerGraph.identifier
    val toFloor = to.ownerGraph.identifier

    // Find the indices in the station name list
    val fromIndex = stationNameList.indexOf(fromFloor)
    val toIndex = stationNameList.indexOf(toFloor)

    // If either floor is not found in the list, return a large penalty
    if (fromIndex == -1 || toIndex == -1) {
      return Double.PositiveInfinity
    }

    // Calculate the absolute index difference and multiply by 5
    val indexDifference = math.abs(fromIndex - toIndex)
    2.5 * indexDifference
  }

  private def distanceBetween(from: StationNode, to: StationNode): Double = {
    // Use the line's travel time
    from.ownerLine.travelTimeBetweenStations(from.ownerGraph, to.ownerGraph)
  }

  private def reconstructPath(cameFrom: mutable.Map[StationNode, StationNode], current: StationNode): Path = {
    val totalPath = mutable.ListBuffer[StationNode]()
    var node = current

    while (cameFrom.contains(node)) {
      totalPath.prepend(node)
      node = cameFrom(node)
    }
    totalPath.prepend(node)

    // Safe conversion with error handling
    val topoNodes = totalPath.map { stationNode =>
      stationNode.ownerLine.stationNodes.get(stationNode.ownerGraph) match {
        case Some(topoNode) => topoNode
        case None => throw new IllegalStateException(s"No TopoNode found for StationNode: ${stationNode.identifier}")
      }
    }.toList

    Path(topoNodes)
  }
  
}




object TransportGraph {
  // Factory method to create TransportGraph
  def apply(
    lines: List[LinearTransport]
  ): TransportGraph = {
    val (nodes, edges) = generateTransGraphEdges(lines)
    val adjacencyList = buildAdjacencyList(edges)
    new TransportGraph(nodes, adjacencyList)
  }

  private def generateTransGraphEdges(lines: List[LinearTransport]): (List[StationNode], Set[TransportEdge]) = {
    val edges = mutable.ListBuffer[TransportEdge]()
    val allNodes = mutable.ListBuffer[StationNode]()

    // Iterate over each line
    for (line: LinearTransport <- lines: List[LinearTransport]) {
      // Get all station nodes for this line
      val stationNodes = createStationNodesForLine(line)
      allNodes ++= stationNodes

      // Create TWO types of edges:

      // 1. Internal edges: Within each elevator line (complete graph)
      for (line <- lines) {
        val stationNodes = allNodes.filter(_.ownerLine == line) // Get nodes for this specific line

        for {
          from <- stationNodes
          to <- stationNodes
          if from != to // Avoid self-loops
        } {
          val cost = line.travelTimeBetweenStations(from.ownerGraph, to.ownerGraph)
          edges += TransportEdge(from, to, cost)
        }
      }

      // 2. Transfer edges: Between different elevator lines serving the same floor
      // Group nodes by the NavigationGraph they serve (same physical floor)
      val nodesByFloor = allNodes.groupBy(_.ownerGraph)

      for {
        (navigationGraph, nodesOnSameFloor) <- nodesByFloor
        if nodesOnSameFloor.size > 1 // Only floors with multiple elevators
      } {
        // Create edges between every pair of different elevator lines on this floor
        for {
          from <- nodesOnSameFloor
          to <- nodesOnSameFloor
          if from != to && from.ownerLine != to.ownerLine // Different elevator lines
        } {
          val transferCost = 30 // TODO: Connect with the NavigationGraph to calculate
          edges += TransportEdge(from, to, transferCost)
        }
      }
    }
    (allNodes.toList, edges.toSet)
  }

  private def createStationNodesForLine(line: LinearTransport): List[StationNode] = {
    line.stationNodes.map { case (navigationGraph, topoNode) =>
      new StationNode {
        def identifier: String = s"${line.identifier}_${navigationGraph.identifier}"

        def ownerGraph: NavigationGraph = navigationGraph

        def ownerLine: LinearTransport = line

        def permission: TransportServicePermission = {
          // Determine permission based on arrival/departure capabilities
          (line.canArriveAt(navigationGraph), line.canDepartFrom(navigationGraph)) match {
            case (true, true) => TransportServicePermission.FullyGranted
            case (true, false) => TransportServicePermission.ArriveOnly
            case (false, true) => TransportServicePermission.DepartOnly
            case (false, false) => TransportServicePermission.NoAccess
          }
        }

        override def toString: String = identifier

        override def equals(obj: Any): Boolean = obj match {
          case other: StationNode => this.identifier == other.identifier
          case _ => false
        }

        override def hashCode(): Int = identifier.hashCode
      }
    }.toList
  }

  private def buildAdjacencyList(edges: Set[TransportEdge]): Map[StationNode, Set[StationNode]] = {
    edges
      .groupBy(_.sourceNode) // Group all edges by their source node
      .view
      .mapValues(_.map(_.destinationNode)) // For each source node, extract all destination nodes
      .toMap
  }

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
        naviGraphB1M -> TransportServicePermission.DepartOnly, // TODO: Fix permission limit
        naviGraphB1 -> TransportServicePermission.FullyGranted,
        naviGraphB2 -> TransportServicePermission.FullyGranted,
        naviGraphB3 -> TransportServicePermission.FullyGranted
      ),
      maxVelocity = 2.0,
      acceleration = 1.0
    )

    TransportGraph(List(OPSBank, BBFFBank, PHFFBank))
  }


}

trait StationNode{
  def identifier: String
  def ownerGraph: NavigationGraph
  def ownerLine: LinearTransport
  def permission: TransportServicePermission
}

case class TransportEdge(
  sourceNode: StationNode,
  destinationNode: StationNode,
  cost: Double
)





object TransportGraphTest extends App {

  def runTest(): Unit = {
    println("=== Creating Test Transport Graph ===")

    // Create the test graph
    val graph = TransportGraph.createTestGraph()

    println(s"Graph created with ${graph.nodes.size} nodes")
    graph.nodes.foreach(node => println(s"  - Node: ${node.identifier}"))

    // Test finding a path
    if (graph.nodes.size >= 2) {
      val start = graph.nodes.head
      val goal = graph.nodes(6)

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



