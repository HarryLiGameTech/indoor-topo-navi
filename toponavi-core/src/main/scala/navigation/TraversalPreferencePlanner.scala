package navigation

import data.*
import enums.{NavigationError, RouteEdgeCategory, RoutePlanningPreferences, VisitingMode}
import enums.NavigationError.{InvalidData, NoRouteFound, PreferenceSearchLimitExceeded}
import enums.RoutePlanningPreferences.{MinimizePhysicalDemands, MinimizeTime, MinimizeTransfers}

import scala.collection.mutable

// Complete-route search for soft tag avoidance and mandatory intermediate-node dwell.
class TraversalPreferencePlanner(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  maxLabels: Int = 50000,
  maxExpansions: Int = 200000
) {
  private def key(node: GlobalNode): String = s"${node.owningGraph.identifier}::${node.localNode.identifier}"
  private case class SearchLimit() extends RuntimeException

  def navigate(
    source: GlobalNode,
    destination: GlobalNode,
    visitingMode: VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean,
    minimizeTag: Option[String]
  ): Either[NavigationError, NavigationOutputPath] = {
    try {
      val start = key(source)
      val goal = key(destination)
      val nodes = graphs.values.flatMap(g => g.nodes.map(n => s"${g.identifier}::${n.identifier}" -> GlobalNode(g, n))).toMap
      val walking = graphs.values.toVector.flatMap { graph =>
        graph.adjacencyList.filter { edge =>
          tagPolicy.allows(edge) && (allowUncertainAccess || !edge.hasFailedUncertainAccess)
        }.map(RouteEdge.fromAtomicPath(graph, _, visitingMode))
      }
      val rides = transportGraph.adjacencyList.toVector.flatMap { case (from, neighbors) =>
        neighbors.toVector.flatMap { case (to, cost) =>
          if (from.ownerLine != to.ownerLine) Vector.empty
          else transportGraph.edgeTraversal(from, to, cost, allowUncertainAccess).toVector.map { case (time, uncertainty) =>
            val traversal = TransportTraversal.between(from.ownerLine, from.ownerGraph, to.ownerGraph)
            RouteEdge(
              nodes(s"${from.ownerGraph.identifier}::${from.localNode.identifier}"),
              nodes(s"${to.ownerGraph.identifier}::${to.localNode.identifier}"),
              time,
              if (traversal.kind == TransportKind.Stairs) RouteEdgeCategory.Climbing else RouteEdgeCategory.Transport,
              s"Take ${from.ownerLine.identifier} from ${from.ownerGraph.identifier} to ${to.ownerGraph.identifier}",
              uncertainAccess = uncertainty,
              transport = Some(traversal)
            )
          }
        }
      }
      val edges = (walking ++ rides).filter(edge => tagPolicy.allowsEntry(edge.target.localNode)).map { edge =>
        val dwell = if (key(edge.target) == goal) 0.0 else edge.target.localNode.minimumDwellSeconds
        require(edge.cost.isFinite && edge.cost >= 0.0, "Traversal costs must be finite and non-negative")
        edge.copy(cost = edge.cost + dwell, nodeDwellSeconds = dwell)
      }.sortBy { edge =>
        (key(edge.source), key(edge.target), edge.transport.map(_.lineIdentifier).getOrElse(""),
          edge.cost, edge.attributes.toVector.sortBy(_._1).mkString,
          edge.uncertainAccess.toList.flatMap(_.uncertaintyReasons)
            .map(reason => (reason.conditionType, reason.reason.getOrElse(""), reason.conditionSatisfied)).sorted.mkString)
      }
      val outgoing = edges.zipWithIndex.groupMap(pair => key(pair._1.source))(identity)
      val reverse = edges.groupBy(edge => key(edge.target))

      def exposure(edge: RouteEdge): Double = minimizeTag.fold(0.0) { tag =>
        val traversal = if (edge.traversalMetadata.tags.contains(tag)) edge.cost - edge.nodeDwellSeconds else 0.0
        val dwell = if (RouteTraversalMetadata.tagsFrom(edge.target.localNode.attributes).contains(tag)) edge.nodeDwellSeconds else 0.0
        traversal + dwell
      }

      def lowerBounds(weight: RouteEdge => Double): Map[String, Double] = {
        given Ordering[(String, Double)] = Ordering.by[(String, Double), Double](_._2).reverse
        val queue = mutable.PriorityQueue(goal -> 0.0)
        val distances = mutable.Map(goal -> 0.0)
        while (queue.nonEmpty) {
          val (current, distance) = queue.dequeue()
          if (distance <= distances(current)) {
            reverse.getOrElse(current, Vector.empty).foreach { edge =>
              val previous = key(edge.source)
              val candidate = distance + weight(edge)
              if (candidate < distances.getOrElse(previous, Double.PositiveInfinity)) {
                distances(previous) = candidate
                queue.enqueue(previous -> candidate)
              }
            }
          }
        }
        distances.toMap
      }

      val timeRemaining = lowerBounds(_.cost)
      if (!timeRemaining.contains(start)) return Left(NoRouteFound(s"No route from $start to $goal"))
      val primaryRemaining = preference match {
        case MinimizeTime => timeRemaining
        case MinimizePhysicalDemands => lowerBounds(_.physicalDemandScore)
        case MinimizeTransfers => timeRemaining.keys.map(_ -> 0.0).toMap
      }
      val exposureRemaining = if (minimizeTag.isDefined) lowerBounds(exposure) else Map.empty[String, Double]

      case class Label(
        node: String,
        lastLine: Option[String],
        time: Double,
        transfers: Int,
        physical: Double,
        tagExposure: Double,
        reversedEdges: List[RouteEdge],
        visited: Set[String],
        signature: String
      ) {
        def primary: Double = preference match {
          case MinimizeTime => time
          case MinimizeTransfers => transfers.toDouble
          case MinimizePhysicalDemands => physical
        }
        def state: (String, Option[String]) = node -> (if (preference == MinimizeTransfers) lastLine else None)
      }

      var generated = 0
      var expanded = 0
      def search(limit: Double, useTags: Boolean): Option[Label] = {
        def priority(label: Label): (Double, Double, Double, String) = {
          val primary = label.primary + primaryRemaining.getOrElse(label.node, Double.PositiveInfinity)
          val time = label.time + timeRemaining.getOrElse(label.node, Double.PositiveInfinity)
          if (useTags) (label.tagExposure + exposureRemaining.getOrElse(label.node, Double.PositiveInfinity), primary, time, label.signature)
          else (primary, time, 0.0, label.signature)
        }
        given Ordering[Label] = Ordering.by[Label, (Double, Double, Double, String)](priority).reverse
        val queue = mutable.PriorityQueue.empty[Label]
        val labels = mutable.Map.empty[(String, Option[String]), Vector[Label]]

        def dominates(a: Label, b: Label): Boolean =
          a.primary <= b.primary && a.time <= b.time && (!useTags || a.tagExposure <= b.tagExposure) &&
            a.visited.subsetOf(b.visited) &&
            (a.primary < b.primary || a.time < b.time || (useTags && a.tagExposure < b.tagExposure) || a.signature <= b.signature)

        def enqueue(label: Label): Unit = {
          val existing = labels.getOrElse(label.state, Vector.empty)
          if (!existing.exists(dominates(_, label))) {
            generated += 1
            if (generated > maxLabels) throw SearchLimit()
            labels(label.state) = existing.filterNot(dominates(label, _)) :+ label
            queue.enqueue(label)
          }
        }

        enqueue(Label(start, None, 0.0, 0, 0.0, 0.0, Nil, Set(start), ""))
        while (queue.nonEmpty) {
          val current = queue.dequeue()
          if (labels.getOrElse(current.state, Vector.empty).contains(current)) {
            if (current.node == goal) return Some(current)
            outgoing.getOrElse(current.node, Vector.empty).foreach { case (edge, index) =>
              expanded += 1
              if (expanded > maxExpansions) throw SearchLimit()
              val next = key(edge.target)
              if (!current.visited.contains(next) && timeRemaining.contains(next)) {
                val line = edge.transport.map(_.lineIdentifier)
                val transfers = current.transfers + (if (line.exists(id => current.lastLine.exists(_ != id))) 1 else 0)
                val candidate = Label(next, line.orElse(current.lastLine), current.time + edge.cost, transfers,
                  current.physical + edge.physicalDemandScore, current.tagExposure + exposure(edge),
                  edge :: current.reversedEdges, current.visited + next, current.signature + f"/$index%08d")
                val tolerance = if (limit == 0.0) 0.0 else 1e-9
                if (candidate.primary + primaryRemaining(next) <= limit + tolerance) enqueue(candidate)
              }
            }
          }
        }
        None
      }

      val primary = search(Double.PositiveInfinity, useTags = false)
      val selected = primary.flatMap { best =>
        if (minimizeTag.isEmpty) Some(best)
        else {
          val limit = preference match {
            case MinimizeTime => best.primary + 60.0
            case MinimizeTransfers => best.primary + 2.0
            case MinimizePhysicalDemands => best.primary * 1.25
          }
          search(limit, useTags = true)
        }
      }
      selected match {
        case None => Left(NoRouteFound(s"No route from $start to $goal"))
        case Some(label) =>
          val routeEdges = label.reversedEdges.reverse
          Right(NavigationOutputPath(source :: routeEdges.map(_.target), routeEdges))
      }
    } catch {
      case _: SearchLimit => Left(PreferenceSearchLimitExceeded(
        s"Traversal preference search exceeded $maxLabels labels or $maxExpansions edge expansions"))
      case error: IllegalArgumentException => Left(InvalidData(error.getMessage))
    }
  }
}
