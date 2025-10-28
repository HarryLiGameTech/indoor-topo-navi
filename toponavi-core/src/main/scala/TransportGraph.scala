import cats.effect.IO
import enums.TransportServicePermission

import scala.collection.mutable

// The RailNetwork shall be generated after all the modifiers are applied. i.e. locked and ineligible floor/elevator pair shall not appear here
private class TransportGraph private(
  val nodes: List[StationNode],
  val adjacencyList: Map[StationNode, List[StationNode]]
){
  def findPath(
    start: StationNode,
    goal: StationNode,
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
    fScore(start) = heuristic(start, goal)
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
            fScore(neighbor) = tentativeGScore + heuristic(neighbor, goal)

            // Add to open set (we don't have decrease-key, so we allow duplicates)
            openSet.enqueue((neighbor, fScore(neighbor)))
          }
        }
      }
    }

    // No path found
    None
  }

  // TODO
  private def heuristic(from: StationNode, to: StationNode): Double = {
    0
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

  private def generateTransGraphEdges(lines: List[LinearTransport]): (List[StationNode], List[TransportEdge]) = {
    val edges = mutable.ListBuffer[TransportEdge]()
    val allNodes = mutable.ListBuffer[StationNode]()

    // Iterate over each line
    for (line: LinearTransport <- lines: List[LinearTransport]) {
      // Get all station nodes for this line
      val stationNodes = createStationNodesForLine(line)
      allNodes ++= stationNodes

      // Create edges between EVERY pair of stations (complete graph)
      for {
        from: StationNode <- stationNodes
        to: StationNode <- stationNodes
        if from != to // Avoid self-loops
      } {
        val cost = line.travelTimeBetweenStations(from.ownerGraph, to.ownerGraph)

        // Add edge
        edges += TransportEdge(from, to, cost)
      }
    }
    (allNodes.toList, edges.toList)
  }

  private def createStationNodesForLine(line: LinearTransport): List[StationNode] = {
    line.stationNodes.map { case (navigationGraph, topoNode) =>
      new StationNode {
        def identifier: String = s"${line.identifier}_${navigationGraph.toString}"

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

  private def buildAdjacencyList(edges: List[TransportEdge]): Map[StationNode, List[StationNode]] = {
    edges
      .groupBy(_.sourceNode) // Group all edges by their source node
      .view
      .mapValues(_.map(_.destinationNode)) // For each source node, extract all destination nodes
      .toMap
  }

  // Create a simple test graph
  def createTestGraph(): TransportGraph = {

//    // Create test nodesW
//    val node1: StationNode = new StationNode {
//      def identifier = "Floor1M"
//
//      def ownerLine: ElevatorBank = elevatorBank
//
//      def permission: TransportServicePermission = TransportServicePermission.FullyGranted
//    }
//
//    val node2: StationNode = new StationNode {
//      def identifier = "Floor1"
//
//      def ownerLine: ElevatorBank = elevatorBank
//
//      def permission: TransportServicePermission = TransportServicePermission.FullyGranted
//    }

    val naviGraph1 = NavigationGraph.createSimpleGraph("Floor1M")
    val naviGraph2 = NavigationGraph.createSimpleGraph("Floor1")
    val naviGraph3 = NavigationGraph.createSimpleGraph("FloorB2")
    val naviGraph4 = NavigationGraph.createSimpleGraph("FloorB3")

    // Create mock elevator bank
    val elevatorBank = ElevatorBank(


      identifier = "OPS",
      // Map with some entries
      stationNodes = Map(
        naviGraph1 -> TopoNode("1M_hall", Map.empty),
        naviGraph2 -> TopoNode("1_hall", Map.empty),
        naviGraph3 -> TopoNode("B2_hall", Map.empty),
        naviGraph4 -> TopoNode("B3_hall", Map.empty)
      ),
      stationLocations = Map(
        naviGraph1 -> 14.5,
        naviGraph2 -> 11.0,
        naviGraph3 -> 3.5,
        naviGraph4 -> 0.0
      ),
      stationPermissions = Map(
        naviGraph1 -> TransportServicePermission.FullyGranted,
        naviGraph2 -> TransportServicePermission.FullyGranted,
        naviGraph3 -> TransportServicePermission.FullyGranted,
        naviGraph4 -> TransportServicePermission.FullyGranted
      ),
      maxVelocity = 2.0,
      acceleration = 1.0
    )

    TransportGraph(List(elevatorBank))
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

//    // Test finding a path
//    if (graph.nodes.size >= 2) {
//      val start = graph.nodes.head
//      val goal = graph.nodes.last
//
//      println(s"\n=== Testing Path Finding ===")
//      println(s"Start: ${start.identifier}")
//      println(s"Goal: ${goal.identifier}")
//
//      // Find path (using unsafeRunSync for testing - in production use proper effect handling)
//      val result = graph.findPath(start, goal).unsafeRunSync()
//
//      result match {
//        case Some(path) =>
//          println(s"Path found with ${path.nodes.size} nodes:")
//          path.nodes.foreach(node => println(s"   → ${node.identifier}"))
//        case None =>
//          println("No path found")
//      }
//    } else {
//      println("Not enough nodes to test path finding")
//    }

    // Test adjacency list
    println(s"\n=== Testing Adjacency List ===")
    graph.adjacencyList.foreach { case (node, neighbors) =>
      println(s"${node.identifier} -> ${neighbors.map(_.identifier).mkString(", ")}")
    }
  }

  // Run the test
  runTest()
}



