import data.{Escalator, NavigationGraph, TopoNode}
import enums.TransportServicePermission
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EscalatorTest extends AnyFlatSpec with Matchers {
  private val lower = NavigationGraph("Lower")
  private val upper = NavigationGraph("Upper")
  private val unknown = NavigationGraph("Unknown")

  private def escalator(
    lowerPermission: TransportServicePermission = TransportServicePermission.FullyGranted,
    upperPermission: TransportServicePermission = TransportServicePermission.FullyGranted
  ): Escalator = Escalator(
    identifier = "Escalator1",
    stationNodes = Map(lower -> TopoNode("lower"), upper -> TopoNode("upper")),
    stationLocations = Map(lower -> 0.0, upper -> 1.0),
    stationPermissions = Map(lower -> lowerPermission, upper -> upperPermission),
    travelTimeSeconds = 30.0
  )

  "Escalator" should "use the fixed travel time in both directions without waiting" in {
    val subject = escalator()

    subject.netTimeBetweenStations(lower, upper) shouldBe 30.0
    subject.travelTimeBetweenStations(lower, upper) shouldBe 30.0
    subject.netTimeBetweenStations(upper, lower) shouldBe 30.0
    subject.travelTimeBetweenStations(upper, lower) shouldBe 30.0
    subject.netTimeBetweenStations(lower, lower) shouldBe 0.0
    subject.travelTimeBetweenStations(upper, upper) shouldBe 0.0
  }

  it should "use synthetic station locations for distance" in {
    val subject = escalator()

    subject.distanceBetweenStations(lower, lower) shouldBe 0.0
    subject.distanceBetweenStations(upper, upper) shouldBe 0.0
    subject.distanceBetweenStations(lower, upper) shouldBe 1.0
    subject.distanceBetweenStations(upper, lower) shouldBe 1.0
  }

  it should "respect all station permission modes and reject unknown graphs" in {
    val fullyGranted = escalator()
    fullyGranted.canArriveAt(lower) shouldBe true
    fullyGranted.canDepartFrom(lower) shouldBe true

    val arriveOnly = escalator(lowerPermission = TransportServicePermission.ArriveOnly)
    arriveOnly.canArriveAt(lower) shouldBe true
    arriveOnly.canDepartFrom(lower) shouldBe false

    val departOnly = escalator(lowerPermission = TransportServicePermission.DepartOnly)
    departOnly.canArriveAt(lower) shouldBe false
    departOnly.canDepartFrom(lower) shouldBe true

    val noAccess = escalator(lowerPermission = TransportServicePermission.NoAccess)
    noAccess.canArriveAt(lower) shouldBe false
    noAccess.canDepartFrom(lower) shouldBe false

    fullyGranted.canArriveAt(unknown) shouldBe false
    fullyGranted.canDepartFrom(unknown) shouldBe false
  }
}
