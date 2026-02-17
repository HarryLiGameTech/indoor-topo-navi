import data.{ElevatorBank, NavigationGraph, TopoNode}
import enums.ElevatorTrafficPattern.UpRush
import enums.TransportServicePermission
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ElevatorRttTest extends AnyFlatSpec with Matchers {
  "Elevator" should "calculate correct times" in {
    // Create a simple transport graph with one elevator
    val floor1 = NavigationGraph("Floor1")
    val floor2 = NavigationGraph("Floor2")
    val floor3 = NavigationGraph("Floor3")
    val elevator = ElevatorBank(
      identifier = "Elevator1",
      stationNodes = Map(floor1 -> TopoNode("s1"), floor2 -> TopoNode("s2")),
      stationLocations = Map(floor1 -> 0.0, floor2 -> 10.0, floor3 -> 20.0),
      stationPermissions = Map(floor1 -> TransportServicePermission.FullyGranted, floor2 -> TransportServicePermission.FullyGranted, floor3 -> TransportServicePermission.FullyGranted),
      stationPopulations = Map(floor1 -> 10, floor2 -> 50, floor3 -> 50),
      departureRate = Map(floor1 -> 0.4, floor2 -> 0.4, floor3 -> 0.2),
      capacity = 13,
      maxVelocity = 2.5,
      acceleration = 1.0,
      carAmount = 1
    )

    // Test net time and travel time calculations
    val netTime = elevator.netTimeBetweenStations(floor1, floor3)
    val travelTime = elevator.travelTimeBetweenStations(floor1, floor3, UpRush)

    println(s"Net time between Floor1 and Floor3: $netTime seconds")
    println(s"Travel time between Floor1 and Floor3: $travelTime seconds")

    // Test upTime() separately with "elevator" value's specification
    val upTimeMethod = classOf[ElevatorBank].getDeclaredMethod("upTime")
    upTimeMethod.setAccessible(true)
    val upTime = upTimeMethod.invoke(elevator).asInstanceOf[Double]
    println(s"Calculated upTime: $upTime seconds")

    val probMatrix = classOf[ElevatorBank].getDeclaredMethod("upPathCandidateProbability", classOf[Int], classOf[Int], classOf[Int])

    probMatrix.setAccessible(true)
    // Invoking for all pairs to check values
    val stations = elevator.orderedStations()
    val n = stations.length
    val probabilities = for {
      i <- 0 until n - 1
      j <- i + 1 until n
    } yield {
      val prob = probMatrix.invoke(elevator, i: java.lang.Integer, j: java.lang.Integer, elevator.capacity: java.lang.Integer).asInstanceOf[Double]
      println(s"Probability for pair (${stations(i)}, ${stations(j)}): $prob")
      prob
    }
    println(s"Calculated upPathCandidateProbabilities: ${probabilities.mkString(", ")}")

    // Add assertions to verify correctness
    netTime should be > 0.0
    travelTime should be > netTime
    upTime should be > netTime
  }
}
