package data

import enums.ElevatorTrafficPattern.{BidirectionalRush, DownRush, Flat, UpRush}
import enums.{ElevatorTrafficPattern, TransportServicePermission}

trait LinearTransport {
  def identifier: String
  def stationNodes: Map[NavigationGraph, TopoNode]
  def stationLocations: Map[NavigationGraph, Double]
  def maxVelocity: Double
  def acceleration: Double

  def stationNode(graph: NavigationGraph): TopoNode = stationNodes(graph)
  def stationLocation(graph: NavigationGraph): Double = stationLocations(graph)

  // Abstract methods
  def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double // Net-moving time, for heuristics use
  def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double // Includes waiting time
  def canArriveAt(target: NavigationGraph): Boolean // eg. An elevator canServe the floor input iff hasStation && hasPermission
  def canDepartFrom(source: NavigationGraph): Boolean
}

case class ElevatorBank(
  identifier: String,
  stationNodes: Map[NavigationGraph, TopoNode],
  stationLocations: Map[NavigationGraph, Double],
  stationPermissions: Map[NavigationGraph, TransportServicePermission],
  stationPopulations: Map[NavigationGraph, Int],
  departureRate: Map[NavigationGraph, Double],
  maxVelocity: Double,
  acceleration: Double,
  carAmount: Int,
  capacity: Int = 21,
  duty: Int = 1600
) extends LinearTransport {

  override def canArriveAt(target: NavigationGraph): Boolean = {
    if (stationNodes.contains(target) && (stationPermissions(target) == TransportServicePermission.FullyGranted || (stationPermissions(target) == TransportServicePermission.ArriveOnly))){
      true
    }
    else{
      false
    }
  }

  override def canDepartFrom(target: NavigationGraph): Boolean = {
    if (stationNodes.contains(target) && (stationPermissions(target) == TransportServicePermission.FullyGranted || (stationPermissions(target) == TransportServicePermission.DepartOnly))) {
      true
    }
    else {
      false
    }
  }

  override def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double = {
    // Get the vertical distance between stations
    val srcLocation = stationLocations(src)
    val dstLocation = stationLocations(dst)
    val distance = math.abs(dstLocation - srcLocation) // meters

    // Kinematic calculations for elevator movement
    // We need to consider acceleration, constant velocity, and deceleration phases

    // 1. Calculate the distance needed to reach max velocity (acceleration phase)
    val accelerationDistance = (maxVelocity * maxVelocity) / (2 * acceleration)

    // 2. Check if the elevator can reach max velocity for this distance
    if (distance >= 2 * accelerationDistance) {
      // Case 1: Enough distance for full acceleration + constant velocity + full deceleration
      // Time = acceleration_time + constant_velocity_time + deceleration_time
      val accelerationTime = maxVelocity / acceleration
      val constantVelocityDistance = distance - 2 * accelerationDistance
      val constantVelocityTime = constantVelocityDistance / maxVelocity
      accelerationTime * 2 + constantVelocityTime
    } else {
      // Case 2: Not enough distance to reach max velocity
      // The elevator will accelerate for half the distance, then decelerate for the other half
      val halfDistance = distance / 2.0

      // Calculate maximum speed achievable in half the distance
      // Using: v² = u² + 2as, where u=0
      val maxAchievableVelocity = math.sqrt(2 * acceleration * halfDistance)

      // Ensure we don't exceed maxVelocity
      val actualMaxVelocity = math.min(maxAchievableVelocity, maxVelocity)

      // Time for acceleration phase + deceleration phase
      val accelerationTime = actualMaxVelocity / acceleration
      accelerationTime * 2
    }
  }

  // TODO: Substitute netTimeBetweenStations(src, dst)
  override def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double = {
    waitingTime(Flat) + netTimeBetweenStations(src, dst)
  }

  private def waitingTime(trafficPattern: ElevatorTrafficPattern): Double = {
    if trafficPattern == Flat then 40.0 // TODO: Modify to idealistic elevator idle-position distribution
    else roundTripTime(trafficPattern) / (carAmount * 2)
  }
  
  private def roundTripTime(trafficPattern: ElevatorTrafficPattern): Double = {
    if true then 120
    else{
      trafficPattern match {
        case UpRush => {
          upTime() + downTime() + stops() * 15 // 15-second dwell time per stop
        }
        case DownRush => {
          999
        } // TODO: Implement
        case BidirectionalRush => {
          999
        }
        case _ => 120
      }
    }
  }
  
  private def upTime(): Double = {
    val stations = orderedStations() // Sorted station list according to height
    val n = stations.length
    var time = 0.0

    for (i <- 0 until n - 1 ) {
      for (j <- i + 1 until n) {
        val start = stations(i)
        val end = stations(j)
        time += upPathCandidateProbability(i, j) * netTimeBetweenStations(start, end)
      }
    }
    time
  }

  // The single-probability that the elevator will travel non-stop
  private def upPathCandidateProbability(start: Int, end: Int, expectedPassengerLoad: Int = 13): Double = {
    val stations = orderedStations()
    val c = expectedPassengerLoad

    val l1 = noPassengerBoardingWithinFloorRangeProbability(start+1, end-1)
    val l2 = noPassengerBoardingWithinFloorRangeProbability(start, end-1)
    val l3 = noPassengerBoardingWithinFloorRangeProbability(start+1, end)
    val l4 = noPassengerBoardingWithinFloorRangeProbability(start, end)

    val r1 = noPassengerAlightingWithinFloorRangeProbability(start+1, end-1)
    val r2 = noPassengerAlightingWithinFloorRangeProbability(start, end-1)
    val r3 = noPassengerAlightingWithinFloorRangeProbability(start+1, end)
    val r4 = noPassengerAlightingWithinFloorRangeProbability(start, end)
    // Probability that ONE passenger does NOT want to go to any of the intermediate floors?
    // The comment says: 1 - sum...
    // Raising to power 'c' (number of passengers) suggests this is the probability that NONE of the 'c' passengers stop at intermediate floors.

    Math.pow((1.0 - l1) * (1.0 - r1), c)
      - Math.pow((1.0 - l2) * (1.0 - r2), c)
        - Math.pow((1.0 - l3) * (1.0 - r3), c)
          + Math.pow((1.0 - l4) * (1.0 - r4), c)
  }
  
  private def downTime(): Double = {
    50.0 // TODO: Implement
  }
  
  private def stops(): Int = {
    if true then 0
    else{
      -1 // TODO: Implement
    }
  }

  private def populationSum(): Int = {
    stationPopulations.values.sum
  }

  // Lower station to higher station
  private def orderedStations(): List[NavigationGraph] = {
    stationLocations.toList.sortBy(_._2).map(_._1)
  }

  private def noPassengerBoardingWithinFloorRangeProbability(i: Int, j: Int): Double = {
    val stations = orderedStations()

    // Calculate P(no stop between start and end)
    // We sum the departure rates of all intermediate stations k where start < k < end
    var sumBoardingRates = 0.0
    for (k <- i until j + 1) {
      val intermediateStation = stations(k)
      // Assuming departureRate is normalized (probability), strictly it might be rate.
      // Based on context commonly seeing such formulas, we treat it as probability mass for that floor.
      if (departureRate.contains(intermediateStation)) {
        sumBoardingRates += departureRate(intermediateStation)
      }
    }

    1.0 - sumBoardingRates
  }

  private def noPassengerAlightingWithinFloorRangeProbability(i: Int, j: Int): Double = {
    val stations = orderedStations()

    // Calculate P(no stop between start and end)
    // We sum the departure rates of all intermediate stations k where start < k < end
    var sumAlightingRates = 0.0
    for (k <- i until j + 1) {
      val intermediateStation = stations(k)

      if (departureRate.contains(intermediateStation)) {
        sumAlightingRates += stationPopulations(stations(k)) / populationSum()
      }
    }

    1.0 - sumAlightingRates
  }
  
}