import data.*
import enums.AttributeValue.{DoubleValue, ListValue, StringValue}
import enums.NavigationError.{DestinationHasBannedTags, NoRouteFound, PreferenceSearchLimitExceeded}
import enums.RoutePlanningPreferences.{MinimizePhysicalDemands, MinimizeTime, MinimizeTransfers}
import enums.{PathType, RoutePlanningPreferences}
import enums.TransportServicePermission.FullyGranted
import enums.VisitingMode.Normal
import navigation.{RoutePlanner, TraversalPreferencePlanner, TraversalTagPolicy}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TraversalPreferenceTest extends AnyFlatSpec with Matchers {
  private val outdoor = Map("tags" -> ListValue(List(StringValue("outdoor"))))
  private val start = TopoNode("start")
  private val goal = TopoNode("goal")
  private val exposed = TopoNode("exposed")
  private val sheltered = TopoNode("sheltered")

  private def path(from: TopoNode, to: TopoNode, cost: Double, tagged: Boolean = false): AtomicPath =
    AtomicPath(from, to, if (tagged) outdoor else Map.empty, Map(Normal -> cost), PathType.General)

  private def alternatives(detourCost: Double, tagDetour: Boolean = false): NavigationGraph = NavigationGraph(
    "Floor", List(start, goal, exposed, sheltered), List(
      path(start, exposed, 30.0, tagged = true), path(exposed, goal, 30.0),
      path(start, sheltered, detourCost, tagDetour), path(sheltered, goal, 0.0)))

  private def route(graph: NavigationGraph, preference: RoutePlanningPreferences = MinimizeTime,
    highRise: Boolean = false, banned: Set[String] = Set.empty, tag: Option[String] = Some("outdoor")) =
    RoutePlanner(Map(graph.identifier -> graph), TransportGraph(Nil), List(graph.identifier), highRise)
      .navigate(graph.identifier, graph.identifier, "start", "goal", Normal, preference,
        TraversalTagPolicy(banned), minimizeTag = tag)

  "minimizeTag" should "allow exactly 60 additional seconds in both routing modes" in {
    Seq(false, true).foreach { highRise =>
      val accepted = route(alternatives(120.0), highRise = highRise).toOption.get
      accepted.totalCost shouldBe 120.0
      accepted.routeNodes.map(_.localNode.identifier) shouldBe List("start", "sheltered", "goal")
      route(alternatives(120.01), highRise = highRise).toOption.get.totalCost shouldBe 60.0
    }
  }

  it should "allow exactly 25 percent additional physical demand" in {
    route(alternatives(75.0), MinimizePhysicalDemands).toOption.get.totalCost shouldBe 75.0
    route(alternatives(75.01), MinimizePhysicalDemands).toOption.get.totalCost shouldBe 60.0
  }

  it should "use time to break equal tag scores and retain the fastest route when the tag is absent" in {
    route(alternatives(70.0), tag = Some("unknown_tag")).toOption.get.totalCost shouldBe 60.0
    route(alternatives(70.0), tag = None).toOption.get.totalCost shouldBe 60.0
  }

  it should "keep a slower untagged partial route after paths merge" in {
    val merge = TopoNode("merge")
    val graph = NavigationGraph("Floor", List(start, goal, exposed, sheltered, merge), List(
      path(start, exposed, 1.0, tagged = true), path(exposed, merge, 1.0),
      path(start, sheltered, 2.0), path(sheltered, merge, 1.0), path(merge, goal, 1.0)))

    route(graph).toOption.get.routeNodes.map(_.localNode.identifier) shouldBe List("start", "sheltered", "merge", "goal")
  }

  it should "preserve hard bans and return a destination conflict" in {
    route(alternatives(120.0), banned = Set("outdoor")).toOption.get.totalCost shouldBe 120.0
    route(alternatives(120.0, tagDetour = true), banned = Set("outdoor"))
      .left.toOption.get shouldBe a[NoRouteFound]
    val destination = goal.copy(attributes = outdoor)
    val graph = NavigationGraph("Floor", List(start, destination), List(path(start, destination, 1.0)))
    route(graph, banned = Set("outdoor")).left.toOption.get shouldBe a[DestinationHasBannedTags]
  }

  it should "require zero physical demand when the primary optimum is zero" in {
    val graph = NavigationGraph("Floor", List(start, goal, sheltered), List(
      path(start, goal, 0.0, tagged = true), path(start, sheltered, 1.0), path(sheltered, goal, 0.0)))
    route(graph, MinimizePhysicalDemands).toOption.get.physicalDemandScore shouldBe 0.0
  }

  it should "add tagged traversal and mandatory tagged-node dwell exposure" in {
    val waiting = exposed.copy(attributes = outdoor + ("minDwellSeconds" -> DoubleValue(20.0)))
    val graph = NavigationGraph("Floor", List(start, goal, waiting, sheltered), List(
      path(start, waiting, 5.0, tagged = true), path(waiting, goal, 0.0),
      path(start, sheltered, 24.0, tagged = true), path(sheltered, goal, 30.0)))
    route(graph).toOption.get.totalCost shouldBe 54.0
  }

  it should "count intermediate dwell in time and tag exposure while excluding endpoint dwell" in {
    val origin = start.copy(attributes = Map("minDwellSeconds" -> DoubleValue(1000.0)))
    val destination = goal.copy(attributes = Map("minDwellSeconds" -> DoubleValue(1000.0)))
    val waiting = exposed.copy(attributes = outdoor + ("minDwellSeconds" -> DoubleValue(20.0)))
    val graph = NavigationGraph("Floor", List(origin, destination, waiting, sheltered), List(
      path(origin, waiting, 5.0), path(waiting, destination, 5.0),
      path(origin, sheltered, 80.0), path(sheltered, destination, 5.0)))

    val primary = route(graph, tag = None).toOption.get
    primary.totalCost shouldBe 30.0
    primary.physicalDemandScore shouldBe 30.0
    primary.routeEdges.map(_.nodeDwellSeconds).sum shouldBe 20.0
    route(graph).toOption.get.totalCost shouldBe 85.0
  }

  it should "not give point-only node tags an implicit exposure score" in {
    val waiting = exposed.copy(attributes = outdoor)
    val graph = NavigationGraph("Floor", List(start, goal, waiting, sheltered), List(
      path(start, waiting, 10.0), path(waiting, goal, 10.0),
      path(start, sheltered, 30.0), path(sheltered, goal, 0.0)))

    route(graph).toOption.get.totalCost shouldBe 20.0
  }

  it should "select deterministic loop-free alternatives regardless of declaration order" in {
    val a = TopoNode("a")
    val b = TopoNode("b")
    val paths = List(path(start, a, 1.0), path(start, b, 1.0), path(a, b, 0.0),
      path(b, a, 0.0), path(a, goal, 1.0), path(b, goal, 1.0))
    val first = route(NavigationGraph("Floor", List(start, goal, a, b), paths)).toOption.get
    val second = route(NavigationGraph("Floor", List(b, a, goal, start), paths.reverse)).toOption.get

    first.routeNodes.map(_.localNode.identifier) shouldBe second.routeNodes.map(_.localNode.identifier)
    first.routeNodes.distinct shouldBe first.routeNodes
  }

  it should "report a search limit instead of silently returning a partial result" in {
    val graph = alternatives(100.0)
    val result = new TraversalPreferencePlanner(Map("Floor" -> graph), TransportGraph(Nil), maxLabels = 1)
      .navigate(GlobalNode(graph, start), GlobalNode(graph, goal), Normal, MinimizeTime,
        TraversalTagPolicy.AllowAll, false, Some("outdoor"))

    result.left.toOption.get shouldBe a[PreferenceSearchLimitExceeded]
  }

  it should "share label and expansion budgets across both search passes" in {
    val graph = NavigationGraph("Floor", List(start, goal), List(path(start, goal, 1.0)))
    Seq((3, 2), (4, 1)).foreach { case (labels, expansions) =>
      val result = new TraversalPreferencePlanner(Map("Floor" -> graph), TransportGraph(Nil), labels, expansions)
        .navigate(GlobalNode(graph, start), GlobalNode(graph, goal), Normal, MinimizeTime,
          TraversalTagPolicy.AllowAll, false, Some("outdoor"))

      result.left.toOption.get shouldBe a[PreferenceSearchLimitExceeded]
    }
  }

  it should "allow the exact search budget and start each request with a fresh budget" in {
    val graph = NavigationGraph("Floor", List(start, goal), List(path(start, goal, 1.0)))
    val planner = new TraversalPreferencePlanner(Map("Floor" -> graph), TransportGraph(Nil),
      maxLabels = 4, maxExpansions = 2)

    (1 to 2).foreach { _ =>
      val result = planner.navigate(GlobalNode(graph, start), GlobalNode(graph, goal), Normal, MinimizeTime,
        TraversalTagPolicy.AllowAll, false, Some("outdoor"))

      result.toOption.get.totalCost shouldBe 1.0
    }
  }

  it should "allow two actual line changes but reject a third" in {
    Seq(3 -> 2, 4 -> 0).foreach { case (rides, expectedTransfers) =>
      val base = TopoNode("base", outdoor + ("minDwellSeconds" -> DoubleValue(1.0)))
      val first = NavigationGraph("Floor0", List(start, base), List(path(start, base, 0.0)))
      val remaining = (1 to rides).map { i =>
        NavigationGraph(s"Floor$i", List(if (i == rides) goal else TopoNode("landing")))
      }.toList
      val floors = first :: remaining
      def line(name: String, lower: NavigationGraph, upper: NavigationGraph, from: TopoNode, to: TopoNode, time: Double) =
        Escalator(name, Map(lower -> from, upper -> to), Map(lower -> 0.0, upper -> 1.0),
          Map(lower -> FullyGranted, upper -> FullyGranted), time)
      val direct = line("Direct", first, floors.last, base, goal, 1.0)
      val chain = floors.sliding(2).zipWithIndex.map { case (pair, i) =>
        line(s"Alternative$i", pair.head, pair.last, pair.head.nodes.head, pair.last.nodes.head, 30.0)
      }.toList
      val planner = RoutePlanner(floors.map(g => g.identifier -> g).toMap,
        TransportGraph(direct :: chain), floors.map(_.identifier), false)
      val result = planner.navigate("Floor0", floors.last.identifier, "start", "goal", Normal,
        MinimizeTransfers, minimizeTag = Some("outdoor")).toOption.get

      result.transferCount shouldBe expectedTransfers
      result.totalCost shouldBe (if (rides == 3) 90.0 else 2.0)
    }
  }
}
