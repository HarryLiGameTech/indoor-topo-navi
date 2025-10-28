import cats.effect.IO
import enums.VisitingMode

import scala.collection.mutable

class NavigationGraph private(
  val identifier: String,
  val nodes: Map[String, TopoNode],
  val adjacencyList: Map[String, List[AtomicPath]], // Single direction!
  val reverseAdjacency: Map[String, List[AtomicPath]]
) {
  
  private def getOutgoingEdges(originEdgeId: String): List[AtomicPath] = {
    adjacencyList.getOrElse(originEdgeId, List.empty)
  }

  private def reconstructPath(cameFrom: mutable.Map[TopoNode, TopoNode], current: TopoNode): Path = {
    val totalPath = mutable.ListBuffer[TopoNode]()
    var node = current

    // Trace back from goal to start using the cameFrom map
    while (cameFrom.contains(node)) {
      totalPath.prepend(node) // Add current node to front of path
      node = cameFrom(node) // Move to parent node
    }
    totalPath.prepend(node) // Add the start node

    Path(totalPath.toList)
  }

  // Dijkstra's Algorithm
  def findPath(
    start: TopoNode,
    goal: TopoNode,
  ): Option[Path] = {
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
          return Some(reconstructPath(cameFrom, current))
        }

        // Explore neighbors
        for (edge <- getOutgoingEdges(current.identifier)) {
          val neighbor = edge.target
          if (!visited.contains(neighbor)) {
            val newDist = dist(current) + edge.costs(VisitingMode.Normal)

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
}


object NavigationGraph {
  // Factory method to create NavigationGraph instances
  def apply(
    identifier: String,
    nodes: Map[String, TopoNode] = Map.empty,
    adjacencyList: Map[String, List[AtomicPath]] = Map.empty,
    reverseAdjacency: Map[String, List[AtomicPath]] = Map.empty
  ): NavigationGraph = {
    new NavigationGraph(identifier, nodes, adjacencyList, reverseAdjacency)
  }

  // Create simple NavigationGraphs for testing
  def createSimpleGraph(identifier: String): NavigationGraph = {
    NavigationGraph(identifier)
  }
}