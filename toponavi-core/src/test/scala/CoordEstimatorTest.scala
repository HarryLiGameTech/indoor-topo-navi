import data.{AtomicPath, NavigationGraph, TpccCoord, TopoNode}
import enums.{PathType, TPCCRelationship, VisitingMode}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import reasoner.{CoordEstimator, DirectionArrow, LinearPath, SpatialMetadata}

/**
 * Tests for CoordEstimator using core-module data types directly (no DSL compilation).
 *
 * Graph layout
 * ─────────────────────────────────────────────────────────────────────────────
 *  Line A  :  n1 ── n2 ── n3 ── n4        (mapped to +Y / FRONT axis)
 *  Line B  :  n5 ── n6 ── n7 ── n8        (parallel, same axis)
 *
 *  Cross-connections (bidirectional AtomicPath):
 *    n1 ↔ n5    (start of A ↔ start of B, cost=10 → step=1 in RIGHT direction)
 *    n4 ↔ n8    (end of A   ↔ end of B)
 *    n7 ↔ n10   (mid-B node  ↔ isolated node)
 *
 *  n9 has no connections at all → should remain estimatedCoord = None.
 *
 * All edge costs = 10.0, sensitivity = 10.0  →  step = ceil(10/10) = 1
 *
 * DirectionArrow declarations (anchor, reference, invertFacing, target, direction):
 *   (n1, n2, false, n2, FRONT)   → line A travels in +Y
 *   (n1, n2, false, n5, RIGHT)   → n5 is RIGHT of n1  → (1, 0)
 *   (n7, n8, false, n10, RIGHT)  → n10 is RIGHT of n7 → (2, 2)
 *
 * Expected coords in graph "G1":
 *   n1  → (0,0)   n2  → (0,1)   n3  → (0,2)   n4  → (0,3)
 *   n5  → (1,0)   n6  → (1,1)   n7  → (1,2)   n8  → (1,3)
 *   n10 → (2,2)
 *   n9  → None
 */
@RunWith(classOf[JUnitRunner])
class CoordEstimatorTest extends AnyFlatSpec with Matchers {

  // ── helpers ─────────────────────────────────────────────────────────────────

  private def costs(v: Double): Map[VisitingMode, Double] = Map(
    VisitingMode.Normal      -> v,
    VisitingMode.Emergency   -> v * 0.5,
    VisitingMode.Prioritized -> v * 0.7,
    VisitingMode.Wheeled     -> v * 2.0
  )

  private def biEdges(a: TopoNode, b: TopoNode, c: Double): List[AtomicPath] = List(
    AtomicPath(a, b, costs(c), PathType.General),
    AtomicPath(b, a, costs(c), PathType.General)
  )

  // ── fixture ─────────────────────────────────────────────────────────────────

  private def buildFixture(): (NavigationGraph, SpatialMetadata, TopoNode) = {
    // Nodes
    val n1  = TopoNode("n1")
    val n2  = TopoNode("n2")
    val n3  = TopoNode("n3")
    val n4  = TopoNode("n4")
    val n5  = TopoNode("n5")
    val n6  = TopoNode("n6")
    val n7  = TopoNode("n7")
    val n8  = TopoNode("n8")
    val n9  = TopoNode("n9")   // isolated
    val n10 = TopoNode("n10")

    // AtomicPaths — line A chain, line B chain, and cross-connections
    val paths =
      biEdges(n1, n2, 10) ++ biEdges(n2, n3, 10) ++ biEdges(n3, n4, 10) ++  // line A
      biEdges(n5, n6, 10) ++ biEdges(n6, n7, 10) ++ biEdges(n7, n8, 10) ++  // line B
      biEdges(n1, n5, 10) ++   // n1 ↔ n5
      biEdges(n4, n8, 10) ++   // n4 ↔ n8
      biEdges(n7, n10, 10)     // n7 ↔ n10

    val allNodes = List(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10)
    val reverseAdj = paths.groupBy(_.target)

    val graph = NavigationGraph(
      identifier       = "G1",
      nodes            = allNodes,
      adjacencyList    = paths,
      reverseAdjacency = reverseAdj
    )

    // LinearPaths
    val lineA = LinearPath(List(n1, n2, n3, n4))
    val lineB = LinearPath(List(n5, n6, n7, n8))

    // DirectionArrows
    val arrows = List(
      // n1's front is n2 → line A travels in FRONT (+Y)
      DirectionArrow(anchor = n1, reference = n2, invertFacing = false, target = n2,  direction = TPCCRelationship.FRONT),
      // n5 is to the RIGHT of n1 (relative to n1's facing toward n2)
      DirectionArrow(anchor = n1, reference = n2, invertFacing = false, target = n5,  direction = TPCCRelationship.RIGHT),
      // n10 is to the RIGHT of n7 (n7's front is n8 → same +Y facing → RIGHT = +X)
      DirectionArrow(anchor = n7, reference = n8, invertFacing = false, target = n10, direction = TPCCRelationship.RIGHT)
    )

    val meta = SpatialMetadata(
      lines         = List(lineA, lineB),
      arrows        = arrows,
      beaconNodes   = Set.empty,
      excludedNodes = Set.empty,
      sensitivity   = 10.0f
    )

    (graph, meta, n1)
  }

