package api

import compiler.CompilationResult
import data.{RouteTraversalMetadata, TopoNode}
import enums.{AttributeValue, VisitingMode}

import java.util.{List as JList, Map as JMap}
import scala.jdk.CollectionConverters.*

// Query records are independent of the response's detail projection.
object TopologyData {
  private def record(fields: (String, AnyRef)*): JMap[String, AnyRef] = fields.toMap.asJava

  private def value(attribute: AttributeValue): AnyRef = attribute match {
    case AttributeValue.IntValue(number) => Int.box(number)
    case AttributeValue.DoubleValue(number) => Double.box(number)
    case AttributeValue.BoolValue(boolean) => Boolean.box(boolean)
    case AttributeValue.StringValue(text) => text
    case AttributeValue.ListValue(values) => values.map(value).asJava
  }

  private def attributes(values: Map[String, AttributeValue]): JMap[String, AnyRef] =
    values.view.mapValues(value).toMap.asJava

  private def displayName(values: JMap[String, AnyRef]): AnyRef =
    List("displayName", "shop_name", "facility_name", "description")
      .flatMap(key => Option(values.get(key)).collect { case text: String if text.nonEmpty => text })
      .headOption.orNull

  private def node(graphId: String, node: TopoNode): JMap[String, AnyRef] = {
    val data = attributes(node.attributes)
    record("nodeId" -> s"$graphId::${node.identifier}", "mapId" -> graphId,
      "localNodeId" -> node.identifier, "displayName" -> displayName(data), "attributes" -> data)
  }

  def maps(result: CompilationResult): JList[JMap[String, AnyRef]] = result.graphs.keys.toList.sorted.map { id =>
    val data = result.mapAttributes.get(id).map(_.toJava(id, "attributes")
      .asInstanceOf[JMap[String, AnyRef]]).getOrElse(Map.empty[String, AnyRef].asJava)
    val params = Option(data.get("params")).collect { case record: JMap[?, ?] =>
      record.asInstanceOf[JMap[String, AnyRef]]
    }.getOrElse(data)
    record("mapId" -> id, "displayName" -> displayName(params), "attributes" -> data)
  }.asJava

  def nodes(result: CompilationResult): JList[JMap[String, AnyRef]] = result.graphs.toList.sortBy(_._1)
    .flatMap { case (id, graph) => graph.nodes.sortBy(_.identifier).map(node(id, _)) }.asJava

  def edges(result: CompilationResult): JList[JMap[String, AnyRef]] = result.graphs.toList.sortBy(_._1)
    .flatMap { case (id, graph) => graph.adjacencyList.map { edge =>
      val traversal = RouteTraversalMetadata.from(edge.attributes)
      record("mapId" -> id, "fromNodeId" -> s"$id::${edge.source.identifier}",
        "toNodeId" -> s"$id::${edge.target.identifier}",
        "cost" -> Double.box(edge.costs(VisitingMode.Normal)),
        "tags" -> traversal.tags.toList.sorted.asJava,
        "requiredActions" -> traversal.requiredActions.asJava,
        "attributes" -> attributes(edge.attributes))
    }}.asJava

  def transports(result: CompilationResult): JList[AnyRef] =
    result.transportDeclarations.sortBy(_.identifier).map(_.toJava).asJava
}
