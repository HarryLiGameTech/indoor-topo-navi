package navigation

import data.{NavigationGraph, TopoNode, TransportGraph, NavigatablePath, NavigationPath}
import enums.NavigationError
import enums.NavigationError.{InvalidData, NoRouteFound}

class RoutePlanner private(graphs: Map[String, NavigationGraph], transportGraph: TransportGraph, isHighRise: Boolean) {

  def navigate(
    sourceGraphName: String,
    goalGraphName: String,
    sourceNodeName: String,
    goalNodeName: String,
    visitingMode: enums.VisitingMode
  ): Either[NavigationError, NavigatablePath] = {
    // This would involve combining intra-map paths and transportation paths
    // to create a complete route from startNode in startGraph to goalNode in goalGraph

    (graphs.get(sourceGraphName), graphs.get(goalGraphName)) match {
      case (Some(sourceGraph), Some(goalGraph)) =>
        (sourceGraph.nodes.find(_.identifier == sourceNodeName),
          goalGraph.nodes.find(_.identifier == goalNodeName)) match {
          case (Some(sourceNode), Some(goalNode)) =>
            // TODO: Route planning logic goes here
            Right(NavigationPath(List.empty, List.empty))
          case (None, _) => Left(InvalidData(s"sourceGraphName and sourceNodeName: ${sourceGraphName}, ${sourceNodeName} not found"))
          case (_, None) => Left(InvalidData(s"goalGraphName and goalNodeName: ${goalGraphName}, ${goalNodeName} not found"))
        }
      case (None, _) => Left(InvalidData(s"sourceGraphName: ${sourceGraphName} not found"))
      case (_, None) => Left(InvalidData(s"goalGraphName: ${goalGraphName} not found"))
    }

  }

  def visualizeGraphs(): String = {
    graphs.keys.mkString("RoutePlanner Graphs: [", ", ", "]")
  }

  override def toString: String = {
    s"RoutePlanner with ${graphs.size}, isHighRise=$isHighRise)"
  }

}