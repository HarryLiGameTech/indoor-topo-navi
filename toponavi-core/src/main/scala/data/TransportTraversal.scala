package data

enum TransportKind {
  case Elevator, Escalator, Stairs
}

case class TransportTraversal(
  lineIdentifier: String,
  kind: TransportKind,
  distanceMeters: Double
) {
  require(distanceMeters.isFinite && distanceMeters >= 0.0, "Transport distance must be finite and non-negative")

  def physicalDemandScore(seconds: Double): Double = kind match {
    case TransportKind.Elevator => 0.1 * distanceMeters + 0.5 * seconds
    case TransportKind.Escalator => 0.1 * distanceMeters + 0.65 * seconds
    case TransportKind.Stairs => 10.0 * distanceMeters
  }
}

object TransportTraversal {
  def between(line: LinearTransport, source: NavigationGraph, target: NavigationGraph): TransportTraversal = {
    val kind = line match {
      case _: ElevatorBank => TransportKind.Elevator
      case _: Escalator => TransportKind.Escalator
      case _: StairCase => TransportKind.Stairs
    }
    TransportTraversal(line.identifier, kind, line.distanceBetweenStations(source, target))
  }

  def countChanges(lines: Iterable[String]): Int = {
    var previous: Option[String] = None
    var changes = 0
    lines.foreach { line =>
      if (previous.exists(_ != line)) changes += 1
      previous = Some(line)
    }
    changes
  }
}
