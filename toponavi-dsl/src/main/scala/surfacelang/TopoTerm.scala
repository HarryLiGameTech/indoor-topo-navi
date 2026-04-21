package surfacelang

import corelang.Value.RecordVal
import corelang.{Env, Type, Value}
import enums.TPCCRelationship

type Context = Env

case class RootValue(
  name: String,
  params: List[(String, Type)],
  context: Context,
) {
  
}

case class TransportValue(
  name: String,
  surfaceType: String,
  stations: List[(TopoNodeRefValue, Value.RecordVal)],
  data: Value.RecordVal,
  context: Context,
)

case class TopoMapValue(
  name: String,
  nodes: Set[TopoNodeValue],
  paths: Set[AtomicPathValue],
  lines: Set[LinearPathValue],
  arrows: Set[DirectionalArrowValue],
  context: Context
)

case class TopoNodeValue(
  name: String,
  context: Context,
  data: Value.RecordVal
) {
  
}

case class AtomicPathValue(
  from: TopoNodeValue,
  to: TopoNodeValue,
  bidirectional: Boolean,
  data: Value.RecordVal,
  context: Context
) {
  
}

case class LinearPathValue(
  nodes: List[TopoNodeValue],
  context: Context
)

case class DirectionalArrowValue(
  anchor: TopoNodeValue,
  reference: TopoNodeValue,
  invertFacing: Boolean,
  target: TopoNodeValue,
  direction: TPCCRelationship, // e.g., "LEFT", "RIGHT", "FORWARD", "BACKWARD"
  context: Context
)

case class TopoNodeRefValue(
  fromMapName: String,
  nodeName: String
)

case class TopoMapRefValue(name: String)
