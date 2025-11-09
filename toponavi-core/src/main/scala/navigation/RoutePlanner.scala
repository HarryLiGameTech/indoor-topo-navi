package navigation

import data.{GlobalNode, NavigatablePath, NavigationGraph, NavigationOutputPath, RouteEdge, TopoNode, TransportGraph}
import enums.NavigationError
import enums.NavigationError.{InvalidData, NoRouteFound}

class RoutePlanner private(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  isHighRise: Boolean
) {


  def navigate(
    sourceGraphName: String,
    goalGraphName: String,
    sourceNodeName: String,
    goalNodeName: String,
    visitingMode: enums.VisitingMode
  ): Either[NavigationError, NavigationOutputPath] = {
    // This would involve combining intra-map paths and transportation paths
    // to create a complete route from startNode in startGraph to goalNode in goalGraph

    (graphs.get(sourceGraphName), graphs.get(goalGraphName)) match {
      case (Some(sourceGraph), Some(goalGraph)) =>
        (sourceGraph.nodes.find(_.identifier == sourceNodeName),
          goalGraph.nodes.find(_.identifier == goalNodeName)) match {
          case (Some(sourceNode), Some(goalNode)) =>
            // TODO: Route planning logic goes here
            if isHighRise then{
              findRouteForHighRiseBuilding(sourceGraph, sourceNode, goalGraph, goalNode, visitingMode)
            }
            else{
              findRouteForStandardBuilding(sourceGraph, sourceNode, goalGraph, goalNode, visitingMode)
            }
          case (None, _) => Left(InvalidData(s"sourceGraphName and sourceNodeName: ${sourceGraphName}, ${sourceNodeName} not found"))
          case (_, None) => Left(InvalidData(s"goalGraphName and goalNodeName: ${goalGraphName}, ${goalNodeName} not found"))
        }
      case (None, _) => Left(InvalidData(s"sourceGraphName: ${sourceGraphName} not found"))
      case (_, None) => Left(InvalidData(s"goalGraphName: ${goalGraphName} not found"))
    }

  }

  private def findRouteForHighRiseBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode
  ): Either[NavigationError, NavigationOutputPath] = {
    // High-rise specific route planning logic
    // Prioritizing elevators, escalators, as T_trans >> T_walk, catching a coming ride is vital saves much more time than over-optimizing walking paths
    if sourceGraph.identifier == goalGraph.identifier then {
      // Intra-map pathfinding within the same high-rise building
      sourceGraph.findPath(sourceNode, goalNode, visitingMode) match {
        case Some(intraPath) =>
          // Convert IntraMapPath to NavigationOutputPath
          val globalNodes = intraPath.routeNodes.map(node => GlobalNode.fromTopoNode(sourceGraph, node))
          val routeEdges = intraPath.routeEdges.map(edge => RouteEdge.fromAtomicPath(sourceGraph, edge, visitingMode))
          return Right(NavigationOutputPath(globalNodes, routeEdges))
        case None => return Left(NoRouteFound(s"No intra-map route found within high-rise building ${sourceGraph.identifier} from ${sourceNode.identifier} to ${goalNode.identifier}"))
      }
    }
    else{

    }
    Left(NoRouteFound("Failed to navigate in high-rise building"))
  }

  private def findRouteForStandardBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode
  ): Either[NavigationError, NavigationOutputPath] = {
    // TODO: Implement standard building route planning logic here
    Left(NoRouteFound("Route planning for standard buildings not yet implemented"))
  }


  def visualizeGraphs(): String = {
    graphs.keys.mkString("RoutePlanner Graphs: [", ", ", "]")
  }

  override def toString: String = {
    s"RoutePlanner with ${graphs.size}, isHighRise=$isHighRise)"
  }

}