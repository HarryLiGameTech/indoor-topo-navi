package compiler

import data.{NavigationGraph, TransportGraph}
import enums.AttributeValue
import surfacelang.{DirectionalArrowValue, LinearPathValue}

case class CompilationResult(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph,
  graphSequence: List[String] = List.empty,
  linearPaths: Map[String, Set[LinearPathValue]] = Map.empty,
  directionalArrows: Map[String, Set[DirectionalArrowValue]] = Map.empty,
  metadata: Map[String, AttributeValue] = Map.empty
)
