package data

import enums.AttributeValue

import java.util.LinkedHashMap
import scala.jdk.CollectionConverters.*

object RouteNodeInfoProjection {
  val exposedAttributeFields: List[ExposedAttributeField] = List(
    ExposedAttributeField("description", List("description")),
    ExposedAttributeField("shopName", List("shopName", "shop_name")),
    ExposedAttributeField("facilityName", List("facilityName", "facility_name")),
    ExposedAttributeField("shopCategory", List("shopCategory", "shop_category")),
    ExposedAttributeField("facilityCategory", List("facilityCategory", "facility_category")),
    ExposedAttributeField("positions", List("positions")),
    ExposedAttributeField("observedThroughput", List("observedThroughput", "observed_throughput")),
    ExposedAttributeField("maxThroughput", List("maxThroughput", "max_throughput")),
    ExposedAttributeField("tags", List("tags"), defaultValue = Some(List.empty[String].asJava)),
    ExposedAttributeField("narration", List("narration")),
    ExposedAttributeField("refVisualUrl", List("refVisualUrl", "ref_visual_url")),
    ExposedAttributeField("beaconId", List("beaconId", "beacon_id"))
  )

  def project(node: GlobalNode, isIntermediate: Boolean, isTrivialFallback: Boolean): java.util.Map[String, Object] = {
    val projected = new LinkedHashMap[String, Object]()
    projected.put("nodeId", node.localNode.identifier)
    projected.put("graph", node.owningGraph.identifier)

    exposedAttributeFields.foreach { field =>
      projected.put(field.outputName, field.valueFrom(node.localNode.attributes).orNull)
    }

    projected.put("isTrivial", Boolean.box(resolveBoolean(
      node.localNode.attributes,
      List("isTrivial", "is_trivial"),
      isTrivialFallback
    )))
    projected.put("isIntermediate", Boolean.box(isIntermediate))
    projected
  }

  def isTrivialNode(node: TopoNode): Boolean = {
    val id = node.identifier
    val isGeneratedInternal = id.startsWith("n_") && id.length == 34 &&
      id.drop(2).forall(c => "0123456789abcdef".contains(c))
    val hasSemanticRouteInfo = exposedAttributeFields
      .filterNot(_.outputName == "tags")
      .exists(_.valueFrom(node.attributes).nonEmpty)

    isGeneratedInternal || !hasSemanticRouteInfo
  }

  private def resolveBoolean(
    attributes: Map[String, AttributeValue],
    aliases: List[String],
    fallback: Boolean
  ): Boolean = {
    aliases.collectFirst {
      case key if attributes.contains(key) =>
        attributes(key) match {
          case AttributeValue.BoolValue(value) => value
          case other => throw IllegalArgumentException(
            s"Node attribute '${key}' must be a boolean, found: $other")
        }
    }.getOrElse(fallback)
  }

  private[data] def toJavaValue(value: AttributeValue): Object = value match {
    case AttributeValue.IntValue(v) => Int.box(v)
    case AttributeValue.StringValue(v) => v
    case AttributeValue.BoolValue(v) => Boolean.box(v)
    case AttributeValue.DoubleValue(v) => Double.box(v)
    case AttributeValue.ListValue(values) => values.map(toJavaValue).asJava
  }
}

case class ExposedAttributeField(
  outputName: String,
  attributeAliases: List[String],
  defaultValue: Option[Object] = None
) {
  def valueFrom(attributes: Map[String, AttributeValue]): Option[Object] =
    attributeAliases.collectFirst {
      case alias if attributes.contains(alias) => RouteNodeInfoProjection.toJavaValue(attributes(alias))
    }.orElse(defaultValue)
}
