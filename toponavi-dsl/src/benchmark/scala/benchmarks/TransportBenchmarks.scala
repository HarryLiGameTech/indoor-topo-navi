package benchmarks

import api.{NavigationRequestException, TopoNaviService}
import compiler.CompilationResult
import data.NavigationOutputPath
import enums.RoutePlanningPreferences.MinimizeTime

import java.io.{OutputStream, PrintStream, PrintWriter}
import java.lang.management.{ManagementFactory, MemoryType}
import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.Instant
import java.util.Properties
import scala.jdk.CollectionConverters.*

object TransportBenchmarks {
  private case class Settings(
    profile: String,
    warmups: Int,
    iterations: Int,
    queries: Int,
    scenarios: Set[String],
    output: Path
  )

  private case class MemorySnapshot(heapUsed: Long, gcCount: Long, gcMillis: Long)

  private val heapPools = ManagementFactory.getMemoryPoolMXBeans.asScala.filter(_.getType == MemoryType.HEAP)
  private val collectors = ManagementFactory.getGarbageCollectorMXBeans.asScala
  private def memory(): MemorySnapshot = MemorySnapshot(
    ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getUsed,
    collectors.map(_.getCollectionCount).filter(_ >= 0).sum,
    collectors.map(_.getCollectionTime).filter(_ >= 0).sum
  )

  private def percentile(values: Vector[Double], fraction: Double): Double =
    values.sorted.apply(math.max(0, math.ceil(values.size * fraction).toInt - 1))

  private def csv(writer: PrintWriter, values: Any*): Unit = {
    writer.println(values.map { value =>
      "\"" + value.toString.replace("\"", "\"\"") + "\""
    }.mkString(","))
    writer.flush()
    require(!writer.checkError(), "Could not write benchmark report")
  }

  def main(args: Array[String]): Unit = {
    if (args.contains("--help")) {
      println("Transport benchmark testers (opt-in, separate from unit tests)")
      println("--profile smoke|standard|stress --warmups N --iterations N --queries N --scenarios name,name --output DIRECTORY")
      println("Defaults: standard; 2 warmups, 5 iterations, 12 queries. Smoke: 1 warmup, 1 iteration, 4 queries.")
      return
    }
    val settings = parse(args)
    val available = TransportScenarios.forProfile(settings.profile)
    require(settings.scenarios.subsetOf(available.map(_.name).toSet),
      s"Unknown scenarios; choose from ${available.map(_.name).mkString(", ")}")
    val scenarios = available.filter(s => settings.scenarios.isEmpty || settings.scenarios.contains(s.name))
    Files.createDirectories(settings.output)
    writeEnvironment(settings)
    val summary = new PrintWriter(Files.newBufferedWriter(settings.output.resolve("summary.csv"), StandardOpenOption.CREATE_NEW))
    val samples = new PrintWriter(Files.newBufferedWriter(settings.output.resolve("samples.csv"), StandardOpenOption.CREATE_NEW))
    val quietOutput = new PrintStream(OutputStream.nullOutputStream())
    try {
      csv(summary, "scenario", "profile", "floors", "lines", "stations", "ride_edges", "transfer_edges",
        "walking_edges", "out_of_order_lines", "access_denied_lines", "phase", "samples",
        "p50_ms", "p95_ms", "max_ms", "heap_before_bytes", "heap_after_bytes", "heap_pool_peak_sum_bytes",
        "gc_count", "gc_ms", "no_route_results", "max_gap_percent")
      csv(samples, "scenario", "phase", "iteration", "query", "start", "destination", "elapsed_ms",
        "outcome", "route_cost_seconds", "reference_cost_seconds", "gap_percent")
      println(s"Transport benchmark testers: ${settings.profile}, ${scenarios.size} scenarios")
      println(s"Reports: ${settings.output.toAbsolutePath}")
      scenarios.foreach { scenario =>
        runScenario(scenario, settings, summary, samples, quietOutput)
      }
      Files.writeString(settings.output.resolve("SUCCESS"), "All benchmark correctness checks passed.\n", StandardOpenOption.CREATE_NEW)
    } finally {
      summary.close()
      samples.close()
      quietOutput.close()
    }
  }

