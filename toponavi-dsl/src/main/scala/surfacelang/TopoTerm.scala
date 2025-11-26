package surfacelang

import corelang.{Environment, Identifier, Type, Value, Term, Expr, Env, TypeOnlyEnvironment, TypeEnvironment}

type Context = Env

case class TopoRoot(
  name: String,
  params: List[(String, Type)],
  submaps: Set[TopoMap],
  transportations: Set[TopoTransportation],
  context: Context,
) {
  def evalaute(ctx: Context = context): Value = {
    corelang.Interpreter.eval(toTerm)(using env)
  }
}

case class TopoTransportation(
  name: String,
  context: Context,
)

case class TopoMap(
  name: String,
  context: Context,
)

case class Constraint(
  name: String,
  context: Context,
) {
  def check(ctx: Context = context): Boolean = {
    // Placeholder for constraint checking logic
    true
  }
}