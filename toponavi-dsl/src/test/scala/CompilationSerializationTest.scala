import api.TopoNaviService
import compiler.CompilationResult
import enums.RoutePlanningPreferences.MinimizeTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import scala.jdk.CollectionConverters.*
import scala.util.Using

class CompilationSerializationTest extends AnyFunSuite with Matchers {
  private val files = Map(
    "configuration" -> "building-includes {\n submap Lower\n submap Upper\n vehicle Lift\n}",
    "root" -> """
      |root Building(haveCard: Bool, walkingSeconds: Int) {
      |  constraint CardAccess { require haveCard }
      |}
      |""".stripMargin,
    "Lower" -> """
      |topo-map Lower() {
      |  let params = {area = 1}
      |  topo-node start
      |  topo-node landing
      |  atomic-path [start -> landing] {cost = walkingSeconds}
      |  atomic-path [start -> landing] {cost = 1} requires CardAccess
      |}
      |""".stripMargin,
    "Upper" -> """
      |topo-map Upper() {
      |  let params = {area = 1}
      |  topo-node landing
      |  topo-node goal
      |  atomic-path [landing -> goal] {cost = 3}
      |}
      |""".stripMargin,
    "Lift" -> """
      |transport Lift is Escalator {
      |  let params = {time = 18.0, distance = 8.5}
      |  station LowerStop at Lower::landing {}
      |  station UpperStop at Upper::landing {}
      |  direction LowerStop -> UpperStop
      |}
      |""".stripMargin
  )

  private def roundTrip(result: CompilationResult): CompilationResult = {
    val bytes = new ByteArrayOutputStream()
    Using.resource(new ObjectOutputStream(bytes))(_.writeObject(result))
    Using.resource(new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray))) {
      _.readObject().asInstanceOf[CompilationResult]
    }
  }

  Seq(false, true).foreach { haveCard =>
    test(s"serialized compilation preserves graph references and routes with haveCard=$haveCard") {
      val compiled = TopoNaviService.compile(files.asJava, Map[String, AnyRef](
        "haveCard" -> java.lang.Boolean.valueOf(haveCard),
        "walkingSeconds" -> java.lang.Integer.valueOf(7)
      ).asJava)
      val restored = roundTrip(compiled)

      restored.graphs.keySet shouldBe Set("Lower", "Upper")
      restored.graphSequence shouldBe List("Lower", "Upper")
      (restored.graphs("Lower") eq compiled.graphs("Lower")) shouldBe false
      restored.graphs("Lower").adjacencyList should have size (if (haveCard) 2 else 1)
      restored.transportGraph.nodes should have size 2
      restored.transportGraph.nodes.foreach { station =>
        (station.ownerGraph eq restored.graphs(station.ownerGraph.identifier)) shouldBe true
        station.ownerGraph.nodes.exists(_ eq station.localNode) shouldBe true
      }
      restored.transportGraph.adjacencyList.toList.flatMap { case (source, neighbors) =>
        neighbors.map { case (target, cost) =>
          (source.ownerGraph.identifier, target.ownerGraph.identifier, cost)
        }
      } shouldBe List(("Lower", "Upper", 18.0))

      val walkingCost = if (haveCard) 1.0 else 7.0
      Seq(false, true).foreach { highRise =>
        val walking = TopoNaviService.findRoutePlan(
          restored, "Lower::start", "Lower::landing", MinimizeTime, Set.empty, highRise)
        walking.totalCost shouldBe walkingCost
        walking.routeNodes.map(_.localNode.identifier) shouldBe List("start", "landing")

        val route = TopoNaviService.findRoutePlan(
          restored, "Lower::start", "Upper::goal", MinimizeTime, Set.empty, highRise)
        route.totalCost shouldBe (walkingCost + 18.0 + 3.0)
        route.routeNodes.map(node => node.owningGraph.identifier -> node.localNode.identifier) shouldBe
          List("Lower" -> "start", "Lower" -> "landing", "Upper" -> "landing", "Upper" -> "goal")
      }
    }
  }
}
