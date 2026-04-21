package compiler

import corelang.{Environment, Identifier, Value}
import data.{LinearTransport, NavigationGraph, TopoNode}
import enums.AttributeValue
import reasoner.{DirectionArrow, LinearPath, SpatialMetadata}
import surfacelang.{DirectionalArrowValue, LinearPathValue, TopoMapValue, TransportValue}

/** Returned by [[SpatialMetadataExtractor.extract]] regardless of whether
  * coord-estimation is enabled.
  *
  * @param linearPaths       surface-level linear paths per map name — always populated
  * @param directionalArrows surface-level directional arrows per map name — always populated
  * @param coordMetadata     one [[SpatialMetadata]] per graph, present only when
  *                          coord-estimation is enabled in `rootEnv`
  */
case class ExtractionResult(
  linearPaths: Map[String, Set[LinearPathValue]],
  directionalArrows: Map[String, Set[DirectionalArrowValue]],
  coordMetadata: Option[Map[String, SpatialMetadata]],
  startMap: Option[NavigationGraph] = None,
  startNode: Option[TopoNode] = None
)

object SpatialMetadataExtractor {

  /** Extracts spatial-reasoning data and, when coord-estimation is enabled,
    * also assembles one [[SpatialMetadata]] per graph.
    *
    * `linearPaths` and `directionalArrows` are **always** present in the result
    * so that the -core module can consume them regardless of the coord-estimation
    * flag.  The core-typed [[LinearPath]] / [[DirectionArrow]] objects needed by
    * [[SpatialMetadata]] are built here as an implementation detail — they are
    * never duplicated outside this method.
    *
    * @param rootEnv              elaborated root environment
    * @param graphs               compiled navigation graphs, keyed by name
    * @param elaboratedMaps       surface-level map values, keyed by name
    * @param elaboratedTransports surface-level transport values (for `_isBeacon`)
    * @param linearTransports     compiled linear transports (reserved for future use)
    */
  def extract(
    rootEnv: Environment[Identifier, corelang.Type, Value],
    graphs: Map[String, NavigationGraph],
    elaboratedMaps: Map[String, TopoMapValue],
    elaboratedTransports: List[TransportValue],
    linearTransports: List[LinearTransport]
  ): ExtractionResult = {

    // 1. Surface-level lines/arrows — always extracted
    val linearPathsPerGraph: Map[String, Set[LinearPathValue]] =
      elaboratedMaps.map { case (name, mapVal) => name -> mapVal.lines }

    val arrowsPerGraph: Map[String, Set[DirectionalArrowValue]] =
      elaboratedMaps.map { case (name, mapVal) => name -> mapVal.arrows }

    // 2. Check coord_estimation_data in rootEnv
    val coordDataOpt = rootEnv.values.get(Identifier.Symbol("coord_estimation_data")) collect {
      case rv: Value.RecordVal => rv
    }

    val enabled = coordDataOpt.flatMap(_.fields.get("enabled")) match {
      case Some(Value.BoolVal(b)) => b
      case _                      => false
    }

    if (!enabled)
      return ExtractionResult(linearPathsPerGraph, arrowsPerGraph, coordMetadata = None)

    val coordData = coordDataOpt.get

    val sensitivity: Float = coordData.fields.get("sensitivity") match {
      case Some(Value.FloatVal(v)) => v.toFloat
      case Some(Value.IntVal(v))   => v.toFloat
      case _ => throw new RuntimeException(
        "coord_estimation_data: 'sensitivity' must be a Float or Int"
      )
    }

    val startNodeName: String = coordData.fields.get("startNode") match {
      case Some(Value.StringVal(s)) => s
      case _ => throw new RuntimeException(
        "coord_estimation_data: 'startNode' must be a String"
      )
    }

    val startMapName: String = coordData.fields.get("startMap") match {
      case Some(Value.StringVal(s)) => s
      case _ => throw new RuntimeException(
        "coord_estimation_data: 'startMap' must be a String"
      )
    }

    val startGraph = graphs.getOrElse(startMapName,
      throw new RuntimeException(
        s"coord_estimation_data: startMap '$startMapName' does not match any compiled NavigationGraph"
      )
    )
    startGraph.nodes.find(_.identifier == startNodeName).getOrElse(
      throw new RuntimeException(
        s"coord_estimation_data: startNode '$startNodeName' not found in graph '$startMapName'"
      )
    )

    // 3. _excluded_from_coord — per graph from node attributes
    val excludedPerGraph: Map[String, Set[TopoNode]] = graphs.map { case (name, graph) =>
      val excluded = graph.nodes.filter { node =>
        node.attributes.get("_excluded_from_coord") match {
          case Some(AttributeValue.BoolValue(true)) => true
          case _                                     => false
        }
      }.toSet
      name -> excluded
    }

    // 4. _isBeacon — per graph from transport params
    val beaconPerGraph: Map[String, Set[TopoNode]] = {
      val acc = scala.collection.mutable.Map[String, Set[TopoNode]]()
      elaboratedTransports.foreach { transVal =>
        val isBeacon = transVal.context.values.get(Identifier.Symbol("params")) match {
          case Some(Value.RecordVal(fields)) =>
            fields.get("_isBeacon") match {
              case Some(Value.BoolVal(b)) => b
              case _                      => false
            }
          case _ => false
        }
        if (isBeacon) {
          transVal.stations.foreach { case (nodeRef, _) =>
            graphs.get(nodeRef.fromMapName).foreach { graph =>
              graph.nodes.find(_.identifier == nodeRef.nodeName).foreach { node =>
                val current = acc.getOrElse(nodeRef.fromMapName, Set.empty)
                acc.put(nodeRef.fromMapName, current + node)
              }
            }
          }
        }
      }
      acc.toMap
    }

    // 5. Convert surface lines/arrows to core types for SpatialMetadata 
    //    This is the single place where LinearPathValue -> LinearPath and
    //    DirectionalArrowValue -> DirectionArrow conversions happen.
    def resolveNode(graph: NavigationGraph, name: String): TopoNode =
      graph.nodes.find(_.identifier == name).getOrElse(
        throw new RuntimeException(
          s"SpatialMetadataExtractor: node '$name' not found in graph '${graph.identifier}'"
        )
      )

    val coreLinearPathsPerGraph: Map[String, List[LinearPath]] = graphs.map { case (mapName, graph) =>
      val lines = linearPathsPerGraph.getOrElse(mapName, Set.empty).map { lp =>
        LinearPath(lp.nodes.map(n => resolveNode(graph, n.name)))
      }.toList
      mapName -> lines
    }

    val coreArrowsPerGraph: Map[String, List[DirectionArrow]] = graphs.map { case (mapName, graph) =>
      val arrows = arrowsPerGraph.getOrElse(mapName, Set.empty).map { da =>
        DirectionArrow(
          anchor       = resolveNode(graph, da.anchor.name),
          reference    = resolveNode(graph, da.reference.name),
          invertFacing = da.invertFacing,
          target       = resolveNode(graph, da.target.name),
          direction    = da.direction
        )
      }.toList
      mapName -> arrows
    }

    // 6. Assemble one SpatialMetadata per graph
    val metadataMap: Map[String, SpatialMetadata] = graphs.map { case (name, _) =>
      val meta = SpatialMetadata(
        lines         = coreLinearPathsPerGraph.getOrElse(name, List.empty),
        arrows        = coreArrowsPerGraph.getOrElse(name, List.empty),
        beaconNodes   = beaconPerGraph.getOrElse(name, Set.empty),
        excludedNodes = excludedPerGraph.getOrElse(name, Set.empty),
        sensitivity   = sensitivity
      )
      name -> meta
    }

    ExtractionResult(linearPathsPerGraph, arrowsPerGraph, coordMetadata = Some(metadataMap), Some(startGraph), Some(startGraph.nodes.find(_.identifier == startNodeName).get))
  }
}

