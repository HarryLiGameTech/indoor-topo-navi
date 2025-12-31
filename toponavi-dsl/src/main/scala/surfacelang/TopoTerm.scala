package surfacelang

import corelang.Value.RecordVal
import corelang.{Env, Type, Value}

type Context = Env

case class TopoRootValue(
  name: String,
  params: List[(String, Type)],
  submaps: Set[TopoMapValue],
  transportations: Set[TopoTransportationValue],
  context: Context,
) {
  
}

case class TopoTransportationValue(
  name: String,
  context: Context,
)

case class TopoMapValue(
  name: String,
  nodes: Set[TopoNodeValue],
  paths: Set[AtomicPathValue],
  context: Context,
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
  context: Context,
) {
  
}