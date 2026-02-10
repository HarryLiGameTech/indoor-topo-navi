package surfacelang

import corelang.Env

case class TopoEnvironment (
  env: Env,
  nodes: Map[String, TopoNodeValue],
  paths: Map[String, AtomicPathValue],
  submaps: Map[String, TopoMapValue]
) {
  def merge(rhsEnv: Env): TopoEnvironment = TopoEnvironment(
    env = env.merge(rhsEnv), nodes = nodes, paths = paths, submaps = submaps
  )

  // Resolve TopoNodeRef
  def resolveNode(mapName: String, nodeName: String): Option[TopoNodeValue] = {
    submaps.get(mapName).flatMap { mapVal =>
      mapVal.nodes.find(n => n.name == nodeName)
    }
  }
}
