import scala.collection.mutable

// The RailNetwork shall be generated after all the modifiers are applied. i.e. locked and ineligible floor/elevator pair shall not appear here
class RailNetwork private(
  val nodes: Array[StationNode],
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

trait StationNode{
  def identifier: String
  def owner: VehicleLine
}




