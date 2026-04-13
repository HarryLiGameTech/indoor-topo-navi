package reasoner

import data.TopoNode
import enums.TPCCRelationship

case class LinearPath(
  nodes: List[TopoNode]
)

case class DirectionArrow(
  anchor: TopoNode,
  reference: TopoNode,
  invertFacing: Boolean, // true when '-' prefix was used
  target: TopoNode,
  direction: TPCCRelationship
) {
  def back: TopoNode = if invertFacing then reference else anchor
  def front: TopoNode = if invertFacing then anchor else reference
}

case class SpatialMetadata(
  lines: List[LinearPath],
  arrows: List[DirectionArrow],
  beaconNodes: Set[TopoNode],   // from _isBeacon transports
  excludedNodes: Set[TopoNode], // from _excluded_from_coord
  sensitivity: Float,
  startNode: TopoNode,
  startMap: String
)

