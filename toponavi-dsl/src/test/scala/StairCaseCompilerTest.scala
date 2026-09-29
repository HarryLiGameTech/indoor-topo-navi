import api.TopoNaviService
import compiler.TopoScriptCompiler
import data.StairCase
import enums.RoutePlanningPreferences.MinimizeTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class StairCaseCompilerTest extends AnyFunSuite with Matchers {
  private def compile(
    params: String = "turnBackCost = 3",
    lowerIndex: String = "0",
    upperIndex: String = "4",
    includeEscalator: Boolean = false,
    includeMiddleLanding: Boolean = false
  ) = {
    val files = Map(
      "configuration.tcfg" -> s"""
        |building-includes {
        |  submap Lower
        |  submap Upper
        |  ${if (includeMiddleLanding) "submap Middle" else ""}
        |  vehicle TestStairs
        |  ${if (includeEscalator) "vehicle TestEscalator" else ""}
        |}
        |""".stripMargin,
      "Lower.tmap" -> """
        |topo-map Lower() {
        |  let params = {area = 1}
        |  topo-node lower
        |}
        |""".stripMargin,
      "Upper.tmap" -> """
        |topo-map Upper() {
        |  let params = {area = 1}
        |  topo-node upper
        |}
        |""".stripMargin,
      "Middle.tmap" -> """
        |topo-map Middle() {
        |  let params = {area = 1}
        |  topo-node middle
        |}
        |""".stripMargin,
      "TestStairs.ttr" -> s"""
        |transport TestStairs is Stairs {
        |  let params = {$params}
        |  station LowerLanding at Lower::lower {
        |    location = 0,
        |    directSegmentIndex = $lowerIndex
        |  }
        |  station UpperLanding at Upper::upper {
        |    location = 3.34,
        |    directSegmentIndex = $upperIndex
        |  }
        |  ${if (includeMiddleLanding) "station MiddleLanding at Middle::middle {location = 1.67, directSegmentIndex = 2}" else ""}
        |}
        |""".stripMargin
    ) ++ Option.when(includeEscalator)("TestEscalator.ttr" -> """
      |transport TestEscalator is Escalator {
      |  let params = {time = 25.0}
      |  station LowerLanding at Lower::lower {}
      |  station UpperLanding at Upper::upper {}
      |}
      |""".stripMargin)

    new TopoScriptCompiler().compileProject(files.asJava)
  }

  Seq("turnBackCost = 5" -> 35.0, "" -> 29.0, "turnBackCost = 0" -> 20.0).foreach {
    case (params, expected) =>
      test(s"compiled stair edges include turn cost for params '{$params}'") {
        val result = compile(params)
        val subject = result.transportGraph.nodes.map(_.ownerLine).collectFirst {
          case stairs: StairCase => stairs
        }.getOrElse(fail("Expected a compiled StairCase"))

        subject.stationRunIndices(result.graphs("Lower")) shouldBe 0
        subject.stationRunIndices(result.graphs("Upper")) shouldBe 4
        val costs = result.transportGraph.adjacencyList.values.flatMap(_.values).toList
        costs should have size 2
        costs.foreach(_ shouldBe (expected +- 1e-9))
      }
  }

  for isHighRise <- Seq(false, true)
      (start, destination) <- Seq("Lower::lower" -> "Upper::upper", "Upper::upper" -> "Lower::lower")
  do {
    test(s"stair route from $start includes turn cost with isHighRise = $isHighRise") {
      val route = TopoNaviService.findRoutePlan(
        compile(), start, destination, MinimizeTime, Set.empty, isHighRise
      )

      route.totalCost shouldBe (29.0 +- 1e-9)
      route.routeEdges.filter(_.movementDescription.startsWith("Take TestStairs ")).map(_.cost).sum shouldBe (29.0 +- 1e-9)
    }

    test(s"turn cost changes transport choice from $start with isHighRise = $isHighRise") {
      val withoutTurns = TopoNaviService.findRoutePlan(
        compile("turnBackCost = 0", includeEscalator = true),
        start, destination, MinimizeTime, Set.empty, isHighRise
      )
      val withTurns = TopoNaviService.findRoutePlan(
        compile(includeEscalator = true),
        start, destination, MinimizeTime, Set.empty, isHighRise
      )

      withoutTurns.totalCost shouldBe (20.0 +- 1e-9)
      withoutTurns.routeEdges.exists(_.movementDescription.startsWith("Take TestStairs ")) shouldBe true
      withTurns.totalCost shouldBe (25.0 +- 1e-9)
      withTurns.routeEdges.exists(_.movementDescription.startsWith("Take TestStairs ")) shouldBe false
      withTurns.routeEdges.exists(_.movementDescription.startsWith("Take TestEscalator ")) shouldBe true
    }
  }

  Seq(false, true).foreach { isHighRise =>
    test(s"intermediate stair landings use the existing per-leg turn formula with isHighRise = $isHighRise") {
      val result = compile(includeMiddleLanding = true)
      val directCosts = for
        (source, neighbors) <- result.transportGraph.adjacencyList.toList
        (destination, cost) <- neighbors
        if source.ownerGraph.identifier == "Lower" && destination.ownerGraph.identifier == "Upper"
      yield cost
      val route = TopoNaviService.findRoutePlan(
        result, "Lower::lower", "Upper::upper", MinimizeTime, Set.empty, isHighRise
      )

      directCosts should have size 1
      directCosts.head shouldBe (29.0 +- 1e-9)
      // Each leg counts its own intermediate turns, so routing via Middle costs less.
      val stairLegs = route.routeEdges.filter(_.movementDescription.startsWith("Take TestStairs "))
      stairLegs should have size 2
      stairLegs.foreach(_.cost shouldBe (13.0 +- 1e-9))
      route.totalCost shouldBe (26.0 +- 1e-9)
    }
  }

  test("negative turnBackCost fails before transport graph construction") {
    val error = intercept[RuntimeException] { compile("turnBackCost = -1") }

    error.getMessage should include("turnBackCost must be a non-negative Int")
  }

  test("turnBackCost is not truncated to a signed 32-bit integer") {
    val result = compile("turnBackCost = 2147483648")

    result.transportGraph.adjacencyList.values.flatMap(_.values).foreach {
      _ shouldBe (20.0 + 3.0 * 2147483648L)
    }
  }

  Seq("2147483648", "-2147483649").foreach { index =>
    test(s"out-of-range directSegmentIndex $index fails without truncation") {
      val error = intercept[RuntimeException] { compile(upperIndex = index) }

      error.getMessage should include("directSegmentIndex must fit in a signed 32-bit Int")
    }
  }
}
