import compiler.TopoScriptCompiler
import corelang.{Environment, Identifier, Type, Value}
import data.Escalator
import enums.{TransportServicePermission, VisitingMode}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import surfacelang.TopoEnvironment

import java.nio.file.Files
import scala.jdk.CollectionConverters.*

class RootConstraintTest extends AnyFunSuite with Matchers {
  private val root = """root Building(weight: Int) {
    |  constraint CanEnter { require allowedWeight(weight) }
    |  constraint CanUseLift { require CanEnter }
    |  def allowedWeight(w: Int): Bool = w < limit
    |  def limit: Int = baseLimit + 5
    |  def baseLimit: Int = 15
    |  def travelCost: Float = 10 / 2
    |}""".stripMargin

  private val files = Map(
    "configuration" -> "building-includes {\nsubmap Lower\nsubmap Upper\nvehicle Lift\n}",
    "root" -> root,
    "Lower" -> """topo-map Lower() {
      |  let params = {}
      |  let limit: Int = 1
      |  constraint LocalAllowed { require allowedWeight(weight) }
      |  topo-node A
      |  topo-node B
      |  atomic-path [A -> B] {cost = travelCost} requires <CanEnter && LocalAllowed>
      |}""".stripMargin,
    "Upper" -> """topo-map Upper() {
      |  let params = {}
      |  topo-node A
      |}""".stripMargin,
    "Lift" -> """transport Lift is Escalator {
      |  let params = {time = travelCost}
      |  constraint CanRide { require allowedWeight(weight) }
      |  station LowerStop at Lower::A {} requires <CanUseLift && CanRide>
      |  station UpperStop at Upper::A {}
      |}""".stripMargin
  )

  for (fromDirectory <- Seq(false, true); weight <- Seq(10, 25)) {
    val entryPoint = if (fromDirectory) "directory" else "in-memory"
    test(s"$entryPoint compilation evaluates root dependencies before constraints for weight $weight") {
      val compiler = new TopoScriptCompiler()
      val result = if (!fromDirectory) {
        val params = Map[String, AnyRef]("weight" -> java.lang.Integer.valueOf(weight))
        compiler.compileProject(files.asJava, params.asJava)
      } else {
        val directory = Files.createTempDirectory("toponavi-root-constraints-")
        try {
          files.foreach { case (name, source) => Files.writeString(directory.resolve(name), source) }
          compiler.compile(directory.toString, Map("weight" -> Value.IntVal(weight)))
        } finally {
          files.keys.foreach(name => Files.deleteIfExists(directory.resolve(name)))
          Files.deleteIfExists(directory)
        }
      }

      val lower = result.graphs("Lower")
      if (weight < 20) {
        lower.adjacencyList.map(_.costs(VisitingMode.Normal)) shouldBe List(5.0)
      } else {
        lower.adjacencyList shouldBe empty
      }
      val lift = result.transportGraph.nodes.map(_.ownerLine).collectFirst {
        case escalator: Escalator => escalator
      }.getOrElse(fail("Expected a compiled escalator"))
      lift.travelTimeSeconds shouldBe 5.0
      lift.stationPermissions(lower) shouldBe (
        if (weight < 20) TransportServicePermission.FullyGranted else TransportServicePermission.NoAccess
      )
    }
  }

  test("root context retains supplied parameters, definitions, and constraint results") {
    val env = Environment.empty[Identifier, Type, Value]
      .addValueVar(Identifier.Symbol("weight"), Value.IntVal(10))
    val result = new TopoScriptCompiler().parseRootFile(root)
      .elaborate(using TopoEnvironment(env, Map.empty, Map.empty, Map.empty))
    result.context.getValue(Identifier.Symbol("weight")) shouldBe Some(Value.IntVal(10))
    result.context.getValue(Identifier.Symbol("limit")) shouldBe Some(Value.IntVal(20))
    result.context.getValue(Identifier.Symbol("CanEnter")) shouldBe Some(Value.BoolVal(true))
    result.context.getValue(Identifier.Symbol("CanUseLift")) shouldBe Some(Value.BoolVal(true))
  }

  test("cyclic root definitions fail before constraint evaluation") {
    val source = """root Building() {
      |  constraint Allowed { require first == 1 }
      |  def first: Int = second
      |  def second: Int = first
      |}""".stripMargin
    val error = intercept[RuntimeException] {
      new TopoScriptCompiler().parseRootFile(source).elaborate(using TopoEnvironment(
        Environment.empty[Identifier, Type, Value], Map.empty, Map.empty, Map.empty
      ))
    }
    error.getMessage should include("Cycle detected")
  }

  test("function-backed root constraints still require Bool results") {
    val source = """root Building() {
      |  constraint Allowed { require identity(1) }
      |  def identity(n: Int): Int = n
      |}""".stripMargin
    val error = intercept[RuntimeException] {
      new TopoScriptCompiler().parseRootFile(source).elaborate(using TopoEnvironment(
        Environment.empty[Identifier, Type, Value], Map.empty, Map.empty, Map.empty
      ))
    }
    error.getMessage should include("must evaluate to Bool")
  }
}
