package data

import cats.effect.IO
import enums.*
import enums.VisitingMode.Normal

import scala.collection.mutable

case class TopoNodeAttribute(
  attribute: Map[String, AttributeValue]
)

case class TopoNode(
  identifier: String,
  attributes: Map[String, AttributeValue] = Map.empty
){
  override def toString: String = identifier
}

case class GlobalNode(
  owningGraph: NavigationGraph,
  localNode: TopoNode
) {
  override def toString: String = s"(${owningGraph.identifier}: ${localNode.identifier})"
}

object GlobalNode {
  def fromTopoNode(graph: NavigationGraph, node: TopoNode): GlobalNode = {
    GlobalNode(graph, node)
  }
}

case class AtomicPath(
  source: TopoNode,
  target: TopoNode,
  attributes: Map[String, AttributeValue],
  costs: Map[VisitingMode, Double],
  pathType: PathType // TODO: modify to "modifiers"
) {
  override def toString: String = s"${source} -> ${target}"
}

case class RouteEdge(
  source: GlobalNode,
  target: GlobalNode,
  cost: Double,
  category: RouteEdgeCategory,
  movementDescription: String
)

object RouteEdge {
  def fromAtomicPath(
    owningGraph: NavigationGraph,
    atomicPath: AtomicPath,
    visitingMode: VisitingMode
  ): RouteEdge = {
    val sourceGlobalNode = GlobalNode.fromTopoNode(owningGraph, atomicPath.source)
    val targetGlobalNode = GlobalNode.fromTopoNode(owningGraph, atomicPath.target)
    RouteEdge(
      source = sourceGlobalNode,
      target = targetGlobalNode,
      cost = atomicPath.costs(visitingMode),
      category = RouteEdgeCategory.Walking,
      movementDescription = s"Move from ${atomicPath.source.identifier} to ${atomicPath.target.identifier} via ${atomicPath.pathType}"
    )
  }
}

// Companion object for alternative constructors
object AtomicPath {
  // Alternative constructor without attributes
  def apply(source: TopoNode, target: TopoNode, costs: Map[VisitingMode, Double], pathType: PathType): AtomicPath = {
    new AtomicPath(source, target, Map.empty, costs, pathType)
  }
}

trait NavigatablePath {
  def routeNodes: List[_]
  def routeEdges: List[_]
  def totalCost: Double
}

case class IntraMapPath(
  routeNodes: List[TopoNode],
  routeEdges: List[AtomicPath]
) extends NavigatablePath {

  def totalCost(visitingMode: VisitingMode): Double = {
    routeEdges.map(_.costs(visitingMode)).sum
  }

  override def totalCost: Double = totalCost(Normal)
}

case class TransportationPath(
  routeNodes: List[StationNode],
  routeEdges: List[TransportEdge]
) extends NavigatablePath {
  def totalCost: Double = {
    routeEdges.map(_.cost).sum
  }
}

case class NavigationOutputPath(
  routeNodes: List[GlobalNode],
  routeEdges: List[RouteEdge]
) extends NavigatablePath {

  def totalCost: Double = {
    routeEdges.map(_.cost).sum
  }

  def prettyPrint: String = {
    val nodeStr = routeNodes.map(n => s"${n.owningGraph.identifier}:${n.localNode.identifier}").mkString(" -> ")
    val edgeStr = routeEdges.map(e => s"[${e.category}, cost=${e.cost}]").mkString(", ")
    s"Nodes: $nodeStr\nEdges: $edgeStr\nTotal Cost: $totalCost"
  }
}