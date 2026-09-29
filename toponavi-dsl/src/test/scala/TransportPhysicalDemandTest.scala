import api.TopoNaviService
import compiler.TopoScriptCompiler
import data.Escalator
import enums.RouteEdgeCategory.Climbing
import enums.RoutePlanningPreferences.{MinimizePhysicalDemands, MinimizeTime}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class TransportPhysicalDemandTest extends AnyFunSuite with Matchers {
  private def compile(kind: String, params: String, lowerData: String = "", upperData: String = "") = {
    new TopoScriptCompiler().compileProject(Map(
      "configuration.tcfg" -> "building-includes {\n  submap Lower\n  submap Upper\n  vehicle TestTransport\n}",
      "Lower.tmap" -> """
        |topo-map Lower() {
        |  let params = {area = 1, permResident = 1}
        |  topo-node start
        |  topo-node landing
        |  atomic-path [start <-> landing] {cost = 2}
        |}
        |""".stripMargin,
      "Upper.tmap" -> """
        |topo-map Upper() {
        |  let params = {area = 1, permResident = 1}
        |  topo-node landing
        |  topo-node goal
        |  atomic-path [landing <-> goal] {cost = 3}
        |}
        |""".stripMargin,
      "TestTransport.ttr" -> s"""
        |transport TestTransport is $kind {
        |  let params = {$params}
        |  station LowerLanding at Lower::landing {$lowerData}
        |  station UpperLanding at Upper::landing {$upperData}
        |}
        |""".stripMargin
    ).asJava)
  }

  for highRise <- Seq(false, true)
      kind <- Seq("Elevator", "Escalator", "Stairs")
  do {
    test(s"$kind planning and complete-route scores agree with isHighRise=$highRise") {
      val result = kind match {
        case "Elevator" => compile(kind, "maxVelocity = 2.5, carAmount = 1",
          "location = 0.0, departureRate = 0.1", "location = 4.0, departureRate = 0.1")
        case "Escalator" => compile(kind, "time = 30.0, distance = 10.0")
        case _ => compile(kind, "turnBackCost = 3",
          "location = 0.0, directSegmentIndex = 0", "location = 4.0, directSegmentIndex = 4")
      }
      val route = TopoNaviService.findRoutePlan(
        result, "Lower::start", "Upper::goal", MinimizeTime, Set.empty, highRise)
      val ride = route.routeEdges.find(_.transport.isDefined).get
      val expected = kind match {
        case "Elevator" => 0.4 + 0.5 * ride.cost
        case "Escalator" => 20.5
        case _ => 40.0
      }

      ride.physicalDemandScore shouldBe (expected +- 1e-9)
      route.physicalDemandScore shouldBe (expected + 5.0 +- 1e-9)
      route.transferCount shouldBe 0
      if (kind == "Stairs") ride.category shouldBe Climbing
    }
  }

  Seq("0" -> 0.0, "12" -> 12.0, "12.5" -> 12.5).foreach { case (literal, expected) =>
    test(s"escalator params.distance accepts $literal metres") {
      val result = compile("Escalator", s"time = 30, distance = $literal")
      val escalator = result.transportGraph.nodes.head.ownerLine.asInstanceOf[Escalator]

      escalator.distanceBetweenStations(result.graphs("Lower"), result.graphs("Upper")) shouldBe expected
      val route = TopoNaviService.findRoutePlan(result, "Lower::start", "Upper::goal", MinimizePhysicalDemands)
      route.physicalDemandScore shouldBe (5.0 + 19.5 + 0.1 * expected +- 1e-9)
    }
  }

  test("legacy time-only escalators do not invent a one-metre distance") {
    val result = compile("Escalator", "time = 30")
    val route = TopoNaviService.findRoutePlan(result, "Lower::start", "Upper::goal", MinimizePhysicalDemands)

    route.physicalDemandScore shouldBe 24.5
  }

  test("the distance and time score changes high-rise transport selection") {
    val result = new TopoScriptCompiler().compileProject(Map(
      "configuration.tcfg" -> "building-includes {\n submap Lower\n submap Upper\n vehicle Fast\n vehicle Short\n}",
      "Lower.tmap" -> """
        |topo-map Lower() {
        |  let params = {area = 1}
        |  topo-node start
        |  topo-node fast
        |  topo-node short
        |  atomic-path [start <-> fast] {cost = 20}
        |  atomic-path [start <-> short] {cost = 20}
        |}
        |""".stripMargin,
      "Upper.tmap" -> """
        |topo-map Upper() {
        |  let params = {area = 1}
        |  topo-node goal
        |  topo-node fast
        |  topo-node short
        |  atomic-path [fast <-> goal] {cost = 20}
        |  atomic-path [short <-> goal] {cost = 20}
        |}
        |""".stripMargin,
      "Fast.ttr" -> """
        |transport Fast is Escalator {
        |  let params = {time = 10, distance = 200}
        |  station Lower at Lower::fast {}
        |  station Upper at Upper::fast {}
        |}
        |""".stripMargin,
      "Short.ttr" -> """
        |transport Short is Escalator {
        |  let params = {time = 20, distance = 10}
        |  station Lower at Lower::short {}
        |  station Upper at Upper::short {}
        |}
        |""".stripMargin
    ).asJava)
    val fastest = TopoNaviService.findRoutePlan(result, "Lower::start", "Upper::goal", MinimizeTime)
    val easiest = TopoNaviService.findRoutePlan(result, "Lower::start", "Upper::goal", MinimizePhysicalDemands)

    fastest.routeEdges.flatMap(_.transport.map(_.lineIdentifier)) shouldBe List("Fast")
    easiest.routeEdges.flatMap(_.transport.map(_.lineIdentifier)) shouldBe List("Short")
    fastest.physicalDemandScore shouldBe 66.5
    easiest.physicalDemandScore shouldBe 54.0
    Seq(false, true).foreach { highRise =>
      val complete = TopoNaviService.findRoutePlan(result, "Lower::start", "Upper::goal",
        MinimizePhysicalDemands, Set.empty, highRise, false, Some("outdoor"))
      complete.physicalDemandScore shouldBe easiest.physicalDemandScore
    }
  }

  Seq("-1", "-0.5", "\"long\"", "9" * 400 + ".0").foreach { literal =>
    test(s"invalid escalator distance ${literal.take(20)} fails compilation") {
      intercept[RuntimeException] {
        compile("Escalator", s"time = 30, distance = $literal")
      }.getMessage should include("params.distance")
    }
  }
}
