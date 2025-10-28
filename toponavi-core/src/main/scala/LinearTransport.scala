import enums.TransportServicePermission

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
  maxVelocity: Double,
  acceleration: Double
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

  override def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double = {
    40 + netTimeBetweenStations(src, dst)
  }

}