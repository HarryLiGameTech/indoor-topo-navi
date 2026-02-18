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
    val floor4 = NavigationGraph("Floor4")
    val floor5 = NavigationGraph("Floor5")
    val floor6 = NavigationGraph("Floor6")
    val floor7 = NavigationGraph("Floor7")
    val floor8 = NavigationGraph("Floor8")

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

    val elevator2 = ElevatorBank(
      identifier = "Elevator2",
      stationNodes = Map(floor1 -> TopoNode("s1"), floor2 -> TopoNode("s2")),
      stationLocations = Map(floor1 -> 0.0, floor2 -> 10.0, floor3 -> 20.0, floor4 -> 30.0, floor5 -> 40.0, floor6 -> 50.0, floor7 -> 60.0, floor8 -> 70.0),
      stationPermissions = Map(
        floor1 -> TransportServicePermission.FullyGranted,
        floor2 -> TransportServicePermission.FullyGranted,
        floor3 -> TransportServicePermission.FullyGranted,
        floor4 -> TransportServicePermission.FullyGranted,
        floor5 -> TransportServicePermission.FullyGranted,
        floor6 -> TransportServicePermission.FullyGranted,
        floor7 -> TransportServicePermission.FullyGranted,
        floor8 -> TransportServicePermission.FullyGranted
      ),
      stationPopulations = Map(floor1 -> 2, floor2 -> 14, floor3 -> 14, floor4 -> 14, floor5 -> 14, floor6 -> 14, floor7 -> 14, floor8 -> 14),
      departureRate = Map(floor1 -> 0.93, floor2 -> 0.01, floor3 -> 0.01, floor4 -> 0.01, floor5 -> 0.01, floor6 -> 0.01, floor7 -> 0.01, floor8 -> 0.01),
      capacity = 1,
      maxVelocity = 2.5,
      acceleration = 1.0,
      carAmount = 1
    )

    // Test net time and travel time calculations
//    val netTime = elevator.netTimeBetweenStations(floor1, floor3)
//    val travelTime = elevator.travelTimeBetweenStations(floor1, floor3, UpRush)
//
//    println(s"Net time between Floor1 and Floor3: $netTime seconds")
//    println(s"Travel time between Floor1 and Floor3: $travelTime seconds")
//
//    // Test upTime() separately with "elevator" value's specification
//    val upTimeMethod = classOf[ElevatorBank].getDeclaredMethod("upTime")
//    upTimeMethod.setAccessible(true)
//    val upTime = upTimeMethod.invoke(elevator).asInstanceOf[Double]
////    println(s"Calculated upTime: $upTime seconds")
//
//    val probMatrix = classOf[ElevatorBank].getDeclaredMethod("upPathCandidateProbability", classOf[Int], classOf[Int], classOf[Int])
//
//    probMatrix.setAccessible(true)
//    // Invoking for all pairs to check values
//    val stations = elevator.orderedStations()
//    val n = stations.length
//    val probabilities = for {
//      i <- 0 until n - 1
//      j <- i + 1 until n
//    } yield {
//      val prob = probMatrix.invoke(elevator, i: java.lang.Integer, j: java.lang.Integer, elevator.capacity: java.lang.Integer).asInstanceOf[Double]
////      println(s"Probability for pair (${stations(i)}, ${stations(j)}): $prob")
//      prob
//    }
//
//    // Add assertions to verify correctness
//    netTime should be > 0.0
//    travelTime should be > netTime
//    upTime should be > netTime

    println("=== Complex elevator with 8 stations ===")
    val netTime2 = elevator2.netTimeBetweenStations(floor1, floor8)
    val travelTime2 = elevator2.travelTimeBetweenStations(floor1, floor8, UpRush)

    println(s"Net time between Floor1 and Floor8: $netTime2 seconds")
    println(s"Travel time between Floor1 and Floor8: $travelTime2 seconds")

    // Test upTime() separately with "elevator" value's specification
    val upTimeMethod2 = classOf[ElevatorBank].getDeclaredMethod("upTime")
    upTimeMethod2.setAccessible(true)
    val upTime2 = upTimeMethod2.invoke(elevator2).asInstanceOf[Double]
//    println(s"Calculated upTime2: $upTime2 seconds")

    val probMatrix2 = classOf[ElevatorBank].getDeclaredMethod("upPathCandidateProbability", classOf[Int], classOf[Int], classOf[Int])

    probMatrix2.setAccessible(true)
    // Invoking for all pairs to check values
    val stations2 = elevator2.orderedStations()
    val n2 = stations2.length
    val probabilities2 = for {
      i <- 0 until n2 - 1
      j <- i + 1 until n2
    } yield {
      val prob = probMatrix2.invoke(elevator2, i: java.lang.Integer, j: java.lang.Integer, elevator.capacity: java.lang.Integer).asInstanceOf[Double]
//      println(s"Probability for pair (${stations2(i)}, ${stations2(j)}): $prob")
      prob
    }

    netTime2 should be > 0.0
    travelTime2 should be > netTime2
    upTime2 should be > netTime2

  }
}
