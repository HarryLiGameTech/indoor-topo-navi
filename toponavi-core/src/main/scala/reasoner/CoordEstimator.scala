package reasoner

import data.{LinearTransport, NavigationGraph, TpccCoord, TopoNode}
import enums.{TPCCRelationship, VisitingMode}
import scala.util.boundary
import scala.util.boundary.break

import scala.collection.mutable

/** Stub coordinate estimator.
  *
  * Takes a set of compiled `NavigationGraph`s together with one
  * `SpatialMetadata` descriptor per graph and produces an enriched copy of
  * each graph whose `TopoNode.estimatedCoord` fields have been filled in.
  *
  * The full TPCC-based estimation algorithm is not yet implemented; the stub
  * returns the graphs unchanged so that the rest of the compiler pipeline can
  * be exercised end-to-end.
  */
object CoordEstimator {

  // ── Helpers ────────────────────────────────────────────────────────────────

  private def discretize(cost: Double, sensitivity: Float): Int =
    math.ceil(cost / sensitivity).toInt

  private def tpccOffset(direction: TPCCRelationship, magnitude: Int): (Int, Int) =
    direction match {
      case TPCCRelationship.FRONT       => (0,          magnitude)
      case TPCCRelationship.FRONT_RIGHT => (magnitude,  magnitude)
      case TPCCRelationship.RIGHT       => (magnitude,  0)
      case TPCCRelationship.REAR_RIGHT  => (magnitude, -magnitude)
      case TPCCRelationship.REAR        => (0,         -magnitude)
      case TPCCRelationship.REAR_LEFT   => (-magnitude,-magnitude)
      case TPCCRelationship.LEFT        => (-magnitude, 0)
      case TPCCRelationship.FRONT_LEFT  => (-magnitude, magnitude)
    }

  // ── Main entry point ───────────────────────────────────────────────────────

  def estimate(
    graphs: Map[String, NavigationGraph],
    metadata: Map[String, SpatialMetadata],
    startGraph: NavigationGraph,
    startNode: TopoNode,
    linearTransports: List[LinearTransport]
  ): Map[String, NavigationGraph] = {

    // coordMap: graphIdentifier -> (TopoNode -> TpccCoord)
    val coordMaps: mutable.Map[String, mutable.Map[TopoNode, TpccCoord]] =
      mutable.Map.empty

    graphs.keys.foreach { name => coordMaps(name) = mutable.Map.empty }

    // ── Phase 1: estimate the start graph ─────────────────────────────────

    estimateGraph(startGraph, startNode, metadata, coordMaps)

    // ── Phase 2: propagate to other graphs via beacons ────────────────────

    // Build beacon cross-graph mapping from linearTransports
    // For each transport that has a station in startGraph and whose node is a
    // beacon in startGraph, carry the same coord to other graphs' stations.
    val startMeta = metadata(startGraph.identifier)

    // Iteratively propagate until no more changes happen (handle chains)
    var changed = true
    while (changed) {
      changed = false
      for (transport <- linearTransports) {
        // Check if any station of this transport already has a coord assigned
        // in some already-estimated graph, and whether its node is a beacon.
        for ((srcGraph, srcNode) <- transport.stationNodes) {
          val srcCoordMap = coordMaps.getOrElse(srcGraph.identifier, mutable.Map.empty)
          srcCoordMap.get(srcNode).foreach { srcCoord =>
            // Propagate this coord to all other stations of the same transport
            for ((dstGraph, dstNode) <- transport.stationNodes if dstGraph != srcGraph) {
              val dstMeta    = metadata.getOrElse(dstGraph.identifier, null)
              val dstCoordMap = coordMaps.getOrElse(dstGraph.identifier, mutable.Map.empty)

              if (dstMeta != null && !dstMeta.excludedNodes.contains(dstNode) && !dstCoordMap.contains(dstNode)) {
                // Assign the same coord as the source graph beacon
                assignCoord(dstNode, srcCoord, dstCoordMap, dstGraph.identifier)
                changed = true
                // Now estimate the rest of that graph using this seed node
                estimateGraph(dstGraph, dstNode, metadata, coordMaps)
              }
            }
          }
        }
      }
    }

    // ── Rebuild enriched NavigationGraphs ─────────────────────────────────

    graphs.map { case (name, graph) =>
      val coordMap = coordMaps.getOrElse(name, mutable.Map.empty)
      name -> rebuildGraph(graph, coordMap)
    }
  }

