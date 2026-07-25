import api.{NavigationRequestException, TopoNaviService}
import corelang.Expr
import enums.AttributeValue
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import syntax.TopoMapVisitor
import topomap.grammar.{MapFileLexer, MapFileParser}

import java.util.HashMap
import scala.jdk.CollectionConverters.*

class ListLiteralCompilationTest extends AnyFunSuite with Matchers {

  private def parseExpr(code: String): Expr = {
    val lexer = new MapFileLexer(CharStreams.fromString(code))
    val parser = new MapFileParser(new CommonTokenStream(lexer))
    new TopoMapVisitor().visit(parser.expr())
  }

  test("surface syntax parses generic and nested list literals") {
    parseExpr("[1, 2, 3]") shouldBe Expr.ListLit(
      None,
      List(Expr.IntLit(1), Expr.IntLit(2), Expr.IntLit(3))
    )

    parseExpr("[[\"outdoor\"], [\"covered\"]]") shouldBe Expr.ListLit(
      None,
      List(
        Expr.ListLit(None, List(Expr.StringLit("outdoor"))),
        Expr.ListLit(None, List(Expr.StringLit("covered")))
      )
    )
  }

  test("list attributes reach the compiler backend as generic AttributeValues") {
    val files = new HashMap[String, String]()
    files.put("configuration.tcfg", "building-includes { submap Floor1 }")
    files.put(
      "Floor1.tmap",
      """
        |topo-map Floor1() {
        |  let params = { permResident = 0 }
        |  topo-node entrance {
        |    tags = ["outdoor", "covered"],
        |    levelNumbers = [1, 2, 3],
        |    nestedWeights = [[1.0, 2.0], [3.0, 4.0]]
        |  }
        |  topo-node lobby
        |  topo-node safe_corridor { tags = ["indoor"] }
        |  atomic-path [entrance -> lobby] {
        |    cost = 5.0,
        |    tags = ["outdoor"],
        |    requiredActions = ["cross_door"]
        |  }
        |  atomic-path [entrance -> safe_corridor] { cost = 4.0 }
        |  atomic-path [safe_corridor -> lobby] { cost = 4.0 }
        |}
        |""".stripMargin
    )

    val result = TopoNaviService.compile(files)
    val graph = result.graphs("Floor1")
    val entrance = graph.nodes.find(_.identifier == "entrance").getOrElse {
      fail("Compiled graph did not contain the entrance node")
    }
    val path = graph.adjacencyList.head

    entrance.attributes("tags") shouldBe AttributeValue.ListValue(List(
      AttributeValue.StringValue("outdoor"),
      AttributeValue.StringValue("covered")
    ))
    entrance.attributes("levelNumbers") shouldBe AttributeValue.ListValue(List(
      AttributeValue.IntValue(1),
      AttributeValue.IntValue(2),
      AttributeValue.IntValue(3)
    ))
    entrance.attributes("nestedWeights") shouldBe AttributeValue.ListValue(List(
      AttributeValue.ListValue(List(AttributeValue.DoubleValue(1.0), AttributeValue.DoubleValue(2.0))),
      AttributeValue.ListValue(List(AttributeValue.DoubleValue(3.0), AttributeValue.DoubleValue(4.0)))
    ))
    path.attributes("tags") shouldBe AttributeValue.ListValue(List(
      AttributeValue.StringValue("outdoor")
    ))

    val plan = TopoNaviService.findRoutePlan(
      result,
      "Floor1::entrance",
      "Floor1::lobby",
      "MinimizeTime"
    )
    val routeEdge = plan.routeEdges.head
    routeEdge.attributes shouldBe path.attributes
    routeEdge.traversalMetadata.tags shouldBe Set("outdoor")
    routeEdge.traversalMetadata.requiredActions shouldBe List("cross_door")

    val step = plan.toStructuredSteps.get(0)
    step.get("tags").asInstanceOf[java.util.List[String]].asScala.toList shouldBe List("outdoor")
    step.get("requiredActions").asInstanceOf[java.util.List[String]].asScala.toList shouldBe List("cross_door")
    step.containsKey("required_actions") shouldBe false
    step.containsKey("attributes") shouldBe false

    val bannedPlan = TopoNaviService.findRoutePlan(
      result,
      "Floor1::entrance",
      "Floor1::lobby",
      "MinimizeTime",
      java.util.List.of("outdoor")
    )
    bannedPlan.routeNodes.map(_.localNode.identifier) shouldBe
      List("entrance", "safe_corridor", "lobby")
    bannedPlan.routeEdges.flatMap(_.traversalMetadata.tags) shouldBe List.empty

    val conflict = intercept[NavigationRequestException] {
      TopoNaviService.findRoutePlan(
        result,
        "Floor1::lobby",
        "Floor1::entrance",
        "MinimizeTime",
        java.util.List.of("outdoor")
      )
    }
    conflict.getCode shouldBe "DESTINATION_HAS_BANNED_TAG"
    conflict.getDetails.get("conflictingTags") shouldBe java.util.List.of("outdoor")

    val noRoute = intercept[NavigationRequestException] {
      TopoNaviService.findRoutePlan(
        result,
        "Floor1::entrance",
        "Floor1::lobby",
        "MinimizeTime",
        java.util.List.of("outdoor", "indoor")
      )
    }
    noRoute.getCode shouldBe "NO_ROUTE_WITH_BAN_TAGS"
    noRoute.getDetails.get("banTags") shouldBe java.util.List.of("indoor", "outdoor")
  }

  test("legacy action_required path attribute reaches structured route actions") {
    val files = new HashMap[String, String]()
    files.put("configuration.tcfg", "building-includes { submap Floor1 }")
    files.put(
      "Floor1.tmap",
      """
        |topo-map Floor1() {
        |  let params = { permResident = 0 }
        |  topo-node shop
        |  topo-node shop_outside
        |  atomic-path [shop <-> shop_outside] {
        |    cost = 3.0,
        |    action_required = ["cross_door"]
        |  }
        |}
        |""".stripMargin
    )

    val result = TopoNaviService.compile(files)
    val plan = TopoNaviService.findRoutePlan(
      result,
      "Floor1::shop",
      "Floor1::shop_outside",
      "MinimizeTime"
    )

    plan.routeEdges.head.traversalMetadata.requiredActions shouldBe List("cross_door")
    val step = plan.toStructuredSteps.get(0)
    step.get("requiredActions").asInstanceOf[java.util.List[String]].asScala.toList shouldBe List("cross_door")
    step.containsKey("required_actions") shouldBe false
  }
}
