trait Vehicle {
  def stationNodes: Map[NavigationGraph, TopoNode]
  def stationLocations: Map[NavigationGraph, Double]
  def maxVelocity: Double
  def acceleration: Double

  def stationNode(graph: NavigationGraph): TopoNode = stationNodes(graph)
  def stationLocation(graph: NavigationGraph): Double = stationLocations(graph)

  // Abstract methods
  def netTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double
  def travelTimeBetweenStations(src: NavigationGraph, dst: NavigationGraph): Double
  def canServe(requirements: Any*): Boolean // eg. An elevator canServe the floor input iff hasStation && hasPermission
}

case class Elevator(
  stationNodes: Map[NavigationGraph, TopoNode],
  stationLocations: Map[NavigationGraph, Double],
  maxVelocity: Double,
  acceleration: Double
) extends Vehicle {
  
  override def canServe(requirements: Any*): Boolean = requirements match {
    case Seq(floor: NavigationGraph, permission: Boolean) =>
      stationNodes.contains(floor) && permission
    case Seq(graph: NavigationGraph) =>
      stationNodes.contains(graph) // Fallback behavior
    case _ => false
  }
  
}