import cats.effect.IO
import scala.collection.mutable
import enums.*

case class TopoNodeAttribute(
  attribute: Map[String, AttributeValue]
)

case class TopoNode(
  identifier: String,
  attributes: Map[String, AttributeValue]
){
  override def toString: String = identifier
}

case class AtomicPath(
  source: TopoNode,
  target: TopoNode,
  attributes: Map[String, AttributeValue],
  costs: Map[VisitingMode, Double],
  pathType: PathType // Temporary, to-be-modified to "modifiers"
) {
  override def toString: String = s"${source} -> ${target}"
  
}

// Companion object for alternative constructors
object AtomicPath {
  // Alternative constructor without attributes
  def apply(source: TopoNode, target: TopoNode, costs: Map[VisitingMode, Double], pathType: PathType): AtomicPath = {
    new AtomicPath(source, target, Map.empty, costs, pathType)
  }
}

case class Path(
  routeNodes: List[TopoNode],
  routeEdges: List[AtomicPath]
) {
  def totalCost(visitingMode: VisitingMode): Double = {
    routeEdges.map(_.costs(visitingMode)).sum
  }
}