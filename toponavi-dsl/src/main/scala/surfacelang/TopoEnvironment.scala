package surfacelang

import corelang.Env

case class TopoEnvironment (
  env: Env,
  nodes: Map[String, TopoNodeValue],
  paths: Map[String, AtomicPathValue],
)
