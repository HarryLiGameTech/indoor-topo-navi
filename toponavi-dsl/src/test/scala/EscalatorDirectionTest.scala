import api.{NavigationRequestException, TopoNaviService}
import compiler.{CompilationResult, TopoScriptCompiler}
import corelang.{Environment, Identifier, Type, Value}
import enums.RoutePlanningPreferences.MinimizeTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import surfacelang.{TopoEnvironment, TopoNodeRefValue, TransportDirectionExpr}
import util.SyntaxError.ParsingError

import scala.jdk.CollectionConverters.*

class EscalatorDirectionTest extends AnyFunSuite with Matchers {
  private def topoMap(name: String): String = s"""
    |topo-map $name() {
    |  let params = {area = 1}
    |  topo-node landing
    |}
    |""".stripMargin

  private def transport(
    declaration: String,
    reverseStations: Boolean = false,
    lowerAccess: String = "",
    upperAccess: String = "",
    policies: String = ""
  ): String = {
    val stations = List(
      s"station LowerStop at Lower::landing {} $lowerAccess",
      s"station UpperStop at Upper::landing {} $upperAccess"
    )
    val orderedStations = if (reverseStations) stations.reverse else stations
    s"""
      |transport TestEscalator is Escalator {
      |  let params = {time = 18.0, distance = 8.5}
      |  constraint Allowed { require true }
      |  constraint Denied { require false }
      |  $declaration
      |  ${orderedStations.mkString("\n  ")}
      |  $policies
      |}
      |""".stripMargin
  }

  private def compile(source: String): CompilationResult = new TopoScriptCompiler().compileProject(Map(
    "configuration.tcfg" -> "building-includes {\n submap Lower managed-by Public\n submap Upper managed-by Restricted\n vehicle TestEscalator\n}",
    "Lower.tmap" -> topoMap("Lower"),
    "Upper.tmap" -> topoMap("Upper"),
    "TestEscalator.ttr" -> source
  ).asJava)

  private def rides(result: CompilationResult): Set[(String, String)] =
    result.transportGraph.adjacencyList.toList.flatMap { case (source, neighbors) =>
      neighbors.keys.map(target => source.ownerGraph.identifier -> target.ownerGraph.identifier)
    }.toSet

  private val upward = "Lower" -> "Upper"
  private val downward = "Upper" -> "Lower"

  Seq("->" -> false, "<->" -> true).foreach { case (arrow, bidirectional) =>
    test(s"$arrow survives parsing and elaborates into station-reference pairs") {
      val compiler = new TopoScriptCompiler()
      val parsed = compiler.parseTransportFile(transport(s"direction LowerStop $arrow UpperStop"))
      parsed.direction shouldBe Some(TransportDirectionExpr("LowerStop", "UpperStop", bidirectional))
      val empty = TopoEnvironment(Environment.empty[Identifier, Type, Value], Map.empty, Map.empty, Map.empty)
      val maps = List("Lower", "Upper").map { name =>
        name -> compiler.parseMapFile(topoMap(name)).elaborate(using empty)
      }.toMap
      val elaborated = parsed.elaborate(using empty.copy(submaps = maps))
      val from = TopoNodeRefValue("Lower", "landing")
      val to = TopoNodeRefValue("Upper", "landing")

      elaborated.rideDirections shouldBe Some(if (bidirectional) Set(from -> to, to -> from) else Set(from -> to))
    }
  }

  Seq(
    "direction LowerStop -> UpperStop" -> Set(upward),
    "direction UpperStop -> LowerStop" -> Set(downward),
    "direction LowerStop <-> UpperStop" -> Set(upward, downward),
    "direction UpperStop <-> LowerStop" -> Set(upward, downward),
    "" -> Set(upward, downward)
  ).foreach { case (declaration, expected) =>
    test(s"${if (declaration.isEmpty) "omitted direction" else declaration} controls rides regardless of station order") {
      Seq(false, true).foreach { reverseStations =>
        val result = compile(transport(declaration, reverseStations))
        rides(result) shouldBe expected
        Seq(false, true).foreach { highRise =>
          Seq(None, Some("outdoor")).foreach { minimizeTag =>
            Seq(upward, downward).foreach { case (from, to) =>
              def route = TopoNaviService.findRoutePlan(result, s"$from::landing", s"$to::landing",
                MinimizeTime, Set.empty, highRise, false, minimizeTag)
              if (expected.contains(from -> to)) {
                val plan = route
                plan.totalCost shouldBe 18.0
                plan.physicalDemandScore shouldBe (12.55 +- 1e-9)
                plan.routeEdges.flatMap(_.transport.map(_.lineIdentifier)) shouldBe List("TestEscalator")
              } else {
                intercept[NavigationRequestException](route).getCode shouldBe "NO_ROUTE_FOUND"
              }
            }
          }
        }
      }
    }
  }

  test("an omitted direction leaves ride directions unrestricted during elaboration") {
    new TopoScriptCompiler().parseTransportFile(transport("")).direction shouldBe None
    val result = compile(transport(""))
    result.transportGraph.nodes.head.ownerLine.asInstanceOf[data.Escalator].allowedRidePairs shouldBe None
  }

