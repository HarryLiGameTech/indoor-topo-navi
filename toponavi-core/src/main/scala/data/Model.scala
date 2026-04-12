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
  attributes: Map[String, AttributeValue] = Map.empty,
  estimatedCoord: Option[TpccCoord] = None
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

  def physicalDemandScore: Double = routeEdges.map { edge =>
    if (edge.source.ownerLine == edge.target.ownerLine)
      edge.source.ownerLine match {
        case _: ElevatorBank => 0.1 * edge.source.ownerLine.distanceBetweenStations(edge.source.ownerGraph, edge.target.ownerGraph)
        case _: StairCase    => 10.0 * edge.source.ownerLine.distanceBetweenStations(edge.source.ownerGraph, edge.target.ownerGraph)
        case _               => 1.0 * edge.cost
      }
    else
      1.0 * edge.cost // walking transfer between different transport lines on the same floor
  }.sum
}

case class NavigationOutputPath(
  routeNodes: List[GlobalNode],
  routeEdges: List[RouteEdge]
) extends NavigatablePath {

  def totalCost: Double = routeEdges.map(_.cost).sum

  def prettyPrint: String = {
    val legs = buildLegs
    if (legs.isEmpty) return "No movement required."
    val totalSec = totalCost
    val totalMin = (totalSec / 60).toInt
    val header = s"${legs.size}-step route · ${totalSec.toInt}s (~$totalMin min)"
    val body = legs.zipWithIndex.map { case (leg, i) => formatLeg(leg, i + 1) }.mkString("\n\n")
    s"$header\n\n$body"
  }

  def toStructuredSteps: java.util.List[java.util.Map[String, Object]] = {
    import scala.jdk.CollectionConverters.*
    buildLegs.zipWithIndex.map { case (leg, i) => legToMap(leg, i + 1) }.asJava
  }

  private sealed trait Leg
  private case class WalkLeg(floor: String, edges: List[RouteEdge]) extends Leg
  private case class TransportLeg(edge: RouteEdge) extends Leg

  private def isInternalNode(id: String): Boolean =
    id.startsWith("n_") && id.length == 34 && id.drop(2).forall(c => "0123456789abcdef".contains(c))

  private def namedNodesOf(edges: List[RouteEdge]): List[String] =
    (edges.head.source :: edges.map(_.target))
      .map(_.localNode.identifier)
      .filterNot(isInternalNode)

  private def formatLeg(leg: Leg, stepNum: Int): String = leg match {
    case WalkLeg(floor, edges) =>
      val named = namedNodesOf(edges)
      val nodeStr = named match {
        case Nil => s"${edges.head.source.localNode.identifier} → ${edges.last.target.localNode.identifier}"
        case ns if ns.size <= 6 => ns.mkString(" → ")
        case ns => ns.take(2).mkString(" → ") + s" ··· (+${ns.size - 3} more) ··· " + ns.last
      }
      s"Step $stepNum | Walk | $floor | ${edges.map(_.cost).sum.toInt}s\n  $nodeStr"
    case TransportLeg(edge) =>
      s"Step $stepNum | ${edge.movementDescription} | ${edge.cost.toInt}s"
  }

  private def legToMap(leg: Leg, stepNum: Int): java.util.Map[String, Object] = {
    import scala.jdk.CollectionConverters.*
    val m = new java.util.LinkedHashMap[String, Object]()
    leg match {
      case WalkLeg(floor, edges) =>
        val named = namedNodesOf(edges)
        m.put("step", Int.box(stepNum))
        m.put("type", "Walk")
        m.put("graph", floor)
        m.put("from", s"${named.headOption.getOrElse(edges.head.source.localNode.identifier)} (${edges.head.source.localNode.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
        m.put("to",   s"${named.lastOption.getOrElse(edges.last.target.localNode.identifier)} (${edges.last.source.localNode.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
        m.put("namedWaypoints", (edges.head.source :: edges.map(_.target))
          .map(_.localNode)
          .filterNot(n => isInternalNode(n.identifier))
          .map(n => s"${n.identifier} (${n.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
          .asJava) // TODO: Include the description of each nodes, refer to prev 2 lines
        m.put("costSeconds", Double.box(edges.map(_.cost).sum))
      case TransportLeg(edge) =>
        m.put("step", Int.box(stepNum))
        m.put("type", edge.category.toString)
        m.put("description", edge.movementDescription)
        m.put("fromGraph", edge.source.owningGraph.identifier)
        m.put("toGraph",   edge.target.owningGraph.identifier)
        m.put("costSeconds", Double.box(edge.cost))
    }
    m
  }

  private def buildLegs: List[Leg] =
    routeEdges.foldLeft(List.empty[Leg]) { (acc, edge) =>
      edge.category match {
        case RouteEdgeCategory.Walking =>
          acc match {
            case WalkLeg(floor, es) :: rest if floor == edge.source.owningGraph.identifier =>
              WalkLeg(floor, es :+ edge) :: rest
            case _ =>
              WalkLeg(edge.source.owningGraph.identifier, List(edge)) :: acc
          }
        case _ => // Transport, Portal
          TransportLeg(edge) :: acc
      }
    }.reverse
}


case class TpccCoord(
  x: Int,
  y: Int
)