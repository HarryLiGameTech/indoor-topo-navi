import data.*
import enums.AttributeValue.{DoubleValue, IntValue, ListValue, StringValue}
import enums.NavigationError.{InvalidData, NoRouteFound}
import enums.RoutePlanningPreferences.*
import enums.TransportServicePermission.FullyGranted
import enums.VisitingMode.Normal
import enums.PathType
import navigation.{CustomRouteCost, CustomRouteObjective, RoutePlanner, TraversalPreferencePlanner, TraversalTagPolicy}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NativeRouteObjectiveTest extends AnyFlatSpec with Matchers {
  private val outdoor = Map("tags" -> ListValue(List(StringValue("outdoor"))))
  private def edge(a: TopoNode, b: TopoNode, time: Double, attributes: Map[String, enums.AttributeValue] = Map.empty) =
    AtomicPath(a, b, attributes, Map(Normal -> time), PathType.General)

  private def line(id: String, a: NavigationGraph, b: NavigationGraph, from: TopoNode, to: TopoNode, time: Double) =
    Escalator(id, Map(a -> from, b -> to), Map(a -> 0.0, b -> 1.0),
      Map(a -> FullyGranted, b -> FullyGranted), time)

  private val start = TopoNode("start")
  private val goal = TopoNode("goal")
  private val directStart = TopoNode("direct")
  private val directEnd = TopoNode("direct")
  private val localStart = TopoNode("local")
  private val localEnd = TopoNode("local")
  private val middleNode = TopoNode("landing")
  private val lower = NavigationGraph("Lower", List(start, directStart, localStart),
    List(edge(start, directStart, 100), edge(start, localStart, 1)))
  private val middle = NavigationGraph("Middle", List(middleNode))
  private val upper = NavigationGraph("Upper", List(goal, directEnd, localEnd),
    List(edge(directEnd, goal, 100), edge(localEnd, goal, 1)))
  private val graphs = List(lower, middle, upper).map(g => g.identifier -> g).toMap
  private val transports = TransportGraph(List(
    line("Direct", lower, upper, directStart, directEnd, 1),
    line("First", lower, middle, localStart, middleNode, 10),
    line("Second", middle, upper, middleNode, localEnd, 10)))

  for highRise <- Seq(false, true) do {
    "Native objectives" should s"optimize transfers before time with transportFirst=$highRise" in {
      val planner = RoutePlanner(graphs, transports, graphs.keys.toList, highRise)
      val transfers = planner.navigate("Lower", "Upper", "start", "goal", Normal, MinimizeTransfers).toOption.get
      val fastest = planner.navigate("Lower", "Upper", "start", "goal", Normal, MinimizeTime).toOption.get
      transfers.totalCost shouldBe 201.0
      transfers.transferCount shouldBe 0
      fastest.totalCost shouldBe 22.0
      fastest.transferCount shouldBe 1
    }

    it should s"apply custom tie-breakers in the declared order with transportFirst=$highRise" in {
      val planner = RoutePlanner(graphs, transports, graphs.keys.toList, highRise)
      def route(order: List[enums.RoutePlanningPreferences]) = planner.navigate(
        "Lower", "Upper", "start", "goal", Normal, MinimizeCustomData,
        customObjective = Some(CustomRouteObjective(CustomRouteCost.TagExposure("absent"), order))).toOption.get

      route(List(MinimizeTransfers, MinimizeTime)).totalCost shouldBe 201.0
      route(List(MinimizeTime, MinimizeTransfers)).totalCost shouldBe 22.0
    }

    it should s"optimize physical demand without a tag preference or node dwell with transportFirst=$highRise" in {
      val bottom = NavigationGraph("Bottom", List(start, directStart, localStart),
        List(edge(start, directStart, 1), edge(start, localStart, 20)))
      val top = NavigationGraph("Top", List(goal, directEnd, localEnd),
        List(edge(directEnd, goal, 1), edge(localEnd, goal, 20)))
      val rides = TransportGraph(List(
        line("Near", bottom, top, directStart, directEnd, 40),
        line("Far", bottom, top, localStart, localEnd, 1)))
      val planner = RoutePlanner(Map("Bottom" -> bottom, "Top" -> top), rides, List("Bottom", "Top"), highRise)
      val physical = planner.navigate("Bottom", "Top", "start", "goal", Normal, MinimizePhysicalDemands).toOption.get
      val fastest = planner.navigate("Bottom", "Top", "start", "goal", Normal, MinimizeTime).toOption.get
      physical.totalCost shouldBe 42.0
      fastest.totalCost shouldBe 41.0
      physical.physicalDemandScore should be < fastest.physicalDemandScore
    }

    it should s"choose custom walking costs before transport and time with transportFirst=$highRise" in {
      val safe = TopoNode("safe")
      val floor = NavigationGraph("Floor", List(start, safe, goal), List(
        edge(start, goal, 1, outdoor),
        edge(start, safe, 200), edge(safe, goal, 1)))
      val planner = RoutePlanner(Map("Floor" -> floor), TransportGraph(Nil), List("Floor"), highRise)
      val native = planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
        customObjective = Some(CustomRouteObjective(CustomRouteCost.TagExposure("outdoor")))).toOption.get
      val nearTie = planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeTime,
        minimizeTag = Some("outdoor")).toOption.get
      native.totalCost shouldBe 201.0
      nearTie.totalCost shouldBe 1.0
    }

    it should s"sum numeric attributes and preserve the selected parallel edge with transportFirst=$highRise" in {
      val floor = NavigationGraph("Floor", List(start, goal), List(
        edge(start, goal, 1, Map("effort" -> DoubleValue(1.5))),
        edge(start, goal, 100, Map("effort" -> IntValue(1)))))
      val objective = CustomRouteObjective(CustomRouteCost.EdgeAttribute("effort"))
      val result = RoutePlanner(Map("Floor" -> floor), TransportGraph(Nil), List("Floor"), highRise)
        .navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
          customObjective = Some(objective)).toOption.get
      result.totalCost shouldBe 100.0
      objective.cost.total(result) shouldBe 1.0
    }

    it should s"retain the last line across intermediate rides with transportFirst=$highRise" in {
      val landing = TopoNode("landing")
      val levels = (0 to 2).map(i => NavigationGraph(s"Floor$i", List(landing))).toList
      val stairs = StairCase("Stairs", levels.map(_ -> landing).toMap,
        levels.zipWithIndex.map((g, i) => g -> i.toDouble).toMap,
        levels.zipWithIndex.toMap, 0.0,
        allowedRidePairs = Some(levels.sliding(2).map(pair => pair.head -> pair.last).toSet))
      val planner = RoutePlanner(levels.map(g => g.identifier -> g).toMap, TransportGraph(List(stairs)),
        levels.map(_.identifier), highRise)
      val route = planner.navigate("Floor0", "Floor2", "landing", "landing", Normal, MinimizeTransfers).toOption.get
      route.transferCount shouldBe 0
      route.transportSegmentCount shouldBe 2
    }

    it should s"avoid history explosion across repeated walking branches with transportFirst=$highRise" in {
      val junctions = (0 to 20).map(i => TopoNode(s"j$i")).toList
      val branches = (0 until 20).flatMap(i => List(TopoNode(s"a$i"), TopoNode(s"b$i"))).toList
      val walking = (0 until 20).flatMap { i =>
        List(edge(junctions(i), branches(2 * i), 1), edge(branches(2 * i), junctions(i + 1), 1),
          edge(junctions(i), branches(2 * i + 1), 1), edge(branches(2 * i + 1), junctions(i + 1), 1))
      }.toList
      val bottom = NavigationGraph("Bottom", junctions ++ branches, walking)
      val floors = bottom :: (1 to 3).map(i => NavigationGraph(s"Floor$i", List(TopoNode("landing")))).toList
      val rides = floors.sliding(2).zipWithIndex.map { case (pair, i) =>
        line(s"Ride$i", pair.head, pair.last, if (i == 0) junctions.last else pair.head.nodes.head,
          pair.last.nodes.head, 1)
      }.toList
      val planner = new TraversalPreferencePlanner(floors.map(g => g.identifier -> g).toMap,
        TransportGraph(rides), maxLabels = 500, maxExpansions = 2000, transportFirst = highRise)
      val route = planner.navigate(GlobalNode(bottom, junctions.head), GlobalNode(floors.last, floors.last.nodes.head),
        Normal, MinimizeTransfers, TraversalTagPolicy.AllowAll, false, Some("absent")).toOption.get
      route.transferCount shouldBe 2
      route.totalCost shouldBe 43.0
      route.routeNodes.distinct shouldBe route.routeNodes
    }
  }

  it should "reject invalid custom costs and mismatched objective declarations" in {
    Seq(DoubleValue(-1), DoubleValue(Double.NaN), DoubleValue(Double.PositiveInfinity), StringValue("high")).foreach { value =>
      val floor = NavigationGraph("Floor", List(start, goal), List(edge(start, goal, 1, Map("effort" -> value))))
      val planner = RoutePlanner(Map("Floor" -> floor), TransportGraph(Nil), List("Floor"), true)
      planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
        customObjective = Some(CustomRouteObjective(CustomRouteCost.EdgeAttribute("effort"))))
        .left.toOption.get shouldBe a[InvalidData]
      planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData)
        .left.toOption.get shouldBe a[InvalidData]
    }
  }

  it should "apply intermediate dwell once and exclude endpoint dwell from custom exposure" in {
    val source = start.copy(attributes = outdoor + ("minDwellSeconds" -> IntValue(1000)))
    val target = goal.copy(attributes = outdoor + ("minDwellSeconds" -> IntValue(1000)))
    val waiting = TopoNode("waiting", outdoor + ("minDwellSeconds" -> IntValue(7)))
    val floor = NavigationGraph("Floor", List(source, waiting, target),
      List(edge(source, waiting, 2, outdoor), edge(waiting, target, 3)))
    val objective = CustomRouteObjective(CustomRouteCost.TagExposure("outdoor"))
    val route = RoutePlanner(Map("Floor" -> floor), TransportGraph(Nil), List("Floor"), true)
      .navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
        customObjective = Some(objective)).toOption.get
    route.totalCost shouldBe 12.0
    objective.cost.total(route) shouldBe 9.0
  }

  it should "keep custom-cost routing subject to hard bans and uncertain-access filtering" in {
    val uncertain = Some(UncertainAccess(List(UncertaintyReason("Permit", Some("May need a permit")))))
    val floor = NavigationGraph("Floor", List(start, goal), List(edge(start, goal, 1, outdoor).copy(uncertainAccess = uncertain)))
    val planner = RoutePlanner(Map("Floor" -> floor), TransportGraph(Nil), List("Floor"), true)
    val objective = Some(CustomRouteObjective(CustomRouteCost.TagExposure("outdoor")))
    planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
      customObjective = objective).left.toOption.get shouldBe a[NoRouteFound]
    planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
      allowUncertainAccess = true, customObjective = objective).isRight shouldBe true
    planner.navigate("Floor", "Floor", "start", "goal", Normal, MinimizeCustomData,
      tagPolicy = TraversalTagPolicy(Set("outdoor")), allowUncertainAccess = true,
      customObjective = objective).left.toOption.get shouldBe a[NoRouteFound]
  }
}
