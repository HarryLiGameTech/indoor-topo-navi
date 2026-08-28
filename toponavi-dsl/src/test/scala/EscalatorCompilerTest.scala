import compiler.TopoScriptCompiler
import data.Escalator
import enums.TransportServicePermission
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class EscalatorCompilerTest extends AnyFunSuite with Matchers {
  private val configuration =
    """
      |building-includes {
      |  submap Lower
      |  submap Upper
      |  submap Third
      |  vehicle TestEscalator
      |}
      |""".stripMargin

  private val root =
    """
      |root EscalatorTests(allowed: Bool) {
      |  constraint Allowed { require allowed }
      |}
      |""".stripMargin

  private val lowerMap =
    """
      |topo-map Lower() {
      |  let params: {area: Int} = {area = 1}
      |  topo-node lower
      |  topo-node lower2
      |}
      |""".stripMargin

  private val upperMap =
    """
      |topo-map Upper() {
      |  let params: {area: Int} = {area = 1}
      |  topo-node upper
      |}
      |""".stripMargin

  private val thirdMap =
    """
      |topo-map Third() {
      |  let params: {area: Int} = {area = 1}
      |  topo-node third
      |}
      |""".stripMargin

  private def compile(transport: String) = {
    val files = Map(
      "configuration.tcfg" -> configuration,
      "root" -> root,
      "Lower.tmap" -> lowerMap,
      "Upper.tmap" -> upperMap,
      "Third.tmap" -> thirdMap,
      "TestEscalator.ttr" -> transport
    ).asJava
    val params: java.util.Map[String, AnyRef] =
      Map("allowed" -> java.lang.Boolean.FALSE.asInstanceOf[AnyRef]).asJava

    new TopoScriptCompiler().compileProject(files, params)
  }

  private def transport(params: String, stations: String): String = {
    val stationLines = stations.replace(" station", "\n  station")
    s"""
       |transport TestEscalator is Escalator {
       |  $params
       |  let displayName: String = "Test escalator"
       |  $stationLines
       |}
       |""".stripMargin
  }

  test("valid escalator data survives compilation") {
    val result = compile(transport(
      "let params: {time: Float} = {time = 12.5}",
      """station UpperLabel at Upper::upper {}
        |  station LowerLabel at Lower::lower {} requires Allowed""".stripMargin
    ))

    val subject = result.transportGraph.nodes.map(_.ownerLine).distinct.collectFirst {
      case escalator: Escalator => escalator
    }.getOrElse(fail("Expected a compiled Escalator"))
    val upper = result.graphs("Upper")
    val lower = result.graphs("Lower")

    subject.identifier shouldBe "TestEscalator"
    subject.displayName shouldBe Some("Test escalator")
    subject.travelTimeSeconds shouldBe 12.5
    subject.stationNodes(upper).identifier shouldBe "upper"
    subject.stationNodes(lower).identifier shouldBe "lower"
    subject.stationLabels(upper) shouldBe "UpperLabel"
    subject.stationLabels(lower) shouldBe "LowerLabel"
    subject.stationLocations(upper) shouldBe 0.0
    subject.stationLocations(lower) shouldBe 1.0
    subject.stationPermissions(upper) shouldBe TransportServicePermission.FullyGranted
    subject.stationPermissions(lower) shouldBe TransportServicePermission.NoAccess
    subject.netTimeBetweenStations(upper, lower) shouldBe 12.5
    subject.netTimeBetweenStations(lower, upper) shouldBe 12.5
  }

  test("missing params.time fails compilation clearly") {
    val error = intercept[RuntimeException] {
      compile(transport(
        "let params: {other: Int} = {other = 1}",
        "station A at Lower::lower {} station B at Upper::upper {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' must contain numeric 'params.time'")
  }

  test("non-numeric params.time fails compilation clearly") {
    val error = intercept[RuntimeException] {
      compile(transport(
        "let params: {time: String} = {time = \"fast\"}",
        "station A at Lower::lower {} station B at Upper::upper {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' must contain numeric 'params.time'")
  }

  Seq("0", "-1", "-0.5").foreach { invalidTime =>
    test(s"params.time = $invalidTime fails compilation clearly") {
      val valueType = if (invalidTime.contains('.')) "Float" else "Int"
      val error = intercept[RuntimeException] {
        compile(transport(
          s"let params: {time: $valueType} = {time = $invalidTime}",
          "station A at Lower::lower {} station B at Upper::upper {}"
        ))
      }

      error.getMessage should include("Escalator 'TestEscalator' params.time must be finite and greater than 0")
    }
  }

  test("a non-finite floating-point params.time fails compilation clearly") {
    val overflowingFloatLiteral = "9" * 400 + ".0"
    val error = intercept[RuntimeException] {
      compile(transport(
        s"let params: {time: Float} = {time = $overflowingFloatLiteral}",
        "station A at Lower::lower {} station B at Upper::upper {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' params.time must be finite and greater than 0")
  }

  test("an escalator with fewer than two stations fails compilation") {
    val error = intercept[RuntimeException] {
      compile(transport(
        "let params: {time: Int} = {time = 30}",
        "station A at Lower::lower {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' must have exactly 2 stations")
  }

  test("an escalator with more than two stations fails compilation") {
    val error = intercept[RuntimeException] {
      compile(transport(
        "let params: {time: Int} = {time = 30}",
        "station A at Lower::lower {} station B at Upper::upper {} station C at Third::third {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' must have exactly 2 stations")
  }

  test("two stations in the same submap fail compilation") {
    val error = intercept[RuntimeException] {
      compile(transport(
        "let params: {time: Int} = {time = 30}",
        "station A at Lower::lower {} station B at Lower::lower2 {}"
      ))
    }

    error.getMessage should include("Escalator 'TestEscalator' stations must belong to two distinct submaps")
  }
}