  // ── Graph-level estimation ─────────────────────────────────────────────────

  private def estimateGraph(
    graph: NavigationGraph,
    seedNode: TopoNode,
    metadata: Map[String, SpatialMetadata],
    coordMaps: mutable.Map[String, mutable.Map[TopoNode, TpccCoord]]
  ): Unit = boundary {
    val meta      = metadata.getOrElse(graph.identifier, break(()))
    val coordMap  = coordMaps.getOrElse(graph.identifier, { val m = mutable.Map.empty[TopoNode, TpccCoord]; coordMaps(graph.identifier) = m; m })
    val sensitivity = meta.sensitivity

    // ── Step 1.1: find seed node's LinearPath ────────────────────────────
    val seedLine = meta.lines.find(_.nodes.contains(seedNode)).orNull

    if (seedLine == null) {
      // No line for this seed; only do arrow/interpolation fixpoint if the seed has a coord.
      if (coordMap.contains(seedNode)) {
        var prevSize = -1
        while (coordMap.size != prevSize) {
          prevSize = coordMap.size
          expandViaArrows(graph, meta, coordMap, sensitivity)
          interpolateLines(graph, meta, coordMap, sensitivity)
        }
      }
      break(())
    }

    // For the very first seed (startNode), validate it is at an end of the line
    if (seedNode != seedLine.nodes.head && seedNode != seedLine.nodes.last) {
      throw new RuntimeException(
        s"CoordEstimator: startNode '${seedNode.identifier}' must be at one end of its LinearPath, not in the middle"
      )
    }

    // ── Step 1.2: place seed at (0,0) if not already assigned ────────────
    if (!coordMap.contains(seedNode) && !meta.excludedNodes.contains(seedNode)) {
      assignCoord(seedNode, TpccCoord(0, 0), coordMap, graph.identifier)
    }

    // ── Step 1.3 + 1.4: traverse the start line ──────────────────────────
    traverseLine(seedLine, graph, meta, coordMap, sensitivity)

    // ── Steps 1.5 / 1.6: fixpoint — keep alternating arrow-expansion and
    //    line-interpolation until no new coords are assigned in a full round.
    //    This handles chains where:
    //      line-traversal → arrow → interpolation → arrow → interpolation → …
    var prevSize = -1
    while (coordMap.size != prevSize) {
      prevSize = coordMap.size
      expandViaArrows(graph, meta, coordMap, sensitivity)
      interpolateLines(graph, meta, coordMap, sensitivity)
    }
  }

  // ── Line traversal ─────────────────────────────────────────────────────────

  private def traverseLine(
    line: LinearPath,
    graph: NavigationGraph,
    meta: SpatialMetadata,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    sensitivity: Float
  ): Unit = boundary {
    val nodes = line.nodes

    // Determine the traversal direction from the end that has a coord assigned
    val (orderedNodes, startAssigned) =
      if (coordMap.contains(nodes.head))      (nodes,          nodes.head)
      else if (coordMap.contains(nodes.last)) (nodes.reverse,  nodes.last)
      else break(())  // Neither end has a coord yet — skip

    // Determine the axis of travel: look for an arrow anchored at the start
    // whose front/back aligns with the next node in the line.
    val nextInLine = orderedNodes.lift(1)
    val (axDx, axDy) = nextInLine.flatMap { nextNode =>
      meta.arrows
        .find(a => a.anchor == startAssigned && a.front == nextNode)
        .map(_ => (0, 1))   // arrow says "front" toward next → FRONT axis
        .orElse(
          meta.arrows
            .find(a => a.anchor == startAssigned && a.back == nextNode)
            .map(_ => (0, -1)) // arrow says "back" toward next → REAR axis
        )
    }.getOrElse((0, 1))  // default: advance in +Y (FRONT)

    // Walk the ordered line
    for (i <- 0 until orderedNodes.size - 1) {
      val nodeA = orderedNodes(i)
      val nodeB = orderedNodes(i + 1)

      if (!meta.excludedNodes.contains(nodeB) && !coordMap.contains(nodeB)) {
        coordMap.get(nodeA).foreach { coordA =>
          val edgeCost = edgeCostBetween(nodeA, nodeB, graph)
          val step     = discretize(edgeCost, sensitivity)
          val newCoord = TpccCoord(coordA.x + axDx * step, coordA.y + axDy * step)
          assignCoord(nodeB, newCoord, coordMap, graph.identifier)
        }
      }
    }
  }

