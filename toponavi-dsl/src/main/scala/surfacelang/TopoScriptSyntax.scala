package surfacelang

import corelang.{Expr, Type}

sealed trait TopoScript {
}

// Root TopoMap definition
case class RootTopoMap(
  name: String,
  params: List[(String, Type)],
  mapTypes: List[String],
  submaps: List[String],
  constraints: List[Constraint]
) extends TopoScript

// Submap definition
case class SubTopoMap(
  name: String,
  params: MapParameters,
  nodes: List[TopoNode],
  paths: List[AtomicPath]
) extends TopoScript

case class Params(
  parameters: Expr.Record
) extends TopoScript {
  
}

// Parameters for maps
case class MapParameters(
  parameters: Params
) {
  
}

// Rush hour definition
//case class RushHourDef(
//  timePeriod: String,
//  gradient: String,
//  category: String
//) {
//  def toExpr: Expr = Expr.Record(Map(
//    "time_period" -> Expr.StringLit(timePeriod),
//    "gradient" -> Expr.StringLit(gradient),
//    "category" -> Expr.StringLit(category)
//  ))
//}

// Constraint definition
// TODO: Modify
case class Constraint(
  name: String,
  params: List[(String, Type)],
  conditions: List[Expr]
) {
  
}

// Node definition
case class TopoNode(
  name: String,
  parameters: Params
) {
  
}

// Path definition
case class AtomicPath(
  from: String,
  to: String,
  bidirectional: Boolean,
  parameters: Params,
  constraints: List[String] // TODO: List[Constraint]???
) {
  
}