  private def runScenario(
    scenario: TransportScenario,
    settings: Settings,
    summary: PrintWriter,
    samples: PrintWriter,
    quietOutput: PrintStream
  ): Unit = {
    println(s"Running ${scenario.name}: ${scenario.floors} floors, ${scenario.lines.size} transport lines")
    scenario.files // Generate source files before measuring compilation.
    def compile(): CompilationResult = Console.withOut(quietOutput)(scenario.compile())
    for (_ <- 0 until settings.warmups) compile()

    var compilation: CompilationResult = null
    heapPools.foreach(_.resetPeakUsage())
    val compileMemory = memory()
    val compileTimes = Vector.tabulate(settings.iterations) { iteration =>
      val start = System.nanoTime()
      compilation = compile()
      val elapsed = (System.nanoTime() - start) / 1e6
      csv(samples, scenario.name, "compile", iteration, "", "", "", elapsed, "compiled", "", "", "")
      elapsed
    }
    report(summary, scenario, settings, compilation, "compile", compileTimes, compileMemory, 0, 0.0)

    val reference = new TransportReference(compilation, scenario)
    val queries = scenario.queries(settings.queries)
    val expected = queries.map { case (start, goal) => reference.shortestCost(start, goal) }
    Seq(false, true).foreach { highRise =>
      val phase = if (highRise) "route-high-rise" else "route-low-rise"
      def route(start: String, goal: String): Option[NavigationOutputPath] = Console.withOut(quietOutput) {
        try Some(TopoNaviService.findRoutePlan(compilation, start, goal, MinimizeTime, Set.empty, highRise))
        catch {
          case error: NavigationRequestException if error.getCode == "NO_ROUTE_FOUND" => None
        }
      }
      for (_ <- 0 until settings.warmups; ((start, goal), index) <- queries.zipWithIndex) {
        reference.validate(start, goal, expected(index), route(start, goal), highRise)
      }
      heapPools.foreach(_.resetPeakUsage())
      val routeMemory = memory()
      var noRouteCount = 0
      var maxGap = 0.0
      val times = (for
        iteration <- 0 until settings.iterations
        ((start, goal), index) <- queries.zipWithIndex
      yield {
        val before = System.nanoTime()
        val result = route(start, goal)
        val elapsed = (System.nanoTime() - before) / 1e6
        val gap = reference.validate(start, goal, expected(index), result, highRise)
        if (result.isEmpty) noRouteCount += 1
        maxGap = math.max(maxGap, gap)
        csv(samples, scenario.name, phase, iteration, index, start, goal, elapsed,
          if (result.isDefined) "route" else "NO_ROUTE_FOUND",
          result.map(_.totalCost.toString).getOrElse(""), expected(index).map(_.toString).getOrElse(""), gap)
        elapsed
      }).toVector
      report(summary, scenario, settings, compilation, phase, times, routeMemory, noRouteCount, maxGap)
    }
  }

  private def report(
    writer: PrintWriter,
    scenario: TransportScenario,
    settings: Settings,
    result: CompilationResult,
    phase: String,
    times: Vector[Double],
    before: MemorySnapshot,
    noRouteCount: Int,
    maxGap: Double
  ): Unit = {
    val after = memory()
    val peak = heapPools.map(_.getPeakUsage.getUsed).sum
    val edges = result.transportGraph.adjacencyList.toVector.flatMap { case (from, neighbors) =>
      neighbors.keys.map(to => from -> to)
    }
    val rides = edges.count { case (from, to) => from.ownerLine == to.ownerLine }
    val p50 = percentile(times, 0.5)
    val p95 = percentile(times, 0.95)
    csv(writer, scenario.name, settings.profile, scenario.floors, scenario.lines.size,
      result.transportGraph.nodes.size, rides, edges.size - rides,
      result.graphs.values.map(_.adjacencyList.size).sum,
      scenario.lines.count(_.outOfOrder), scenario.lines.count(line => line.restricted && !scenario.accessGranted),
      phase, times.size, p50, p95, times.max, before.heapUsed, after.heapUsed, peak,
      after.gcCount - before.gcCount, after.gcMillis - before.gcMillis, noRouteCount, maxGap)
    println(f"  $phase: n=${times.size}%d, p50=$p50%.3f ms, p95=$p95%.3f ms, max gap=$maxGap%.3f%%")
  }

  private def parse(args: Array[String]): Settings = {
    require(args.length % 2 == 0, "Options need values; use --help")
    val pairs = args.grouped(2).map(pair => pair(0) -> pair(1)).toVector
    require(pairs.map(_._1).distinct.size == pairs.size, "Duplicate benchmark option")
    val options = pairs.toMap
    val names = Set("--profile", "--warmups", "--iterations", "--queries", "--scenarios", "--output")
    require(options.keySet.subsetOf(names), "Unknown benchmark option; use --help")
    val profile = options.getOrElse("--profile", "standard")
    val smoke = profile == "smoke"
    val warmups = options.get("--warmups").map(_.toInt).getOrElse(if (smoke) 1 else 2)
    val iterations = options.get("--iterations").map(_.toInt).getOrElse(if (smoke) 1 else 5)
    val queries = options.get("--queries").map(_.toInt).getOrElse(if (smoke) 4 else 12)
    require(warmups >= 0 && iterations > 0 && queries >= 4, "Need warmups >= 0, iterations > 0, queries >= 4")
    val output = options.get("--output").map(Path.of(_)).getOrElse(Path.of(
      "toponavi-dsl/build/reports/benchmarks/transport", s"$profile-${Instant.now().toString.replace(':', '-')}"))
    Settings(profile, warmups, iterations, queries,
      options.get("--scenarios").map(_.split(',').toSet).getOrElse(Set.empty), output)
  }

  private def writeEnvironment(settings: Settings): Unit = {
    val environment = new Properties()
    Map(
      "timestamp_utc" -> Instant.now().toString,
      "java_version" -> System.getProperty("java.version"),
      "java_vm" -> System.getProperty("java.vm.name"),
      "jvm_arguments" -> ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.mkString(" "),
      "os" -> System.getProperty("os.name"),
      "os_version" -> System.getProperty("os.version"),
      "architecture" -> System.getProperty("os.arch"),
      "available_processors" -> Runtime.getRuntime.availableProcessors().toString,
      "max_heap_bytes" -> Runtime.getRuntime.maxMemory().toString,
      "profile" -> settings.profile,
      "warmups" -> settings.warmups.toString,
      "iterations" -> settings.iterations.toString,
      "queries_per_iteration" -> settings.queries.toString,
      "query_seed" -> "611",
      "scenarios" -> settings.scenarios.toVector.sorted.mkString(",")
    ).foreach { case (key, value) => environment.setProperty(key, value) }
    val output = Files.newBufferedWriter(settings.output.resolve("environment.properties"), StandardOpenOption.CREATE_NEW)
    try environment.store(output, "Transport benchmark environment") finally output.close()
  }
}
