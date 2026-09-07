import api.TopoNaviService
import data.TransportGraph
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

class SwfcTransportGraphGroundTruthTest extends AnyFunSuite {
  import SwfcTransportGraphGroundTruth.*

  test("SWFC transport graph matches ground-truth-for-test") {
    val swfcDirectory = locateSwfcDirectory()
    val compileStartedAt = System.nanoTime()
    val actualGraph = compileSwfc(swfcDirectory)
    val compileElapsedSeconds = (System.nanoTime() - compileStartedAt).toDouble / 1_000_000_000.0
    val fixturePath = fixturePathFor(swfcDirectory)

    println(f"Compiled SWFC transport graph in $compileElapsedSeconds%.3f seconds")

    if (sys.env.get("UPDATE_SWFC_TRANSPORT_GROUND_TRUTH").contains("1")) {
      Files.createDirectories(fixturePath.getParent)
      Files.writeString(fixturePath, render(actualGraph), StandardCharsets.UTF_8)
      info(s"Updated SWFC transport graph ground truth at ${fixturePath.toAbsolutePath}")
    }

    assert(
      Files.isRegularFile(fixturePath),
      s"Missing SWFC ground truth fixture: ${fixturePath.toAbsolutePath}. " +
        "Regenerate it explicitly with UPDATE_SWFC_TRANSPORT_GROUND_TRUTH=1."
    )

    val expected = parse(Files.readString(fixturePath, StandardCharsets.UTF_8))
    val actual = snapshot(actualGraph)

    val missingNodes = expected.nodes -- actual.nodes
    val extraNodes = actual.nodes -- expected.nodes
    assert(
      missingNodes.isEmpty && extraNodes.isEmpty,
      connectivityFailure("station nodes", missingNodes, extraNodes)
    )

    val expectedConnectivity = expected.edgeCosts.keySet
    val actualConnectivity = actual.edgeCosts.keySet
    val missingEdges = expectedConnectivity -- actualConnectivity
    val extraEdges = actualConnectivity -- expectedConnectivity
    assert(
      missingEdges.isEmpty && extraEdges.isEmpty,
      connectivityFailure("directed edges", missingEdges, extraEdges)
    )

    val timeDifferences = expected.edgeCosts.toList.flatMap { case (edge, expectedSeconds) =>
      actual.edgeCosts.get(edge).map { actualSeconds =>
        (edge, expectedSeconds, actualSeconds, math.abs(actualSeconds - expectedSeconds))
      }
    }
    val maximumTimeDifference = timeDifferences.map(_._4).maxOption.getOrElse(0.0)
    println(
      f"Compared ${timeDifferences.size} directed edge times; " +
        f"maximum absolute difference was $maximumTimeDifference%.6f seconds"
    )

    val timeViolations = timeDifferences.flatMap { case (edge, expectedSeconds, actualSeconds, difference) =>
      Option.when(
        !expectedSeconds.isFinite || !actualSeconds.isFinite || difference > EdgeTimeToleranceSeconds
      )(
        f"${edge.source} -> ${edge.target}: expected=$expectedSeconds%.6f s, " +
          f"actual=$actualSeconds%.6f s, difference=$difference%.6f s"
      )
    }

    assert(
      timeViolations.isEmpty,
      f"${timeViolations.size} transport edge(s) exceed the $EdgeTimeToleranceSeconds%.1f-second tolerance:\n" +
        timeViolations.take(MaxReportedDifferences).mkString("\n")
    )
  }
}

object SwfcTransportGraphGroundTruth {
  val EdgeTimeToleranceSeconds = 2.0
  val MaxReportedDifferences = 20
  private val FixtureRelativePath = Paths.get(
    "toponavi-dsl",
    "src",
    "test",
    "resources",
    "ground-truth-for-test",
    "swfc-transport-graph.tsv"
  )

  case class EdgeKey(source: String, target: String)
  case class GraphSnapshot(nodes: Set[String], edgeCosts: Map[EdgeKey, Double])

  private val CompilationParameters: Map[String, AnyRef] = Map(
    "haveStaffCard" -> java.lang.Boolean.TRUE,
    "haveManagementCard" -> java.lang.Boolean.TRUE,
    "haveRoomKey" -> java.lang.Boolean.TRUE,
    "id" -> java.lang.Integer.valueOf(1919810),
    "aggregatedWeight" -> java.lang.Integer.valueOf(10)
  )

  def locateSwfcDirectory(): Path = {
    val candidates = List(
      Paths.get("examples", "swfc"),
      Paths.get("..", "examples", "swfc")
    ).map(_.toAbsolutePath.normalize())

    candidates.find(Files.isDirectory(_)).getOrElse {
      throw new AssertionError(
        s"examples/swfc not found. Checked: ${candidates.mkString(", ")}"
      )
    }
  }