  test("a direction may follow its station declarations") {
    val source = transport("", policies = "direction LowerStop -> UpperStop")
    rides(compile(source)) shouldBe Set(upward)
  }

  Seq(
    "direction Unknown -> UpperStop" -> "Unknown direction station label 'Unknown'",
    "direction LowerStop -> Unknown" -> "Unknown direction station label 'Unknown'",
    "direction Lower -> UpperStop" -> "Unknown direction station label 'Lower'",
    "direction Public -> UpperStop" -> "Unknown direction station label 'Public'",
    "direction lowerStop -> UpperStop" -> "Unknown direction station label 'lowerStop'",
    "direction LowerStop -> LowerStop" -> "must connect distinct stations",
    "direction UpperStop <-> UpperStop" -> "must connect distinct stations",
    "direction LowerStop -> UpperStop\ndirection LowerStop -> UpperStop" -> "may declare only one direction",
    "direction LowerStop -> UpperStop\ndirection UpperStop -> LowerStop" -> "may declare only one direction"
  ).foreach { case (declaration, message) =>
    test(s"invalid declaration $declaration is rejected") {
      intercept[RuntimeException](compile(transport(declaration))).getMessage should include(message)
    }
  }

  test("ambiguous station labels cannot be used as direction operands") {
    val source = transport("direction LowerStop -> UpperStop")
      .replace("station UpperStop", "station LowerStop")
    intercept[RuntimeException](compile(source)).getMessage should include("Duplicate station label 'LowerStop'")
  }

  test("different labels cannot direct a ride back to the same node") {
    val source = transport("direction LowerStop -> UpperStop")
      .replace("Upper::landing", "Lower::landing")
    intercept[RuntimeException](compile(source)).getMessage should include("must connect distinct stations")
  }

  Seq("Elevator", "Stairs").foreach { kind =>
    test(s"direction is rejected on $kind") {
      val source = transport("direction LowerStop -> UpperStop").replace("is Escalator", s"is $kind")
      intercept[RuntimeException](compile(source)).getMessage should include("direction is only supported for Escalator")
    }
  }

  test("direction outside a transport is rejected instead of ignored") {
    val compiler = new TopoScriptCompiler()
    intercept[RuntimeException](compiler.parseRootFile("root Test() { direction A -> B }"))
      .getMessage should include("direction is only supported inside Escalator")
    intercept[RuntimeException](compiler.parseMapFile("topo-map Test() { direction A -> B }"))
      .getMessage should include("direction is only supported inside Escalator")
  }

  Seq("direction LowerStop <- UpperStop", "direction LowerStop ->", "direction Lower::landing -> UpperStop").foreach { declaration =>
    test(s"malformed direction $declaration fails parsing") {
      intercept[ParsingError](new TopoScriptCompiler().parseTransportFile(transport(declaration)))
    }
  }

  test("ride policies can restrict an operating direction but cannot restore its reverse") {
    val forward = "direction LowerStop -> UpperStop"
    rides(compile(transport(forward, policies = "ride-from any to any requires Allowed"))) shouldBe Set(upward)
    rides(compile(transport(forward, policies = "ride-from LowerStop to UpperStop requires Denied"))) shouldBe empty
    rides(compile(transport("direction LowerStop <-> UpperStop",
      policies = "ride-from Restricted to Public requires Denied"))) shouldBe Set(upward)
  }

  test("station restrictions and out-of-order still remove directed rides") {
    val declaration = "direction LowerStop -> UpperStop"
    rides(compile(transport(declaration, lowerAccess = "requires Denied on Depart"))) shouldBe empty
    rides(compile(transport(declaration, upperAccess = "requires Denied on Arrive"))) shouldBe empty
    rides(compile(transport(declaration, upperAccess = "out-of-order"))) shouldBe empty
    rides(compile(transport(declaration, lowerAccess = "requires Denied on Arrive"))) shouldBe Set(upward)
  }

  test("uncertain access never restores the excluded direction") {
    val result = compile(transport("direction LowerStop -> UpperStop",
      upperAccess = "subject-to Denied because \"May require a card.\" on Arrive"))
    rides(result) shouldBe Set(upward)
    Seq(false, true).foreach { highRise =>
      Seq(None, Some("outdoor")).foreach { minimizeTag =>
        intercept[NavigationRequestException] {
          TopoNaviService.findRoutePlan(result, "Lower::landing", "Upper::landing",
            MinimizeTime, Set.empty, highRise, false, minimizeTag)
        }.getCode shouldBe "NO_ROUTE_FOUND"
        val forward = TopoNaviService.findRoutePlan(result, "Lower::landing", "Upper::landing",
          MinimizeTime, Set.empty, highRise, true, minimizeTag)
        forward.uncertainAccess.isDefined shouldBe true
        intercept[NavigationRequestException] {
          TopoNaviService.findRoutePlan(result, "Upper::landing", "Lower::landing",
            MinimizeTime, Set.empty, highRise, true, minimizeTag)
        }.getCode shouldBe "NO_ROUTE_FOUND"
      }
    }
  }
}
