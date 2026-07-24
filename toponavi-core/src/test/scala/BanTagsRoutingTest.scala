import data.{AtomicPath, NavigationGraph, TopoNode, TpccCoord, TransportGraph}
import enums.AttributeValue.{ListValue, StringValue}
import enums.NavigationError.DestinationHasBannedTags
import enums.RoutePlanningPreferences.MinimizeTime
import enums.VisitingMode.Normal
import enums.PathType
import navigation.{RoutePlanner, TraversalTagPolicy}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BanTagsRoutingTest extends AnyFlatSpec with Matchers {

  "NavigationGraph.findPath" should "exclude tagged edges at runtime" in {
    val start = TopoNode("start")
    val detour = TopoNode("detour")
    val goal = TopoNode("goal")
    val direct = path(start, goal, 1.0, tags = List("outdoor"))
    val safeFirst = path(start, detour, 2.0)
    val safeSecond = path(detour, goal, 2.0)
    val graph = NavigationGraph(
      "Floor1",
      List(start, detour, goal),
      List(direct, safeFirst, safeSecond)
    )

    val result = graph.findPath(
      start,
      goal,
      Normal,
      TraversalTagPolicy(Set("outdoor")),
      allowBannedStart = true
    )

    result.map(_.routeNodes.map(_.identifier)) shouldBe Some(List("start", "detour", "goal"))
    result.get.routeEdges shouldBe List(safeFirst, safeSecond)
  }

  it should "exclude tagged intermediate nodes at runtime" in {
    val start = TopoNode("start")
    val staffed = TopoNode("staffed", tagged("staffed"))
    val quiet = TopoNode("quiet")
    val goal = TopoNode("goal")
    val graph = NavigationGraph(
      "Floor1",
      List(start, staffed, quiet, goal),
      List(
        path(start, staffed, 1.0),
        path(staffed, goal, 1.0),
        path(start, quiet, 2.0),
        path(quiet, goal, 2.0)
      )
    )

    val result = graph.findPath(
      start,
      goal,
      Normal,
      TraversalTagPolicy(Set("staffed")),
      allowBannedStart = true
    )

    result.map(_.routeNodes.map(_.identifier)) shouldBe Some(List("start", "quiet", "goal"))
  }

  it should "allow departure from a banned source only when explicitly exempted" in {
    val start = TopoNode("start", tagged("outdoor"))
    val goal = TopoNode("goal")
    val graph = NavigationGraph("Floor1", List(start, goal), List(path(start, goal, 1.0)))
    val policy = TraversalTagPolicy(Set("outdoor"))

    graph.findPath(start, goal, Normal, policy, allowBannedStart = true).isDefined shouldBe true
    graph.findPath(start, goal, Normal, policy, allowBannedStart = false) shouldBe None
  }

  it should "reconstruct the exact selected edge when parallel edges exist" in {
    val start = TopoNode("start")
    val goal = TopoNode("goal")
    val slow = path(start, goal, 10.0, tags = List("slow"))
    val fast = path(start, goal, 1.0, tags = List("fast"))
    val graph = NavigationGraph("Floor1", List(start, goal), List(slow, fast))

    val result = graph.findPath(start, goal, Normal).get

    result.routeEdges shouldBe List(fast)
    result.totalCost shouldBe 1.0
  }

  "RoutePlanner" should "return a destination conflict before navigation" in {
    val start = TopoNode("start")
    val goal = TopoNode("goal", tagged("outdoor"))
    val graph = NavigationGraph("Floor1", List(start, goal), List(path(start, goal, 1.0)))
    val planner = RoutePlanner(
      Map(graph.identifier -> graph),
      TransportGraph(List.empty),
      List(graph.identifier),
      isHighRise = true
    )

    planner.navigate(
      graph.identifier,
      graph.identifier,
      start.identifier,
      goal.identifier,
      Normal,
      MinimizeTime,
      TraversalTagPolicy(Set("outdoor"))
    ) shouldBe Left(DestinationHasBannedTags("Floor1::goal", List("outdoor")))
  }

  it should "apply edge bans in standard-building A-star" in {
    val start = TopoNode("start", estimatedCoord = Some(TpccCoord(0, 0)))
    val detour = TopoNode("detour", estimatedCoord = Some(TpccCoord(1, 1)))
    val goal = TopoNode("goal", estimatedCoord = Some(TpccCoord(2, 0)))
    val graph = NavigationGraph(
      "Floor1",
      List(start, detour, goal),
      List(
        path(start, goal, 1.0, tags = List("outdoor")),
        path(start, detour, 2.0),
        path(detour, goal, 2.0)
      )
    )
    val planner = RoutePlanner(
      Map(graph.identifier -> graph),
      TransportGraph(List.empty),
      List(graph.identifier),
      isHighRise = false
    )

    val result = planner.navigate(
      graph.identifier,
      graph.identifier,
      start.identifier,
      goal.identifier,
      Normal,
      MinimizeTime,
      TraversalTagPolicy(Set("outdoor"))
    )

    result.toOption.get.routeNodes.map(_.localNode.identifier) shouldBe List("start", "detour", "goal")
  }

  private def tagged(tags: String*) =
    Map("tags" -> ListValue(tags.toList.map(StringValue.apply)))

  private def path(
    source: TopoNode,
    target: TopoNode,
    cost: Double,
    tags: List[String] = List.empty
  ): AtomicPath =
    AtomicPath(
      source,
      target,
      if tags.isEmpty then Map.empty else tagged(tags*),
      Map(Normal -> cost),
      PathType.General
    )
}