  def fixturePathFor(swfcDirectory: Path): Path = {
    val repositoryRoot = swfcDirectory.getParent.getParent
    repositoryRoot.resolve(FixtureRelativePath)
  }

  def compileSwfc(swfcDirectory: Path): TransportGraph = {
    val files = mutable.Map.empty[String, String]
    Using.resource(Files.walk(swfcDirectory)) { paths =>
      paths.iterator().asScala
        .filter(Files.isRegularFile(_))
        .foreach { file =>
          val relativePath = swfcDirectory.relativize(file).toString.replace('\\', '/')
          files(relativePath) = Files.readString(file, StandardCharsets.UTF_8)
        }
    }

    TopoNaviService
      .compile(files.asJava, CompilationParameters.asJava)
      .transportGraph
  }

  def snapshot(graph: TransportGraph): GraphSnapshot = {
    val nodeIdentifiers = graph.nodes.map(_.identifier)
    require(
      nodeIdentifiers.distinct.size == nodeIdentifiers.size,
      "Transport graph contains duplicate station-node identifiers"
    )

    val edges = graph.adjacencyList.iterator.flatMap { case (source, outgoing) =>
      outgoing.iterator.map { case (target, cost) =>
        EdgeKey(source.identifier, target.identifier) -> cost
      }
    }.toMap

    GraphSnapshot(nodeIdentifiers.toSet, edges)
  }

  def render(graph: TransportGraph): String = {
    val graphSnapshot = snapshot(graph)
    val nodeLines = graphSnapshot.nodes.toList.sorted.map(identifier => s"NODE\t$identifier")
    val edgeLines = graphSnapshot.edgeCosts.toList
      .sortBy { case (edge, _) => edge.source -> edge.target }
      .map { case (edge, cost) =>
        s"EDGE\t${edge.source}\t${edge.target}\t${java.lang.Double.toString(cost)}"
      }

    (
      List(
        "# TopoNavi transport graph ground truth",
        "# fixture: ground-truth-for-test",
        "# source: examples/swfc",
        "# parameters: haveStaffCard=true, haveManagementCard=true, haveRoomKey=true, id=1919810, aggregatedWeight=10",
        "# comparison: exact station nodes and directed connectivity; absolute edge-time difference <= 2.0 seconds",
        s"# nodes: ${graphSnapshot.nodes.size}",
        s"# edges: ${graphSnapshot.edgeCosts.size}"
      ) ++ nodeLines ++ edgeLines
    ).mkString("\n") + "\n"
  }

  def parse(content: String): GraphSnapshot = {
    val nodes = mutable.Set.empty[String]
    val edgeCosts = mutable.Map.empty[EdgeKey, Double]

    content.linesIterator.zipWithIndex.foreach { case (line, zeroBasedIndex) =>
      val lineNumber = zeroBasedIndex + 1
      if (line.startsWith("NODE\t")) {
        val identifier = line.stripPrefix("NODE\t")
        require(identifier.nonEmpty, s"Empty node identifier at fixture line $lineNumber")
        require(nodes.add(identifier), s"Duplicate node '$identifier' at fixture line $lineNumber")
      } else if (line.startsWith("EDGE\t")) {
        line.split("\t", -1).toList match {
          case "EDGE" :: source :: target :: cost :: Nil =>
            val key = EdgeKey(source, target)
            require(source.nonEmpty && target.nonEmpty, s"Empty edge endpoint at fixture line $lineNumber")
            require(!edgeCosts.contains(key), s"Duplicate edge '$source -> $target' at fixture line $lineNumber")
            edgeCosts(key) = cost.toDouble
          case _ =>
            throw new IllegalArgumentException(s"Malformed edge record at fixture line $lineNumber: $line")
        }
      } else if (line.nonEmpty && !line.startsWith("#")) {
        throw new IllegalArgumentException(s"Unknown record at fixture line $lineNumber: $line")
      }
    }

    require(nodes.nonEmpty, "Ground truth contains no station nodes")
    require(edgeCosts.nonEmpty, "Ground truth contains no directed edges")
    GraphSnapshot(nodes.toSet, edgeCosts.toMap)
  }

  def connectivityFailure[A](
    itemType: String,
    missing: Set[A],
    extra: Set[A]
  ): String = {
    val missingText = missing.toList.sortBy(_.toString).take(MaxReportedDifferences).mkString(", ")
    val extraText = extra.toList.sortBy(_.toString).take(MaxReportedDifferences).mkString(", ")
    s"SWFC transport $itemType differ from ground truth. " +
      s"Missing (${missing.size}): [$missingText]. Extra (${extra.size}): [$extraText]."
  }
}
