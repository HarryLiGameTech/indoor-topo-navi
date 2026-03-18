package data

import enums.VisitingMode.Normal
import enums.{PathType, VisitingMode}

import scala.collection.mutable

class NavigationGraph private(
  val identifier: String,
  val nodes: List[TopoNode] = List.empty,
  val adjacencyList: List[AtomicPath] = List.empty, // Single direction!
  val reverseAdjacency: Map[TopoNode, List[AtomicPath]] = Map.empty
) extends Serializable {
  
  private def getOutgoingEdges(originNode: TopoNode): List[AtomicPath] = {
    adjacencyList.filter(_.source == originNode)
  }

  private def reconstructPath(cameFrom: mutable.Map[TopoNode, TopoNode], current: TopoNode, edges: List[AtomicPath]): IntraMapPath = {
    val totalPath = mutable.ListBuffer[TopoNode]()
    val pathEdges = mutable.ListBuffer[AtomicPath]() // Track the edges used
    var node = current

    // Trace back from goal to start using the cameFrom map
    while (cameFrom.contains(node)) {
      val parent = cameFrom(node)
      totalPath.prepend(node) // Add current node to front of path

      // Find the edge that connects parent -> node
      val edge = edges.find(e => e.source == parent && e.target == node)
      edge.foreach(pathEdges.prepend) // Add the edge to the path

      node = parent // Move to parent node
    }
    totalPath.prepend(node) // Add the start node

    IntraMapPath(totalPath.toList, pathEdges.toList) // Include edges in the Path
  }

  // Intra-Graph Pathfinding - Dijkstra's Algorithm
  def findPath(
    start: TopoNode,
    goal: TopoNode,
    visitingMode: VisitingMode
  ): Option[IntraMapPath] = {
    // Priority queue for nodes to explore (min-heap based on cost)
    implicit val ordering: Ordering[(TopoNode, Double)] =
      Ordering.by[(TopoNode, Double), Double](_._2).reverse

    val openSet = mutable.PriorityQueue.empty[(TopoNode, Double)]
    val dist = mutable.Map[TopoNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val cameFrom = mutable.Map[TopoNode, TopoNode]()
    val visited = mutable.Set[TopoNode]()

    // Initialize starting node
    dist(start) = 0.0
    openSet.enqueue((start, 0.0))

    while (openSet.nonEmpty) {
      val (current, currentDist) = openSet.dequeue()

      // Skip if we've already found a better path to this node
      if (currentDist > dist(current)) {
        // This can happen because priority queue doesn't support decrease-key
        ()
      } else if (!visited.contains(current)) {
        visited.add(current)

        // If we reached the goal, reconstruct the path
        if (current == goal) {
          return Some(reconstructPath(cameFrom, current, adjacencyList))
        }

        // Explore neighbors using the new adjacency list structure
        for (edge <- getOutgoingEdges(current)) {
          val neighbor = edge.target
          if (!visited.contains(neighbor)) {
            val newDist = dist(current) + edge.costs(visitingMode)

            // If we found a better path to neighbor
            if (newDist < dist(neighbor)) {
              dist(neighbor) = newDist
              cameFrom(neighbor) = current
              openSet.enqueue((neighbor, newDist))
            }
          }
        }
      }
    }

    // No path found
    None
  }

  override def toString: String = identifier
}


object NavigationGraph extends App{

  // Factory method to create NavigationGraph instances
  def apply(
    identifier: String,
    nodes: List[TopoNode] = List.empty,
    adjacencyList: List[AtomicPath] = List.empty,
    reverseAdjacency: Map[TopoNode, List[AtomicPath]] = Map.empty
  ): NavigationGraph = {
    new NavigationGraph(identifier, nodes, adjacencyList, reverseAdjacency)
  }

  // Create simple NavigationGraphs for testing
  def createSimpleGraph(identifier: String): NavigationGraph = {
    NavigationGraph(identifier)
  }

}