  // ── Arrow-based expansion (BFS) ────────────────────────────────────────────

  private def expandViaArrows(
    graph: NavigationGraph,
    meta: SpatialMetadata,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    sensitivity: Float
  ): Unit = {
    val queue = mutable.Queue[TopoNode]()
    // Seed the queue with all already-assigned, non-excluded nodes
    coordMap.keys.filterNot(meta.excludedNodes.contains).foreach(queue.enqueue)

    while (queue.nonEmpty) {
      val n = queue.dequeue()
      val coordN = coordMap.getOrElse(n, null)
      if (coordN == null) {} else {
        // Determine the "FRONT" direction for node n from arrows anchored there
        val frontArrow = meta.arrows.find(a => a.anchor == n)

        for (arrow <- meta.arrows if arrow.anchor == n) {
          val target = arrow.target
          if (!meta.excludedNodes.contains(target) && !coordMap.contains(target)) {
            // Get path cost from n to target
            val costOpt = graph.findPath(n, target, VisitingMode.Normal).map(_.totalCost(VisitingMode.Normal))
            costOpt.foreach { cost =>
              val magnitude = discretize(cost, sensitivity)
              // Rotate direction according to facing: determine what absolute
              // direction "FRONT" corresponds to for this node.
              val rotatedDir = rotateDirection(arrow.direction, arrow, coordMap, n, graph, meta, sensitivity)
              val (dx, dy)   = tpccOffset(rotatedDir, magnitude)
              val newCoord   = TpccCoord(coordN.x + dx, coordN.y + dy)
              assignCoord(target, newCoord, coordMap, graph.identifier)
              queue.enqueue(target)
            }
          }
        }
      }
    }
  }

  /** Rotate the arrow's declared direction to account for the anchor's facing.
    * We use the anchor→front vector already in the coordMap to determine what
    * "FRONT" physically is, then rotate the declared direction accordingly.
    * If we cannot determine the facing, we return the direction unchanged.
    */
  private def rotateDirection(
    direction: TPCCRelationship,
    arrow: DirectionArrow,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    anchor: TopoNode,
    graph: NavigationGraph,
    meta: SpatialMetadata,
    sensitivity: Float
  ): TPCCRelationship = {
    // Find the "front" node of this anchor to determine its facing
    val frontNode = arrow.front
    val backNode  = arrow.back

    // Try to read the front node's coord (may not be assigned yet)
    (coordMap.get(anchor), coordMap.get(frontNode)) match {
      case (Some(anchorCoord), Some(frontCoord)) =>
        val dxFront = frontCoord.x - anchorCoord.x
        val dyFront = frontCoord.y - anchorCoord.y
        // Determine canonical "FRONT" rotation offset (in 45° steps)
        // +Y is canonical FRONT (0 steps). Compute how many 45-deg steps the
        // actual front vector is rotated from +Y.
        val steps = vectorToSteps(dxFront, dyFront)
        rotateBySteps(direction, steps)
      case _ =>
        // Cannot determine facing — return direction unchanged
        direction
    }
  }

  /** Map a (dx,dy) vector to the number of 45-degree clockwise steps from FRONT (+Y). */
  private def vectorToSteps(dx: Int, dy: Int): Int = {
    if (dx == 0 && dy > 0) 0       // FRONT
    else if (dx > 0 && dy > 0) 1   // FRONT_RIGHT
    else if (dx > 0 && dy == 0) 2  // RIGHT
    else if (dx > 0 && dy < 0) 3   // REAR_RIGHT
    else if (dx == 0 && dy < 0) 4  // REAR
    else if (dx < 0 && dy < 0) 5   // REAR_LEFT
    else if (dx < 0 && dy == 0) 6  // LEFT
    else if (dx < 0 && dy > 0) 7   // FRONT_LEFT
    else 0
  }

