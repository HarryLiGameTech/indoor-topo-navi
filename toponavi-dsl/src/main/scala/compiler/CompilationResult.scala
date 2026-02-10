package compiler

import data.{NavigationGraph, TransportGraph}

case class CompilationResult(
  graphs: Map[String, NavigationGraph],
  transportGraph: TransportGraph
)
