package data

import enums.ElevatorStationCategory.{Entrance, Occupant}
import enums.ElevatorTrafficPattern.{BidirectionalRush, DownRush, Flat, UpRush}
import enums.{ElevatorStationCategory, ElevatorTrafficPattern, TransportServicePermission}

trait LinearTransport extends Serializable {
  def identifier: String
  def stationNodes: Map[NavigationGraph, TopoNode]
  def stationLocations: Map[NavigationGraph, Double]
  def maxVelocity: Double
  def acceleration: Double

  def stationNode(graph: NavigationGraph): TopoNode = stationNodes(graph)
  def stationLocation(graph: NavigationGraph): Double = stationLocations(graph)

  // Abstract methods
  def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double // Net-moving time, for heuristics use
  def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph, trafficPattern: ElevatorTrafficPattern = Flat): Double // Includes waiting time // TODO: Refactor with better type constraint
  def canArriveAt(target: NavigationGraph): Boolean // eg. An elevator canServe the floor input iff hasStation && hasPermission
  def canDepartFrom(source: NavigationGraph): Boolean
  def distanceBetweenStations(a: NavigationGraph, b: NavigationGraph): Double
}




case class StairCase(
  identifier: String,
  stationNodes: Map[NavigationGraph, TopoNode],
  stationLocations: Map[NavigationGraph, Double],
  stationRunIndices: Map[NavigationGraph, Int], // For turn-around loss calculation, indicating which flight of stairs the station is on. Usually starts from 0 at the bottom.
  turnAroundLoss: Double // Additional time loss for "turning-around" between different flights in the staircase
) extends LinearTransport {

  override def maxVelocity: Double = 0.0
  override def acceleration: Double = 0.0


  override def canArriveAt(target: NavigationGraph): Boolean = {
    true // Stairs always allow you to arrive. Constraints are managed by relevant atomic-paths
  }

  override def canDepartFrom(source: NavigationGraph): Boolean = {
    true // Stairs always allow you to depart. Constraints are managed by relevant atomic-paths
  }

  override def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double = {
    val distance = distanceBetweenStations(src, dst)
    distance / 0.167 // Assume average vertical speed of 0.167 m/s for stairs (a.k.a. Covering 100m height in 10 minutes)
  }

  def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph, verticalSpeed: Double): Double = {
    val distance = distanceBetweenStations(src, dst) // Add turn-around loss if changing flights
    distance / verticalSpeed + turnAroundLoss * Math.max(0, Math.abs(stationRunIndices(src) - stationRunIndices(dst)) - 1)
  }

  override def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph, trafficPattern: ElevatorTrafficPattern): Double = {
    netTimeBetweenStations(src, dst) // Climbing stairs does not involve randomness
  }

  override def distanceBetweenStations(a: NavigationGraph, b: NavigationGraph): Double = {
    Math.abs(stationLocations(a) - stationLocations(b))
  }
}




