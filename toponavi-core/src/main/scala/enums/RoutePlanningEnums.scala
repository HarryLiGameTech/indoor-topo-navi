package enums

enum NavigationError:
  case NoRouteFound(message: String)
  case InvalidData(message: String)
  case ConstraintFailure(message: String)

enum RoutePlanningPreferences:
  case MinimizeTime
  case MinimizeTransfers
  case MinimizePhysicalDemands

enum RouteEdgeCategory:
  case Walking
  case Transport
  case Climbing
  case Portal