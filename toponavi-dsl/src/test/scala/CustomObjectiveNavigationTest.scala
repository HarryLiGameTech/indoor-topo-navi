import api.TopoNaviService
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class CustomObjectiveNavigationTest extends AnyFunSuite with Matchers {
  private def compile(uncertainCost: Double) = TopoNaviService.compile(Map(
    "configuration" -> "building-includes { submap Floor }",
    "Floor" -> s"""
      |topo-map Floor() {
      |  let params = {area = 1}
      |  constraint Permit { require false }
      |  topo-node start
      |  topo-node goal
      |  atomic-path [start -> goal] {cost = 10, effort = 8}
      |  atomic-path [start -> goal] {cost = 1, effort = $uncertainCost} subject-to Permit because "May need a permit."
      |}
      |""".stripMargin
  ).asJava)

  for highRise <- Seq(false, true) do {
    test(s"custom primary improvement controls permissive access with transportFirst=$highRise") {
      val objective = TopoNaviService.customRouteObjective("edgeAttribute", "effort", 0, null)
      Seq(2.0 -> 1.0, 8.0 -> 10.0).foreach { case (uncertainCost, expectedTime) =>
        val compiled = compile(uncertainCost)
        val route = TopoNaviService.findRoutePlanWithRiskPreference(compiled, "Floor::start", "Floor::goal",
          "MinimizeCustomData", List.empty[String].asJava, highRise, "permissive", null, objective).plan
        route.totalCost shouldBe expectedTime
        route.uncertainAccess.isDefined shouldBe (uncertainCost < 8)
      }
    }

    test(s"custom objectives preserve conservative and aggressive policies with transportFirst=$highRise") {
      val compiled = compile(2)
      val objective = TopoNaviService.customRouteObjective("edgeAttribute", "effort", 0, null)
      Seq("conservative" -> 10.0, "aggressive" -> 1.0).foreach { case (risk, expectedTime) =>
        val route = TopoNaviService.findRoutePlanWithRiskPreference(compiled, "Floor::start", "Floor::goal",
          "MinimizeCustomData", List.empty[String].asJava, highRise, risk, null, objective).plan
        route.totalCost shouldBe expectedTime
      }
    }
  }
}
