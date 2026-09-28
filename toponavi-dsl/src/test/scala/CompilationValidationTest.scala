import compiler.{CompilationResult, TopoScriptCompiler}
import enums.VisitingMode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files
import scala.jdk.CollectionConverters.*

class CompilationValidationTest extends AnyFunSuite with Matchers {
  private def compile(files: Map[String, String], fromDirectory: Boolean): CompilationResult = {
    val compiler = new TopoScriptCompiler()
    if (!fromDirectory) compiler.compileProject(files.asJava)
    else {
      val directory = Files.createTempDirectory("toponavi-validation-")
      try {
        files.foreach { case (name, source) => Files.writeString(directory.resolve(name), source) }
        compiler.compile(directory.toString)
      } finally {
        files.keys.foreach(name => Files.deleteIfExists(directory.resolve(name)))
        Files.deleteIfExists(directory)
      }
    }
  }

  private def mapSource(cost: String, arrow: String = "->"): String =
    s"""topo-map Floor() {
       |  let params = {permResident = 0}
       |  topo-node A
       |  topo-node B
       |  atomic-path [A $arrow B] {cost = $cost}
       |}
       |""".stripMargin

  private val invalidConfigurations = Seq(
    ("duplicate plain submaps", "submap Floor submap Floor", "Duplicate submap instance 'Floor'"),
    ("duplicate aliases of one base", "submap Floor using Base submap Floor using Base", "Duplicate submap instance 'Floor'"),
    ("duplicate aliases of different bases", "submap Floor using Base submap Floor using Other", "Duplicate submap instance 'Floor'"),
    ("plain then alias collision", "submap Floor submap Floor using Base", "Duplicate submap instance 'Floor'"),
    ("alias then plain collision", "submap Floor using Base submap Floor", "Duplicate submap instance 'Floor'"),
    ("duplicate submaps with different domains", "submap Floor managed-by Public submap Floor managed-by Private", "Duplicate submap instance 'Floor'"),
    ("repeated vehicles", "vehicle Lift vehicle Lift", "Duplicate vehicle 'Lift'"),
    ("alias colliding with an implicit base", "submap Floor using Base submap Base using Other", "Submap instance 'Base' collides with a reusable base map"),
    ("implicit base colliding with an earlier alias", "submap Base using Other submap Floor using Base", "Submap instance 'Base' collides with a reusable base map"),
    ("self-referencing alias", "submap Floor using Floor", "Submap instance 'Floor' collides with a reusable base map"),
    ("cyclic aliases", "submap Floor using Base submap Base using Floor", "collides with a reusable base map")
  )

