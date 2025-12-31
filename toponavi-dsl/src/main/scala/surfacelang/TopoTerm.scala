package surfacelang

import corelang.Value.RecordVal
import corelang.{Env, Environment, Expr, Identifier, Term, Type, TypeEnvironment, TypeOnlyEnvironment, Value}

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