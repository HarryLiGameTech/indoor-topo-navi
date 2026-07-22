import api.TopoNaviService
import corelang.Expr
import enums.AttributeValue
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import syntax.TopoMapVisitor
import topomap.grammar.{MapFileLexer, MapFileParser}

import java.util.HashMap

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
        |  atomic-path [entrance -> lobby] {
        |    cost = 5.0,
        |    tags = ["outdoor"]
        |  }
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
  }
}
