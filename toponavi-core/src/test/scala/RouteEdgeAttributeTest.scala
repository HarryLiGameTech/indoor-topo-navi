import data.{AtomicPath, GlobalNode, NavigationGraph, NavigationOutputPath, RouteEdge, RouteTraversalMetadata, TopoNode, TpccCoord, TransportGraph}
import enums.AttributeValue.{BoolValue, DoubleValue, IntValue, ListValue, StringValue}
import enums.VisitingMode.Normal
import enums.RoutePlanningPreferences.MinimizeTime
import enums.{PathType, RouteEdgeCategory}
import navigation.RoutePlanner
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.jdk.CollectionConverters.*

@RunWith(classOf[JUnitRunner])
class RouteEdgeAttributeTest extends AnyFlatSpec with Matchers {

  "RouteEdge.fromAtomicPath" should "preserve raw attributes and expose typed traversal metadata" in {
    val source = TopoNode("source")
    val target = TopoNode("target")
    val graph = NavigationGraph("Floor1", List(source, target))
    val attributes = Map(
      "cost" -> IntValue(4),
      "tags" -> ListValue(List(StringValue("door"), StringValue("indoor"))),
      "requiredActions" -> ListValue(List(StringValue("tap_access_card"), StringValue("cross_door"))),
      "experimentalFlag" -> BoolValue(true)
    )
    val atomicPath = AtomicPath(
      source,
      target,
      attributes,
      Map(Normal -> 4.0),
      PathType.General
    )

    val routeEdge = RouteEdge.fromAtomicPath(graph, atomicPath, Normal)

    routeEdge.attributes shouldBe attributes
    routeEdge.traversalMetadata shouldBe RouteTraversalMetadata(
      tags = Set("door", "indoor"),
      requiredActions = List("tap_access_card", "cross_door")
    )
  }

  "NavigationOutputPath.toStructuredSteps" should "expose only typed metadata with stable tag and action ordering" in {
    val source = TopoNode("source")
    val middle = TopoNode("middle")
    val target = TopoNode("target")
    val graph = NavigationGraph("Floor1", List(source, middle, target))

    val first = RouteEdge.fromAtomicPath(
      graph,
      AtomicPath(
        source,
        middle,
        Map(
          "tags" -> ListValue(List(StringValue("indoor"), StringValue("door"))),
          "requiredActions" -> ListValue(List(StringValue("tap_access_card"), StringValue("cross_door"))),
          "internalNote" -> StringValue("must not be exposed")
        ),
        Map(Normal -> 4.0),
        PathType.General
      ),
      Normal
    )
    val second = RouteEdge.fromAtomicPath(
      graph,
      AtomicPath(
        middle,
        target,
        Map(
          "tags" -> ListValue(List(StringValue("shop"), StringValue("indoor"))),
          "requiredActions" -> ListValue(List(StringValue("cross_turnstile")))
        ),
        Map(Normal -> 6.0),
        PathType.General
      ),
      Normal
    )
    val output = NavigationOutputPath(
      routeNodes = List(GlobalNode(graph, source), GlobalNode(graph, middle), GlobalNode(graph, target)),
      routeEdges = List(first, second)
    )

    val step = output.toStructuredSteps.get(0)

    step.get("tags").asInstanceOf[java.util.List[String]].asScala.toList shouldBe
      List("door", "indoor", "shop")
    step.get("requiredActions").asInstanceOf[java.util.List[String]].asScala.toList shouldBe
      List("tap_access_card", "cross_door", "cross_turnstile")
    step.containsKey("required_actions") shouldBe false
    val actionEvents = step.get("requiredActionEvents")
      .asInstanceOf[java.util.List[java.util.Map[String, Object]]]
      .asScala
      .toList
    actionEvents.map(_.get("action")) shouldBe
      List("tap_access_card", "cross_door", "cross_turnstile")
    step.get("waypoints")
      .asInstanceOf[java.util.List[java.util.Map[String, Object]]]
      .asScala
      .map(_.get("nodeId"))
      .toList shouldBe List("source", "middle", "target")
    step.containsKey("attributes") shouldBe false
    step.containsKey("internalNote") shouldBe false
  }

  "RouteTraversalMetadata" should "accept legacy and snake-case required action aliases" in {
    val metadata = RouteTraversalMetadata.from(Map(
      "requiredActions" -> ListValue(List(StringValue("tap_access_card"), StringValue("cross_door"))),
      "required_actions" -> ListValue(List(StringValue("cross_door"), StringValue("cross_turnstile"))),
      "action_required" -> ListValue(List(StringValue("cross_door")))
    ))

    metadata.requiredActions shouldBe
      List("tap_access_card", "cross_door", "cross_turnstile")
  }

