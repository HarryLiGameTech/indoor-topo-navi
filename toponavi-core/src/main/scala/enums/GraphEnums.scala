package enums

enum VisitingMode:
  case Normal
  case Wheeled
  case Prioritized
  case Emergency

// TODO: Modify to modifiers
enum PathType:
  case General
  case Accessibility
  case Restricted
  case EmergencyOnly

enum AttributeValue:
  case IntValue(value: Int)
  case StringValue(value: String)
  case BoolValue(value: Boolean)
  case DoubleValue(value: Double)