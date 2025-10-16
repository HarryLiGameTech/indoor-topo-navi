class NavigationGraph private(
  val identifier: String,
  val nodes: Map[String, TopoNode],
  val adjacencyList: Map[String, List[Edge]], // Single direction!
  val reverseAdjacency: Map[String, List[Edge]]
) {
  
  private def getOutgoingEdges(originEdgeId: String): List[Edge] = {
    adjacencyList.getOrElse(originEdgeId, List.empty)
  }

  // TODO  
  private def heuristic(from: TopoNode, to: TopoNode) = {
    
  }

  // A* Pathfinding with functional style
  def findPath(
    start: TopoNode,
    goal: TopoNode,
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
        edge <- getOutgoingEdges(current.id) // Iterate over the neighbors of current node
        if constraints.satisfies(edge)
        neighbor = edge.target
        tentativeGScore = gScore(current.id) + edge.weights(mode)
        if tentativeGScore < gScore.getOrElse(neighbor, Double.MaxValue)
      } yield { // When the 2 ifs passes, exec the things below
        gScore(neighbor) = tentativeGScore
        val fScore = tentativeGScore + heuristic(neighbor, goal)
        openSet.enqueue(SearchNode(neighbor, tentativeGScore, fScore, Some(current.id)))
      }
    }

    None
  }
}
