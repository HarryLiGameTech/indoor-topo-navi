package navigation

import data.{GlobalNode, NavigationGraph, NavigationOutputPath, RouteEdge, StairCase, TopoNode, TransportGraph, UncertainAccess}
import enums.{AttributeValue, NavigationError, RouteEdgeCategory, RoutePlanningPreferences}
import enums.NavigationError.{DestinationHasBannedTags, InvalidData, NoRouteFound}
import enums.ElevatorTrafficPattern.UpRush
import enums.ElevatorTrafficPattern.Flat

import scala.collection.mutable
import scala.util.boundary.break

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
    allowUncertainAccess: Boolean = false
  ): Either[NavigationError, NavigationOutputPath] = {
    // This would involve combining intra-map paths and transportation paths
    // to create a complete route from startNode in startGraph to goalNode in goalGraph

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
            else if isHighRise then{
              findRouteForHighRiseBuilding(
                sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
                preference, tagPolicy, allowUncertainAccess)
            }
            else{
              findRouteForFullyInformedLowRiseBuilding(
                sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
                preference, tagPolicy, allowUncertainAccess)
            }
          case (None, _) => Left(InvalidData(s"sourceGraphName and sourceNodeName: ${sourceGraphName}, ${sourceNodeName} not found"))
          case (_, None) => Left(InvalidData(s"goalGraphName and goalNodeName: ${goalGraphName}, ${goalNodeName} not found"))
        }
      case (None, _) => Left(InvalidData(s"sourceGraphName: ${sourceGraphName} not found"))
      case (_, None) => Left(InvalidData(s"goalGraphName: ${goalGraphName} not found"))
    }

  }

  /**
   * Low-rise hierarchical routing with exact, end-to-end costs.
   *
   * The hierarchy consists of all transport stations plus the exact source and
   * destination. In particular, the source is treated just like an interchange:
   * it has an exact intra-floor shortest-path edge to every reachable station on
   * its floor. This means the selected route includes the cost of getting to the
   * first ride (and from the last ride to the destination), unlike fuzzy
   * transport-only routing.
   */
  private def findRouteForFullyInformedLowRiseBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): Either[NavigationError, NavigationOutputPath] = {
    if sourceGraph.identifier == goalGraph.identifier then {
      return sourceGraph.findPath(
        sourceNode, goalNode, visitingMode, tagPolicy,
        allowBannedStart = true,
        allowUncertainAccess = allowUncertainAccess
      ) match {
        case Some(path) =>
          Right(NavigationOutputPath(
            path.routeNodes.map(GlobalNode.fromTopoNode(sourceGraph, _)),
            path.routeEdges.map(RouteEdge.fromAtomicPath(sourceGraph, _, visitingMode))
          ))
        case None => Left(NoRouteFound(
          s"No intra-map route found within low-rise building ${sourceGraph.identifier} " +
            s"from ${sourceNode.identifier} to ${goalNode.identifier}"))
      }
    }

    type HierarchyNode = (NavigationGraph, TopoNode)

    sealed trait HierarchyHop {
      def parent: HierarchyNode
      def cost: Double
    }
    case class IntraFloorHop(
      parent: HierarchyNode,
      graph: NavigationGraph,
      path: data.IntraMapPath
    ) extends HierarchyHop {
      override val cost: Double = path.totalCost(visitingMode)
    }
    case class TransportHop(
      parent: HierarchyNode,
      cost: Double,
      category: RouteEdgeCategory,
      description: String,
      uncertainAccess: Option[UncertainAccess]
    ) extends HierarchyHop

    def resolveStation(station: data.StationNode): Option[HierarchyNode] =
      graphs.get(station.ownerGraph.identifier).flatMap { graph =>
        graph.nodes.find(_.identifier == station.localNode.identifier).map(graph -> _)
      }

    val start: HierarchyNode = sourceGraph -> sourceNode
    val goal: HierarchyNode = goalGraph -> goalNode
    val stationNodes = transportGraph.nodes.flatMap(resolveStation)
    val hierarchyNodes = (start :: goal :: stationNodes).distinct
    val nodesByGraph = hierarchyNodes.groupBy(_._1.identifier)

    // The search is deliberately Dijkstra rather than heuristic A*: every
    // intra-floor hierarchy edge already has its exact shortest-path cost.
    implicit val ordering: Ordering[(HierarchyNode, Double)] =
      Ordering.by[(HierarchyNode, Double), Double](_._2).reverse
    val openSet = mutable.PriorityQueue.empty[(HierarchyNode, Double)]
    val distance = mutable.Map[HierarchyNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val cameFrom = mutable.Map[HierarchyNode, HierarchyHop]()
    val visited = mutable.Set[HierarchyNode]()
    val intraFloorPathCache = mutable.Map[(HierarchyNode, HierarchyNode), Option[data.IntraMapPath]]()

    distance(start) = 0.0
    openSet.enqueue(start -> 0.0)

    while openSet.nonEmpty && !visited.contains(goal) do {
      val (current, currentDistance) = openSet.dequeue()
      if currentDistance <= distance(current) && !visited.contains(current) then {
        visited.add(current)
        val (currentGraph, currentNode) = current

        // Fully-informed walking edges to every hierarchy node on this floor.
        for target <- nodesByGraph.getOrElse(currentGraph.identifier, List.empty)
            if target != current && !visited.contains(target) && tagPolicy.allowsEntry(target._2)
        do {
          val path = intraFloorPathCache.getOrElseUpdate(
            current -> target,
            currentGraph.findPath(
              currentNode,
              target._2,
              visitingMode,
              tagPolicy,
              allowBannedStart = current == start,
              allowUncertainAccess = allowUncertainAccess
            )
          )
          path.foreach { intraPath =>
            val candidateDistance = currentDistance + intraPath.totalCost(visitingMode)
            if candidateDistance < distance(target) then {
              distance(target) = candidateDistance
              cameFrom(target) = IntraFloorHop(current, currentGraph, intraPath)
              openSet.enqueue(target -> candidateDistance)
            }
          }
        }

        // A physical station can belong to multiple transport lines. Explore
        // every matching line, but leave same-floor transfers to the exact
        // intra-floor search above.
        for station <- transportGraph.nodes
            if station.ownerGraph.identifier == currentGraph.identifier &&
              station.localNode.identifier == currentNode.identifier
            (neighborStation, compiledCost) <- transportGraph.adjacencyList.getOrElse(station, Map.empty)
            if neighborStation.ownerGraph.identifier != currentGraph.identifier
            (edgeCost, uncertainty) <- transportGraph.edgeTraversal(
              station, neighborStation, compiledCost, allowUncertainAccess)
            neighbor <- resolveStation(neighborStation)
            if !visited.contains(neighbor) && tagPolicy.allowsEntry(neighbor._2)
        do {
          val candidateDistance = currentDistance + edgeCost
          if candidateDistance < distance(neighbor) then {
            distance(neighbor) = candidateDistance
            val category = station.ownerLine match {
              case _: StairCase => RouteEdgeCategory.Climbing
              case _ => RouteEdgeCategory.Transport
            }
            cameFrom(neighbor) = TransportHop(
              current,
              edgeCost,
              category,
              s"Take ${station.ownerLine.identifier} from ${currentGraph.identifier} " +
                s"to ${neighborStation.ownerGraph.identifier}",
              uncertainty
            )
            openSet.enqueue(neighbor -> candidateDistance)
          }
        }
      }
    }

    if !visited.contains(goal) then {
      return Left(NoRouteFound(
        s"No fully-informed low-rise route found from " +
          s"${sourceGraph.identifier}::${sourceNode.identifier} to " +
          s"${goalGraph.identifier}::${goalNode.identifier}"))
    }

    val hops = mutable.ListBuffer[(HierarchyNode, HierarchyHop)]()
    var cursor = goal
    while cameFrom.contains(cursor) do {
      val hop = cameFrom(cursor)
      hops.prepend(cursor -> hop)
      cursor = hop.parent
    }

    val routeNodes = mutable.ListBuffer(GlobalNode(sourceGraph, sourceNode))
    val routeEdges = mutable.ListBuffer[RouteEdge]()
    hops.foreach {
      case (_, hop: IntraFloorHop) =>
        appendIntraPath(
          hop.path, hop.graph, visitingMode, routeNodes, routeEdges, skipFirstNode = true)
      case (target, hop: TransportHop) =>
        val targetGlobalNode = GlobalNode(target._1, target._2)
        routeEdges += RouteEdge(
          source = GlobalNode(hop.parent._1, hop.parent._2),
          target = targetGlobalNode,
          cost = hop.cost,
          category = hop.category,
          movementDescription = hop.description,
          uncertainAccess = hop.uncertainAccess
        )
        routeNodes += targetGlobalNode
    }

    Right(NavigationOutputPath(routeNodes.toList, routeEdges.toList))
  }

  private def findRouteForHighRiseBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): Either[NavigationError, NavigationOutputPath] = {
    // High-rise specific route planning logic
    // Prioritizing elevators, escalators, as T_trans >> T_walk, catching a coming ride saves much more time than over-optimizing walking paths
    
    if sourceGraph.identifier == goalGraph.identifier then { // Intra-map pathfinding within the same graph
      sourceGraph.findPath(
        sourceNode, goalNode, visitingMode, tagPolicy,
        allowBannedStart = true,
        allowUncertainAccess = allowUncertainAccess
      ) match {
        case Some(intraPath) =>
          // Convert IntraMapPath to NavigationOutputPath
          val globalNodes = intraPath.routeNodes.map(node => GlobalNode.fromTopoNode(sourceGraph, node))
          val routeEdges = intraPath.routeEdges.map(edge => RouteEdge.fromAtomicPath(sourceGraph, edge, visitingMode))
          Right(NavigationOutputPath(globalNodes, routeEdges))
        case None => Left(NoRouteFound(s"No intra-map route found within high-rise building ${sourceGraph.identifier} from ${sourceNode.identifier} to ${goalNode.identifier}"))
      }
    }
    else{ // Collect all path segments between interchange nodes
      findCrossGraphRouteForHighRiseBuilding(
        sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
        preference, tagPolicy, allowUncertainAccess, 0)
    }
  }

  private def findCrossGraphRouteForHighRiseBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean,
    transportSolutionIndex: Int
  ): Either[NavigationError, NavigationOutputPath] = {
    val allRouteEdges = mutable.ListBuffer[RouteEdge]()
    val allGlobalNodes = mutable.ListBuffer[GlobalNode]()
    transportGraph.findPathFuzzy(
      sourceGraph, goalGraph, subMapNames, preference,
      transportSolutionIndex, allowUncertainAccess
    ) match {
      case Some(transportPath) =>
        val interchangeNodes = transportPath.routeNodes
        println("interchangeNodes.size = " + interchangeNodes.size)

        val containsBannedInterchange = interchangeNodes.exists { stationNode =>
          val resolvedNode = graphs
            .get(stationNode.ownerGraph.identifier)
            .flatMap(_.nodes.find(_.identifier == stationNode.localNode.identifier))
            .getOrElse(stationNode.localNode)
          val isActualSource = stationNode.ownerGraph.identifier == sourceGraph.identifier &&
            resolvedNode.identifier == sourceNode.identifier
          !isActualSource && !tagPolicy.allowsEntry(resolvedNode)
        }
        if containsBannedInterchange then
          return findCrossGraphRouteForHighRiseBuilding(
            sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
            preference, tagPolicy, allowUncertainAccess, transportSolutionIndex + 1)

        // 1. Add the starting point (sourceNode to first interchange)
        sourceGraph.findPath(
          sourceNode, interchangeNodes.head.localNode, visitingMode,
          tagPolicy,
          allowBannedStart = true,
          allowUncertainAccess = allowUncertainAccess
        ) match {
          case Some(startPath) =>
            allGlobalNodes ++= startPath.routeNodes.map(GlobalNode.fromTopoNode(sourceGraph, _))
            allRouteEdges ++= startPath.routeEdges.map(RouteEdge.fromAtomicPath(sourceGraph, _, visitingMode))
          case None =>
            // Recurse with next index
            return findCrossGraphRouteForHighRiseBuilding(
              sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
              preference, tagPolicy, allowUncertainAccess, transportSolutionIndex + 1)
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
              visitingMode,
              tagPolicy,
              allowUncertainAccess = allowUncertainAccess
            ) match {
              case Some(path) =>
                // Skip the first node to avoid duplicates
                allGlobalNodes ++= path.routeNodes.tail.map(GlobalNode.fromTopoNode(currentInterchange.ownerGraph, _))
                allRouteEdges ++= path.routeEdges.map(RouteEdge.fromAtomicPath(currentInterchange.ownerGraph, _, visitingMode))
              case None =>
                // Recurse with next index
                return findCrossGraphRouteForHighRiseBuilding(
                  sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
                  preference, tagPolicy, allowUncertainAccess, transportSolutionIndex + 1)
            }
          } else {
            // Different floors - this is a vehicle-ride, so create direct transport edge
            val transportEdge = RouteEdge(
              source = GlobalNode(currentInterchange.ownerGraph, currentInterchange.localNode),
              target = GlobalNode(nextInterchange.ownerGraph, nextInterchange.localNode),
              cost = currentInterchange.ownerLine.travelTimeBetweenStations(
                currentInterchange.ownerGraph,
                nextInterchange.ownerGraph,
                UpRush
              ),
              category = RouteEdgeCategory.Transport,
              movementDescription = s"Take ${currentInterchange.ownerLine.identifier} from ${currentInterchange.ownerGraph.identifier} to ${nextInterchange.ownerGraph.identifier}",
              uncertainAccess = currentInterchange.ownerLine.uncertainAccessBetween(
                currentInterchange.ownerGraph,
                nextInterchange.ownerGraph
              )
            )
            allRouteEdges += transportEdge
            allGlobalNodes += GlobalNode(nextInterchange.ownerGraph, nextInterchange.localNode) // Add the target node
          }
        }

        // 3. Add the final segment (last interchange to goalNode) - only if on same floor
        if (interchangeNodes.last.ownerGraph == goalGraph) {
          goalGraph.findPath(
            interchangeNodes.last.localNode,
            goalNode,
            visitingMode,
            tagPolicy,
            allowUncertainAccess = allowUncertainAccess
          ) match {
            case Some(finalPath) =>
              // Skip the first node to avoid duplicates
              allGlobalNodes ++= finalPath.routeNodes.tail.map(GlobalNode.fromTopoNode(goalGraph, _))
              allRouteEdges ++= finalPath.routeEdges.map(RouteEdge.fromAtomicPath(goalGraph, _, visitingMode))
            case None =>
              // Recurse with next index
              return findCrossGraphRouteForHighRiseBuilding(
                sourceGraph, sourceNode, goalGraph, goalNode, visitingMode,
                preference, tagPolicy, allowUncertainAccess, transportSolutionIndex + 1)
          }
        } else {
          
          return Left(NoRouteFound("Final segment at fault"))
        }

        Right(NavigationOutputPath(allGlobalNodes.toList, allRouteEdges.toList))

      case None =>
        // No more transport solutions available
        Left(NoRouteFound(s"No feasible transport path found after $transportSolutionIndex attempts"))
    }
  }

  private def findRouteForStandardBuilding(
    sourceGraph: NavigationGraph,
    sourceNode: TopoNode,
    goalGraph: NavigationGraph,
    goalNode: TopoNode,
    visitingMode: enums.VisitingMode,
    preference: RoutePlanningPreferences,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): Either[NavigationError, NavigationOutputPath] = {
    // Cross-graph A* over the super-graph formed by:
    //   - all intra-graph AtomicPath edges (one direction each)
    //   - all inter-graph transport edges from transportGraph (respects canDepart/canArrive)
    //
    // Heuristic (inadmissible but acceptable for indoor environments):
    //   h = horizontal_component + vertical_component
    //   horizontal: Manhattan distance on TPCC grid (no sensitivity scaling needed — grid units ≈ cost units)
    //   vertical:   |floorIndex(src) - floorIndex(dst)| × 3.0  (3 m/floor ÷ 1 m·s⁻¹ = 3 s lower bound)
    //
    // Nodes without estimatedCoord are too trivial to be part of the TPCC grid and are therefore
    // excluded from the A* super-graph.  We first find the nearest coord-having node reachable
    // forward from sourceNode (effectiveSource) and the nearest one reachable backward from
    // goalNode (effectiveGoal), walk to/from them with plain intra-graph Dijkstra, and stitch
    // those legs around the cross-graph A* result.

    // ── Step 0: resolve effective source and goal ──────────────────────────
    val effectiveSource: TopoNode = findNearestCoordNodeForward(
      sourceGraph, sourceNode, visitingMode, tagPolicy, allowUncertainAccess
    ) match {
      case Some(n) => n
      case None    => return Left(NoRouteFound(
        s"No coord-estimated node reachable from ${sourceGraph.identifier}::${sourceNode.identifier}"))
    }
    val effectiveGoal: TopoNode = findNearestCoordNodeBackward(
      goalGraph, goalNode, visitingMode, tagPolicy, allowUncertainAccess
    ) match {
      case Some(n) => n
      case None    => return Left(NoRouteFound(
        s"No coord-estimated node that can reach ${goalGraph.identifier}::${goalNode.identifier}"))
    }

    // Prefix leg: sourceNode → effectiveSource  (empty if they are the same)
    val prefixPath: Option[data.IntraMapPath] =
      if (sourceNode == effectiveSource) None
      else sourceGraph.findPath(
        sourceNode, effectiveSource, visitingMode, tagPolicy,
        allowBannedStart = true,
        allowUncertainAccess = allowUncertainAccess
      ) match {
        case Some(p) => Some(p)
        case None    => return Left(NoRouteFound(
          s"Cannot build prefix leg from ${sourceNode.identifier} to ${effectiveSource.identifier}"))
      }

    // Suffix leg: effectiveGoal → goalNode  (empty if they are the same)
    val suffixPath: Option[data.IntraMapPath] =
      if (effectiveGoal == goalNode) None
      else goalGraph.findPath(
        effectiveGoal, goalNode, visitingMode, tagPolicy,
        allowUncertainAccess = allowUncertainAccess
      ) match {
        case Some(p) => Some(p)
        case None    => return Left(NoRouteFound(
          s"Cannot build suffix leg from ${effectiveGoal.identifier} to ${goalNode.identifier}"))
      }

    type LowRiseGlobalNode = (NavigationGraph, TopoNode)
    case class RouteHop(
      parent: LowRiseGlobalNode,
      cost: Double,
      category: RouteEdgeCategory,
      description: String,
      attributes: Map[String, AttributeValue] = Map.empty,
      uncertainAccess: Option[UncertainAccess] = None
    )

    // Floor index derived from subMapNames ordering
    val floorIndex: Map[String, Int] = subMapNames.zipWithIndex.toMap

    def verticalH(aFloor: NavigationGraph, bFloor: NavigationGraph): Double = {
      val aFloorIndex = floorIndex.getOrElse(aFloor.identifier, 0)
      val bFloorIndex = floorIndex.getOrElse(bFloor.identifier, 0)
      math.abs(aFloorIndex - bFloorIndex) * 3.0
    }

    def horizontalH(a: TopoNode, b: TopoNode): Double = {
      (a.estimatedCoord, b.estimatedCoord) match {
        case (Some(ca), Some(cb)) => (math.abs(ca.x - cb.x) + math.abs(ca.y - cb.y)).toDouble
        case _ => 0.0
      }
    }

    def heuristic(v: LowRiseGlobalNode): Double =
      horizontalH(v._2, effectiveGoal) + verticalH(v._1, goalGraph)

    // Priority queue: (vertex, gScore), min-heap
    implicit val ord: Ordering[(LowRiseGlobalNode, Double)] = Ordering.by[(LowRiseGlobalNode, Double), Double](_._2).reverse

    val openSet  = mutable.PriorityQueue.empty[(LowRiseGlobalNode, Double)]
    val gScore   = mutable.Map[LowRiseGlobalNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val fScore   = mutable.Map[LowRiseGlobalNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val cameFrom = mutable.Map[LowRiseGlobalNode, RouteHop]()
    val visited  = mutable.Set[LowRiseGlobalNode]()

    val start: LowRiseGlobalNode = (sourceGraph, effectiveSource)
    val goal:  LowRiseGlobalNode = (goalGraph,   effectiveGoal)

    gScore(start) = 0.0
    fScore(start) = heuristic(start)
    openSet.enqueue((start, fScore(start)))

    var found = false

    while (openSet.nonEmpty && !found) {
      val (current, currentF) = openSet.dequeue()

      if (currentF > fScore(current)) {
        // stale entry — skip
      } else if (!visited.contains(current)) {
        visited.add(current)

        if (current == goal) {
          found = true
        } else {
          val (curGraph, curNode) = current

          // ── Intra-graph edges ──────────────────────────────────────────
          for (edge <- curGraph.adjacencyList
               if edge.source == curNode && tagPolicy.allows(edge) &&
                 (allowUncertainAccess || !edge.hasFailedUncertainAccess)) {
            val nb: LowRiseGlobalNode = (curGraph, edge.target)
            // All intra-graph neighbours are traversable — coord-less nodes may appear
            // as intermediate hops.  The heuristic already falls back to 0.0 when a
            // node has no estimatedCoord, so they are handled gracefully.
            if (!visited.contains(nb) && tagPolicy.allowsEntry(edge.target)) {
              val tentG = gScore(current) + edge.costs(visitingMode)
              if (tentG < gScore(nb)) {
                gScore(nb) = tentG
                fScore(nb) = tentG + heuristic(nb)
                cameFrom(nb) = RouteHop(
                  parent = current,
                  cost = edge.costs(visitingMode),
                  category = RouteEdgeCategory.Walking,
                  description = s"Walk from ${curNode.identifier} to ${edge.target.identifier}",
                  attributes = edge.attributes,
                  uncertainAccess = edge.uncertainAccess
                )
                openSet.enqueue((nb, fScore(nb)))
              }
            }
          }

          // ── Inter-graph edges via TransportGraph ───────────────────────
          // Match by graph identifier, not reference equality: CoordEstimator rebuilds
          // NavigationGraph instances (new object identity) while TransportGraph still
          // holds references to the original pre-enrichment graphs.
          for (stationNode <- transportGraph.nodes
               if stationNode.ownerGraph.identifier == curGraph.identifier
               && stationNode.localNode.identifier == curNode.identifier) {
            for ((neighborStation, compiledCost) <- transportGraph.adjacencyList.getOrElse(stationNode, Map.empty);
                 (edgeCost, uncertainty) <- transportGraph.edgeTraversal(
                   stationNode, neighborStation, compiledCost, allowUncertainAccess)) {
              // Resolve to enriched graph/node by identifier
              val nbGraphOpt = graphs.get(neighborStation.ownerGraph.identifier)
              val nbNodeOpt  = nbGraphOpt.flatMap(g => g.nodes.find(_.identifier == neighborStation.localNode.identifier))
              for (nbGraph <- nbGraphOpt; nbNode <- nbNodeOpt) {
                val nb: LowRiseGlobalNode = (nbGraph, nbNode)
                if (!visited.contains(nb) && tagPolicy.allowsEntry(nbNode)) {
                  val tentG = gScore(current) + edgeCost
                  if (tentG < gScore(nb)) {
                    gScore(nb) = tentG
                    fScore(nb) = tentG + heuristic(nb)
                    val category = stationNode.ownerLine match {
                      case _: StairCase => RouteEdgeCategory.Climbing
                      case _            => RouteEdgeCategory.Transport
                    }
                    cameFrom(nb) = RouteHop(
                      parent = current,
                      cost = edgeCost,
                      category = category,
                      description = s"Take ${stationNode.ownerLine.identifier} from ${curGraph.identifier} to ${neighborStation.ownerGraph.identifier}",
                      uncertainAccess = uncertainty
                    )
                    openSet.enqueue((nb, fScore(nb)))
                  }
                }
              }
            }
          }
        }
      }
    }

    // ── Step 3: stitch prefix ++ A* ++ suffix ─────────────────────────────
    if (!found) {
      return Left(NoRouteFound(
        s"No route found in standard building from ${sourceGraph.identifier}::${effectiveSource.identifier} to ${goalGraph.identifier}::${effectiveGoal.identifier}"))
    }

    val allGlobalNodes = mutable.ListBuffer[GlobalNode]()
    val allRouteEdges  = mutable.ListBuffer[RouteEdge]()

    // Prefix leg (sourceNode → effectiveSource), if any.
    // appendIntraPath emits one RouteEdge per AtomicPath
    prefixPath match {
      case Some(p) => appendIntraPath(p, sourceGraph, visitingMode, allGlobalNodes, allRouteEdges, skipFirstNode = false)
      case None    => allGlobalNodes += GlobalNode(sourceGraph, sourceNode) // just the start node
    }

    // A* middle segment
    var cur = goal
    val reversePath = mutable.ListBuffer[(LowRiseGlobalNode, LowRiseGlobalNode, RouteHop)]()

    while (cameFrom.contains(cur)) {
      val hop = cameFrom(cur)
      reversePath.prepend((hop.parent, cur, hop))
      cur = hop.parent
    }

    // Append A* nodes/edges (skip the very first node — already in allGlobalNodes from prefix or seed)
    val astarIsNoop = (effectiveSource == effectiveGoal && sourceGraph == goalGraph)
    if (!astarIsNoop) {
      for ((from, to, hop) <- reversePath) {
        allGlobalNodes += GlobalNode(to._1, to._2)
        allRouteEdges  += RouteEdge(
          source = GlobalNode(from._1, from._2),
          target = GlobalNode(to._1,   to._2),
          cost = hop.cost,
          category = hop.category,
          movementDescription = hop.description,
          attributes = hop.attributes,
          uncertainAccess = hop.uncertainAccess
        )
      }
    }

    // Suffix leg (effectiveGoal → goalNode), if any — skip first node (effectiveGoal already added).
    // Also atomic-path-level via appendIntraPath.
    suffixPath match {
      case Some(p) => appendIntraPath(p, goalGraph, visitingMode, allGlobalNodes, allRouteEdges, skipFirstNode = true)
      case None    => () // goalNode already the last node added
    }

    Right(NavigationOutputPath(allGlobalNodes.toList, allRouteEdges.toList))
  }

  // ── Helpers for coord-less source / goal ─────────────────────────────────

  /**
   * Forward Dijkstra within `graph` starting from `start`.
   * Returns the first (cheapest-to-reach) node whose `estimatedCoord` is defined.
   * Returns None if no coord-having node is reachable at all (i.e. the whole graph is coord-less).
   */
  private def findNearestCoordNodeForward(
    graph: NavigationGraph,
    start: TopoNode,
    visitingMode: enums.VisitingMode,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): Option[TopoNode] = {
    if (start.estimatedCoord.isDefined) return Some(start)

    implicit val ord: Ordering[(TopoNode, Double)] = Ordering.by[(TopoNode, Double), Double](_._2).reverse
    val pq      = mutable.PriorityQueue.empty[(TopoNode, Double)]
    val dist    = mutable.Map[TopoNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val visited = mutable.Set[TopoNode]()

    dist(start) = 0.0
    pq.enqueue((start, 0.0))

    while (pq.nonEmpty) {
      val (cur, curDist) = pq.dequeue()
      if (curDist <= dist(cur) && !visited.contains(cur)) {
        visited.add(cur)
        if (cur.estimatedCoord.isDefined) return Some(cur)
        for (edge <- graph.adjacencyList
             if edge.source == cur && tagPolicy.allows(edge) &&
               (allowUncertainAccess || !edge.hasFailedUncertainAccess)) {
          val nb = edge.target
          if (tagPolicy.allowsEntry(nb)) {
            val tentG = curDist + edge.costs(visitingMode)
            if (tentG < dist(nb)) {
              dist(nb) = tentG
              pq.enqueue((nb, tentG))
            }
          }
        }
      }
    }
    None
  }

  /**
   * Backward Dijkstra within `graph` starting from `end` (traversing edges in reverse).
   * Returns the first (cheapest-to-reach-backwards) node whose `estimatedCoord` is defined —
   * i.e. the nearest coord-having node from which `end` is forward-reachable.
   * Returns None if no such node exists.
   */
  private def findNearestCoordNodeBackward(
    graph: NavigationGraph,
    end: TopoNode,
    visitingMode: enums.VisitingMode,
    tagPolicy: TraversalTagPolicy,
    allowUncertainAccess: Boolean
  ): Option[TopoNode] = {
    if (end.estimatedCoord.isDefined) return Some(end)

    // Build reverse adjacency on the fly while preserving the traversed AtomicPath.
    val reverseAdj = mutable.Map[TopoNode, mutable.ListBuffer[data.AtomicPath]]()
    for (edge <- graph.adjacencyList
         if tagPolicy.allows(edge) && (allowUncertainAccess || !edge.hasFailedUncertainAccess)) {
      reverseAdj.getOrElseUpdate(edge.target, mutable.ListBuffer()).addOne(edge)
    }

    implicit val ord: Ordering[(TopoNode, Double)] = Ordering.by[(TopoNode, Double), Double](_._2).reverse
    val pq      = mutable.PriorityQueue.empty[(TopoNode, Double)]
    val dist    = mutable.Map[TopoNode, Double]().withDefaultValue(Double.PositiveInfinity)
    val visited = mutable.Set[TopoNode]()

    dist(end) = 0.0
    pq.enqueue((end, 0.0))

    while (pq.nonEmpty) {
      val (cur, curDist) = pq.dequeue()
      if (curDist <= dist(cur) && !visited.contains(cur)) {
        visited.add(cur)
        if (cur != end && cur.estimatedCoord.isDefined) return Some(cur)
        for (edge <- reverseAdj.getOrElse(cur, mutable.ListBuffer())) {
          val pred = edge.source
          if (tagPolicy.allowsEntry(pred)) {
            val tentG = curDist + edge.costs(visitingMode)
            if (tentG < dist(pred)) {
              dist(pred) = tentG
              pq.enqueue((pred, tentG))
            }
          }
        }
      }
    }
    None
  }

  /**
   * Converts an IntraMapPath into (GlobalNode list, RouteEdge list) appended into the given buffers.
   * `skipFirstNode` avoids duplicate junction nodes when concatenating segments.
   */
  private def appendIntraPath(
    path: data.IntraMapPath,
    graph: NavigationGraph,
    visitingMode: enums.VisitingMode,
    nodesOut: mutable.ListBuffer[GlobalNode],
    edgesOut: mutable.ListBuffer[RouteEdge],
    skipFirstNode: Boolean
  ): Unit = {
    val nodes = if (skipFirstNode) path.routeNodes.tail else path.routeNodes
    nodesOut ++= nodes.map(GlobalNode(graph, _))
    edgesOut ++= path.routeEdges.map(RouteEdge.fromAtomicPath(graph, _, visitingMode))
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
