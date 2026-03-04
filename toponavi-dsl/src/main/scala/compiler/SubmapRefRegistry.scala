package compiler

import surfacelang.TopoMapRef

case class SubmapRefRegistry(
  submapUsages: Map[TopoMapRef, List[String]]
)

