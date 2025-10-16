import enums.TransportServicePermission

// The RailNetwork shall be generated after all the modifiers are applied. i.e. locked and ineligible floor/elevator pair shall not appear here
class TransportGraph private(
  val nodes: List[StationNode],
  val adjacencyList: Map[StationNode, List[StationNode]]
){
  def findPath(
    start: StationNode,
    goal: StationNode,
    constraints: NavigationConstraints = NavigationConstraints.default
  ): IO[Option[Path]] = IO {
    // Priority queue for open set (min-heap based on fScore)
    implicit val nodeOrdering: Ordering[(StationNode, Double)] =
      Ordering.by[(StationNode, Double), Double](_._2).reverse

    val openSet = mutable.PriorityQueue.empty[(StationNode, Double)]
    val gScore = mutable.Map[StationNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val fScore = mutable.Map[StationNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val cameFrom = mutable.Map[StationNode, StationNode]()

    // Initialize starting node
    gScore(start) = 0.0
    fScore(start) = heuristic(start, goal, constraints)
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
          if (constraints.isAccessible(neighbor)) {
            // Tentative gScore is distance from start to neighbor through current
            val tentativeGScore = gScore(current) + distanceBetween(current, neighbor, constraints)

            // If we found a better path to neighbor
            if (tentativeGScore < gScore(neighbor)) {
              // This path to neighbor is better than any previous one
              cameFrom(neighbor) = current
              gScore(neighbor) = tentativeGScore
              fScore(neighbor) = tentativeGScore + heuristic(neighbor, goal, constraints)

              // Add to open set (we don't have decrease-key, so we allow duplicates)
              openSet.enqueue((neighbor, fScore(neighbor)))
            }
          }
        }
      }
    }

    // No path found
    None
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
    val edges = new List[TransportEdge]

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
        val edgeType = TransportEdgeType.ELEVATOR_TRAVEL

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

  }

  // Create a simple test graph
  def createTestGraph(): TransportGraph = {
    // Create mock elevator bank
    val elevatorBank = ElevatorBank(
      identifier = "TestElevator",
      stationNodes = Map.empty, // Fill with actual data
      stationLocations = Map.empty,
      stationPermissions = Map.empty,
      maxVelocity = 2.0,
      acceleration = 1.0
    )

    // Create test nodes
    val node1 = new StationNode {
      def identifier = "Floor1"
      def ownerLine = elevatorBank
      def permission = TransportServicePermission.FullyGranted
      def getVerticalPosition: Double = 0.0
      def getHorizontalPosition: Double = 0.0
    }

    val node2 = new StationNode {
      def identifier = "Floor2"
      def ownerLine = elevatorBank
      def permission = TransportServicePermission.FullyGranted
      def getVerticalPosition: Double = 10.0
      def getHorizontalPosition: Double = 0.0
    }

    val nodes = Array(node1, node2)
    val edges = List((node1, node2))

    TransportGraph(nodes, edges)
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




