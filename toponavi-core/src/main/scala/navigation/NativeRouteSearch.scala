package navigation

import data.{GlobalNode, NavigationOutputPath, RouteEdge}
import enums.{NavigationError, RoutePlanningPreferences}
import enums.NavigationError.NoRouteFound
import enums.RoutePlanningPreferences.{MinimizeCustomData, MinimizeTransfers}

import scala.math.Ordering.Implicits.infixOrderingOps
import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

private[navigation] case class PrimaryRoute(path: NavigationOutputPath, budget: RouteSearchBudget)

// Primary objectives use one best score per location and previous transport line.
private[navigation] class NativeRouteSearch(
  network: RouteNetwork,
  preference: RoutePlanningPreferences,
  customObjective: Option[CustomRouteObjective],
  transportFirst: Boolean
) {
  private val customCosts = network.edges.map(edge => customObjective.fold(0.0)(_.cost.cost(edge)))
  private val indices = network.indexedEdges.toMap
  private val needsLine = preference == MinimizeTransfers || preference == MinimizeCustomData
  private type StateKey = (String, Option[String])

  private case class Label(node: String, lastLine: Option[String], score: RouteScore, reversed: List[RouteEdge]) {
    def state: StateKey = node -> (if (needsLine) lastLine else None)
    def rank: (Double, Double, Double) = score.rank(preference, customObjective)
    def append(leg: Vector[(RouteEdge, Int)]): Label = leg.foldLeft(this) { case (current, (edge, index)) =>
      Label(RouteNetwork.key(edge.target), edge.transport.map(_.lineIdentifier).orElse(current.lastLine),
        current.score.append(edge, current.lastLine, customCosts(index)), edge :: current.reversed)
    }
  }

  private given Ordering[Label] = Ordering.by(label => (label.rank, label.node, label.state._2.getOrElse("")))

  private case class SearchState(
    queue: TreeSet[Label],
    best: Map[StateKey, Label],
    settled: Set[StateKey],
    budget: RouteSearchBudget,
    walkingCache: Map[String, Vector[Vector[(RouteEdge, Int)]]] = Map.empty
  )

  private def initial(node: String, budget: RouteSearchBudget): Either[NavigationError, SearchState] =
    budget.generate.map { next =>
      val label = Label(node, None, RouteScore(), Nil)
      SearchState(TreeSet(label), Map(label.state -> label), Set.empty, next)
    }

  private def relax(state: SearchState, current: Label, leg: Vector[(RouteEdge, Int)]): Either[NavigationError, SearchState] =
    state.budget.expand.flatMap { budget =>
      val candidate = current.append(leg)
      val next = state.copy(budget = budget)
      if (state.settled(candidate.state) || state.best.get(candidate.state).exists(_.rank <= candidate.rank)) Right(next)
      else budget.generate.map { generated =>
        next.copy(queue = next.queue + candidate, best = next.best.updated(candidate.state, candidate), budget = generated)
      }
    }

  // Walking connections include both endpoint walks and intermediate-node dwell.
  // They are computed once per departure node and reused for every arrival line.
  private def walkingConnections(
    origin: String,
    destinations: Set[String],
    budget: RouteSearchBudget
  ): Either[NavigationError, (Vector[Vector[(RouteEdge, Int)]], RouteSearchBudget)] = {
    @tailrec
    def visit(state: SearchState): Either[NavigationError, SearchState] = state.queue.headOption match {
      case None => Right(state)
      case Some(current) if state.settled(current.state) || state.best(current.state).rank != current.rank =>
        visit(state.copy(queue = state.queue.tail))
      case Some(current) =>
        val next = state.copy(queue = state.queue.tail, settled = state.settled + current.state)
        val expanded = network.walking.getOrElse(current.node, Vector.empty)
          .foldLeft[Either[NavigationError, SearchState]](Right(next)) { case (result, edge) =>
            result.flatMap(relax(_, current, Vector(edge)))
          }
        expanded match {
          case Left(error) => Left(error)
          case Right(updated) => visit(updated)
        }
    }

    initial(origin, budget).flatMap(visit).map { state =>
      val legs = state.best.valuesIterator.filter(label => label.node != origin && destinations(label.node))
        .toVector.sortBy(_.node).map(label => label.reversed.reverse.map(edge => edge -> indices(edge)).toVector)
      legs -> state.budget
    }
  }

  def navigate(source: GlobalNode, destination: GlobalNode, budget: RouteSearchBudget): Either[NavigationError, PrimaryRoute] = {
    val start = RouteNetwork.key(source)
    val goal = RouteNetwork.key(destination)
    val hierarchyNodes = network.stations + start + goal

    def connections(state: SearchState, current: Label): Either[NavigationError, (SearchState, Vector[Vector[(RouteEdge, Int)]])] =
      if (!transportFirst) Right(state -> network.outgoing.getOrElse(current.node, Vector.empty).map(Vector(_)))
      else {
        val cached = state.walkingCache.get(current.node) match {
          case Some(legs) => Right(state -> legs)
          case None => walkingConnections(current.node, hierarchyNodes, state.budget).map { case (legs, nextBudget) =>
            state.copy(budget = nextBudget, walkingCache = state.walkingCache.updated(current.node, legs)) -> legs
          }
        }
        cached.map { case (next, walking) =>
          next -> (walking ++ network.rides.getOrElse(current.node, Vector.empty).map(Vector(_)))
        }
      }

    @tailrec
    def visit(state: SearchState): Either[NavigationError, PrimaryRoute] = state.queue.headOption match {
      case None => Left(NoRouteFound(s"No route from $start to $goal"))
      case Some(current) if state.settled(current.state) || state.best(current.state).rank != current.rank =>
        visit(state.copy(queue = state.queue.tail))
      case Some(current) if current.node == goal =>
        Right(PrimaryRoute(loopFree(source, current.reversed.reverse), state.budget))
      case Some(current) =>
        val expanded = connections(state, current).flatMap { case (prepared, legs) =>
          val next = prepared.copy(queue = prepared.queue.tail, settled = prepared.settled + current.state)
          legs.foldLeft[Either[NavigationError, SearchState]](Right(next)) { (result, leg) =>
            result.flatMap(relax(_, current, leg))
          }
        }
        expanded match {
          case Left(error) => Left(error)
          case Right(next) => visit(next)
        }
    }

    initial(start, budget).flatMap(visit)
  }

  // Zero-cost cycles can tie in the expanded state graph; remove physical-node
  // repetitions without increasing any additive cost or transport-line changes.
  private def loopFree(source: GlobalNode, edges: List[RouteEdge]): NavigationOutputPath = {
    val (nodes, selected) = edges.foldLeft((Vector(source), Vector.empty[RouteEdge])) {
      case ((nodes, selected), edge) =>
        val previous = nodes.indexWhere(node => RouteNetwork.key(node) == RouteNetwork.key(edge.target))
        if (previous >= 0) (nodes.take(previous + 1), selected.take(previous))
        else (nodes :+ edge.target, selected :+ edge)
    }
    NavigationOutputPath(nodes.toList, selected.toList)
  }
}
