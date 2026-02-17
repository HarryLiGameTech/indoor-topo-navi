import data.{ElevatorBank, NavigationGraph, TopoNode}
import enums.ElevatorTrafficPattern.UpRush
import enums.TransportServicePermission

object ElevatorRttTest extends App{
  def testElevatorRtt(): Unit = {
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
      departureRate = Map(floor1 -> 0.8, floor2 -> 0.1, floor3 -> 0.1),
      maxVelocity = 2.5,
      acceleration = 0.8,
      carAmount = 1
    )

    // Test net time and travel time calculations
    val netTime = elevator.netTimeBetweenStations(floor1, floor3)
    val travelTime = elevator.travelTimeBetweenStations(floor1, floor3, UpRush)

    println(s"Net time between Floor1 and Floor3: $netTime seconds")
    println(s"Travel time between Floor1 and Floor3: $travelTime seconds")

    // TODO: Test upTime() separately with "elevator" value's specification
    val upTimeMethod = classOf[ElevatorBank].getDeclaredMethod("upTime")
    upTimeMethod.setAccessible(true)
    val upTime = upTimeMethod.invoke(elevator).asInstanceOf[Double]
    println(s"Calculated upTime: $upTime seconds")

  }

  testElevatorRtt()
}