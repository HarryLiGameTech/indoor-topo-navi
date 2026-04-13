package reasoner

import data.NavigationGraph

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
    graphs: Map[String, NavigationGraph],
    metadata: Map[String, SpatialMetadata]
  ): Map[String, NavigationGraph] = {
    // TODO: Implement full TPCC-based coordinate estimation.
    // For now return the graphs unchanged (all TopoNode.estimatedCoord remain None).
    graphs
  }
}

