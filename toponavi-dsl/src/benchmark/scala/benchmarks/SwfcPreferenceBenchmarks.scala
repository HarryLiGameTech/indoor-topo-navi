package benchmarks

import api.{NavigationRequestException, TopoNaviService}
import java.nio.file.{Files, Path}
import java.io.PrintStream
import scala.jdk.CollectionConverters.*
import scala.util.Using

// Opt-in SWFC reproduction for native objectives and the former label-limit failures.
object SwfcPreferenceBenchmarks {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, "Usage: swfcPreferenceBenchmark --args='/path/to/swfc'")
    val project = Path.of(args(0))
    val files = Using.resource(Files.list(project)) { paths =>
      paths.iterator.asScala.filter(Files.isRegularFile(_)).filter { file =>
        val name = file.getFileName.toString
        !name.contains(".") || List(".tcfg", ".tmap", ".ttr").exists(name.endsWith)
      }.map(file => file.getFileName.toString -> Files.readString(file)).toMap.asJava
    }
    val params: Map[String, AnyRef] = Map(
      "haveStaffCard" -> java.lang.Boolean.FALSE, "haveManagementCard" -> java.lang.Boolean.FALSE,
      "haveRoomKey" -> java.lang.Boolean.FALSE, "haveHotelStaffCard" -> java.lang.Boolean.FALSE,
      "id" -> Long.box(0), "aggregatedWeight" -> Long.box(200))
    val compiled = Using.resource(new PrintStream(java.io.OutputStream.nullOutputStream())) { output =>
      Console.withOut(output)(TopoNaviService.compile(files, params.asJava))
    }
    val source = "LowerLobby::gate_1"
    val custom = TopoNaviService.customRouteObjective("tagExposure", "audit-nonexistent-tag", 0, null)
    println("mode,destination,preference,minimizeTag,iteration,milliseconds,seconds,transfers,customCost")
    for {
      highRise <- List(true, false)
      destination <- List("Floor98::staircase_U2", "SkyWalk100::skywalk_booth_1")
      (preference, tag) <- List("MinimizeTransfers" -> null, "MinimizeTransfers" -> "audit-nonexistent-tag",
        "MinimizeCustomData" -> null, "MinimizeTime" -> null, "MinimizePhysicalDemands" -> null)
    } {
      val objective = if (preference == "MinimizeCustomData") custom else null
      val paths = (0 to 3).map { iteration =>
        val start = System.nanoTime()
        val path = TopoNaviService.findRoutePlanWithRiskPreference(compiled, source, destination,
          preference, List.empty[String].asJava, highRise, "conservative", tag, objective).plan
        val elapsed = (System.nanoTime() - start) / 1e6
        require(path.routeNodes.distinct == path.routeNodes, "Route must not repeat a physical node")
        require(path.routeNodes.head.owningGraph.identifier == "LowerLobby")
        require(path.routeNodes.last.owningGraph.identifier == destination.split("::")(0))
        require(path.uncertainAccess.isEmpty, "A deterministic route exists for the public destinations")
        if (preference == "MinimizeTransfers" || preference == "MinimizeCustomData")
          require(path.transferCount <= 2, "A known public route uses two line changes")
        if (iteration > 0) {
          val mode = if (highRise) "partiallyInformed" else "fullyInformed"
          val score = if (objective == null) "" else objective.cost.total(path).toString
          println(s"$mode,$destination,$preference,${Option(tag).getOrElse("")},$iteration,$elapsed,${path.totalCost},${path.transferCount},$score")
        }
        path
      }
      require(paths.map(_.routeNodes).distinct.size == 1, "Repeated requests must select the same route")
    }
    for highRise <- List(true, false) do {
      try {
        TopoNaviService.findRoutePlanWithRiskPreference(compiled, source, "Floor7::L1_hall",
          "MinimizeTransfers", List.empty[String].asJava, highRise, "conservative",
          "audit-nonexistent-tag")
        throw new IllegalStateException("An all-false visitor must not enter Floor7::L1_hall")
      } catch {
        case error: NavigationRequestException => require(error.getCode == "NO_ROUTE_FOUND", error.getMessage)
      }
    }
    println("SWFC preference checks passed")
  }
}
