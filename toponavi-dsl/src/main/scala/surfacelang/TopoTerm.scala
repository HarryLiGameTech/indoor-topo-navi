package surfacelang

import corelang.{Environment, Identifier, Type, Value, Term, Expr, Env, TypeOnlyEnvironment, TypeEnvironment}

type Context = Env

case class TopoRootTerm(
  name: String,
  params: List[(String, Type)],
  submaps: Set[TopoMapTerm],
  transportations: Set[TopoTransportationTerm],
  context: Context,
) {
  
}

case class TopoTransportationTerm(
  name: String,
  context: Context,
)

case class TopoMapTerm(
  name: String,
  context: Context,
)

case class ConstraintTerm(
  name: String,
  context: Context,
) {
  def check(ctx: Context = context): Boolean = {
    // Placeholder for constraint checking logic
    true
  }
}