case class ElevatorBank(
  identifier: String,
  stationNodes: Map[NavigationGraph, TopoNode],
  stationLocations: Map[NavigationGraph, Double],
  stationPermissions: Map[NavigationGraph, TransportServicePermission],
  stationPopulations: Map[NavigationGraph, Int] = Map.empty,
  departureRate: Map[NavigationGraph, Double] = Map.empty,
  stationCategories: Map[NavigationGraph, ElevatorStationCategory] = Map.empty,
  maxVelocity: Double,
  acceleration: Double,
  carAmount: Int = 1,
  capacity: Int = 21,
  duty: Int = 1600,
  dwellTime: Double = 15.0
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

    netTimeToCoverDistance(distance)

//    // 1. Calculate the distance needed to reach max velocity (acceleration phase)
//    val accelerationDistance = (maxVelocity * maxVelocity) / (2 * acceleration)
//
//    // 2. Check if the elevator can reach max velocity for this distance
//    if (distance >= 2 * accelerationDistance) {
//      // Case 1: Enough distance for full acceleration + constant velocity + full deceleration
//      // Time = acceleration_time + constant_velocity_time + deceleration_time
//      val accelerationTime = maxVelocity / acceleration
//      val constantVelocityDistance = distance - 2 * accelerationDistance
//      val constantVelocityTime = constantVelocityDistance / maxVelocity
//      accelerationTime * 2 + constantVelocityTime
//    } else {
//      // Case 2: Not enough distance to reach max velocity
//      // The elevator will accelerate for half the distance, then decelerate for the other half
//      val halfDistance = distance / 2.0
//
//      // Calculate maximum speed achievable in half the distance
//      // Using: v² = u² + 2as, where u=0
//      val maxAchievableVelocity = math.sqrt(2 * acceleration * halfDistance)
//
//      // Ensure we don't exceed maxVelocity
//      val actualMaxVelocity = math.min(maxAchievableVelocity, maxVelocity)
//
//      // Time for acceleration phase + deceleration phase
//      val accelerationTime = actualMaxVelocity / acceleration
//      accelerationTime * 2
//    }
  }

  private def netTimeToCoverDistance(distance: Double): Double = {
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
  override def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph, trafficPattern: ElevatorTrafficPattern): Double = {
    waitingTime(trafficPattern, src) + netTimeBetweenStations(src, dst)
  }

  // TODO: Consider deleting this overload?
  private def waitingTime(trafficPattern: ElevatorTrafficPattern): Double = {
    if trafficPattern == Flat then idleStateWaitingTime()
    else roundTripTime(trafficPattern) / (carAmount * 2)
  }

  private def waitingTime(trafficPattern: ElevatorTrafficPattern, departureFloor: NavigationGraph): Double = {
    if trafficPattern == Flat then idleStateWaitingTime(departureFloor)
    else roundTripTime(trafficPattern) / (carAmount * 2)
  }

  private def idleStateWaitingTime(): Double = {
    val expectedNearestElevatorFloorDiff = stationLocations.size / (carAmount * 4) // Assume all elevator cars uniformly distributed across the building
    // println(s"Nearest elevator expected to be ${expectedNearestElevatorFloorDiff} floors away")
    val stations = orderedStations()
    if (stations.isEmpty || stations.size == 1) then throw RuntimeException("Elevator must have at least 2 stations")

    var totalHeight = 0.0
    for (i <- 0 until stations.size - 1) {
      totalHeight += floorHeight(i)
    }
    val avgFloorHeight = totalHeight / (stations.size - 1)

    // println(s"Static idleWaitingTime: ${netTimeToCoverDistance(expectedNearestElevatorFloorDiff * avgFloorHeight)}")
    netTimeToCoverDistance(expectedNearestElevatorFloorDiff * avgFloorHeight)
  }

  private def idleStateWaitingTime(departureFloor: NavigationGraph): Double = {
    val carDistributionGap = stationLocations.size / carAmount

    // For example, a 25-story building with 5 elevator, then elevator distributed at 2, 7, 12, 17, 22 (at every place, no more than waiting for 25/(5*2) = 2.5 floors)
    // So calculate this real distribution, and find actualNearestElevatorFloorDiff by finding the nearest elevator to the departureFloor. This is more accurate than the static estimation above.
    val elevatorPositions = (0 until carAmount).map(i => (i * carDistributionGap + carDistributionGap / 2) % stationLocations.size).toList // Midpoint distribution
    // println(s"elevatorPositions for Flat pattern: $elevatorPositions")
    // Find the nearest one
    val stations = orderedStations()
    val departureFloorIndex = stations.indexOf(departureFloor)
    val actualNearestElevatorFloorDiff = elevatorPositions.map(pos => Math.abs(pos - departureFloorIndex)).min
    // println(s"Nearest elevator at ${actualNearestElevatorFloorDiff} floors away")

    if (stations.isEmpty || stations.size == 1) then throw RuntimeException("Elevator must have at least 2 stations")

    var totalHeight = 0.0
    for (i <- 0 until stations.size - 1) {
      totalHeight += floorHeight(i)
    }
    val avgFloorHeight = totalHeight / (stations.size - 1)

    // println(s"idleWaitingTime: ${netTimeToCoverDistance(actualNearestElevatorFloorDiff * avgFloorHeight)}")
    netTimeToCoverDistance(actualNearestElevatorFloorDiff * avgFloorHeight)
  }
  
  private def roundTripTime(trafficPattern: ElevatorTrafficPattern): Double = {

    trafficPattern match {
      case UpRush => {
        upTime() + downTime() + stops() * dwellTime
      }
      case DownRush => {
        999
      } // TODO: Implement
      case BidirectionalRush => {
        999
      }
      case _ => throw new RuntimeException("Illegal traffic pattern for round trip time calculation")
    }

  }
  
  private def upTime(): Double = {
    val stations = orderedStations() // Sorted station list according to height
//    println("Floor Sequence: " + stations.map(_.identifier).mkString(", "))
//    println("Floor DepartureRate: " + stations.map(s => s"${s.identifier}:${departureRate(s)}").mkString(", "))
//    println("Floor population: " + stations.map(s => s"${s.identifier}:${stationPopulations(s) / populationSum().toDouble}").mkString(", "))
    val n = stations.length
    var time = 0.0
    var totalProb = 0.0

    for (i <- 0 until n - 1 ) {
      for (j <- i + 1 until n) {
        val start = stations(i)
        val end = stations(j)
        val prob = upPathCandidateProbability(i, j, capacity)
        val netTime = netTimeBetweenStations(start, end)
        val contribution = prob * netTime

        // println(s"${start.identifier}->${end.identifier}: prob=$prob, netTime=$netTime, contribution=$contribution")

        time += contribution
        totalProb += prob
      }
    }

    // println(s"Sum of Probs: $totalProb")
    // println(s"upTime: $time")
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

    // println(s"l1=$l1, l2=$l2, l3=$l3, l4=$l4, r1=$r1, r2=$r2, r3=$r3, r4=$r4")

    Math.pow(l1 * r1, c)
      - Math.pow(l2 * r2, c)
        - Math.pow(l3 * r3, c)
          + Math.pow(l4 * r4, c)
  }
  
  private def downTime(): Double = {
    val expectedDistance = downPathHighRevPointToRefPointDistance() + downPathLowRevPointToRefPointDistance()
    // println(s"downTime: ${netTimeToCoverDistance(expectedDistance)}")
    netTimeToCoverDistance(expectedDistance)
  }
  
  private def stops(): Double = {

    val entranceStations = orderedEntranceStations()
    val occupantStations = orderedOccupantStations()

    var expectedNonStopsInOccupantFloors: Double = 0
    var expectedNonStopsInEntranceFloors: Double = 0

    val totalPopulation = populationSum()

    for (i <- occupantStations.indices){
      val prob = if (totalPopulation > 0) stationPopulations.getOrElse(occupantStations(i), 0).toDouble / totalPopulation.toDouble else 0.0
      expectedNonStopsInOccupantFloors += Math.pow(1.0 - prob, capacity)
    }

    for (i <- entranceStations.indices){
      expectedNonStopsInEntranceFloors += Math.pow(1.0 - departureRate.getOrElse(entranceStations(i), 0.0), capacity)
    }

    val result = (occupantStations.length - expectedNonStopsInOccupantFloors) + (entranceStations.length - expectedNonStopsInEntranceFloors)
    // println(s"Expected Stops: ${result}")
    result
  }

  private def populationSum(): Int = {
    stationPopulations.values.sum
  }

  // Stations in ascending order of relativeLocation
  def orderedStations(): List[NavigationGraph] = {
    stationLocations.toList.sortBy(_._2).map(_._1)
  }

  // Entrance stations in ascending order of relativeLocation
  def orderedEntranceStations(): List[NavigationGraph] = {
    stationLocations.toList.filter(s => stationCategories.getOrElse(s._1, throw new RuntimeException("Missing stationCategories data for RTT estimation")) == Entrance).sortBy(_._2).reverse.map(_._1)
  }

  // Occupant stations in ascending order of relativeLocation
  def orderedOccupantStations(): List[NavigationGraph] = {
    stationLocations.toList.filter(s => stationCategories.getOrElse(s._1, throw new RuntimeException("Missing stationCategories data for RTT estimation")) == Occupant).sortBy(_._2).map(_._1)
  }

  override def distanceBetweenStations(a: NavigationGraph, b: NavigationGraph): Double = {
    Math.abs(stationLocations(a) - stationLocations(b))
  }

  private def floorHeight(floorIndex: Int): Double = {
    val stations = orderedStations()
    distanceBetweenStations(stations(floorIndex), stations(floorIndex + 1))
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

      if (stationPopulations.contains(intermediateStation)) {
        sumAlightingRates += stationPopulations(stations(k)).toDouble / populationSum().toDouble
      }
    }

    1.0 - sumAlightingRates
  }

  private def downPathHighRevPointToRefPointDistance(): Double = {
    val occupantStations = orderedOccupantStations()
    val allStations = orderedStations()
    val totalPopulation = populationSum().toDouble

    var result = 0.0

    // We iterate through occupant stations to calculate the expected high reversal point
    // The formula is essentially Sum(h_i * P(H >= i))
    // We interpret the user's pseudocode:
    // floorHeightOf(occupantStations(i)) * (1 - Math.pow((sumOf(j is_in [0,i), population(occupantStations(j)) / sumOfPopulation()), c))

    for (i <- 0 until occupantStations.length - 1) { // Iterate up to second to last, as floorHeight is between i and i+1
       val currentFloor = occupantStations(i)
       val currentFloorIndex = orderedStations().indexOf(currentFloor)


       // Calculate "floor height" which is distance between this floor and next (in the occupant list? or physical?)
       // The pseudocode implies iterating through floors and adding contribution.
       // Usually H = Sum_{i=1}^{N} h_i * P(H >= i)
       // Here "floorHeightOf(occupantStations(i))" likely means the height segment associated with floor i.
       // Given the loop `0 until occupantStations.length - 1`, it seems to be summing segments between occupant floors.

//       val heightOfFloor = distanceBetweenStations(currentFloor, nextFloor)
       val heightOfFloor = floorHeight(currentFloorIndex) // Height of the floor segment associated with occupant station i
       // Calculate sum of population for floors below (or equal to?) index i
       // The pseudocode says: sumOf(j is_in [0,i), ...). Range [0, i) means 0, 1, ..., i-1.
       // So it is sum of populations of floors strictly below current floor i in the occupant list.

       var populationSumBelow = 0.0
       for (j <- 0 until i + 1) { // Adjusted to match typical formulas: P(dest <= i). If loop is [0, i), it is P(dest < i).
         // If term is (1 - (sum/total)^c), then (sum/total)^c is P(all passengers <= i).
         // So sum should include i.
         // Let's stick to the user hint: "sumOf(j is_in [0,i)" which usually means excluding i in 0-based indexing if it was an int range,
         // but wait, if it says "j is_in [0,i)", let's look at mathematical notation. [0, i) usually excludes i.
         // However, standard formula for Expected Reversal Floor H = Sum ( P(H > i) ) = Sum ( 1 - P(H <= i) ).
         // P(H <= i) is probability that all passengers align at or below floor i.
         // So we need sum of populations for floors 0..i.

         populationSumBelow += stationPopulations.getOrElse(occupantStations(j), 0)
       }

       val probAllPassengersAlightBelowOrAtI = Math.pow(populationSumBelow / totalPopulation, capacity)
       val probReversalAboveI = 1.0 - probAllPassengersAlightBelowOrAtI

       result += heightOfFloor * probReversalAboveI
    }
    // println(s"dH value = $result")
    result
  }

  private def downPathLowRevPointToRefPointDistance(): Double = {
    val entranceStations = orderedEntranceStations() // Sorted by height (low to high)

    if (entranceStations.length <= 1) {
      val currentStation = entranceStations.head
      val currentStationIndex = orderedStations().indexOf(currentStation)
      // println(s"dL value = ${floorHeight(currentStationIndex)}")
      return floorHeight(currentStationIndex)
    }

    var expectedDistance = 0.0

    // We iterate from bottom (0) to top (length-1) of entrance stations.
    for (i <- entranceStations.indices) {
      val currentStation = entranceStations(i)
      val currentStationIndex = orderedStations().indexOf(currentStation)

      val dist = floorHeight(currentStationIndex)

      // Probability that Low Reversal is HIGHER than i.
      // i.e. Minimum destination > i.
      // i.e. NO ONE wants to go to floors 0..i.
      // i.e. All passengers want to go to floors > i.

      var probNoStopAtOrBelowI = 1.0
      for (j <- 0 to i) {
        probNoStopAtOrBelowI *= (1.0 - departureRate.getOrElse(entranceStations(j), 0.0))
      }

      expectedDistance += dist * probNoStopAtOrBelowI
    }

    // println(s"dL value = $expectedDistance")

    expectedDistance
  }
}