  private val allDirections: Vector[TPCCRelationship] = Vector(
    TPCCRelationship.FRONT,
    TPCCRelationship.FRONT_RIGHT,
    TPCCRelationship.RIGHT,
    TPCCRelationship.REAR_RIGHT,
    TPCCRelationship.REAR,
    TPCCRelationship.REAR_LEFT,
    TPCCRelationship.LEFT,
    TPCCRelationship.FRONT_LEFT
  )

  private def directionToIndex(d: TPCCRelationship): Int = allDirections.indexOf(d)

  private def rotateBySteps(direction: TPCCRelationship, steps: Int): TPCCRelationship =
    allDirections((directionToIndex(direction) + steps) % 8)

  // ── Line interpolation ─────────────────────────────────────────────────────

  private def interpolateLines(
    graph: NavigationGraph,
    meta: SpatialMetadata,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    sensitivity: Float
  ): Unit = {
    for (line <- meta.lines) {
      val nodes = line.nodes
      val assignedIndices = nodes.indices.filter(i => coordMap.contains(nodes(i)) && !meta.excludedNodes.contains(nodes(i)))

      if (assignedIndices.size >= 2) {
        // Interpolate between each consecutive pair of assigned anchors
        for (Seq(iA, iB) <- assignedIndices.sliding(2)) {
          interpolateSegment(nodes, iA, iB, graph, meta, coordMap, sensitivity)
        }
        // Extrapolate before first anchor and after last anchor
        val first = assignedIndices.head
        val last  = assignedIndices.last
        extrapolateSegment(nodes, first, 0,            graph, meta, coordMap, sensitivity, forward = false)
        extrapolateSegment(nodes, last,  nodes.size-1, graph, meta, coordMap, sensitivity, forward = true)

      } else if (assignedIndices.size == 1) {
        val anchor = assignedIndices.head
        // Extrapolate in both directions along the line's axis
        extrapolateSegment(nodes, anchor, 0,            graph, meta, coordMap, sensitivity, forward = false)
        extrapolateSegment(nodes, anchor, nodes.size-1, graph, meta, coordMap, sensitivity, forward = true)
      }
      // size == 0: defer
    }
  }

  /** Fill unassigned nodes between index iA and iB (exclusive) by linear interpolation. */
  private def interpolateSegment(
    nodes: List[TopoNode],
    iA: Int, iB: Int,
    graph: NavigationGraph,
    meta: SpatialMetadata,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    sensitivity: Float
  ): Unit = {
    val coordA = coordMap(nodes(iA))
    val coordB = coordMap(nodes(iB))
    for (i <- iA + 1 until iB) {
      val node = nodes(i)
      if (!meta.excludedNodes.contains(node) && !coordMap.contains(node)) {
        // Linear fraction based on path cost from A to i vs A to B
        val costAI = pathCost(nodes(iA), nodes(i), graph)
        val costAB = pathCost(nodes(iA), nodes(iB), graph)
        if (costAB > 0) {
          val t = costAI / costAB
          val x = math.round(coordA.x + t * (coordB.x - coordA.x)).toInt
          val y = math.round(coordA.y + t * (coordB.y - coordA.y)).toInt
          assignCoord(node, TpccCoord(x, y), coordMap, graph.identifier)
        }
      }
    }
  }

