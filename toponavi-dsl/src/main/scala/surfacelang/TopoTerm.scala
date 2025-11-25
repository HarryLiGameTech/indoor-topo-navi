package surfacelang

import corelang.{Env, Type}

type Context = Env

case class TopoRoot(
  params: List[(String, Type)],
  submaps: Set[TopoMap],
  transportations: Set[TopoTransportation],
  context: Context,
) {

}

case class TopoTransportation(
  context: Context,
)

case class TopoMap(

  context: Context,
)

case class Constraint(
  context: Context,
)