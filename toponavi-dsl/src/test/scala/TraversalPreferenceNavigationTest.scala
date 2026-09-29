import api.TopoNaviService
import compiler.TopoScriptCompiler
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class TraversalPreferenceNavigationTest extends AnyFunSuite with Matchers {
  private def compile(dwell: String = "20") = new TopoScriptCompiler().compileProject(Map(
    "configuration.tcfg" -> "building-includes { submap Floor }",
    "root" -> "root Preferences(haveCard: Bool) { constraint CardAccess { require haveCard } }",
    "Floor.tmap" -> s"""
      |topo-map Floor() {
      |  let params = {area = 1}
      |  topo-node start
      |  topo-node goal
      |  topo-node exposed {tags = ["outdoor"], minDwellSeconds = $dwell}
      |  topo-node sheltered
      |  atomic-path [start -> exposed] {cost = 5}
      |  atomic-path [exposed -> goal] {cost = 5}
      |  atomic-path [start -> sheltered] {cost = 40}
      |  atomic-path [sheltered -> goal] {cost = 40}
      |  atomic-path [start -> goal] {cost = 1} requires CardAccess
      |}
      |""".stripMargin
  ).asJava, Map("haveCard" -> java.lang.Boolean.FALSE.asInstanceOf[AnyRef]).asJava)

  for highRise <- Seq(false, true)
      risk <- Seq("conservative", "permissive", "aggressive")
  do {
    test(s"soft avoidance respects hard access with $risk and isHighRise=$highRise") {
      val result = compile()
      val route = TopoNaviService.findRoutePlanWithRiskPreference(result, "Floor::start", "Floor::goal",
        "MinimizeTime", List.empty[String].asJava, highRise, risk, "outdoor").plan

      route.totalCost shouldBe 80.0
      route.routeNodes.map(_.localNode.identifier) shouldBe List("start", "sheltered", "goal")
    }
  }

  test("mandatory node dwell is compiled from Int and Float and applied without a soft preference") {
    Seq("20" -> 30.0, "20.5" -> 30.5).foreach { case (literal, expected) =>
      val result = compile(literal)
      Seq(false, true).foreach { highRise =>
        val route = TopoNaviService.findRoutePlanWithRiskPreference(result, "Floor::start", "Floor::goal",
          "MinimizeTime", List.empty[String].asJava, highRise, "conservative").plan
        route.totalCost shouldBe expected
        route.physicalDemandScore shouldBe expected
      }
    }
  }

  Seq("-1", "-0.5", "\"slow\"", "9" * 400 + ".0").foreach { literal =>
    test(s"invalid minDwellSeconds ${literal.take(20)} fails compilation") {
      intercept[RuntimeException](compile(literal)).getMessage should include("minDwellSeconds")
    }
  }

  test("a hard tag ban still applies when the requested soft tag is absent") {
    val route = TopoNaviService.findRoutePlanWithRiskPreference(compile(), "Floor::start", "Floor::goal",
      "MinimizeTime", List("outdoor").asJava, false, "aggressive", "odor_prone").plan
    route.totalCost shouldBe 80.0
  }

  test("soft preferences respect uncertain access and retain fallback annotations") {
    val result = new TopoScriptCompiler().compileProject(Map(
      "configuration.tcfg" -> "building-includes { submap Floor }",
      "root" -> "root Preferences(haveCard: Bool) { constraint CardAccess { require haveCard } }",
      "Floor.tmap" -> """
        |topo-map Floor() {
        |  let params = {area = 1}
        |  topo-node start
        |  topo-node goal
        |  atomic-path [start -> goal] {cost = 30, tags = ["outdoor"]}
        |  atomic-path [start -> goal] {cost = 10} subject-to CardAccess because "May need a card."
        |}
        |""".stripMargin
    ).asJava, Map("haveCard" -> java.lang.Boolean.FALSE.asInstanceOf[AnyRef]).asJava)

    Seq(false, true).foreach { highRise =>
      Seq("conservative" -> 30.0, "permissive" -> 10.0, "aggressive" -> 10.0).foreach { case (risk, expected) =>
        val route = TopoNaviService.findRoutePlanWithRiskPreference(result, "Floor::start", "Floor::goal",
          "MinimizeTime", List.empty[String].asJava, highRise, risk, "outdoor").plan
        route.totalCost shouldBe expected
        route.uncertainAccess.isDefined shouldBe (risk != "conservative")
      }
    }
  }
}