  "NavigationOutputPath.toStructuredSteps" should "project route waypoint attributes through the route node convention" in {
    val source = TopoNode(
      "shop_101",
      attributes = Map(
        "description" -> StringValue("Main entrance side"),
        "shop_name" -> StringValue("Example Shop"),
        "shop_category" -> StringValue("Costume"),
        "tags" -> ListValue(List(StringValue("indoor"), StringValue("shop"))),
        "narration" -> StringValue("explicit"),
        "refVisualUrl" -> StringValue("https://example.invalid/shop_101.jpg"),
        "beacon_id" -> StringValue("ble-101")
      )
    )
    val target = TopoNode(
      "toilet_north",
      attributes = Map(
        "facilityName" -> StringValue("North Toilet"),
        "facilityCategory" -> StringValue("Toilet"),
        "positions" -> IntValue(8),
        "observedThroughput" -> IntValue(4),
        "maxThroughput" -> DoubleValue(6.5),
        "tags" -> ListValue(List(StringValue("indoor"), StringValue("toilet")))
      )
    )
    val graph = NavigationGraph("Floor1", List(source, target))
    val edge = RouteEdge.fromAtomicPath(
      graph,
      AtomicPath(source, target, Map.empty, Map(Normal -> 3.0), PathType.General),
      Normal
    )

    val step = NavigationOutputPath(
      routeNodes = List(GlobalNode(graph, source), GlobalNode(graph, target)),
      routeEdges = List(edge)
    ).toStructuredSteps.get(0)

    val waypoints = step.get("waypoints")
      .asInstanceOf[java.util.List[java.util.Map[String, Object]]]
      .asScala
      .toList
    val shop = waypoints.head
    shop.get("nodeId") shouldBe "shop_101"
    shop.get("graph") shouldBe "Floor1"
    shop.get("description") shouldBe "Main entrance side"
    shop.get("shopName") shouldBe "Example Shop"
    shop.get("shopCategory") shouldBe "Costume"
    shop.get("tags").asInstanceOf[java.util.List[String]].asScala.toList shouldBe List("indoor", "shop")
    shop.get("narration") shouldBe "explicit"
    shop.get("refVisualUrl") shouldBe "https://example.invalid/shop_101.jpg"
    shop.get("beaconId") shouldBe "ble-101"
    shop.get("isTrivial").asInstanceOf[java.lang.Boolean].booleanValue() shouldBe false
    shop.get("isIntermediate").asInstanceOf[java.lang.Boolean].booleanValue() shouldBe false

    val facility = waypoints.last
    facility.get("facilityName") shouldBe "North Toilet"
    facility.get("facilityCategory") shouldBe "Toilet"
    facility.get("positions").asInstanceOf[java.lang.Integer].intValue() shouldBe 8
    facility.get("observedThroughput").asInstanceOf[java.lang.Integer].intValue() shouldBe 4
    facility.get("maxThroughput").asInstanceOf[java.lang.Double].doubleValue() shouldBe 6.5
  }

  "RouteEdge" should "default transport traversal metadata to empty" in {
    val sourceGraph = NavigationGraph("Floor1")
    val targetGraph = NavigationGraph("Floor2")
    val edge = RouteEdge(
      source = GlobalNode(sourceGraph, TopoNode("elevator_1")),
      target = GlobalNode(targetGraph, TopoNode("elevator_2")),
      cost = 10.0,
      category = RouteEdgeCategory.Transport,
      movementDescription = "Take elevator"
    )

    edge.attributes shouldBe Map.empty
    edge.traversalMetadata shouldBe RouteTraversalMetadata()
  }

  "RoutePlanner standard-building A*" should "carry AtomicPath attributes into its output RouteEdge" in {
    val source = TopoNode("source", estimatedCoord = Some(TpccCoord(0, 0)))
    val target = TopoNode("target", estimatedCoord = Some(TpccCoord(1, 0)))
    val attributes = Map(
      "tags" -> ListValue(List(StringValue("outdoor"))),
      "requiredActions" -> ListValue(List(StringValue("cross_door")))
    )
    val atomicPath = AtomicPath(
      source,
      target,
      attributes,
      Map(Normal -> 3.0),
      PathType.General
    )
    val graph = NavigationGraph(
      identifier = "Floor1",
      nodes = List(source, target),
      adjacencyList = List(atomicPath)
    )
    val planner = RoutePlanner(
      graphs = Map(graph.identifier -> graph),
      transportGraph = TransportGraph(List.empty),
      subMapNames = List(graph.identifier),
      isHighRise = false
    )

    val result = planner.navigate(
      graph.identifier,
      graph.identifier,
      source.identifier,
      target.identifier,
      Normal,
      MinimizeTime
    )

    result.isRight shouldBe true
    val routeEdge = result.toOption.get.routeEdges.head
    routeEdge.attributes shouldBe attributes
    routeEdge.traversalMetadata shouldBe RouteTraversalMetadata(
      tags = Set("outdoor"),
      requiredActions = List("cross_door")
    )
  }
}
