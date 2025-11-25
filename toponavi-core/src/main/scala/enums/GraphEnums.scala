package enums

enum VisitingMode:
  case Normal
  case Wheeled
  case Prioritized
  case Emergency

// TODO: Consider delete these
enum PathType:
  case General
  case Accessibility
  case Restricted
  case EmergencyOnly
  
enum TransportServicePermission:
  case NoAccess // Can neither arrive nor depart
  case ArriveOnly
  case DepartOnly
  case FullyGranted // Both arriving and departing is allowed

enum AttributeValue: // TODO: Use dsl.Value
  case IntValue(value: Int)
  case StringValue(value: String)
  case BoolValue(value: Boolean)
  case DoubleValue(value: Double)