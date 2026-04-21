package reasoner

import data.{NavigationGraph, TopoNode}

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

  def estimate(
    graphs: Map[String, NavigationGraph], // for graph's O(1) lookup by name
    metadata: Map[String, SpatialMetadata], // per graph. Map from graph name to graph's metadata
    startGraph: NavigationGraph,
    startNode: TopoNode
  ): Map[String, NavigationGraph] = {
    // TODO: Implement full TPCC-based coordinate estimation.
    // For now return the graphs unchanged (all TopoNode.estimatedCoord remain None).
    
    
    // Start from the startNode in the startGraph, assign it to coordinate (0, 0)
    // Exploit the line the startNode is in. If this node is not at an end of the line, throw exception. The line is in the metadata
    
    
    
    // When finished the startGraph's estimation, handle other graphs. First look at the beacon nodes, and then try to interpolate between them. The beacon nodes are in the metadata.
    
  }
}

