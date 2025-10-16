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
  def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double
  def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double
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

}