package compiler

import corelang.{Identifier, Value}
import surfacelang.{TopoMapValue, TransportValue}

import scala.jdk.CollectionConverters.*

// Preserve discovery data without retaining interpreter environments in cached results.
enum DiscoveryValue extends Serializable {
  case Scalar(value: AnyRef)
  case Record(fields: Map[String, DiscoveryValue])
  case Sequence(values: List[DiscoveryValue])
  case Unsupported

  def toJava(owner: String, path: String): AnyRef = this match {
    case Scalar(value) => value
    case Record(fields) => fields.toList.sortBy(_._1).map { case (key, value) =>
      key -> value.toJava(owner, s"$path.$key")
    }.toMap.asJava
    case Sequence(values) => values.zipWithIndex.map { case (value, index) =>
      value.toJava(owner, s"$path[$index]")
    }.asJava
    case Unsupported => throw DiscoveryProjectionException(owner, path)
  }
}

case class DiscoveryProjectionException(owner: String, fieldPath: String)
  extends RuntimeException("Compiled discovery metadata cannot be represented as JSON") {
  def getOwner: String = owner
  def getFieldPath: String = fieldPath
}

object DiscoveryValue {
  def from(value: Value): DiscoveryValue = value match {
    case Value.IntVal(value) => Scalar(java.lang.Long.valueOf(value))
    case Value.FloatVal(value) if value.isFinite => Scalar(java.lang.Double.valueOf(value))
    case Value.BoolVal(value) => Scalar(java.lang.Boolean.valueOf(value))
    case Value.StringVal(value) => Scalar(value)
    case Value.ListVal(_, values) => Sequence(values.map(from))
    case Value.RecordVal(fields) => Record(fields.view.mapValues(from).toMap)
    case _ => Unsupported
  }
}

case class TransportDiscovery(identifier: String, record: DiscoveryValue) extends Serializable {
  def toJava: AnyRef = record.toJava(identifier, "")
}

object DiscoveryMetadata {
  import DiscoveryValue.*

  def mapAttributes(value: TopoMapValue): DiscoveryValue = Record(
    value.context.values.get(Identifier.Symbol("params")).map(params =>
      Map("params" -> from(params))).getOrElse(Map.empty) ++
      value.managementDomainOverride.map(domain => "managementDomain" -> Scalar(domain))
  )

  def transport(value: TransportValue): TransportDiscovery = {
    val params = value.context.values.get(Identifier.Symbol("params"))
      .map(from).getOrElse(Record(Map.empty))
    val attributes = from(value.data)
    val displayName = value.data.fields.get("displayName")
      .orElse(value.context.values.get(Identifier.Symbol("params")).collect {
        case Value.RecordVal(fields) => fields
      }.flatMap(_.get("displayName")))
      .map(from).getOrElse(Scalar(null))
    def nodeId(ref: surfacelang.TopoNodeRefValue): String = s"${ref.fromMapName}::${ref.nodeName}"

    val stations = value.stations.map { case (ref, data) =>
      def access(condition: Option[surfacelang.UncertainAccessValue]): DiscoveryValue =
        condition.map(item => Sequence(item.uncertaintyReasons.map(reason => Record(Map(
          "type" -> Scalar(reason.conditionType), "reason" -> Scalar(reason.reason.orNull),
          "conditionSatisfied" -> Scalar(java.lang.Boolean.valueOf(reason.conditionSatisfied))
        ))))).getOrElse(Scalar(null))
      val uncertainty = value.stationUncertainAccess.get(ref).map(item => "uncertainAccess" -> Record(Map(
        "departure" -> access(item.departure), "arrival" -> access(item.arrival)
      )))
      Record(Map(
        "label" -> Scalar(value.stationLabels(ref)),
        "nodeId" -> Scalar(nodeId(ref)),
        "permissions" -> from(data.fields.getOrElse("_permission", Value.StringVal("FullyGranted"))),
        "attributes" -> from(Value.RecordVal(data.fields - "_permission"))
      ) ++ data.fields.get("location").map(location => "location" -> from(location)) ++ uncertainty)
    }.sortBy {
      case Record(fields) => fields("nodeId").toJava(value.name, "stations").toString
      case _ => ""
    }
    val direction = value.rideDirections.map(pairs => Sequence(pairs.toList
      .sortBy { case (source, target) => (nodeId(source), nodeId(target)) }
      .map { case (source, target) => Record(Map(
        "fromNodeId" -> Scalar(nodeId(source)), "toNodeId" -> Scalar(nodeId(target)))) }))
      .getOrElse(Scalar(null))
    val policies = value.ridePolicies.map(policy => Record(Map(
      "source" -> Scalar(policy.source), "target" -> Scalar(policy.target),
      "allowed" -> Scalar(java.lang.Boolean.valueOf(policy.allowed)))))

    TransportDiscovery(value.name, Record(Map(
      "transportId" -> Scalar(value.name), "type" -> Scalar(value.surfaceType),
      "displayName" -> displayName, "params" -> params,
      "stations" -> Sequence(stations), "attributes" -> attributes,
      "direction" -> direction, "ridePermissions" -> Sequence(policies)
    )))
  }
}
