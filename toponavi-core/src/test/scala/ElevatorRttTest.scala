import data.{ElevatorBank, NavigationGraph, TopoNode}
import enums.ElevatorTrafficPattern.{Flat, UpRush}
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
      stationLocations = Map(floor1 -> 0.0, floor2 -> 4.0, floor3 -> 8.0, floor4 -> 12.0, floor5 -> 16.0, floor6 -> 20.0, floor7 -> 25.0, floor8 -> 30.0),
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
      stationCategories = Map(floor1 -> enums.ElevatorStationCategory.Entrance, floor2 -> enums.ElevatorStationCategory.Occupant, floor3 -> enums.ElevatorStationCategory.Occupant, floor4 -> enums.ElevatorStationCategory.Occupant, floor5 -> enums.ElevatorStationCategory.Occupant, floor6 -> enums.ElevatorStationCategory.Occupant, floor7 -> enums.ElevatorStationCategory.Occupant, floor8 -> enums.ElevatorStationCategory.Occupant),
      capacity = 18,
      maxVelocity = 2.5,
      acceleration = 0.8,
      carAmount = 4
    )



    println("=== Complex elevator with 8 stations ===")
    val netTime2 = elevator2.netTimeBetweenStations(floor1, floor8)
    val travelTime2 = elevator2.travelTimeBetweenStations(floor1, floor8, UpRush)

    val travelTime3 = elevator2.travelTimeBetweenStations(floor2, floor8, Flat)

    println(s"Net time between Floor1 and Floor8: $netTime2 seconds")
    println(s"Travel time between Floor1 and Floor8: $travelTime2 seconds")

    netTime2 should be > 0.0
    travelTime2 should be > netTime2

  }

  "SWFC_GE4_Elevator" should "calculate correct times" in {
    val floorB2 = NavigationGraph("FloorB2")
    val floorB1 = NavigationGraph("FloorB1")
    val floor2 = NavigationGraph("Floor2")
    val floor3 = NavigationGraph("Floor3")
    val floor94 = NavigationGraph("Floor94")
    val floor95 = NavigationGraph("Floor95")

    val elevator = ElevatorBank(
      identifier = "GE4",
      stationNodes = Map(floorB2 -> TopoNode("s1"), floorB1 -> TopoNode("s2"), floor2 -> TopoNode("s3"), floor3 -> TopoNode("s4"), floor94 -> TopoNode("s5"), floor95 -> TopoNode("s6")),
      stationLocations = Map(floorB2 -> -10.0, floorB1 -> -5.0, floor2 -> 5.0, floor3 -> 10.0, floor94 -> 425.0, floor95 -> 430.0),
      stationPermissions = Map(
        floorB2 -> TransportServicePermission.FullyGranted,
        floorB1 -> TransportServicePermission.FullyGranted,
        floor2 -> TransportServicePermission.FullyGranted,
        floor3 -> TransportServicePermission.FullyGranted,
        floor94 -> TransportServicePermission.FullyGranted,
        floor95 -> TransportServicePermission.FullyGranted
      ),
      stationPopulations = Map(floorB2 -> 3, floorB1 -> 3, floor2 -> 10, floor3 -> 10, floor94 -> 37, floor95 -> 37),
      departureRate = Map(floorB2 -> 0.25, floorB1 -> 0.25, floor2 -> 0.0, floor3 -> 0.0, floor94 -> 0.25, floor95 -> 0.25),
      stationCategories = Map(
        floorB2 -> enums.ElevatorStationCategory.Entrance,
        floorB1 -> enums.ElevatorStationCategory.Entrance,
        floor2 -> enums.ElevatorStationCategory.Occupant,
        floor3 -> enums.ElevatorStationCategory.Occupant,
        floor94 -> enums.ElevatorStationCategory.Occupant,
        floor95 -> enums.ElevatorStationCategory.Occupant
      ),
      capacity = 17,
      maxVelocity = 8.0,
      acceleration = 1.0,
      carAmount = 1
    )

    println("=== SWFC GE4 Tester ===")
    val netTime3 = elevator.netTimeBetweenStations(floorB2, floor95)
    val travelTime3 = elevator.travelTimeBetweenStations(floorB2, floor95, UpRush)

    println(s"Net time between FloorB2 and Floor95: $netTime3 seconds")
    println(s"Travel time between FloorB2 and Floor95: $travelTime3 seconds")
  }
}
