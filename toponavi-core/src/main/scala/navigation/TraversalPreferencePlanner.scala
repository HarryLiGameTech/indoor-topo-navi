package navigation

import data.*
import enums.{NavigationError, RoutePlanningPreferences, VisitingMode}
import enums.NavigationError.{InvalidData, NoRouteFound}
import enums.RoutePlanningPreferences.{MinimizePhysicalDemands, MinimizeTime, MinimizeTransfers}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

// Native primary selection followed, when necessary, by constrained soft-tag avoidance.
class TraversalPreferencePlanner(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  maxLabels: Int = 50000,
  maxExpansions: Int = 200000,
  transportFirst: Boolean = false
) {
  private def key(node: GlobalNode): String = s"${node.owningGraph.identifier}::${node.localNode.identifier}"

  def navigate(
    source: GlobalNode,
    destination: GlobalNode,
    visitingMode: VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean,
    minimizeTag: Option[String],
    customObjective: Option[CustomRouteObjective] = None
  ): Either[NavigationError, NavigationOutputPath] = {
    try {
      val start = key(source)
      val goal = key(destination)
      require((preference == RoutePlanningPreferences.MinimizeCustomData) == customObjective.isDefined,
        "MinimizeCustomData requires a custom objective; other preferences cannot provide one")
      require(customObjective.isEmpty || minimizeTag.isEmpty, "Custom objectives cannot be combined with minimizeTag")
      val network = RouteNetwork.build(graphs, transportGraph, source, destination, visitingMode, tagPolicy, allowUncertainAccess)
      val edges = network.edges
      val outgoing = edges.zipWithIndex.groupMap(pair => key(pair._1.source))(identity)
      val reverse = edges.groupBy(edge => key(edge.target))

      val exposureCost = minimizeTag.map(CustomRouteCost.TagExposure.apply)
      def exposure(edge: RouteEdge): Double = exposureCost.fold(0.0)(_.cost(edge))

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

      val initialBudget = RouteSearchBudget(maxLabels, maxExpansions)
      new NativeRouteSearch(network, preference, customObjective, transportFirst)
        .navigate(source, destination, initialBudget).flatMap { primary =>
        // Zero exposure is already optimal; the native primary search supplies
        // the remaining tie-breaks without enumerating alternative histories.
        if (minimizeTag.isEmpty || primary.path.routeEdges.map(exposure).sum == 0.0) Right(primary.path)
        else {
          val timeRemaining = lowerBounds(_.cost)
          val noRoute = NoRouteFound(s"No route from $start to $goal")
          val primaryRemaining = preference match {
            case MinimizeTime => timeRemaining
            case MinimizePhysicalDemands => lowerBounds(_.physicalDemandScore)
            case MinimizeTransfers => timeRemaining.keys.map(_ -> 0.0).toMap
            case RoutePlanningPreferences.MinimizeCustomData => throw new IllegalArgumentException("Custom objectives use native search")
          }
          val exposureRemaining = lowerBounds(exposure)

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
              case RoutePlanningPreferences.MinimizeCustomData => throw new IllegalArgumentException("Custom objectives use native search")
            }
            def state: (String, Option[String]) = node -> (if (preference == MinimizeTransfers) lastLine else None)
          }

          case class SearchState(
            queue: TreeSet[Label],
            labels: Map[(String, Option[String]), Vector[Label]],
            budget: RouteSearchBudget
          )
          case class SearchResult(label: Option[Label], budget: RouteSearchBudget)

          def search(limit: Double, budget: RouteSearchBudget): Either[NavigationError, SearchResult] = {
            def priority(label: Label): (Double, Double, Double, String) = {
              val primary = label.primary + primaryRemaining.getOrElse(label.node, Double.PositiveInfinity)
              val time = label.time + timeRemaining.getOrElse(label.node, Double.PositiveInfinity)
              (label.tagExposure + exposureRemaining.getOrElse(label.node, Double.PositiveInfinity), primary, time, label.signature)
            }
            // The path signature keeps equally scored routes distinct in the ordered set.
            given Ordering[Label] = Ordering.by[Label, (Double, Double, Double, String)](priority)

            def dominates(a: Label, b: Label): Boolean =
              a.primary <= b.primary && a.time <= b.time && a.tagExposure <= b.tagExposure &&
                a.visited.subsetOf(b.visited) &&
                (a.primary < b.primary || a.time < b.time || a.tagExposure < b.tagExposure || a.signature <= b.signature)

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

          val limit = preference match {
            case MinimizeTime => primary.path.totalCost + 60.0
            case MinimizeTransfers => primary.path.transferCount.toDouble + 2.0
            case MinimizePhysicalDemands => primary.path.physicalDemandScore * 1.25
            case RoutePlanningPreferences.MinimizeCustomData => throw new IllegalArgumentException("Custom objectives use native search")
          }
          for {
            selected <- search(limit, primary.budget)
            label <- selected.label.toRight(noRoute)
          } yield {
            val routeEdges = label.reversedEdges.reverse
            NavigationOutputPath(source :: routeEdges.map(_.target), routeEdges)
          }
        }
      }
    } catch {
      case error: IllegalArgumentException => Left(InvalidData(error.getMessage))
    }
  }
}
