package navigation

import data.{AtomicPath, RouteTraversalMetadata, TopoNode}
import enums.AttributeValue

case class TraversalTagPolicy(banTags: Set[String] = Set.empty) {
  def conflictingTags(attributes: Map[String, AttributeValue]): Set[String] =
    RouteTraversalMetadata.tagsFrom(attributes).intersect(banTags)

  def conflictingTags(node: TopoNode): Set[String] =
    conflictingTags(node.attributes)

  def allows(edge: AtomicPath): Boolean =
    conflictingTags(edge.attributes).isEmpty

  def allowsEntry(node: TopoNode): Boolean =
    conflictingTags(node).isEmpty
}

object TraversalTagPolicy {
  val AllowAll: TraversalTagPolicy = TraversalTagPolicy()
}
