package reasoner

import corelang.{Environment, Identifier, Value}
import data.{LinearTransport, NavigationGraph, TopoNode}
import enums.AttributeValue
import surfacelang.TransportValue

object SpatialMetadataExtractor {

  /** Attempts to extract one `SpatialMetadata` per graph.
    *
    * Returns `None` when coord estimation is disabled (i.e. `coord_estimation_data`
    * is absent from `rootEnv` or its `enabled` flag is `false`).
    *
    * @param rootEnv             elaborated root environment (contains `coord_estimation_data` if present)
    * @param graphs              compiled navigation graphs, keyed by name
    * @param elaboratedTransports surface-level transport values (used for `_isBeacon` extraction)
    * @param linearTransports    compiled linear transports (unused for now — reserved for future use)
    */
  def extract(
    rootEnv: Environment[Identifier, corelang.Type, Value],
    graphs: Map[String, NavigationGraph],
    elaboratedTransports: List[TransportValue],
    linearTransports: List[LinearTransport]
  ): Option[Map[String, SpatialMetadata]] = {

    // ── 1. Check coord_estimation_data in rootEnv ─────────────────────────
    val coordDataOpt = rootEnv.values.get(Identifier.Symbol("coord_estimation_data")) collect {
      case rv: Value.RecordVal => rv
    }

    val enabled = coordDataOpt.flatMap(_.fields.get("enabled")) match {
      case Some(Value.BoolVal(b)) => b
      case None                   => false
      case _                      => false
    }

    if (!enabled) return None

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

    // Validate startMap + startNode exist
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

    // ── 2. _excluded_from_coord — collected per graph from node attributes ─
    val excludedPerGraph: Map[String, Set[TopoNode]] = graphs.map { case (name, graph) =>
      val excluded = graph.nodes.filter { node =>
        node.attributes.get("_excluded_from_coord") match {
          case Some(AttributeValue.BoolValue(true)) => true
          case _                                     => false
        }
      }.toSet
      name -> excluded
    }

    // ── 3. _isBeacon — collect beacon nodes per graph from transport params ─
    val beaconPerGraph: Map[String, Set[TopoNode]] = {
      val mutable = scala.collection.mutable.Map[String, Set[TopoNode]]()

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
                val current = mutable.getOrElse(nodeRef.fromMapName, Set.empty)
                mutable.put(nodeRef.fromMapName, current + node)
              }
            }
          }
        }
      }

      mutable.toMap
    }

    // ── 4. LinearPath / DirectionArrow — not yet parseable; emit stubs ─────
    // TODO: Extract LinearPath and DirectionArrow annotations from topo-map
    //       let-bindings once the grammar and visitor support has been added.

    // ── 5. Assemble one SpatialMetadata per graph ─────────────────────────
    val metadataMap: Map[String, SpatialMetadata] = graphs.map { case (name, _) =>
      val meta = SpatialMetadata(
        lines        = List.empty,   // TODO: populate when grammar supports it
        arrows       = List.empty,   // TODO: populate when grammar supports it
        beaconNodes  = beaconPerGraph.getOrElse(name, Set.empty),
        excludedNodes = excludedPerGraph.getOrElse(name, Set.empty),
        sensitivity  = sensitivity,
        startNode    = startGraph.nodes.find(_.identifier == startNodeName).get,
        startMap     = startMapName
      )
      name -> meta
    }

    Some(metadataMap)
  }
}

