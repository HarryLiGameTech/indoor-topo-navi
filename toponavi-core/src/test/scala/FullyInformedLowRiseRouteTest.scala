import data.{AtomicPath, Escalator, NavigationGraph, TopoNode, TransportGraph}
import enums.PathType
import enums.RouteEdgeCategory.Transport
import enums.RoutePlanningPreferences.MinimizeTime
import enums.TransportServicePermission.FullyGranted
import enums.VisitingMode.Normal
import navigation.RoutePlanner
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FullyInformedLowRiseRouteTest extends AnyFlatSpec with Matchers {
  private val start = TopoNode("start")
  private val express1 = TopoNode("express")
  private val local1 = TopoNode("local")
  private val floor1 = NavigationGraph(
    "Floor1",
    List(start, express1, local1),
    List(path(start, express1, 100.0), path(start, local1, 1.0))
  )

  private val local2Arrival = TopoNode("local_arrival")
  private val local2Departure = TopoNode("local_departure")
  private val floor2 = NavigationGraph(
    "Floor2",
    List(local2Arrival, local2Departure),
    List(path(local2Arrival, local2Departure, 1.0))
  )

  private val express3 = TopoNode("express")
  private val local3 = TopoNode("local")
  private val goal = TopoNode("goal")
  private val floor3 = NavigationGraph(
    "Floor3",
    List(express3, local3, goal),
    List(path(express3, goal, 100.0), path(local3, goal, 1.0))
  )

  private val express = escalator(
    "Express",
    floor1 -> express1,
    floor3 -> express3,
    travelTime = 1.0
  )
  private val localLower = escalator(
    "LocalLower",
    floor1 -> local1,
    floor2 -> local2Arrival,
    travelTime = 10.0
  )
  private val localUpper = escalator(
    "LocalUpper",
    floor2 -> local2Departure,
    floor3 -> local3,
    travelTime = 10.0
  )

  private val graphs = List(floor1, floor2, floor3).map(graph => graph.identifier -> graph).toMap
  private val transportGraph = TransportGraph(List(express, localLower, localUpper))

  "RoutePlanner low-rise routing" should "include source and destination walking costs in its hierarchy" in {
    val result = planner(isHighRise = false).navigate(
      floor1.identifier,
      floor3.identifier,
      start.identifier,
      goal.identifier,
      Normal,
      MinimizeTime
    ).toOption.get

    result.routeEdges.filter(_.category == Transport).map(_.movementDescription) shouldBe List(
      "Take LocalLower from Floor1 to Floor2",
      "Take LocalUpper from Floor2 to Floor3"
    )
    result.totalCost shouldBe 23.0
  }

  it should "retain transport-first fuzzy routing in high-rise mode" in {
    val result = planner(isHighRise = true).navigate(
      floor1.identifier,
      floor3.identifier,
      start.identifier,
      goal.identifier,
      Normal,
      MinimizeTime
    ).toOption.get

    result.routeEdges.filter(_.category == Transport).map(_.movementDescription) shouldBe List(
      "Take Express from Floor1 to Floor3"
    )
    result.totalCost shouldBe 201.0
  }

  private def planner(isHighRise: Boolean): RoutePlanner = RoutePlanner(
    graphs,
    transportGraph,
    List(floor1.identifier, floor2.identifier, floor3.identifier),
    isHighRise
  )

  private def escalator(
    identifier: String,
    first: (NavigationGraph, TopoNode),
    second: (NavigationGraph, TopoNode),
    travelTime: Double
  ): Escalator = {
    val stations = Map(first, second)
    Escalator(
      identifier,
      stations,
      Map(first._1 -> 0.0, second._1 -> 1.0),
      stations.keys.map(_ -> FullyGranted).toMap,
      travelTime
    )
  }

  private def path(source: TopoNode, target: TopoNode, cost: Double): AtomicPath =
    AtomicPath(source, target, Map(Normal -> cost), PathType.General)
}