  for (fromDirectory <- Seq(false, true)) {
    val entryPoint = if (fromDirectory) "directory" else "in-memory"

    for (duplicate <- Seq("topo-node A", "topo-node A {label = \"other\"}", "topo-node A {label = \"first\"}")) {
      test(s"$entryPoint compiler rejects duplicate node declaration: $duplicate") {
        val source = s"""topo-map Floor() {
          |  let params = {}
          |  topo-node A {label = "first"}
          |  topo-node B
          |  atomic-path [A -> B] {cost = 1}
          |  $duplicate
          |}""".stripMargin
        val error = intercept[RuntimeException] {
          compile(Map("configuration" -> "building-includes { submap Floor }", "Floor" -> source), fromDirectory)
        }
        error.getMessage should include("Duplicate topo-node 'A' in topo-map 'Floor'")
      }
    }

    test(s"$entryPoint compiler rejects duplicate nodes in a reusable base before evaluating data") {
      val source = """topo-map Base() {
        |  let params = {}
        |  topo-node A {label = missingValue}
        |  topo-node A
        |}""".stripMargin
      val error = intercept[RuntimeException] {
        compile(Map("configuration" -> "building-includes { submap Floor using Base }", "Base" -> source), fromDirectory)
      }
      error.getMessage should include("Duplicate topo-node 'A' in topo-map 'Base'")
    }

    test(s"$entryPoint compiler permits identical node identifiers in different maps") {
      val result = compile(Map(
        "configuration" -> "building-includes {\nsubmap Floor\nsubmap Other\n}",
        "Floor" -> mapSource("1"),
        "Other" -> mapSource("2").replace("topo-map Floor", "topo-map Other")
      ), fromDirectory)
      result.graphs.keySet shouldBe Set("Floor", "Other")
      result.graphs.values.foreach(_.nodes.map(_.identifier).toSet shouldBe Set("A", "B"))
      result.graphs("Floor").adjacencyList.head.costs(VisitingMode.Normal) shouldBe 1.0
      result.graphs("Other").adjacencyList.head.costs(VisitingMode.Normal) shouldBe 2.0
    }

    test(s"$entryPoint compiler keeps node identifiers case-sensitive") {
      val result = compile(Map(
        "configuration" -> "building-includes { submap Floor }",
        "Floor" -> mapSource("1").replace("topo-node B", "topo-node a").replace("A -> B", "A -> a")
      ), fromDirectory)
      result.graphs("Floor").nodes.map(_.identifier).toSet shouldBe Set("A", "a")
    }

    test(s"$entryPoint compiler accepts a map with no nodes") {
      val result = compile(Map(
        "configuration" -> "building-includes { submap Floor }",
        "Floor" -> "topo-map Floor() { let params = {} }"
      ), fromDirectory)
      result.graphs("Floor").nodes shouldBe empty
    }

    invalidConfigurations.foreach { case (description, declarations, expectedMessage) =>
      test(s"$entryPoint compiler rejects $description before loading sources") {
        val error = intercept[RuntimeException] {
          val lines = declarations.replace(" submap", "\nsubmap").replace(" vehicle", "\nvehicle")
          compile(Map("configuration" -> s"building-includes {\n$lines\n}"), fromDirectory)
        }
        error.getMessage should include(expectedMessage)
        error.getMessage should include("building-includes")
      }
    }

    for (cost <- Seq("-1", "-0.5", "1 - 2", "1.0 - 1.5"); arrow <- Seq("->", "<->")) {
      test(s"$entryPoint compiler rejects evaluated cost $cost on $arrow paths") {
        val error = intercept[RuntimeException] {
          compile(Map(
            "configuration.tcfg" -> "building-includes { submap Floor }",
            "Floor.tmap" -> mapSource(cost, arrow)
          ), fromDirectory)
        }
        error.getMessage should include("atomic-path")
        error.getMessage should include("Floor::A")
        error.getMessage should include("Floor::B")
        error.getMessage should include("cost must be non-negative")
      }
    }

    for (cost <- Seq("0", "0.0", "2", "2.5"); arrow <- Seq("->", "<->")) {
      test(s"$entryPoint compiler preserves valid cost $cost on $arrow paths") {
        val result = compile(Map(
          "configuration.tcfg" -> "building-includes { submap Floor }",
          "Floor.tmap" -> mapSource(cost, arrow)
        ), fromDirectory)
        val paths = result.graphs("Floor").adjacencyList
        paths.size shouldBe (if (arrow == "<->") 2 else 1)
        paths.foreach(_.costs(VisitingMode.Normal) shouldBe cost.toDouble)
      }
    }

    test(s"$entryPoint compiler preserves distinct instances sharing an explicitly included base") {
      val result = compile(Map(
        "configuration" -> """building-includes {
          |  submap Base
          |  submap Floor1 using Base managed-by Public
          |  submap Floor2 using Base managed-by Private
          |}""".stripMargin,
        "Base" -> mapSource("2.5").replace("topo-map Floor", "topo-map Base")
      ), fromDirectory)
      result.graphs.keySet shouldBe Set("Base", "Floor1", "Floor2")
      result.graphSequence shouldBe List("Base", "Floor1", "Floor2")
      result.graphs.values.foreach(_.adjacencyList.head.costs(VisitingMode.Normal) shouldBe 2.5)
    }

    test(s"$entryPoint compiler rejects negative costs from shared definitions in a reusable base") {
      val error = intercept[RuntimeException] {
        compile(Map(
          "configuration" -> "building-includes { submap Floor1 using Base }",
          "root" -> "root Test() { let walkingCost: Float = -2.5 }",
          "Base" -> mapSource("walkingCost").replace("topo-map Floor", "topo-map Base")
        ), fromDirectory)
      }
      error.getMessage should include("Base::A")
      error.getMessage should include("cost must be non-negative")
    }
  }

  test("configuration preserves distinct vehicles and permits separate map and vehicle namespaces") {
    val config = new TopoScriptCompiler().parseConfigFile(
      "building-includes {\nsubmap Lift\nvehicle Lift\nvehicle Stairs\n}"
    )
    config.vehicles.map(_.name) shouldBe List("Lift", "Stairs")
    config.orderedSubmapNames shouldBe List("Lift")
  }
}
