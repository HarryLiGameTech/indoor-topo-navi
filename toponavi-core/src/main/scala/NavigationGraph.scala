import enums.VisitingMode.Normal
import enums.{PathType, VisitingMode}

import scala.collection.mutable

class NavigationGraph private(
  val identifier: String,
  val nodes: List[TopoNode],
  val adjacencyList: Map[TopoNode, List[AtomicPath]], // Single direction!
  val reverseAdjacency: Map[TopoNode, List[AtomicPath]]
) {
  
  private def getOutgoingEdges(originEdge: TopoNode): List[AtomicPath] = {
    adjacencyList.getOrElse(originEdge, List.empty)
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

  // Intra-Graph Pathfinding - Dijkstra's Algorithm
  def findPath(
    start: TopoNode,
    goal: TopoNode,
    visitingMode: VisitingMode
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

  def runTest(): Unit = {
    val graph = createSimpleGraph("testMap")
    val nodes = mutable.ListBuffer[TopoNode]()
    val edges = mutable.ListBuffer[AtomicPath]()

    // Create all TopoNodes according to the DSL
    val node1 = TopoNode("NW_corner", Map.empty)
    val node2 = TopoNode("NE_corner", Map.empty)
    val node3 = TopoNode("SW_corner", Map.empty)
    val node4 = TopoNode("SE_corner", Map.empty)

    val node5 = TopoNode("SouthExitOutside", Map.empty)
    val node6 = TopoNode("SouthExitInside", Map.empty)
    val node7 = TopoNode("WestExitOutside", Map.empty)
    val node8 = TopoNode("WestExitInside", Map.empty)
    val node9 = TopoNode("EastExitOutside", Map.empty)
    val node10 = TopoNode("EastExitInside", Map.empty) // == OS_hall_outside
    val node11 = TopoNode("hankyu_exit_outside", Map.empty)
    val node12 = TopoNode("hankyu_exit_inside", Map.empty)

    val node13 = TopoNode("OP1_hall", Map.empty)
    val node14 = TopoNode("OP1_hall_outside", Map.empty)
    val node15 = TopoNode("OP2_hall", Map.empty)
    val node16 = TopoNode("OP2_hall_outside", Map.empty)
    val node17 = TopoNode("OEX_hall_low", Map.empty)
    val node18 = TopoNode("OEX_hall_upp", Map.empty)
    val node19 = TopoNode("OEX_hall_upp_outside", Map.empty)
    val node20 = TopoNode("OPS_hall", Map.empty)
    val node22 = TopoNode("OS_hall", Map.empty)

    // Add all nodes to the list
    nodes ++= List(
      node1, node2, node3, node4, node5, node6, node7, node8, node9, node10,
      node11, node12, node13, node14, node15, node16, node17, node18, node19,
      node20, node22
    )

    println(s"Created ${nodes.size} TopoNodes:")
    nodes.foreach(node => println(s"  - ${node}"))

    // Normal paths on the south
    edges += AtomicPath(node5, node6, Map(VisitingMode.Normal -> 5.0), PathType.General) // SouthExitOutside <-> SouthExitInside
    edges += AtomicPath(node6, node5, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node3, node6, Map(VisitingMode.Normal -> 8.0), PathType.General) // SW_corner <-> SouthExitInside
    edges += AtomicPath(node6, node3, Map(VisitingMode.Normal -> 8.0), PathType.General)

    edges += AtomicPath(node4, node6, Map(VisitingMode.Normal -> 8.0), PathType.General) // SE_corner <-> SouthExitInside
    edges += AtomicPath(node6, node4, Map(VisitingMode.Normal -> 8.0), PathType.General)

    // Normal paths on the west
    edges += AtomicPath(node7, node8, Map(VisitingMode.Normal -> 5.0), PathType.General) // WestExitOutside <-> WestExitInside
    edges += AtomicPath(node8, node7, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node3, node14, Map(VisitingMode.Normal -> 5.0), PathType.General) // SW_corner <-> OP1_hall_outside
    edges += AtomicPath(node14, node3, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node14, node8, Map(VisitingMode.Normal -> 3.0), PathType.General) // OP1_hall_outside <-> WestExitInside
    edges += AtomicPath(node8, node14, Map(VisitingMode.Normal -> 3.0), PathType.General)

    // Normal paths on the east
    edges += AtomicPath(node9, node10, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitOutside <-> EastExitInside
    edges += AtomicPath(node10, node9, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node9, node16, Map(VisitingMode.Normal -> 6.0), PathType.General) // EastExitOutside <-> OP2_hall_outside
    edges += AtomicPath(node16, node9, Map(VisitingMode.Normal -> 6.0), PathType.General)

    edges += AtomicPath(node4, node16, Map(VisitingMode.Normal -> 5.0), PathType.General) // SE_corner <-> OP2_hall_outside
    edges += AtomicPath(node16, node4, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node10, node16, Map(VisitingMode.Normal -> 5.0), PathType.General) // EastExitInside <-> OP2_hall_outside
    edges += AtomicPath(node16, node10, Map(VisitingMode.Normal -> 5.0), PathType.General)

    edges += AtomicPath(node11, node12, Map(VisitingMode.Normal -> 3.0), PathType.General) // hankyu_exit_inside <-> hankyu_exit_outside
    edges += AtomicPath(node12, node11, Map(VisitingMode.Normal -> 3.0), PathType.General)

    // Restricted paths (TenantsOnly)
    edges += AtomicPath(node13, node14, Map(VisitingMode.Normal -> 5.0), PathType.Restricted) // OP1_hall_outside <-> OP1_hall
    edges += AtomicPath(node14, node13, Map(VisitingMode.Normal -> 5.0), PathType.Restricted)

    edges += AtomicPath(node15, node16, Map(VisitingMode.Normal -> 5.0), PathType.Restricted) // OP2_hall_outside <-> OP2_hall
    edges += AtomicPath(node16, node15, Map(VisitingMode.Normal -> 5.0), PathType.Restricted)

    edges += AtomicPath(node10, node20, Map(VisitingMode.Normal -> 5.0), PathType.Restricted) // EastExitInside -> OPS_hall_1

    edges += AtomicPath(node22, node10, Map(VisitingMode.Normal -> 4.0), PathType.General) // OS_hall -> EastExitInside (no restriction)

    edges += AtomicPath(node17, node18, Map(VisitingMode.Normal -> 5.0), PathType.Restricted) // OEX_hall_low <-> OEX_hall_upp
    edges += AtomicPath(node18, node17, Map(VisitingMode.Normal -> 5.0), PathType.Restricted)

    edges += AtomicPath(node18, node19, Map(VisitingMode.Normal -> 5.0), PathType.Restricted) // OEX_hall_upp <-> OEX_hall_upp_outside
    edges += AtomicPath(node19, node18, Map(VisitingMode.Normal -> 5.0), PathType.Restricted)

    // Create some basic connections between corners to form a walkable area
    edges += AtomicPath(node1, node2, Map(VisitingMode.Normal -> 10.0), PathType.General) // NW_corner <-> NE_corner
    edges += AtomicPath(node2, node1, Map(VisitingMode.Normal -> 10.0), PathType.General)

    edges += AtomicPath(node2, node4, Map(VisitingMode.Normal -> 10.0), PathType.General) // NE_corner <-> SE_corner
    edges += AtomicPath(node4, node2, Map(VisitingMode.Normal -> 10.0), PathType.General)

    edges += AtomicPath(node4, node3, Map(VisitingMode.Normal -> 10.0), PathType.General) // SE_corner <-> SW_corner
    edges += AtomicPath(node3, node4, Map(VisitingMode.Normal -> 10.0), PathType.General)

    edges += AtomicPath(node3, node1, Map(VisitingMode.Normal -> 10.0), PathType.General) // SW_corner <-> NW_corner
    edges += AtomicPath(node1, node3, Map(VisitingMode.Normal -> 10.0), PathType.General)

    println(s"Created ${edges.size} AtomicPaths")
    edges.foreach(edge => println(s"  - ${edge}"))

    // Test the graph
    val start = node1 // NW_corner
    val goal = node4 // SE_corner

    println(s"Testing path from ${start.identifier} to ${goal.identifier}")
    val path = graph.findPath(start, goal, Normal) // TODO: Fix functionality. Easiest pathfinding still fails

    path match {
      case Some(foundPath) =>
        println(s"✅ Path found: ${foundPath.routeNodes.map(_.identifier).mkString(" -> ")}")
      case None =>
        println("❌ No path found")
    }
  }

  // Factory method to create NavigationGraph instances
  def apply(
    identifier: String,
    nodes: List[TopoNode] = List.empty,
    adjacencyList: Map[TopoNode, List[AtomicPath]] = Map.empty,
    reverseAdjacency: Map[TopoNode, List[AtomicPath]] = Map.empty
  ): NavigationGraph = {
    new NavigationGraph(identifier, nodes, adjacencyList, reverseAdjacency)
  }

  // Create simple NavigationGraphs for testing
  def createSimpleGraph(identifier: String): NavigationGraph = {
    NavigationGraph(identifier)
  }

  runTest()
}