import compiler.{CompilationResult, TopoScriptCompiler}
import data.StationNode
import enums.AttributeValue
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class ManagementDomainRidePolicyTest extends AnyFunSuite with Matchers {
  private def topoMap(name: String, overrideDomain: Option[String] = None): String = {
    val overrideClause = overrideDomain.map(domain => s" override-managed-by $domain").getOrElse("")
    s"""
       |topo-map $name()$overrideClause {
       |  let params: {area: Int} = {area = 1}
       |  topo-node hall
       |}
       |""".stripMargin
  }

  private def elevator(extraLines: String): String =
    s"""
       |transport Lift is Elevator {
       |  let params: {maxVelocity: Float, acceleration: Float, carAmount: Int} = {
       |    maxVelocity = 2.0,
       |    acceleration = 0.8,
       |    carAmount = 2
       |  }
       |  station Lobby at Lobby::hall {location = 0.0, departureRate = 0.1}
       |  station Office at OfficeFloor::hall {location = 4.0, departureRate = 0.1}
       |  station Hotel at HotelFloor::hall {location = 8.0, departureRate = 0.1}
       |  $extraLines
       |}
       |""".stripMargin

  private def compile(
    configuration: String,
    transport: String,
    root: String,
    mapOverrides: Map[String, Option[String]] = Map.empty,
    params: Map[String, AnyRef] = Map.empty
  ): CompilationResult = {
    val mapNames = List("Lobby", "OfficeFloor", "HotelFloor")
    val files = Map(
      "configuration.tcfg" -> configuration,
      "root" -> root,
      "Lift.ttr" -> transport
    ) ++ mapNames.map { name =>
      s"$name.tmap" -> topoMap(name, mapOverrides.getOrElse(name, None))
    }

    new TopoScriptCompiler().compileProject(files.asJava, params.asJava)
  }

  private def station(result: CompilationResult, floor: String): StationNode =
    result.transportGraph.nodes.find(_.identifier == s"Lift@$floor")
      .getOrElse(fail(s"Missing Lift station for $floor"))

  private def hasRide(result: CompilationResult, source: String, target: String): Boolean = {
    val sourceNode = station(result, source)
    val targetNode = station(result, target)
    result.transportGraph.adjacencyList.getOrElse(sourceNode, Map.empty).contains(targetNode)
  }

  private val configuration =
    """
      |building-includes {
      |  submap Lobby managed-by Public
      |  submap OfficeFloor managed-by Office
      |  submap HotelFloor managed-by Hotel
      |  vehicle Lift
      |}
      |""".stripMargin

  private val root =
    """
      |root RidePolicyTests(allowed: Bool, special: Bool) {
      |  constraint Allowed { require allowed }
      |  constraint Special { require special }
      |}
      |""".stripMargin

  test("root parameter lists may span multiple lines") {
    val compiler = new TopoScriptCompiler()
    val multilineRoot = compiler.parseRootFile(
      """
        |root MultilineRoot(
        |  first: Bool,
        |  second: Int
        |) {}
        |""".stripMargin
    )
    val singleLineRoot = compiler.parseRootFile("root SingleLineRoot(first: Bool, second: Int) {}")

    multilineRoot.params.map(_._1) shouldBe List("first", "second")
    singleLineRoot.params.map(_._1) shouldBe List("first", "second")
  }

  test("configuration, topo-map override, and ride-from syntax are retained by the surface AST") {
    val compiler = new TopoScriptCompiler()
    val config = compiler.parseConfigFile(
      """
        |building-includes {
        |  submap OfficeFloor using StandardOffice managed-by Office
        |  vehicle Lift
        |}
        |""".stripMargin
    )
    config.configuredManagementDomains shouldBe Map("OfficeFloor" -> "Office")
    config.submapUsages.values.flatten.toList should contain("OfficeFloor")

    val map = compiler.parseMapFile(topoMap("OfficeFloor", Some("Hotel")))
    map.managementDomainOverride shouldBe Some("Hotel")

    val transport = compiler.parseTransportFile(
      """
        |transport Lift is Elevator {
        |  ride-from Office to Hotel requires <Allowed && Special>
        |}
        |""".stripMargin
    )
    transport.ridePolicies.map(policy => (policy.source, policy.target, policy.constraints.size)) shouldBe
      List(("Office", "Hotel", 2))
  }

  test("on Depart and on Arrive constraints create directional station permissions") {
    val directionalElevator =
      """
        |transport Lift is Elevator {
        |  let params: {maxVelocity: Float, acceleration: Float, carAmount: Int} = {
        |    maxVelocity = 2.0,
        |    acceleration = 0.8,
        |    carAmount = 2
        |  }
        |  station Lobby at Lobby::hall {location = 0.0, departureRate = 0.1}
        |  station Office at OfficeFloor::hall {location = 4.0, departureRate = 0.1} requires Allowed on Depart
        |  station Hotel at HotelFloor::hall {location = 8.0, departureRate = 0.1} requires Allowed on Arrive
        |}
        |""".stripMargin
    val result = compile(
      configuration,
      directionalElevator,
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.FALSE
      )
    )

    hasRide(result, "OfficeFloor", "Lobby") shouldBe false
    hasRide(result, "Lobby", "OfficeFloor") shouldBe true
    hasRide(result, "Lobby", "HotelFloor") shouldBe false
    hasRide(result, "HotelFloor", "Lobby") shouldBe true
  }

  test("an unscoped failing station constraint removes arrival and departure rides") {
    val restrictedElevator =
      """
        |transport Lift is Elevator {
        |  let params: {maxVelocity: Float, acceleration: Float, carAmount: Int} = {
        |    maxVelocity = 2.0,
        |    acceleration = 0.8,
        |    carAmount = 2
        |  }
        |  station Lobby at Lobby::hall {location = 0.0, departureRate = 0.1}
        |  station Office at OfficeFloor::hall {location = 4.0, departureRate = 0.1} requires Allowed
        |  station Hotel at HotelFloor::hall {location = 8.0, departureRate = 0.1}
        |}
        |""".stripMargin
    val result = compile(
      configuration,
      restrictedElevator,
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.FALSE
      )
    )

    hasRide(result, "OfficeFloor", "Lobby") shouldBe false
    hasRide(result, "Lobby", "OfficeFloor") shouldBe false
  }

  test("an out-of-order station allows neither departure nor arrival") {
    val outOfOrderElevator =
      """
        |transport Lift is Elevator {
        |  let params: {maxVelocity: Float, acceleration: Float, carAmount: Int} = {
        |    maxVelocity = 2.0,
        |    acceleration = 0.8,
        |    carAmount = 2
        |  }
        |  station Lobby at Lobby::hall {location = 0.0, departureRate = 0.1}
        |  station Office at OfficeFloor::hall {location = 4.0, departureRate = 0.1} out-of-order
        |  station Hotel at HotelFloor::hall {location = 8.0, departureRate = 0.1}
        |}
        |""".stripMargin
    val result = compile(
      configuration,
      outOfOrderElevator,
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.TRUE,
        "special" -> java.lang.Boolean.TRUE
      )
    )

    hasRide(result, "OfficeFloor", "Lobby") shouldBe false
    hasRide(result, "Lobby", "OfficeFloor") shouldBe false
    hasRide(result, "Lobby", "HotelFloor") shouldBe true
  }

  test("a failing domain ride policy removes only matching directed rides") {
    val result = compile(
      configuration,
      elevator("ride-from Office to Hotel requires Allowed"),
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.FALSE
      )
    )

    hasRide(result, "OfficeFloor", "HotelFloor") shouldBe false
    hasRide(result, "HotelFloor", "OfficeFloor") shouldBe true
    hasRide(result, "OfficeFloor", "Lobby") shouldBe true
  }

  test("any matches every source station on the transport") {
    val result = compile(
      configuration,
      elevator("ride-from any to Hotel requires Allowed"),
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.FALSE
      )
    )

    hasRide(result, "Lobby", "HotelFloor") shouldBe false
    hasRide(result, "OfficeFloor", "HotelFloor") shouldBe false
    hasRide(result, "HotelFloor", "Lobby") shouldBe true
  }

  test("an exact-submap ride policy overrides a broader domain policy") {
    val result = compile(
      configuration,
      elevator(
        """ride-from Office to Hotel requires Allowed
          |  ride-from OfficeFloor to Hotel requires Special""".stripMargin
      ),
      root,
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.TRUE
      )
    )

    hasRide(result, "OfficeFloor", "HotelFloor") shouldBe true
  }

  test("topo-map overrides determine effective domains and unspecified domains default to Misc") {
    val configWithDefault =
      """
        |building-includes {
        |  submap Lobby managed-by Public
        |  submap OfficeFloor managed-by Office
        |  submap HotelFloor
        |  vehicle Lift
        |}
        |""".stripMargin
    val result = compile(
      configWithDefault,
      elevator("ride-from Public to Hotel requires Allowed"),
      root,
      mapOverrides = Map("OfficeFloor" -> Some("Hotel")),
      params = Map(
        "allowed" -> java.lang.Boolean.FALSE,
        "special" -> java.lang.Boolean.FALSE
      )
    )

    hasRide(result, "Lobby", "OfficeFloor") shouldBe false
    result.metadata("managementDomain.OfficeFloor") shouldBe AttributeValue.StringValue("Hotel")
    result.metadata("managementDomain.HotelFloor") shouldBe AttributeValue.StringValue("Misc")
  }

  test("management domain names may not collide with submap names") {
    val invalidConfiguration =
      """
        |building-includes {
        |  submap Lobby managed-by Public
        |  submap OfficeFloor managed-by Lobby
        |  submap HotelFloor managed-by Hotel
        |  vehicle Lift
        |}
        |""".stripMargin

    val error = intercept[RuntimeException] {
      compile(
        invalidConfiguration,
        elevator(""),
        root,
        params = Map(
          "allowed" -> java.lang.Boolean.TRUE,
          "special" -> java.lang.Boolean.TRUE
        )
      )
    }
    error.getMessage should include("Management domain names must not equal submap names: Lobby")
  }

  test("unknown ride-from operands fail compilation") {
    val error = intercept[RuntimeException] {
      compile(
        configuration,
        elevator("ride-from UnknownDomain to Hotel requires Allowed"),
        root,
        params = Map(
          "allowed" -> java.lang.Boolean.TRUE,
          "special" -> java.lang.Boolean.TRUE
        )
      )
    }
    error.getMessage should include("Unknown ride-from operand 'UnknownDomain'")
  }
}
