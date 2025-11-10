package data

import cats.effect.IO
import enums.TransportServicePermission.{ArriveOnly, DepartOnly}
import enums.VisitingMode.Normal
import enums.{PathType, RoutePlanningPreferences, TransportServicePermission, VisitingMode}

import scala.collection.mutable

// The RailNetwork shall be generated after all the modifiers are applied. i.e. locked and ineligible floor/elevator pair shall not appear here
class TransportGraph private(
  val nodes: List[StationNode],
  val adjacencyList: Map[StationNode, Set[StationNode]]
){
  def findPath(
    start: StationNode,
    goal: StationNode,
    floorNameList: List[String]
  ): Option[TransportationPath] = {
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
        return Some(reconstructPath(cameFrom, current)) // This step is OK, getAllEdges() works fine
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


  // This one only provide cross-graph fuzzy pathfinding without considering the start and goal nodes inside each graph
  // Outputs the returnIndex-th quickest cross-graph traveling solution (0 means quickest and n means slowest)
  def findPathFuzzy(
    startGraph: NavigationGraph,
    goalGraph: NavigationGraph,
    floorNameList: List[String],
    preference: RoutePlanningPreferences,
    returnIndex: Int
  ): Option[TransportationPath] = {
    val startNodes = nodes.filter(_.ownerGraph == startGraph)
    val goalNodes = nodes.filter(_.ownerGraph == goalGraph)
    preference match{
      case RoutePlanningPreferences.MinimizeTime =>
        val allPaths = mutable.ListBuffer[TransportationPath]()
        for (startNode <- startNodes){
          for (goalNode <- goalNodes){
            val pathOption = findPath(startNode, goalNode, floorNameList)
            pathOption match{
              case Some(path) => allPaths += path
              case None => ()
            }
          }
        }
        val sortedPaths = allPaths.sortBy(_.totalCost)
        if (sortedPaths.isDefinedAt(returnIndex)){
          Some(TransportationPath(sortedPaths(returnIndex).routeNodes, sortedPaths(returnIndex).routeEdges))
        }
        else{
          None
        }
      case _ => None
    }

  }

  def findPathFuzzy(
    startGraph: NavigationGraph,
    goal: StationNode,
    floorNameList: List[String],
    returnIndex: Int
  ): TransportationPath = {
    val startNodes = nodes.filter(_.ownerGraph == startGraph)
    // TODO: Try findPath for all possible combinations
    TransportationPath(List.empty, List.empty)
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

    // Calculate the absolute index difference and multiply by 2.5
    val indexDifference = math.abs(fromIndex - toIndex)
    2.5 * indexDifference
  }

  private def distanceBetween(from: StationNode, to: StationNode): Double = {
    // Use the line's travel time
    from.ownerLine.travelTimeBetweenStations(from.ownerGraph, to.ownerGraph)
  }

  // TODO: Option[TransportationPath]
  private def reconstructPath(cameFrom: mutable.Map[StationNode, StationNode], current: StationNode): TransportationPath = {
    val totalPath = mutable.ListBuffer[StationNode]()
    val pathEdges = mutable.ListBuffer[TransportEdge]()
    var node = current

    while (cameFrom.contains(node)) {
      val parent = cameFrom(node)

      totalPath.prepend(node)

      if (parent.ownerLine == node.ownerLine){
        pathEdges.prepend(TransportEdge(parent, node, parent.ownerLine.travelTimeBetweenStations(parent.ownerGraph, node.ownerGraph)))
      }
      else{
        val transferCost: Double = parent.ownerGraph.findPath(
          parent.ownerLine.stationNodes(parent.ownerGraph),
          node.ownerLine.stationNodes(node.ownerGraph),
          VisitingMode.Normal
        ).getOrElse(throw RuntimeException("Interchange: Path not found")).totalCost(VisitingMode.Normal)
        pathEdges.prepend(TransportEdge(parent, node, transferCost))
      }

      node = parent  // Move to the next node
    }
    totalPath.prepend(node)

    TransportationPath(totalPath.toList, pathEdges.toList)
  }
  
}

def trim(stationNodeId: String): String = {
  // Assuming the format is "LineID@GraphID", we split by "@" and take the first part
  stationNodeId.split("@").headOption.getOrElse(stationNodeId)
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
          // Calculate the transfer-time according to the intra-map navigation
          val transferCost: Double = from.ownerGraph.findPath(
            from.ownerLine.stationNodes(from.ownerGraph),
            to.ownerLine.stationNodes(to.ownerGraph),
            VisitingMode.Normal
          ).getOrElse(throw RuntimeException("Interchange: Path not found")).totalCost(VisitingMode.Normal)

          edges += TransportEdge(from, to, transferCost)
        }
      }
    }
    (allNodes.toList, edges.toSet)
  }

  private def createStationNodesForLine(line: LinearTransport): List[StationNode] = {
    line.stationNodes.map {
      case (navigationGraph, topoNode) =>
        StationNode (
          identifier = s"${line.identifier}@${navigationGraph.identifier}",

          ownerGraph = navigationGraph,

          ownerLine = line,

          permission = // Determine permission based on arrival/departure capabilities
            (line.canArriveAt(navigationGraph), line.canDepartFrom(navigationGraph)) match {
              case (true, true) => TransportServicePermission.FullyGranted
              case (true, false) => TransportServicePermission.ArriveOnly
              case (false, true) => TransportServicePermission.DepartOnly
              case (false, false) => TransportServicePermission.NoAccess
            }
        )
    }
  }.toList
}

  private def buildAdjacencyList(edges: Set[TransportEdge]): Map[StationNode, Set[StationNode]] = {
    edges
      .groupBy(_.source) // Group all edges by their source node
      .view
      .mapValues(_.map(_.target)) // For each source node, extract all destination nodes
      .toMap
  }

// This one might have fucked up
case class StationNode(
  identifier: String,
  ownerGraph: NavigationGraph,
  ownerLine: LinearTransport,
  permission: TransportServicePermission
){
//  def localNode: TopoNode = ownerGraph.nodes.find(n => n.identifier == trim(identifier)).getOrElse(throw RuntimeException(s"StationNode: Local node for ${identifier} not found"))
  def localNode: TopoNode = ownerLine.stationNodes(ownerGraph)

  override def toString: String = identifier

  override def equals(obj: Any): Boolean = obj match {
    case other: StationNode => this.identifier == other.identifier
    case _ => false
  }

  override def hashCode(): Int = identifier.hashCode
}

case class TransportEdge(
  source: StationNode,
  target: StationNode,
  cost: Double
)



