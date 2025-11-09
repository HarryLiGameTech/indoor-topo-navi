package navigation

import data.{GlobalNode, NavigatablePath, NavigationGraph, NavigationOutputPath, RouteEdge, StationNode, TopoNode, TransportGraph}
import enums.{NavigationError, RouteEdgeCategory}
import enums.NavigationError.{InvalidData, NoRouteFound}

import scala.collection.mutable

class RoutePlanner private(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  subMapNames: List[String], // TODO: Consider getting rid of this
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
    // Prioritizing elevators, escalators, as T_trans >> T_walk, catching a coming ride saves much more time than over-optimizing walking paths
    
    if sourceGraph.identifier == goalGraph.identifier then { // Intra-map pathfinding within the same graph
      sourceGraph.findPath(sourceNode, goalNode, visitingMode) match {
        case Some(intraPath) =>
          // Convert IntraMapPath to NavigationOutputPath
          val globalNodes = intraPath.routeNodes.map(node => GlobalNode.fromTopoNode(sourceGraph, node))
          val routeEdges = intraPath.routeEdges.map(edge => RouteEdge.fromAtomicPath(sourceGraph, edge, visitingMode))
          Right(NavigationOutputPath(globalNodes, routeEdges))
        case None => Left(NoRouteFound(s"No intra-map route found within high-rise building ${sourceGraph.identifier} from ${sourceNode.identifier} to ${goalNode.identifier}"))
      }
    }
    else{ // Collect all path segments between interchange nodes
      val allRouteEdges = mutable.ListBuffer[RouteEdge]()
      val allGlobalNodes = mutable.ListBuffer[GlobalNode]()
      val interchangeNodes: List[StationNode] = transportGraph.findPathFuzzy(sourceGraph, goalGraph, subMapNames, 0).routeNodes
      
      // 1. Add the starting point (sourceNode to first interchange)
      sourceGraph.findPath(sourceNode, interchangeNodes.head.localNode, visitingMode) match {
        case Some(startPath) =>
          allGlobalNodes ++= startPath.routeNodes.map(GlobalNode.fromTopoNode(sourceGraph, _))
          allRouteEdges ++= startPath.routeEdges.map(RouteEdge.fromAtomicPath(sourceGraph, _, visitingMode))
        case None =>
          return Left(NoRouteFound("Cannot reach first interchange point from source"))
      }

      // 2. Add paths between interchange nodes - only when on the same floor
      for (i <- 0 until interchangeNodes.size - 1) {
        val currentInterchange = interchangeNodes(i)
        val nextInterchange = interchangeNodes(i + 1)

        if (currentInterchange.ownerGraph == nextInterchange.ownerGraph) {
          // Same floor - need pathfinding between them
          currentInterchange.ownerGraph.findPath(
            currentInterchange.localNode,
            nextInterchange.localNode,
            visitingMode
          ) match {
            case Some(path) =>
              // Skip the first node to avoid duplicates
              allGlobalNodes ++= path.routeNodes.tail.map(GlobalNode.fromTopoNode(currentInterchange.ownerGraph, _))
              allRouteEdges ++= path.routeEdges.map(RouteEdge.fromAtomicPath(currentInterchange.ownerGraph, _, visitingMode))
            case None =>
              return Left(NoRouteFound(s"Cannot navigate between interchange points ${i} and ${i + 1} on same floor"))
          }
        } else {
          // Different floors - this is a vehicle-ride, so create direct transport edge
          val transportEdge = RouteEdge(
            source = GlobalNode(currentInterchange.ownerGraph, currentInterchange.localNode),
            target = GlobalNode(nextInterchange.ownerGraph, nextInterchange.localNode),
            cost = currentInterchange.ownerLine.travelTimeBetweenStations(
              currentInterchange.ownerGraph,
              nextInterchange.ownerGraph
            ),
            category = RouteEdgeCategory.Transport,
            movementDescription = s"Take ${currentInterchange.ownerLine.identifier} from ${currentInterchange.ownerGraph.identifier} to ${nextInterchange.ownerGraph.identifier}"
          )
          allRouteEdges += transportEdge
          allGlobalNodes += GlobalNode(nextInterchange.ownerGraph, nextInterchange.localNode) // Add the target node
        }
      }
      
      // 3. Add the final segment (last interchange to goalNode) - only if on same floor
      if (interchangeNodes.last.ownerGraph == goalGraph) {
        goalGraph.findPath(interchangeNodes.last.localNode, goalNode, visitingMode) match {
          case Some(finalPath) =>
            // Skip the first node to avoid duplicates
            allGlobalNodes ++= finalPath.routeNodes.tail.map(GlobalNode.fromTopoNode(goalGraph, _))
            allRouteEdges ++= finalPath.routeEdges.map(RouteEdge.fromAtomicPath(goalGraph, _, visitingMode))
          case None =>
            return Left(NoRouteFound("Cannot reach goal from last interchange point on same floor"))
        }
      } else {
        // Last interchange and goal are on different floors - need additional elevator
        val transportEdge = RouteEdge(
          source = GlobalNode(interchangeNodes.last.ownerGraph, interchangeNodes.last.localNode),
          target = GlobalNode(goalGraph, goalNode), // Assuming goalNode needs to be converted to GlobalNode
          cost = interchangeNodes.last.ownerLine.travelTimeBetweenStations(
            interchangeNodes.last.ownerGraph,
            goalGraph
          ),
          category = RouteEdgeCategory.Transport,
          movementDescription = s"Take ${interchangeNodes.last.ownerLine.identifier} from ${interchangeNodes.last.ownerGraph.identifier} to ${goalGraph.identifier}"
        )
        allRouteEdges += transportEdge
        allGlobalNodes += GlobalNode(goalGraph, goalNode)
      }

      Right(NavigationOutputPath(allGlobalNodes.toList, allRouteEdges.toList))
    }
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

def trim(stationNodeId: String): String = {
  // Assuming the format is "LineID@GraphID", we split by "@" and take the first part
  stationNodeId.split("@").headOption.getOrElse(stationNodeId)
}