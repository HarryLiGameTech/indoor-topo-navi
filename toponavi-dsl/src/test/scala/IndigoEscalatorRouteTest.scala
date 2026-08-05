import api.TopoNaviService
import compiler.TopoScriptCompiler
import enums.RoutePlanningPreferences.MinimizeTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.jdk.CollectionConverters.*

class IndigoEscalatorRouteTest extends AnyFunSuite with Matchers {
  private val possibleExamplePaths = List(
    new File("../examples/indigoBJ"),
    new File("examples/indigoBJ")
  )
  private val exampleDirectory = possibleExamplePaths.find(_.isDirectory)
    .getOrElse(fail(s"indigoBJ example not found: ${possibleExamplePaths.map(_.getAbsolutePath).mkString(", ")}"))
  private lazy val compilation = new TopoScriptCompiler().compile(exampleDirectory.getAbsolutePath)

  private def structuredSteps(start: String, destination: String): List[java.util.Map[String, Object]] =
    TopoNaviService.findRoutePlan(
      compilation,
      start,
      destination,
      MinimizeTime,
      Set.empty,
      isHighRise = false
    )
      .toStructuredSteps.asScala.toList

  private def transportSteps(steps: List[java.util.Map[String, Object]]) =
    steps.filter(_.get("type") == "Transport")

  test("Level1 to Level3 exact-node route uses the two mall escalators in order") {
    val steps = structuredSteps(
      "Level1::mid_gate_inside",
      "Level3::shop_341_outside"
    )
    val transports = transportSteps(steps)

    transports.map(_.get("description")) shouldBe List(
      "Take CentralEscalator from Level1 to Level2",
      "Take Escalator_18 from Level2 to Level3"
    )
    transports.map(_.get("costSeconds")) shouldBe List(Double.box(30.0), Double.box(30.0))
  }

  test("McDonald's to cinema exact-node route uses only the penthouse escalator") {
    val transports = transportSteps(structuredSteps(
      "Level3::shop_359_outside",
      "Penthouse::check_in_gate_outside"
    ))

    transports.map(_.get("description")) shouldBe List(
      "Take PenthouseEscalator from Level3 to Penthouse"
    )
    transports.map(_.get("costSeconds")) shouldBe List(Double.box(35.0))
  }

  test("Starbucks to subway exact-node route is one walking step without transport") {
    val steps = structuredSteps(
      "LowerGround::shop_036",
      "LowerGround::subway_station"
    )

    transportSteps(steps) shouldBe empty
    steps should have size 1
    Set("Walk", "WalkStraight") should contain(steps.head.get("type"))
  }
}
