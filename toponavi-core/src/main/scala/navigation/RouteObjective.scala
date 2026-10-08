package navigation

import data.{NavigationOutputPath, RouteEdge, RouteTraversalMetadata}
import enums.AttributeValue.{DoubleValue, IntValue}
import enums.RoutePlanningPreferences
import enums.RoutePlanningPreferences.*

sealed trait CustomRouteCost {
  def cost(edge: RouteEdge): Double
  def total(path: NavigationOutputPath): Double = path.routeEdges.map(cost).sum
}

object CustomRouteCost {
  case class TagExposure(tag: String) extends CustomRouteCost {
    require(tag.trim.nonEmpty, "Custom tag must be nonempty")

    override def cost(edge: RouteEdge): Double = {
      val traversal = if (edge.traversalMetadata.tags.contains(tag)) edge.cost - edge.nodeDwellSeconds else 0.0
      val dwell = if (RouteTraversalMetadata.tagsFrom(edge.target.localNode.attributes).contains(tag))
        edge.nodeDwellSeconds else 0.0
      traversal + dwell
    }
  }

  case class EdgeAttribute(attribute: String, missingValue: Double = 0.0) extends CustomRouteCost {
    require(attribute.trim.nonEmpty, "Custom attribute must be nonempty")
    require(missingValue.isFinite && missingValue >= 0.0, "Custom missingValue must be finite and non-negative")

    override def cost(edge: RouteEdge): Double = {
      val value = if (edge.transport.isDefined) 0.0 else edge.attributes.get(attribute) match {
        case None => missingValue
        case Some(IntValue(number)) => number.toDouble
        case Some(DoubleValue(number)) => number
        case _ => throw new IllegalArgumentException(s"Custom path attribute '$attribute' must be numeric")
      }
      require(value.isFinite && value >= 0.0, s"Custom path attribute '$attribute' must be finite and non-negative")
      value
    }
  }
}

case class CustomRouteObjective(
  cost: CustomRouteCost,
  tieBreakers: List[RoutePlanningPreferences] = List(MinimizeTransfers, MinimizeTime)
) {
  require(tieBreakers.toSet == Set(MinimizeTransfers, MinimizeTime) && tieBreakers.size == 2,
    "Custom tieBreakers must contain MinimizeTransfers and MinimizeTime exactly once")
}

private[navigation] case class RouteScore(
  time: Double = 0.0,
  transfers: Int = 0,
  physical: Double = 0.0,
  custom: Double = 0.0
) {
  def append(edge: RouteEdge, lastLine: Option[String], customCost: Double): RouteScore = {
    val change = if (edge.transport.exists(ride => lastLine.exists(_ != ride.lineIdentifier))) 1 else 0
    val next = RouteScore(time + edge.cost, transfers + change, physical + edge.physicalDemandScore, custom + customCost)
    require(next.time.isFinite && next.physical.isFinite && next.custom.isFinite, "Route scores must be finite")
    next
  }

  def rank(preference: RoutePlanningPreferences, objective: Option[CustomRouteObjective]): (Double, Double, Double) =
    preference match {
      case MinimizeTime => (time, 0.0, 0.0)
      case MinimizeTransfers => (transfers.toDouble, time, 0.0)
      case MinimizePhysicalDemands => (physical, time, 0.0)
      case MinimizeCustomData =>
        val values = objective.get.tieBreakers.map {
          case MinimizeTransfers => transfers.toDouble
          case MinimizeTime => time
          case _ => throw new IllegalArgumentException("Unsupported custom tie-breaker")
        }
        (custom, values.head, values(1))
    }
}
