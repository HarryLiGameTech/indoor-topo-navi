package benchmarks

import compiler.{CompilationResult, TopoScriptCompiler}

import scala.jdk.CollectionConverters.*
import scala.util.Random

case class BenchmarkLine(
  name: String,
  floors: Vector[Int],
  kind: String = "Elevator",
  restricted: Boolean = false,
  outOfOrder: Boolean = false
)

case class TransportScenario(
  name: String,
  floors: Int,
  lines: Vector[BenchmarkLine],
  accessGranted: Boolean = true,
  expectRoute: Boolean = true
) {
  val unavailableLines: Set[String] = lines.filter { line =>
    line.outOfOrder || (line.restricted && !accessGranted)
  }.map(_.name).toSet

  lazy val files: java.util.Map[String, String] = {
    val maps = (0 until floors).map { floor =>
      val servingLines = lines.filter(_.floors.contains(floor))
      val landings = servingLines.zipWithIndex.map { case (line, index) =>
        s"""  topo-node ${line.name}
           |  atomic-path [hub <-> ${line.name}] {cost = ${1 + index % 5}}""".stripMargin
      }.mkString("\n")
      s"Floor$floor.tmap" -> s"""
        |topo-map Floor$floor() {
        |  let params = {area = 100, permResident = 40}
        |  topo-node hub
        |$landings
        |}
        |""".stripMargin
    }
    val transports = lines.map { line =>
      val params = line.kind match {
        case "Stairs" => "turnBackCost = 3"
        case "Escalator" => "time = 20.0"
        case _ => "maxVelocity = 3.0, acceleration = 0.8, capacity = 18, carAmount = 3, duty = 1600"
      }
      val stations = line.floors.map { floor =>
        val fields = line.kind match {
          case "Stairs" => s"location = ${floor * 4}.0, directSegmentIndex = ${floor * 2}"
          case "Escalator" => ""
          case _ => s"location = ${floor * 4}.0, departureRate = 0.05"
        }
        val restriction = if (line.outOfOrder) "out-of-order"
          else if (line.restricted) "requires StaffAccess" else ""
        s"  station Stop$floor at Floor$floor::${line.name} {$fields} $restriction"
      }.mkString("\n")
      s"${line.name}.ttr" -> s"""
        |transport ${line.name} is ${line.kind} {
        |  let params = {$params}
        |$stations
        |}
        |""".stripMargin
    }
    (maps ++ transports ++ Seq(
      "root" -> "root Benchmarks(haveStaffCard: Bool) { constraint StaffAccess { require haveStaffCard } }",
      "configuration.tcfg" -> s"""
        |building-includes {
        |${(0 until floors).map(f => s"  submap Floor$f").mkString("\n")}
        |${lines.map(line => s"  vehicle ${line.name}").mkString("\n")}
        |}
        |""".stripMargin
    )).toMap.asJava
  }

  def compile(): CompilationResult = new TopoScriptCompiler().compileProject(
    files, Map("haveStaffCard" -> java.lang.Boolean.valueOf(accessGranted).asInstanceOf[AnyRef]).asJava
  )

  def queries(count: Int): Vector[(String, String)] = {
    val random = new Random(611)
    val endpoints = Vector(0 -> (floors - 1), (floors - 1) -> 0,
      0 -> (floors / 2), (floors / 2) -> (floors - 1)) ++
      Vector.fill(count - 4) {
        val from = random.nextInt(floors)
        val to = (from + 1 + random.nextInt(floors - 1)) % floors
        from -> to
      }
    endpoints.map { case (from, to) => s"Floor$from::hub" -> s"Floor$to::hub" }
  }
}

object TransportScenarios {
  def forProfile(profile: String): Vector[TransportScenario] = {
    val (small, medium, large, banks, zoneSize, zones) = profile match {
      case "smoke" => (4, 6, 8, 3, 4, 2)
      case "standard" => (8, 16, 32, 10, 8, 4)
      case "stress" => (16, 32, 64, 16, 8, 8)
      case _ => throw new IllegalArgumentException(s"Unknown profile '$profile'; use smoke, standard, or stress")
    }
    def elevators(floors: Int, count: Int): Vector[BenchmarkLine] =
      Vector.tabulate(count)(i => BenchmarkLine(s"Lift$i", (0 until floors).toVector))

    val mediumBanks = math.max(2, banks / 2)
    val restricted = elevators(medium, mediumBanks).zipWithIndex.map { case (line, index) =>
      line.copy(restricted = index % 2 == 0)
    }
    val zoned = (0 until zones).toVector.flatMap { zone =>
      Vector.tabulate(3) { bank =>
        BenchmarkLine(s"Zone${zone}Lift$bank", (zone * zoneSize to (zone + 1) * zoneSize).toVector)
      }
    }
    val mixed = elevators(medium, mediumBanks) ++ Vector(
      BenchmarkLine("Stairs", (0 until medium).toVector, "Stairs")
    ) ++ Vector.tabulate(medium - 1)(f => BenchmarkLine(s"Escalator$f", Vector(f, f + 1), "Escalator"))

    Vector(
      TransportScenario("dense-small", small, elevators(small, 2)),
      TransportScenario("dense-medium", medium, elevators(medium, mediumBanks)),
      TransportScenario("dense-large", large, elevators(large, banks)),
      TransportScenario("zoned-transfers", zoneSize * zones + 1, zoned),
      TransportScenario("mixed-transports", medium, mixed),
      TransportScenario("access-granted", medium, restricted),
      TransportScenario("access-partially-denied", medium, restricted, accessGranted = false),
      TransportScenario("partial-outage", medium, elevators(medium, mediumBanks).zipWithIndex.map {
        case (line, index) => line.copy(outOfOrder = index % 2 == 0)
      }),
      TransportScenario("all-access-denied", small, elevators(small, 3).map(_.copy(restricted = true)),
        accessGranted = false, expectRoute = false),
      TransportScenario("all-out-of-order", small, elevators(small, 3).map(_.copy(outOfOrder = true)),
        expectRoute = false)
    )
  }
}
