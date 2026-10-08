package navigation

import data.*
import enums.{NavigationError, RouteEdgeCategory, VisitingMode}
import enums.NavigationError.PreferenceSearchLimitExceeded

private[navigation] case class RouteSearchBudget(
  maxLabels: Int,
  maxExpansions: Int,
  generated: Int = 0,
  expanded: Int = 0
) {
  private def exceeded: NavigationError = PreferenceSearchLimitExceeded(
    s"Traversal preference search exceeded $maxLabels labels or $maxExpansions edge expansions")

  def generate: Either[NavigationError, RouteSearchBudget] =
    Either.cond(generated < maxLabels, copy(generated = generated + 1), exceeded)

  def expand: Either[NavigationError, RouteSearchBudget] =
    Either.cond(expanded < maxExpansions, copy(expanded = expanded + 1), exceeded)
}

private[navigation] case class RouteNetwork(
  nodes: Map[String, GlobalNode],
  edges: Vector[RouteEdge]
) {
  val indexedEdges: Vector[(RouteEdge, Int)] = edges.zipWithIndex
  val outgoing: Map[String, Vector[(RouteEdge, Int)]] = indexedEdges.groupMap(pair => RouteNetwork.key(pair._1.source))(identity)
  val walking: Map[String, Vector[(RouteEdge, Int)]] = indexedEdges.filter(_._1.transport.isEmpty)
    .groupMap(pair => RouteNetwork.key(pair._1.source))(identity)
  val rides: Map[String, Vector[(RouteEdge, Int)]] = indexedEdges.filter(_._1.transport.isDefined)
    .groupMap(pair => RouteNetwork.key(pair._1.source))(identity)
  val stations: Set[String] = edges.filter(_.transport.isDefined)
    .flatMap(edge => Vector(RouteNetwork.key(edge.source), RouteNetwork.key(edge.target))).toSet
}

private[navigation] object RouteNetwork {
  def key(node: GlobalNode): String = s"${node.owningGraph.identifier}::${node.localNode.identifier}"

  def build(
    graphs: Map[String, NavigationGraph],
    transportGraph: TransportGraph,
    source: GlobalNode,
    destination: GlobalNode,
    visitingMode: VisitingMode,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): RouteNetwork = {
    val nodes = graphs.values.flatMap(graph => graph.nodes.map { node =>
      val global = GlobalNode(graph, node)
      key(global) -> global
    }).toMap
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
      val dwell = if (key(edge.target) == key(destination) || key(edge.target) == key(source)) 0.0
        else edge.target.localNode.minimumDwellSeconds
      require(edge.cost.isFinite && edge.cost >= 0.0, "Traversal costs must be finite and non-negative")
      require((edge.cost + dwell).isFinite, "Traversal costs including dwell must be finite")
      edge.copy(cost = edge.cost + dwell, nodeDwellSeconds = dwell)
    }.sortBy { edge =>
      (key(edge.source), key(edge.target), edge.transport.map(_.lineIdentifier).getOrElse(""),
        edge.cost, edge.attributes.toVector.sortBy(_._1).mkString,
        edge.uncertainAccess.toList.flatMap(_.uncertaintyReasons)
          .map(reason => (reason.conditionType, reason.reason.getOrElse(""), reason.conditionSatisfied)).sorted.mkString)
    }
    RouteNetwork(nodes, edges)
  }
}