  /** Fill unassigned nodes between fromIdx and toIdx (inclusive, walking from anchor outward). */
  private def extrapolateSegment(
    nodes: List[TopoNode],
    anchorIdx: Int,
    toIdx: Int,
    graph: NavigationGraph,
    meta: SpatialMetadata,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    sensitivity: Float,
    forward: Boolean
  ): Unit = {
    val range = if (forward) (anchorIdx + 1 to toIdx) else (anchorIdx - 1 to toIdx by -1)
    var prevIdx = anchorIdx
    for (i <- range) {
      val node = nodes(i)
      val prev = nodes(prevIdx)
      if (!meta.excludedNodes.contains(node) && !coordMap.contains(node)) {
        coordMap.get(prev).foreach { prevCoord =>
          val cost = edgeCostBetween(prev, node, graph)
          val step = discretize(cost, sensitivity)
          // Use same axis as existing traversal: derive from coordMap diff of previous two nodes if available
          val (axDx, axDy) = axisFromPrevious(prevIdx, nodes, coordMap, forward)
          assignCoord(node, TpccCoord(prevCoord.x + axDx * step, prevCoord.y + axDy * step), coordMap, graph.identifier)
        }
      }
      prevIdx = i
    }
  }

  private def axisFromPrevious(
    prevIdx: Int,
    nodes: List[TopoNode],
    coordMap: mutable.Map[TopoNode, TpccCoord],
    forward: Boolean
  ): (Int, Int) = {
    // Try to derive axis from the two most recent assigned nodes
    val referenceIdx = if (forward) prevIdx - 1 else prevIdx + 1
    if (referenceIdx >= 0 && referenceIdx < nodes.size) {
      (coordMap.get(nodes(referenceIdx)), coordMap.get(nodes(prevIdx))) match {
        case (Some(cRef), Some(cPrev)) =>
          val dx = Integer.signum(cPrev.x - cRef.x)
          val dy = Integer.signum(cPrev.y - cRef.y)
          if (dx == 0 && dy == 0) (0, 1) else (dx, dy)
        case _ => (0, 1)
      }
    } else (0, 1)
  }

  // ── Coord assignment with conflict detection ───────────────────────────────

  private def assignCoord(
    node: TopoNode,
    coord: TpccCoord,
    coordMap: mutable.Map[TopoNode, TpccCoord],
    graphId: String
  ): Unit = {
    if (coordMap.contains(node)) return  // immutability: never reassign
    // Duplicate coord check
    coordMap.find { case (existingNode, existingCoord) =>
      existingCoord == coord && existingNode != node
    }.foreach { case (conflicting, _) =>
      throw new RuntimeException(
        s"CoordEstimator: coord conflict at (${coord.x},${coord.y}) between '${conflicting.identifier}' and '${node.identifier}' in graph '$graphId'"
      )
    }
    coordMap(node) = coord
  }

  // ── Graph rebuilding ───────────────────────────────────────────────────────

  private def rebuildGraph(
    graph: NavigationGraph,
    coordMap: mutable.Map[TopoNode, TpccCoord]
  ): NavigationGraph = {
    val enrichedNodes: List[TopoNode] = graph.nodes.map { node =>
      coordMap.get(node) match {
        case Some(coord) => node.copy(estimatedCoord = Some(coord))
        case None        => node
      }
    }
    val nodeMap: Map[TopoNode, TopoNode] = graph.nodes.zip(enrichedNodes).toMap
    val enrichedPaths = graph.adjacencyList.map { path =>
      path.copy(source = nodeMap(path.source), target = nodeMap(path.target))
    }
    NavigationGraph(
      identifier       = graph.identifier,
      nodes            = enrichedNodes,
      adjacencyList    = enrichedPaths,
      reverseAdjacency = graph.reverseAdjacency.map { case (k, v) =>
        nodeMap(k) -> v.map(p => p.copy(source = nodeMap(p.source), target = nodeMap(p.target)))
      }
    )
  }

  // ── Edge/path cost helpers ─────────────────────────────────────────────────

  private def edgeCostBetween(a: TopoNode, b: TopoNode, graph: NavigationGraph): Double = {
    graph.adjacencyList
      .find(p => (p.source == a && p.target == b) || (p.source == b && p.target == a))
      .map(_.costs(VisitingMode.Normal))
      .getOrElse(1.0)
  }

  private def pathCost(a: TopoNode, b: TopoNode, graph: NavigationGraph): Double = {
    graph.findPath(a, b, VisitingMode.Normal)
      .map(_.totalCost(VisitingMode.Normal))
      .getOrElse(0.0)
  }
}
