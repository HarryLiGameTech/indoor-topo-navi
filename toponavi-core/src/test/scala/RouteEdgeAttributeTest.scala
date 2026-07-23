import data.{AtomicPath, GlobalNode, NavigationGraph, NavigationOutputPath, RouteEdge, RouteTraversalMetadata, TopoNode, TpccCoord, TransportGraph}
import enums.AttributeValue.{BoolValue, IntValue, ListValue, StringValue}
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
    step.containsKey("attributes") shouldBe false
    step.containsKey("internalNote") shouldBe false
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
