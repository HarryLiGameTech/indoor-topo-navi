package data

import enums.TPCCRelationship

/** A slim, serialisable representation of a `linear-path` declaration,
  * scoped to a single topo-map.
  *
  * @param graphId the name of the topo-map this segment belongs to
  * @param nodes   ordered list of node identifiers that form the straight corridor
  */
case class LinearSegment(
  graphId: String,
  nodes: List[String]
) extends Serializable {
  /** True if both nodeA and nodeB appear in this segment (in any order). */
  def contains(nodeA: String, nodeB: String): Boolean =
    nodes.contains(nodeA) && nodes.contains(nodeB)

  /** True if nodeA and nodeB are adjacent in this segment (in either direction). */
  def areAdjacent(nodeA: String, nodeB: String): Boolean = {
    val i = nodes.indexOf(nodeA)
    if (i < 0) return false
    val j = nodes.indexOf(nodeB)
    if (j < 0) return false
    math.abs(i - j) == 1
  }
}

/** A slim, serialisable representation of a `directional-arrow` declaration,
  * carrying only the navigation-relevant data needed at runtime.
  *
  * ### Semantics of `->` vs `<-`
  * A `directional-arrow` in the DSL is:
  * {{{
  *   directional-arrow [anchor -> faceNode] target DIRECTION   // invertFacing = false
  *   directional-arrow [anchor <- backNode] target DIRECTION   // invertFacing = true
  * }}}
  *
  * - `->` (`invertFacing = false`): the anchor is **facing toward** `referenceNode`.
  *   The anchor's *front* is `referenceNode`.
  * - `<-` (`invertFacing = true`): the anchor is **facing away from** `referenceNode`
  *   (its back is toward it).  The anchor's *front* is **away** from `referenceNode`.
  *
  * For turn-hint purposes what matters is:
  *   - `fromNode` = anchor (the node where you are about to turn / where the hint applies)
  *   - `toNode`   = target (the node whose direction is being annotated)
  *   - `direction` = the TPCC relationship *as declared* in the script
  *   - `invertFacing` is preserved so consumers can derive the real bearing if needed
  *
  * @param graphId       the topo-map this arrow belongs to
  * @param fromNode      anchor node identifier (the point-of-reference)
  * @param referenceNode the node the anchor is facing toward (`->`) or away from (`<-`)
  * @param invertFacing  true when the DSL used `<-` (anchor faces *away* from referenceNode)
  * @param toNode        the target node being described
  * @param direction     TPCC spatial relationship of toNode relative to the anchor's facing
  */
case class TurnHint(
  graphId: String,
  fromNode: String,
  referenceNode: String,
  invertFacing: Boolean,
  toNode: String,
  direction: TPCCRelationship
) extends Serializable {

  /** Resolves the effective TPCC direction by combining `invertFacing` with the
    * *actual* traversal direction through the previous segment.
    *
    * ### Why traversal direction matters
    * The DSL declares facing relative to `referenceNode`, but the navigator may
    * walk the segment in either direction.  If the walker arrives at the anchor
    * having just come FROM the `referenceNode` side, the anchor's declared facing
    * is actually pointing backward — so the direction must be inverted.
    *
    * Rule (XNOR): `shouldInvert = (invertFacing == refIsAhead)`
    *   - `->` (facing toward ref), ref is ahead  → match → no invert
    *   - `->` (facing toward ref), ref is behind → mismatch → invert
    *   - `<-` (facing away from ref), ref is ahead  → mismatch → invert
    *   - `<-` (facing away from ref), ref is behind → match → no invert
    *
    * @param traversingForward true if the previous segment was walked in the
    *                          direction of increasing index in `segment.nodes`
    * @param segment           the `LinearSegment` the anchor belongs to
    */
  def resolvedDirection(traversingForward: Boolean, segment: LinearSegment): TPCCRelationship = {
    val anchorIdx = segment.nodes.indexOf(fromNode)
    val refIdx    = segment.nodes.indexOf(referenceNode)
    // Is referenceNode ahead of us (in the direction we just walked)?
    val refIsAhead =
      if (traversingForward) refIdx > anchorIdx
      else                   refIdx < anchorIdx
    // Invert iff facing declaration and actual traversal direction disagree
    val shouldInvert = invertFacing == refIsAhead
    if (shouldInvert) TurnHint.invertDirection(direction) else direction
  }
}

object TurnHint {
  /** Returns the opposite TPCC direction (180° rotation). */
  def invertDirection(d: TPCCRelationship): TPCCRelationship = d match {
    case TPCCRelationship.FRONT       => TPCCRelationship.REAR
    case TPCCRelationship.FRONT_RIGHT => TPCCRelationship.REAR_LEFT
    case TPCCRelationship.RIGHT       => TPCCRelationship.LEFT
    case TPCCRelationship.REAR_RIGHT  => TPCCRelationship.FRONT_LEFT
    case TPCCRelationship.REAR        => TPCCRelationship.FRONT
    case TPCCRelationship.REAR_LEFT   => TPCCRelationship.FRONT_RIGHT
    case TPCCRelationship.LEFT        => TPCCRelationship.RIGHT
    case TPCCRelationship.FRONT_LEFT  => TPCCRelationship.REAR_RIGHT
  }
}