  // ── test: line A coords ─────────────────────────────────────────────────────

  "CoordEstimator" should "assign (0,0) to the startNode n1" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val coord = result("G1").nodes.find(_.identifier == "n1").flatMap(_.estimatedCoord)
    coord shouldBe Some(TpccCoord(0, 0))
  }

  it should "place line-A nodes along the +Y axis" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val nodes = result("G1").nodes

    def c(id: String) = nodes.find(_.identifier == id).flatMap(_.estimatedCoord)

    c("n1") shouldBe Some(TpccCoord(0, 0))
    c("n2") shouldBe Some(TpccCoord(0, 1))
    c("n3") shouldBe Some(TpccCoord(0, 2))
    c("n4") shouldBe Some(TpccCoord(0, 3))
  }

  // ── test: cross-line arrow expansion ────────────────────────────────────────

  it should "place n5 at (1,0) via RIGHT arrow from n1" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val coord = result("G1").nodes.find(_.identifier == "n5").flatMap(_.estimatedCoord)
    coord shouldBe Some(TpccCoord(1, 0))
  }

  it should "place line-B nodes along +Y from n5's seed" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val nodes = result("G1").nodes

    def c(id: String) = nodes.find(_.identifier == id).flatMap(_.estimatedCoord)

    c("n5") shouldBe Some(TpccCoord(1, 0))
    c("n6") shouldBe Some(TpccCoord(1, 1))
    c("n7") shouldBe Some(TpccCoord(1, 2))
    c("n8") shouldBe Some(TpccCoord(1, 3))
  }

  // ── test: secondary arrow expansion (n10) ───────────────────────────────────

  it should "place n10 at (2,2) via RIGHT arrow from n7" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val coord = result("G1").nodes.find(_.identifier == "n10").flatMap(_.estimatedCoord)
    coord shouldBe Some(TpccCoord(2, 2))
  }

  // ── test: isolated node remains None ────────────────────────────────────────

  it should "leave isolated node n9 with estimatedCoord = None" in {
    val (graph, meta, startNode) = buildFixture()
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> meta), graph, startNode, List.empty)
    val coord = result("G1").nodes.find(_.identifier == "n9").flatMap(_.estimatedCoord)
    coord shouldBe None
  }

  // ── test: excluded node is skipped ──────────────────────────────────────────

  it should "leave excluded node n3 with estimatedCoord = None" in {
    val (graph, meta, startNode) = buildFixture()
    val n3 = graph.nodes.find(_.identifier == "n3").get
    val metaWithExcluded = meta.copy(excludedNodes = Set(n3))
    val result = CoordEstimator.estimate(Map("G1" -> graph), Map("G1" -> metaWithExcluded), graph, startNode, List.empty)
    val nodes = result("G1").nodes

    def c(id: String) = nodes.find(_.identifier == id).flatMap(_.estimatedCoord)

    c("n3") shouldBe None   // excluded
    // n4 is beyond the excluded n3 — the line traversal stops at n3, and no
    // arrow points to n4 directly, so n4 also stays None.
    c("n4") shouldBe None
  }

  // ── test: error on duplicate coord ──────────────────────────────────────────

  it should "throw RuntimeException when two nodes are forced to the same coord" in {
    // Build a graph where n1→n2 costs 0 after discretization (cost < sensitivity)
    // and n1→n5 costs 0 too, forcing n2 and n5 to land on the same coord (0,1) vs (1,0)
    // Instead: we force a collision by giving both n2 (FRONT, step=1) and n5 (FRONT, step=1)
    // the same direction from n1 — both land on (0,1).
    val n1  = TopoNode("n1_dup")
    val n2  = TopoNode("n2_dup")
    val n5  = TopoNode("n5_dup")

    val paths = biEdges(n1, n2, 10) ++ biEdges(n1, n5, 10)
    val graph = NavigationGraph("G_dup",
      nodes = List(n1, n2, n5),
      adjacencyList = paths,
      reverseAdjacency = paths.groupBy(_.target)
    )
    val lineA = LinearPath(List(n1, n2))
    val arrows = List(
      DirectionArrow(anchor = n1, reference = n2, invertFacing = false, target = n2, direction = TPCCRelationship.FRONT),
      // intentionally duplicate: n5 also placed in FRONT → same (0,1) as n2
      DirectionArrow(anchor = n1, reference = n2, invertFacing = false, target = n5, direction = TPCCRelationship.FRONT)
    )
    val meta = SpatialMetadata(List(lineA), arrows, Set.empty, Set.empty, 10.0f)

    an [RuntimeException] should be thrownBy {
      CoordEstimator.estimate(Map("G_dup" -> graph), Map("G_dup" -> meta), graph, n1, List.empty)
    }
  }
}
