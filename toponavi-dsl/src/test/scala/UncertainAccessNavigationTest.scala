import api.TopoNaviService
import compiler.TopoScriptCompiler
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.Collections
import scala.jdk.CollectionConverters.*

class UncertainAccessNavigationTest extends AnyFunSuite with Matchers {

  private val root =
    """
      |root UncertainAccessTests(haveCard: Bool) {
      |  constraint CardAccess { require haveCard }
      |}
      |""".stripMargin

  private val params: java.util.Map[String, AnyRef] =
    Map("haveCard" -> java.lang.Boolean.FALSE.asInstanceOf[AnyRef]).asJava

  private def compile(
    files: Map[String, String],
    userParams: java.util.Map[String, AnyRef] = params
  ) = new TopoScriptCompiler().compileProject(files.asJava, userParams)

  test("all risk preferences select the expected route through a subject-to atomic path") {
    val files = Map(
      "configuration.tcfg" ->
        """
          |building-includes {
          |  submap TestMap
          |}
          |""".stripMargin,
      "root" -> root,
      "TestMap.tmap" ->
        """
          |topo-map TestMap() {
          |  let params: {area: Int} = {area = 1}
          |  topo-node A
          |  topo-node B
          |  topo-node C
          |  topo-node D
          |  atomic-path [A -> B] {cost = 15}
          |  atomic-path [B -> C] {cost = 15}
          |  atomic-path [C -> D] {cost = 15}
          |  atomic-path [A -> D] {cost = 15}
          |    subject-to CardAccess because "Door access may require a card."
          |}
          |""".stripMargin
    )
    val result = compile(files)

    val directEdge = result.graphs("TestMap").adjacencyList
      .find(edge => edge.source.identifier == "A" && edge.target.identifier == "D")
      .getOrElse(fail("Expected the subject-to edge to remain compiled"))
    directEdge.uncertainAccess.get.uncertaintyReasons.head.conditionType shouldBe "CardAccess"

    val withCard = compile(files, Map(
      "haveCard" -> java.lang.Boolean.TRUE.asInstanceOf[AnyRef]
    ).asJava)
    val satisfiedDirectEdge = withCard.graphs("TestMap").adjacencyList
      .find(edge => edge.source.identifier == "A" && edge.target.identifier == "D")
      .getOrElse(fail("Expected the satisfied subject-to edge to remain annotated"))
    satisfiedDirectEdge.uncertainAccess.get.uncertaintyReasons.head.conditionSatisfied shouldBe true
    satisfiedDirectEdge.hasFailedUncertainAccess shouldBe false

    val plans = List("conservative", "permissive", "aggressive").map { riskPreference =>
      riskPreference -> TopoNaviService.findRoutePlanWithRiskPreference(
        result,
        "TestMap::A",
        "TestMap::D",
        "MinimizeTime",
        Collections.emptyList[String](),
        false,
        riskPreference
      )
    }.toMap

    plans("conservative").plan.routeNodes.map(_.localNode.identifier) shouldBe List("A", "B", "C", "D")
    plans("conservative").plan.totalCost shouldBe 45.0
    plans("conservative").plan.uncertainAccess shouldBe None
    plans("conservative").appliedRiskPreference shouldBe "conservative"

    plans("permissive").plan.routeNodes.map(_.localNode.identifier) shouldBe List("A", "D")
    plans("permissive").plan.totalCost shouldBe 15.0
    plans("permissive").plan.uncertainAccess.get.uncertaintyReasons.head.reason.get shouldBe
      "Door access may require a card."
    plans("permissive").appliedRiskPreference shouldBe "permissive"

    plans("aggressive").plan.routeNodes.map(_.localNode.identifier) shouldBe List("A", "D")
    plans("aggressive").appliedRiskPreference shouldBe "aggressive"
  }

  test("a failed subject-to elevator station is filtered per search and disclosed on fallback") {
    val files = Map(
      "configuration.tcfg" ->
        """
          |building-includes {
          |  submap Lower
          |  submap Upper
          |  vehicle TestLift
          |}
          |""".stripMargin,
      "root" -> root,
      "Lower.tmap" ->
        """
          |topo-map Lower() {
          |  let params: {area: Int, permResident: Int} = {area = 1, permResident = 1}
          |  topo-node hall
          |}
          |""".stripMargin,
      "Upper.tmap" ->
        """
          |topo-map Upper() {
          |  let params: {area: Int, permResident: Int} = {area = 1, permResident = 1}
          |  topo-node hall
          |}
          |""".stripMargin,
      "TestLift.ttr" ->
        """
          |transport TestLift is Elevator {
          |  let params: {maxVelocity: Float, acceleration: Float, duty: Int, capacity: Int, carAmount: Int} = {maxVelocity = 2.5, acceleration = 0.8, duty = 100, capacity = 10, carAmount = 1}
          |  station LowerStop at Lower::hall {location = 0.0, departureRate = 0.1}
          |  station UpperStop at Upper::hall {location = 4.0, departureRate = 0.1}
          |    subject-to CardAccess because "The floor button may require a card."
          |}
          |""".stripMargin
    )
    val result = compile(files)

    List(false, true).foreach { isHighRise =>
      val planned = TopoNaviService.findRoutePlanWithRiskPreference(
        result,
        "Lower::hall",
        "Upper::hall",
        "MinimizeTime",
        Collections.emptyList[String](),
        isHighRise,
        "conservative"
      )

      planned.appliedRiskPreference shouldBe "permissive"
      planned.plan.uncertainAccess.get.uncertaintyReasons should contain (
        data.UncertaintyReason("CardAccess", Some("The floor button may require a card."))
      )
      planned.plan.routeEdges.exists(_.category == enums.RouteEdgeCategory.Transport) shouldBe true
    }
  }

  test("requires and subject-to cannot be combined on the same element") {
    val files = Map(
      "configuration.tcfg" -> "building-includes { submap TestMap }",
      "root" -> root,
      "TestMap.tmap" ->
        """
          |topo-map TestMap() {
          |  let params: {area: Int} = {area = 1}
          |  topo-node A
          |  topo-node B
          |  atomic-path [A -> B] {cost = 1} requires CardAccess subject-to CardAccess
          |}
          |""".stripMargin
    )

    val error = intercept[RuntimeException](compile(files))
    error.getMessage should include("cannot use both requires and subject-to")
  }
}
