package enums

enum RCCRelationship {
  case DC
  case EC
  case EQ
  case PO
  case TPP
  case TPPi
  case NTPP
  case NTPPi
}

enum TPCCRelationship {
  case FRONT
  case FRONT_RIGHT
  case RIGHT
  case REAR_RIGHT
  case REAR
  case REAR_LEFT
  case LEFT
  case FRONT_LEFT
}