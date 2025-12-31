package surfacelang

import corelang.Env

case class TopoEnvironment (
  env: Env,
  nodes: Map[String, TopoNodeValue],
  paths: Map[String, AtomicPathValue],
) {
  def merge(rhsEnv: Env): TopoEnvironment = TopoEnvironment(
    env = env.merge(rhsEnv), nodes = nodes, paths = paths
  )
}
