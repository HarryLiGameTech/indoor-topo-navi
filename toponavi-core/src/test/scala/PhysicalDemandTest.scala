import data.*
import enums.RouteEdgeCategory.{Climbing, Transport, Walking}
import enums.TransportServicePermission.FullyGranted
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PhysicalDemandTest extends AnyFlatSpec with Matchers {
  private val lowerNode = TopoNode("lower")
  private val upperNode = TopoNode("upper")
  private val lower = NavigationGraph("Lower", List(lowerNode))
  private val upper = NavigationGraph("Upper", List(upperNode))
  private val stations = Map(lower -> lowerNode, upper -> upperNode)
  private val locations = Map(lower -> 0.0, upper -> 10.0)
  private val permissions = Map(lower -> FullyGranted, upper -> FullyGranted)

  private val elevator = ElevatorBank("Lift", stations, locations, permissions,
    maxVelocity = 2.5, acceleration = 0.8)
  private val escalator = Escalator("Escalator", stations, locations, permissions,
    travelTimeSeconds = 30.0, distanceMeters = 10.0)
  private val stairs = StairCase("Stairs", stations, locations, Map(lower -> 0, upper -> 4), 3.0)

  "Physical-demand scoring" should "use the same distance and time formulas in transport plans and route results" in {
    Seq(elevator -> 16.0, escalator -> 20.5, stairs -> 100.0).foreach { case (line, expected) =>
      Seq(lower -> upper, upper -> lower).foreach { case (source, target) =>
        val from = StationNode(s"${line.identifier}@${source.identifier}", source, line, FullyGranted)
        val to = StationNode(s"${line.identifier}@${target.identifier}", target, line, FullyGranted)
        val plan = TransportationPath(List(from, to), List(TransportEdge(from, to, 30.0)))
        val traversal = TransportTraversal.between(line, source, target)
        val edge = RouteEdge(GlobalNode(source, line.stationNode(source)), GlobalNode(target, line.stationNode(target)),
          30.0, Transport, "Ride", transport = Some(traversal))
        val output = NavigationOutputPath(List(edge.source, edge.target), List(edge))

        plan.physicalDemandScore shouldBe expected
        output.physicalDemandScore shouldBe expected
      }
    }
  }

  it should "count walking time and preserve the physical score regardless of the presentation category" in {
    val from = GlobalNode(lower, lowerNode)
    val to = GlobalNode(upper, upperNode)
    val stair = RouteEdge(from, to, 60.0, Climbing, "Climb",
      transport = Some(TransportTraversal.between(stairs, lower, upper)))
    val walk = RouteEdge(from, from, 5.0, Walking, "Walk")

    NavigationOutputPath(List(from, to), List(walk, stair)).physicalDemandScore shouldBe 105.0
    stair.copy(category = Transport).physicalDemandScore shouldBe 100.0
  }

  it should "reject transport scoring without distance and transport-kind metadata" in {
    val edge = RouteEdge(GlobalNode(lower, lowerNode), GlobalNode(upper, upperNode), 30.0, Transport, "Ride")

    intercept[IllegalStateException](edge.physicalDemandScore).getMessage should include("traversal metadata")
  }

  "Transfer counting" should "count changes of lines rather than rides or walking edges" in {
    TransportTraversal.countChanges(List.empty) shouldBe 0
    TransportTraversal.countChanges(List("A")) shouldBe 0
    TransportTraversal.countChanges(List("A", "A", "B", "B", "C", "A")) shouldBe 3

    val node = GlobalNode(lower, lowerNode)
    val ride = RouteEdge(node, node, 10.0, Transport, "Ride",
      transport = Some(TransportTraversal("A", TransportKind.Elevator, 2.0)))
    val walk = RouteEdge(node, node, 1.0, Walking, "Walk")
    val other = ride.copy(transport = Some(TransportTraversal("B", TransportKind.Escalator, 2.0)))
    NavigationOutputPath(List(node), List(walk)).transferCount shouldBe 0
    NavigationOutputPath(List(node), List(ride, walk, ride)).transferCount shouldBe 0
    NavigationOutputPath(List(node), List(ride, walk, other)).transferCount shouldBe 1
  }
}
