package navigation

import data.*
import enums.AttributeValue.IntValue
import enums.PathType
import enums.RoutePlanningPreferences.*
import enums.TransportServicePermission.FullyGranted
import enums.VisitingMode.Normal
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NativeObjectiveGroundTruthTest extends AnyFlatSpec with Matchers {
  "Native objective search" should "match exhaustive loop-free routing on small cyclic buildings" in {
    (0 until 10).foreach { seed =>
      val random = new scala.util.Random(seed)
      def floor(id: String): NavigationGraph = {
        val nodes = List("a", "b", "c").map(TopoNode(_))
        val edges = for {
          from <- nodes
          to <- nodes if from != to
        } yield AtomicPath(from, to, Map("effort" -> IntValue(random.nextInt(3))),
          Map(Normal -> random.nextInt(5).toDouble), PathType.General)
        NavigationGraph(id, nodes, edges)
      }
      val lower = floor("Lower")
      val upper = floor("Upper")
      val graphs = Map("Lower" -> lower, "Upper" -> upper)
      val lines = List(0, 1).map { i =>
        Escalator(s"Line$i", Map(lower -> lower.nodes(i), upper -> upper.nodes(i)),
          Map(lower -> 0.0, upper -> 1.0), Map(lower -> FullyGranted, upper -> FullyGranted),
          random.nextInt(4).toDouble)
      }
      val transport = TransportGraph(lines)
      val start = GlobalNode(lower, lower.nodes.head)
      val goal = GlobalNode(upper, upper.nodes.last)
      val network = RouteNetwork.build(graphs, transport, start, goal, Normal, TraversalTagPolicy.AllowAll, false)
      def paths(node: String, visited: Set[String]): List[List[RouteEdge]] =
        if (node == RouteNetwork.key(goal)) List(Nil)
        else network.outgoing.getOrElse(node, Vector.empty).toList.flatMap { case (edge, _) =>
          val target = RouteNetwork.key(edge.target)
          if (visited(target)) Nil else paths(target, visited + target).map(edge :: _)
        }
      val candidates = paths(RouteNetwork.key(start), Set(RouteNetwork.key(start))).map { edges =>
        NavigationOutputPath(start :: edges.map(_.target), edges)
      }
      for {
        preference <- List(MinimizeTime, MinimizeTransfers, MinimizePhysicalDemands, MinimizeCustomData)
        transportFirst <- List(false, true)
        order <- (if (preference == MinimizeCustomData)
          List(List(MinimizeTransfers, MinimizeTime), List(MinimizeTime, MinimizeTransfers))
          else List(List(MinimizeTransfers, MinimizeTime)))
      } {
        val custom = Option.when(preference == MinimizeCustomData)(
          CustomRouteObjective(CustomRouteCost.EdgeAttribute("effort"), order))
        def score(path: NavigationOutputPath): (Double, Double, Double) = preference match {
          case MinimizeTime => (path.totalCost, 0.0, 0.0)
          case MinimizeTransfers => (path.transferCount.toDouble, path.totalCost, 0.0)
          case MinimizePhysicalDemands => (path.physicalDemandScore, path.totalCost, 0.0)
          case MinimizeCustomData =>
            val sum = path.routeEdges.map(_.attributes.get("effort").collect { case IntValue(n) => n.toDouble }.getOrElse(0.0)).sum
            if (order.head == MinimizeTransfers) (sum, path.transferCount.toDouble, path.totalCost)
            else (sum, path.totalCost, path.transferCount.toDouble)
        }
        val best = candidates.map(score).min
        val actual = new NativeRouteSearch(network, preference, custom, transportFirst)
          .navigate(start, goal, RouteSearchBudget(50000, 200000)).toOption.get.path
        withClue(s"seed=$seed preference=$preference transportFirst=$transportFirst order=$order: ") {
          score(actual) shouldBe best
          actual.routeNodes.distinct shouldBe actual.routeNodes
        }
      }
    }
  }
}
