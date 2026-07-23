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
  movementDescription: String,
  attributes: Map[String, AttributeValue] = Map.empty
) {
  def traversalMetadata: RouteTraversalMetadata =
    RouteTraversalMetadata.from(attributes)
}

case class RouteTraversalMetadata(
  tags: Set[String] = Set.empty,
  requiredActions: List[String] = List.empty
)

object RouteTraversalMetadata {
  def from(attributes: Map[String, AttributeValue]): RouteTraversalMetadata =
    RouteTraversalMetadata(
      tags = stringList(attributes, "tags").toSet,
      requiredActions = stringList(attributes, "requiredActions")
    )

  private def stringList(attributes: Map[String, AttributeValue], key: String): List[String] =
    attributes.get(key) match {
      case None => List.empty
      case Some(AttributeValue.ListValue(values)) =>
        values.map {
          case AttributeValue.StringValue(value) => value
          case other => throw IllegalArgumentException(
            s"RouteEdge attribute '$key' must contain only strings, found: $other")
        }
      case Some(other) => throw IllegalArgumentException(
        s"RouteEdge attribute '$key' must be a list of strings, found: $other")
    }
}

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
      movementDescription = s"Move from ${atomicPath.source.identifier} to ${atomicPath.target.identifier} via ${atomicPath.pathType}",
      attributes = atomicPath.attributes
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
  routeEdges: List[RouteEdge],
  linearSegments: List[LinearSegment] = List.empty,
  turnHints: List[TurnHint] = List.empty
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
    buildLegs.zipWithIndex.map { case (leg, i) => legToStructuredMap(leg, i + 1) }.asJava
  }

  private sealed trait Leg
  private case class WalkLeg(floor: String, edges: List[RouteEdge]) extends Leg
  /** A sub-segment of a WalkLeg that lies entirely within one linear-path corridor.
    * @param turnHint optionally describes the turn taken when leaving the *previous* segment
    *                 to arrive at this one (direction already resolved for the actual traversal,
    *                 paired with the anchor node identifier).
    */
  private case class StraightLineLeg(floor: String, edges: List[RouteEdge], turnHint: Option[(TPCCRelationship, String)] = None) extends Leg
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
    case StraightLineLeg(floor, edges, hint) =>
      val named = namedNodesOf(edges)
      val nodeStr = named match {
        case Nil => s"${edges.head.source.localNode.identifier} → ${edges.last.target.localNode.identifier}"
        case ns if ns.size <= 6 => ns.mkString(" → ")
        case ns => ns.take(2).mkString(" → ") + s" ··· (+${ns.size - 3} more) ··· " + ns.last
      }
      val turnPrefix = hint.map { case (dir, _) => s"[Turn $dir] " }.getOrElse("")
      s"Step $stepNum | ${turnPrefix}Walk (straight) | $floor | ${edges.map(_.cost).sum.toInt}s\n  $nodeStr"
    case TransportLeg(edge) =>
      s"Step $stepNum | ${edge.movementDescription} | ${edge.cost.toInt}s"
  }

  private def legToStructuredMap(leg: Leg, stepNum: Int): java.util.Map[String, Object] = {
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
          .asJava)
        m.put("costSeconds", Double.box(edges.map(_.cost).sum))
        putTraversalMetadata(m, edges)
      case StraightLineLeg(floor, edges, hint) =>
        val named = namedNodesOf(edges)
        m.put("step", Int.box(stepNum))
        m.put("type", "WalkStraight")
        m.put("graph", floor)
        m.put("from", s"${named.headOption.getOrElse(edges.head.source.localNode.identifier)} (${edges.head.source.localNode.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
        m.put("to",   s"${named.lastOption.getOrElse(edges.last.target.localNode.identifier)} (${edges.last.source.localNode.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
        m.put("namedWaypoints", (edges.head.source :: edges.map(_.target))
          .map(_.localNode)
          .filterNot(n => isInternalNode(n.identifier))
          .map(n => s"${n.identifier} (${n.attributes.getOrElse("description", "{NO-DESCRIPTION-FOUND}").toString})")
          .asJava)
        hint.foreach { case (dir, atNode) =>
          m.put("turnDirection", dir.toString)
          m.put("turnAtNode", atNode)
        }
        m.put("costSeconds", Double.box(edges.map(_.cost).sum))
        putTraversalMetadata(m, edges)
      case TransportLeg(edge) =>
        m.put("step", Int.box(stepNum))
        m.put("type", edge.category.toString)
        m.put("description", edge.movementDescription)
        m.put("fromGraph", edge.source.owningGraph.identifier)
        m.put("toGraph",   edge.target.owningGraph.identifier)
        m.put("costSeconds", Double.box(edge.cost))
        putTraversalMetadata(m, List(edge))
    }
    m
  }

  private def putTraversalMetadata(
    target: java.util.Map[String, Object],
    edges: List[RouteEdge]
  ): Unit = {
    import scala.jdk.CollectionConverters.*
    val metadata = edges.map(_.traversalMetadata)
    val tags = metadata.flatMap(_.tags).distinct.sorted
    val requiredActions = metadata.flatMap(_.requiredActions)
    target.put("tags", tags.asJava)
    target.put("requiredActions", requiredActions.asJava)
  }

  private def buildLegs: List[Leg] = {
    // ── Phase 1: group consecutive Walking edges by floor into coarse WalkLegs ──
    val coarseLegs: List[Leg] = routeEdges.foldLeft(List.empty[Leg]) { (acc, edge) =>
      edge.category match {
        case RouteEdgeCategory.Walking =>
          acc match {
            case WalkLeg(floor, es) :: rest if floor == edge.source.owningGraph.identifier =>
              WalkLeg(floor, es :+ edge) :: rest
            case _ =>
              WalkLeg(edge.source.owningGraph.identifier, List(edge)) :: acc
          }
        case _ =>
          TransportLeg(edge) :: acc
      }
    }.reverse

    // If no spatial annotations are present, return coarse legs as-is
    if (linearSegments.isEmpty) return coarseLegs

    // ── Phase 2: split each WalkLeg into StraightLineLegs ──
    // Build lookup: graphId -> list of LinearSegments for that graph
    val linearSegmentsByGraph: Map[String, List[LinearSegment]] =
      linearSegments.groupBy(_.graphId)

    // Build lookup: (graphId, fromNode, toNode) -> TurnHint
    // Key uses sorted pair to allow bidirectional lookup, but we also index by (fromNode, toNode) exactly
    val hintsByAnchorTarget: Map[(String, String, String), TurnHint] =
      turnHints.map(h => (h.graphId, h.fromNode, h.toNode) -> h).toMap

    coarseLegs.flatMap {
      case TransportLeg(e) => List(TransportLeg(e))

      case WalkLeg(floor, edges) =>
        val linearSegments = linearSegmentsByGraph.getOrElse(floor, List.empty)

        if (linearSegments.isEmpty) {
          // No linear-path info for this floor — keep as a plain WalkLeg
          List(WalkLeg(floor, edges))
        } else {
          // Find which LinearSegment an edge belongs to (by matching source+target)
          def segmentOf(edge: RouteEdge): Option[LinearSegment] =
            linearSegments.find(_.contains(
              edge.source.localNode.identifier,
              edge.target.localNode.identifier
            ))

          // Walk through edges, accumulating StraightLineLegs.
          // A new StraightLineLeg begins when:
          //   (a) the current edge has no matching segment, or
          //   (b) it belongs to a *different* segment than the previous edge.
          case class Acc(
            currentEdges: List[RouteEdge],
            currentSeg: Option[LinearSegment],
            pendingHint: Option[(TPCCRelationship, String)], // resolved (direction, atNode) for the *next* StraightLineLeg
            completed: List[Leg]
          )

          val result = edges.foldLeft(Acc(List.empty, None, None, List.empty)) { (acc, edge) =>
            val thisSeg = segmentOf(edge)

            val sameSegment = (acc.currentSeg, thisSeg) match {
              case (Some(a), Some(b)) => a.asInstanceOf[AnyRef] eq b.asInstanceOf[AnyRef]  // reference equality — same object
              case (None, None)       => true     // both unclassified: keep grouping
              case _                  => false
            }

            if (sameSegment && acc.currentEdges.nonEmpty) {
              // Continue in the same segment
              acc.copy(currentEdges = acc.currentEdges :+ edge)
            } else {
              // Segment boundary — close the current group and open a new one
              val closedLeg: Option[Leg] = if (acc.currentEdges.isEmpty) None else {
                Some(acc.currentSeg match {
                  case Some(_) => StraightLineLeg(floor, acc.currentEdges, acc.pendingHint)
                  case None    => WalkLeg(floor, acc.currentEdges)
                })
              }

              // Look up the turn hint at the boundary node (last node of closed group)
              val boundaryNode = acc.currentEdges.lastOption
                .map(_.target.localNode.identifier)
                .getOrElse(edge.source.localNode.identifier)
              val nextNodeId = edge.target.localNode.identifier
              val hint: Option[(TPCCRelationship, String)] =
                hintsByAnchorTarget.get((floor, boundaryNode, nextNodeId)).flatMap { h =>
                  // Determine traversal direction through the previous segment
                  acc.currentSeg.map { seg =>
                    val firstNodeId = acc.currentEdges.head.source.localNode.identifier
                    val lastNodeId  = boundaryNode
                    val firstIdx    = seg.nodes.indexOf(firstNodeId)
                    val lastIdx     = seg.nodes.indexOf(lastNodeId)
                    // traversingForward = we walked in the direction of increasing index
                    val traversingForward = lastIdx >= firstIdx
                    (h.resolvedDirection(traversingForward, seg), h.fromNode)
                  }
                }

              acc.copy(
                currentEdges  = List(edge),
                currentSeg    = thisSeg,
                pendingHint   = hint,
                completed     = acc.completed ++ closedLeg.toList
              )
            }
          }

          // Flush the last open group
          val finalLeg: Option[Leg] = if (result.currentEdges.isEmpty) None else {
            Some(result.currentSeg match {
              case Some(_) => StraightLineLeg(floor, result.currentEdges, result.pendingHint)
              case None    => WalkLeg(floor, result.currentEdges)
            })
          }

          result.completed ++ finalLeg.toList
        }

      case other => List(other) // should not occur but kept for exhaustiveness
    }
  }
}


case class TpccCoord(
  x: Int,
  y: Int
)
