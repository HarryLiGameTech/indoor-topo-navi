class NavigationGraph private(
  val nodes: Map[String, TopoNode],
  val adjacencyList: Map[String, List[Edge]],
  val reverseAdjacency: Map[String, List[Edge]]
) {
  // A* Pathfinding with functional style
  def findPath(
                start: String,
                goal: String,
                mode: RoutingMode,
                constraints: NavigationConstraints = NavigationConstraints.default
              ): IO[Option[Path]] = IO {

    case class SearchNode(
                           id: String,
                           cost: Double,
                           heuristic: Double,
                           parent: Option[String]
                         )

    implicit val ordering: Ordering[SearchNode] = Ordering.by(-_.heuristic)
    val openSet = mutable.PriorityQueue[SearchNode]()
    val gScore = mutable.Map(start -> 0.0)

    openSet.enqueue(SearchNode(start, 0, heuristic(start, goal), None))

    while (openSet.nonEmpty) {
      val current = openSet.dequeue()

      if (current.id == goal) {
        return Some(reconstructPath(current))
      }

      for {
        edge <- getOutgoingEdges(current.id)
        if constraints.satisfies(edge)
        neighbor = edge.target
        tentativeGScore = gScore(current.id) + edge.weights(mode)
        if tentativeGScore < gScore.getOrElse(neighbor, Double.MaxValue)
      } yield {
        gScore(neighbor) = tentativeGScore
        val fScore = tentativeGScore + heuristic(neighbor, goal)
        openSet.enqueue(SearchNode(neighbor, tentativeGScore, fScore, Some(current.id)))
      }
    }

    None
  }
}
