import cats.effect.IO
import scala.collection.mutable
import enums.*

case class TopoNodeAttribute(
  attribute: Map[String, AttributeValue]
)

case class TopoNode(
  id: String,
  regionType: RegionType,
  attributes: Map[String, AttributeValue]
)

case class AtomicPath(
  source: TopoNode,
  target: TopoNode,
  costs: Map[VisitingMode, Int],
  pathType: PathType // Temporary, to-be-modified to "modifiers"
)