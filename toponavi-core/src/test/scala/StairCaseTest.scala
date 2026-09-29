import data.{NavigationGraph, StairCase, TopoNode}
import enums.ElevatorTrafficPattern
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StairCaseTest extends AnyFlatSpec with Matchers {
  private val lower = NavigationGraph("Lower")
  private val upper = NavigationGraph("Upper")

  private def stairs(
    lowerIndex: Int = 0,
    upperIndex: Int = 4,
    turnBackCost: Double = 3.0
  ): StairCase = StairCase(
    identifier = "TestStairs",
    stationNodes = Map(lower -> TopoNode("lower"), upper -> TopoNode("upper")),
    stationLocations = Map(lower -> 0.0, upper -> 3.34),
    stationRunIndices = Map(lower -> lowerIndex, upper -> upperIndex),
    turnAroundLoss = turnBackCost
  )

  "StairCase" should "include intermediate turns in default traversal time in both directions" in {
    val subject = stairs()

    subject.netTimeBetweenStations(lower, upper) shouldBe (29.0 +- 1e-9)
    subject.netTimeBetweenStations(upper, lower) shouldBe (29.0 +- 1e-9)
    ElevatorTrafficPattern.values.foreach { pattern =>
      subject.travelTimeBetweenStations(lower, upper, pattern) shouldBe (29.0 +- 1e-9)
      subject.travelTimeBetweenStations(upper, lower, pattern) shouldBe (29.0 +- 1e-9)
    }
  }

  it should "charge only for intermediate changes between segment indices" in {
    Seq(0 -> 20.0, 1 -> 20.0, 2 -> 23.0, 4 -> 29.0).foreach { case (index, expected) =>
      val subject = stairs(lowerIndex = -2, upperIndex = -2 + index)
      subject.netTimeBetweenStations(lower, upper) shouldBe (expected +- 1e-9)
      subject.netTimeBetweenStations(upper, lower) shouldBe (expected +- 1e-9)
    }
  }

  it should "apply custom vertical speed only to the vertical travel time" in {
    val subject = stairs()

    subject.netTimeBetweenStations(lower, upper, 0.334) shouldBe (19.0 +- 1e-9)
    subject.netTimeBetweenStations(upper, lower, 0.334) shouldBe (19.0 +- 1e-9)
  }

  it should "allow zero turn cost and return zero time at the same station" in {
    stairs(turnBackCost = 0.0).travelTimeBetweenStations(lower, upper) shouldBe (20.0 +- 1e-9)
    stairs().travelTimeBetweenStations(lower, lower) shouldBe 0.0
    stairs().netTimeBetweenStations(upper, upper, 0.334) shouldBe 0.0
  }

  it should "calculate segment differences without integer overflow" in {
    val subject = stairs(lowerIndex = Int.MinValue, upperIndex = Int.MaxValue)
    val expected = 20.0 + 3.0 * 4294967294L

    subject.netTimeBetweenStations(lower, upper) shouldBe expected
    subject.netTimeBetweenStations(upper, lower) shouldBe expected
  }
}
