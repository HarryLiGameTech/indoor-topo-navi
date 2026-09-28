import compiler.TopoScriptCompiler
import enums.VisitingMode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files
import scala.jdk.CollectionConverters.*

// Mixed addition, subtraction, and multiplication remain failing acceptance tests.
class ArithmeticCompilationTest extends AnyFunSuite with Matchers {
  private def compileCost(expression: String, root: Option[String] = None, fromDirectory: Boolean = false): Double = {
    val files = Map(
      "configuration" -> "building-includes { submap Concourse }",
      "Concourse" -> s"""topo-map Concourse() {
        |  let params = {}
        |  topo-node north_entrance
        |  topo-node platform_lobby
        |  atomic-path [north_entrance -> platform_lobby] {cost = $expression}
        |}""".stripMargin
    ) ++ root.map("root" -> _)
    val compiler = new TopoScriptCompiler()
    val result = if (!fromDirectory) compiler.compileProject(files.asJava)
    else {
      val directory = Files.createTempDirectory("toponavi-arithmetic-")
      try {
        files.foreach { case (name, source) => Files.writeString(directory.resolve(name), source) }
        compiler.compile(directory.toString)
      } finally {
        files.keys.foreach(name => Files.deleteIfExists(directory.resolve(name)))
        Files.deleteIfExists(directory)
      }
    }
    result.graphs("Concourse").adjacencyList.head.costs(VisitingMode.Normal)
  }

  Seq(
    "5 + 2" -> 7.0,
    "5 - 2" -> 3.0,
    "5 * 2" -> 10.0,
    "5.0 + 2.0" -> 7.0,
    "5.0 - 2.0" -> 3.0,
    "5.0 * 2.0" -> 10.0
  ).foreach { case (expression, expected) =>
    test(s"same-type arithmetic compiles and evaluates: $expression") {
      compileCost(expression) shouldBe expected
    }
  }

  Seq(
    "5 + 2.5" -> 7.5,
    "5.5 + 2" -> 7.5,
    "5 - 2.5" -> 2.5,
    "5.5 - 2" -> 3.5,
    "5 * 2.5" -> 12.5,
    "5.5 * 2" -> 11.0
  ).foreach { case (expression, expected) =>
    test(s"mixed Int/Float arithmetic compiles and evaluates: $expression") {
      compileCost(expression) shouldBe expected
    }
  }

  private val runningExample = """root RunningExample() {
    |  def timeWhenRushing(stdCost: Float, timeToRun1KmInSeconds: Int): Float = {
    |    stdCost * (timeToRun1KmInSeconds / 300)
    |  }
    |}""".stripMargin

  for (fromDirectory <- Seq(false, true)) {
    val entryPoint = if (fromDirectory) "directory" else "in-memory"

    // Fractional Int/Int division preserves the documented proportional calculation.
    Seq("240 / 300", "240.0 / 300.0", "240 / 300.0", "240.0 / 300").foreach { expression =>
      test(s"$entryPoint division preserves the proportional ratio: $expression") {
        compileCost(expression, fromDirectory = fromDirectory) shouldBe (0.8 +- 1e-12)
      }
    }

    Seq(150 -> 5.0, 240 -> 8.0, 300 -> 10.0, 450 -> 15.0).foreach { case (seconds, expected) =>
      test(s"$entryPoint timeWhenRushing scales cost for a $seconds second pace") {
        compileCost(s"timeWhenRushing(10.0, $seconds)", Some(runningExample), fromDirectory) shouldBe (expected +- 1e-12)
      }
    }

    test(s"$entryPoint compilation propagates division-by-zero errors") {
      intercept[ArithmeticException] {
        compileCost("1 / (2 - 2)", fromDirectory = fromDirectory)
      }.getMessage shouldBe "Division by zero"
    }

    test(s"$entryPoint compilation rejects negative costs calculated by division") {
      intercept[RuntimeException] {
        compileCost("-1 / 2", fromDirectory = fromDirectory)
      }.getMessage should include("cost must be non-negative")
    }
  }
}
