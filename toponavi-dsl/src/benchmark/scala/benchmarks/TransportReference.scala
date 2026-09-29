package benchmarks

import compiler.CompilationResult
import data.{GlobalNode, NavigationOutputPath}
import enums.VisitingMode.Normal

import scala.collection.mutable

// Independent shortest-path oracle over physical nodes and compiled directed edges.
// It checks planner behavior, not the correctness of the elevator timing model.
class TransportReference(result: CompilationResult, scenario: TransportScenario) {
  private def key(node: GlobalNode): String = s"${node.owningGraph.identifier}::${node.localNode.identifier}"

  private val walking = result.graphs.values.toVector.flatMap { graph =>
    graph.adjacencyList.map { edge =>
      (s"${graph.identifier}::${edge.source.identifier}",
        s"${graph.identifier}::${edge.target.identifier}", edge.costs(Normal))
    }
  }
  private val rides = result.transportGraph.adjacencyList.toVector.flatMap { case (source, neighbors) =>
    neighbors.toVector.collect {
      case (target, cost) if source.ownerLine == target.ownerLine =>
        require(!scenario.unavailableLines.contains(source.ownerLine.identifier),
          s"Unavailable line ${source.ownerLine.identifier} has a ride edge")
        (s"${source.ownerGraph.identifier}::${source.localNode.identifier}",
          s"${target.ownerGraph.identifier}::${target.localNode.identifier}", cost)
    }
  }
  private val edges = walking ++ rides
  require(edges.forall { case (_, _, cost) => cost.isFinite && cost >= 0.0 },
    "Reference graph has non-finite or negative costs")
  require(result.graphs.size == scenario.floors, "Unexpected compiled floor count")
  require(result.transportGraph.nodes.size == scenario.lines.map(_.floors.size).sum,
    "Unexpected compiled station count")

  private val outgoing = edges.groupMap(_._1)(edge => edge._2 -> edge._3)
  private val costsByPair = edges.groupMap(edge => edge._1 -> edge._2)(_._3)

  def shortestCost(start: String, goal: String): Option[Double] = {
    given Ordering[(String, Double)] = Ordering.by[(String, Double), Double](_._2).reverse
    val queue = mutable.PriorityQueue(start -> 0.0)
    val distance = mutable.Map(start -> 0.0)
    while (queue.nonEmpty) {
      val (current, cost) = queue.dequeue()
      if (cost <= distance.getOrElse(current, Double.PositiveInfinity)) {
        if (current == goal) return Some(cost)
        outgoing.getOrElse(current, Vector.empty).foreach { case (next, edgeCost) =>
          val candidate = cost + edgeCost
          if (candidate < distance.getOrElse(next, Double.PositiveInfinity)) {
            distance(next) = candidate
            queue.enqueue(next -> candidate)
          }
        }
      }
    }
    None
  }

  def validate(
    start: String,
    goal: String,
    expected: Option[Double],
    actual: Option[NavigationOutputPath],
    isHighRise: Boolean
  ): Double = {
    require(expected.isDefined == scenario.expectRoute, s"Fixture reachability mismatch: $start -> $goal")
    require(actual.isDefined == expected.isDefined, s"Route reachability mismatch: $start -> $goal")
    actual.fold(0.0) { route =>
      require(route.routeNodes.nonEmpty && key(route.routeNodes.head) == start && key(route.routeNodes.last) == goal,
        s"Wrong route endpoints: $start -> $goal")
      var current = start
      route.routeEdges.foreach { edge =>
        require(key(edge.source) == current, "Discontinuous route")
        val legalCosts = costsByPair.getOrElse(key(edge.source) -> key(edge.target), Vector.empty)
        require(edge.cost.isFinite && legalCosts.exists(cost => math.abs(cost - edge.cost) <= 1e-7),
          s"Route contains an unavailable edge or incorrect cost: ${key(edge.source)} -> ${key(edge.target)}")
        current = key(edge.target)
      }
      require(current == goal, "Route edges do not reach the destination")
      val optimum = expected.get
      val tolerance = math.max(1e-7, optimum * 1e-9)
      require(route.totalCost >= optimum - tolerance, "Route cost is below reference optimum")
      if (!isHighRise) require(math.abs(route.totalCost - optimum) <= tolerance,
        s"Low-rise cost ${route.totalCost} differs from reference optimum $optimum")
      // High-rise routing is heuristic; report its gap instead of requiring optimality.
      math.max(0.0, (route.totalCost / optimum - 1.0) * 100.0)
    }
  }
}
