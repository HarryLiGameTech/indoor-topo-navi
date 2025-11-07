package surfacelang

import corelang.{Expr, Type}

sealed trait TopoScript {
  def toExpr: Expr
}

// Root TopoMap definition
case class RootTopoMap(
  name: String,
  params: List[(String, Type)],
  mapTypes: List[String],
  submaps: List[String],
  constraints: List[Constraint]
) extends TopoScript {
  def toExpr: Expr = Expr.Record(Map(
    "name" -> Expr.StringLit(name),
    "params" -> Expr.ListLit(None, params.map { case (name, tpe) =>
      Expr.Record(Map(
        "name" -> Expr.StringLit(name),
        "type" -> Expr.StringLit(tpe.toString)
      ))
    }),
    "map_types" -> Expr.ListLit(None, mapTypes.map(Expr.StringLit(_))),
    "submaps" -> Expr.ListLit(None, submaps.map(Expr.StringLit(_))),
    "constraints" -> Expr.ListLit(None, constraints.map(_.toExpr))
  ))
}

// Submap definition
case class SubTopoMap(
  name: String,
  params: MapParameters,
  nodes: List[TopoNode],
  paths: List[AtomicPath]
) extends TopoScript {
  def toExpr: Expr = Expr.Record(Map(
    "name" -> Expr.StringLit(name),
    "params" -> params.toExpr,
    "nodes" -> Expr.ListLit(None, nodes.map(_.toExpr)),
    "paths" -> Expr.ListLit(None, paths.map(_.toExpr))
  ))
}

// Parameters for maps
case class MapParameters(
  mapType: String,
  permResident: Option[Int],
  rushHour: Option[List[RushHourDef]],
  movement: Option[List[String]]
) {
  def toExpr: Expr = {
    val fields = Map(
      "map_type" -> Expr.StringLit(mapType)
    ) ++ permResident.map(pr => "perm_resident" -> Expr.IntLit(pr))
      ++ rushHour.map(rh => "rush_hour" -> Expr.ListLit(None, rh.map(_.toExpr)))
      ++ movement.map(m => "movement" -> Expr.ListLit(None, m.map(Expr.StringLit(_))))

    Expr.Record(fields)
  }
}

// Rush hour definition
case class RushHourDef(
  timePeriod: String,
  gradient: String,
  category: String
) {
  def toExpr: Expr = Expr.Record(Map(
    "time_period" -> Expr.StringLit(timePeriod),
    "gradient" -> Expr.StringLit(gradient),
    "category" -> Expr.StringLit(category)
  ))
}

// Constraint definition
case class Constraint(
  name: String,
  params: List[(String, Type)],
  conditions: List[Expr]
) {
  def toExpr: Expr = Expr.Record(Map(
    "name" -> Expr.StringLit(name),
    "params" -> Expr.ListLit(None, params.map { case (name, tpe) =>
      Expr.Record(Map(
        "name" -> Expr.StringLit(name),
        "type" -> Expr.StringLit(tpe.toString)
      ))
    }),
    "conditions" -> Expr.ListLit(None, conditions)
  ))
}

// Node definition
case class TopoNode(
  name: String,
  description: Option[String]
) {
  def toExpr: Expr = {
    val fields = Map(
      "name" -> Expr.StringLit(name)
    ) ++ description.map(d => "description" -> Expr.StringLit(d))

    Expr.Record(fields)
  }
}

// Path definition
case class AtomicPath(
  from: String,
  to: String,
  bidirectional: Boolean,
  cost: Int,
  movement: Option[String],
  constraints: List[String]
) {
  def toExpr: Expr = {
    val fields = Map(
      "from" -> Expr.StringLit(from),
      "to" -> Expr.StringLit(to),
      "bidirectional" -> Expr.BoolLit(bidirectional),
      "cost" -> Expr.IntLit(cost)
    ) ++ movement.map(m => "movement" -> Expr.StringLit(m))
      ++ (if (constraints.nonEmpty) Some("constraints" -> Expr.ListLit(None, constraints.map(Expr.StringLit(_)))) else None)

    Expr.Record(fields)
  }
}
