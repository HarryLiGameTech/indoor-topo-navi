package navigation

import data.*
import enums.{NavigationError, RouteEdgeCategory, RoutePlanningPreferences, VisitingMode}
import enums.NavigationError.{InvalidData, NoRouteFound, PreferenceSearchLimitExceeded}
import enums.RoutePlanningPreferences.{MinimizePhysicalDemands, MinimizeTime, MinimizeTransfers}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

// Complete-route search for soft tag avoidance and mandatory intermediate-node dwell.
class TraversalPreferencePlanner(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  maxLabels: Int = 50000,
  maxExpansions: Int = 200000
) {
  private def key(node: GlobalNode): String = s"${node.owningGraph.identifier}::${node.localNode.identifier}"

  private case class SearchBudget(generated: Int = 0, expanded: Int = 0) {
    private def exceeded: NavigationError = PreferenceSearchLimitExceeded(
      s"Traversal preference search exceeded $maxLabels labels or $maxExpansions edge expansions")

    def generate: Either[NavigationError, SearchBudget] =
      Either.cond(generated < maxLabels, copy(generated = generated + 1), exceeded)

    def expand: Either[NavigationError, SearchBudget] =
      Either.cond(expanded < maxExpansions, copy(expanded = expanded + 1), exceeded)
  }

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
        given Ordering[(String, Double)] = Ordering.by { case (node, distance) => (distance, node) }

        @tailrec
        def visit(queue: TreeSet[(String, Double)], distances: Map[String, Double]): Map[String, Double] =
          queue.headOption match {
            case None => distances
            case Some((current, distance)) if distance > distances(current) => visit(queue.tail, distances)
            case Some((current, distance)) =>
              val (nextQueue, nextDistances) = reverse.getOrElse(current, Vector.empty)
                .foldLeft((queue.tail, distances)) { case ((pending, known), edge) =>
                  val previous = key(edge.source)
                  val candidate = distance + weight(edge)
                  if (candidate < known.getOrElse(previous, Double.PositiveInfinity))
                    (pending + (previous -> candidate), known.updated(previous, candidate))
                  else (pending, known)
                }
              visit(nextQueue, nextDistances)
          }

        visit(TreeSet(goal -> 0.0), Map(goal -> 0.0))
      }

      val timeRemaining = lowerBounds(_.cost)
      val noRoute = NoRouteFound(s"No route from $start to $goal")
      if (!timeRemaining.contains(start)) Left(noRoute)
      else {
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

        case class SearchState(
          queue: TreeSet[Label],
          labels: Map[(String, Option[String]), Vector[Label]],
          budget: SearchBudget
        )
        case class SearchResult(label: Option[Label], budget: SearchBudget)

        def search(limit: Double, useTags: Boolean, budget: SearchBudget): Either[NavigationError, SearchResult] = {
          def priority(label: Label): (Double, Double, Double, String) = {
            val primary = label.primary + primaryRemaining.getOrElse(label.node, Double.PositiveInfinity)
            val time = label.time + timeRemaining.getOrElse(label.node, Double.PositiveInfinity)
            if (useTags) (label.tagExposure + exposureRemaining.getOrElse(label.node, Double.PositiveInfinity), primary, time, label.signature)
            else (primary, time, 0.0, label.signature)
          }
          // The path signature keeps equally scored routes distinct in the ordered set.
          given Ordering[Label] = Ordering.by[Label, (Double, Double, Double, String)](priority)

          def dominates(a: Label, b: Label): Boolean =
            a.primary <= b.primary && a.time <= b.time && (!useTags || a.tagExposure <= b.tagExposure) &&
              a.visited.subsetOf(b.visited) &&
              (a.primary < b.primary || a.time < b.time || (useTags && a.tagExposure < b.tagExposure) || a.signature <= b.signature)

          def enqueue(state: SearchState, label: Label): Either[NavigationError, SearchState] = {
            val existing = state.labels.getOrElse(label.state, Vector.empty)
            if (existing.exists(dominates(_, label))) Right(state)
            else state.budget.generate.map { nextBudget =>
              state.copy(
                queue = state.queue + label,
                labels = state.labels.updated(label.state, existing.filterNot(dominates(label, _)) :+ label),
                budget = nextBudget
              )
            }
          }

          def expand(state: SearchState, current: Label, edge: RouteEdge, index: Int): Either[NavigationError, SearchState] =
            state.budget.expand.flatMap { nextBudget =>
              val nextState = state.copy(budget = nextBudget)
              val next = key(edge.target)
              if (current.visited.contains(next) || !timeRemaining.contains(next)) Right(nextState)
              else {
                val line = edge.transport.map(_.lineIdentifier)
                val transfers = current.transfers + (if (line.exists(id => current.lastLine.exists(_ != id))) 1 else 0)
                val candidate = Label(next, line.orElse(current.lastLine), current.time + edge.cost, transfers,
                  current.physical + edge.physicalDemandScore, current.tagExposure + exposure(edge),
                  edge :: current.reversedEdges, current.visited + next, current.signature + f"/$index%08d")
                val tolerance = if (limit == 0.0) 0.0 else 1e-9
                if (candidate.primary + primaryRemaining(next) <= limit + tolerance) enqueue(nextState, candidate)
                else Right(nextState)
              }
            }

          @tailrec
          def visit(state: SearchState): Either[NavigationError, SearchResult] = state.queue.headOption match {
            case None => Right(SearchResult(None, state.budget))
            case Some(current) if !state.labels.getOrElse(current.state, Vector.empty).contains(current) =>
              visit(state.copy(queue = state.queue.tail))
            case Some(current) if current.node == goal => Right(SearchResult(Some(current), state.budget))
            case Some(current) =>
              val expanded = outgoing.getOrElse(current.node, Vector.empty)
                .foldLeft[Either[NavigationError, SearchState]](Right(state.copy(queue = state.queue.tail))) {
                  case (result, (edge, index)) => result.flatMap(expand(_, current, edge, index))
                }
              expanded match {
                case Left(error) => Left(error)
                case Right(next) => visit(next)
              }
          }

          val initial = Label(start, None, 0.0, 0, 0.0, 0.0, Nil, Set(start), "")
          enqueue(SearchState(TreeSet.empty[Label], Map.empty, budget), initial).flatMap(visit)
        }

        for {
          primary <- search(Double.PositiveInfinity, useTags = false, SearchBudget())
          selected <- primary.label match {
            case Some(best) if minimizeTag.isDefined =>
              val limit = preference match {
                case MinimizeTime => best.primary + 60.0
                case MinimizeTransfers => best.primary + 2.0
                case MinimizePhysicalDemands => best.primary * 1.25
              }
              search(limit, useTags = true, primary.budget)
            case _ => Right(primary)
          }
          label <- selected.label.toRight(noRoute)
        } yield {
          val routeEdges = label.reversedEdges.reverse
          NavigationOutputPath(source :: routeEdges.map(_.target), routeEdges)
        }
      }
    } catch {
      case error: IllegalArgumentException => Left(InvalidData(error.getMessage))
    }
  }
}
