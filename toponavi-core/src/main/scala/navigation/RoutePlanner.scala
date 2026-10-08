package navigation

import data.{GlobalNode, NavigationGraph, NavigationOutputPath, TransportGraph}
import enums.{NavigationError, RoutePlanningPreferences}
import enums.NavigationError.{DestinationHasBannedTags, InvalidData}

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
    visitingMode: enums.VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy = TraversalTagPolicy.AllowAll,
    allowUncertainAccess: Boolean = false,
    minimizeTag: Option[String] = None,
    customObjective: Option[CustomRouteObjective] = None
  ): Either[NavigationError, NavigationOutputPath] = {
    (graphs.get(sourceGraphName), graphs.get(goalGraphName)) match {
      case (Some(sourceGraph), Some(goalGraph)) =>
        (sourceGraph.nodes.find(_.identifier == sourceNodeName),
          goalGraph.nodes.find(_.identifier == goalNodeName)) match {
          case (Some(sourceNode), Some(goalNode)) =>
            val destinationConflicts = tagPolicy.conflictingTags(goalNode)
            if destinationConflicts.nonEmpty then {
              Left(DestinationHasBannedTags(
                s"${goalGraph.identifier}::${goalNode.identifier}",
                destinationConflicts.toList.sorted
              ))
            }
            else {
              new TraversalPreferencePlanner(graphs, transportGraph, transportFirst = isHighRise).navigate(
                GlobalNode(sourceGraph, sourceNode), GlobalNode(goalGraph, goalNode),
                visitingMode, preference, tagPolicy, allowUncertainAccess,
                minimizeTag.map(_.trim).filter(_.nonEmpty), customObjective)
            }
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

object RoutePlanner {
  def apply(
    graphs: Map[String, NavigationGraph],
    transportGraph: TransportGraph,
    subMapNames: List[String],
    isHighRise: Boolean
  ): RoutePlanner = new RoutePlanner(graphs, transportGraph, subMapNames, isHighRise)
}

def trim(stationNodeId: String): String = {
  // Assuming the format is "LineID@GraphID", we split by "@" and take the first part
  stationNodeId.split("@").headOption.getOrElse(stationNodeId)
}
