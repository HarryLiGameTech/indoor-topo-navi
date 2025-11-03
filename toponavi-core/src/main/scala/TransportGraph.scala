import cats.effect.IO
import enums.TransportServicePermission
import enums.TransportServicePermission.{ArriveOnly, DepartOnly}

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

    Path(topoNodes, List.empty)
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
          if ((from != to && from.permission != ArriveOnly) && to.permission != DepartOnly) // Avoid self-loops and takes permission into account
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









