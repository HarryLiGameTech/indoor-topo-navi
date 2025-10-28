import cats.effect.IO
import scala.collection.mutable
import enums.*

case class TopoNodeAttribute(
  attribute: Map[String, AttributeValue]
)

case class TopoNode(
  identifier: String,
  attributes: Map[String, AttributeValue]
)

case class AtomicPath(
  source: TopoNode,
  target: TopoNode,
  costs: Map[VisitingMode, Double],
  pathType: PathType // Temporary, to-be-modified to "modifiers"
)

case class Path(
  routeNodes: List[TopoNode]
)