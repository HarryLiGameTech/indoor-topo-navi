package syntax

import corelang.{Environment, Expr, Identifier, OpKind, Type}
import org.antlr.v4.runtime.tree.ParseTree
import surfacelang.{AtomicPathExpr, ConstraintExpr, GlobalConfigExpr, RootExpr, StationDef, SubTopoMapExpr, SurfaceSyntax, TopoMapRef, TopoNodeExpr, TopoNodeRef, TransportExpr, VehicleRef}
import topomap.grammar.MapFileParser.*
import topomap.grammar.{MapFileBaseVisitor, MapFileParser, MapFileVisitor}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*


class TopoMapVisitor extends CoreLangVisitor[SurfaceSyntax] {

  override def visitSurfaceDefRootExpr(ctx: SurfaceDefRootExprContext): RootExpr = {
    val name = ctx.ID().getText

    val params = if (ctx.paramList() == null) List.empty
    else ctx.paramList().param().asScala.map { paramAssign =>
      val paramName = paramAssign.ID().getText
      val paramType = visitTypeExpr(paramAssign.typeExpr())
      (paramName, paramType)
    }.toList
    
    ctx.surfaceBody.surfaceBodyElement.asScala.foldLeft(
      RootExpr(name, params)
    ) { (acc, element) => element match
      case coreDefCtx: SurfaceElementCoreDefContext =>
        val envUpdate = visitSurfaceElementCoreDef(coreDefCtx)
        acc.copy(env = acc.env.merge(envUpdate))
      case constraintCtx: SurfaceElementConstraintContext =>
        val constraint = visitSurfaceElementConstraint(constraintCtx)
        acc.copy(constraints = acc.constraints :+ constraint)
      case _ => acc // Ignore other elements for now
    }
  }

  override def visitSurfaceDefTopoMapExpr(ctx: SurfaceDefTopoMapExprContext): SubTopoMapExpr = {
    val name = ctx.ID().getText

    val params = if (ctx.paramList() == null) List.empty
    else ctx.paramList().param().asScala.map { paramAssign =>
      val paramName = paramAssign.ID().getText
      val paramType = visitTypeExpr(paramAssign.typeExpr())
      (paramName, paramType)
    }.toList

    ctx.surfaceBody().surfaceBodyElement().asScala.foldLeft(
      SubTopoMapExpr(name, params)
    ) { (acc, element) => element match
      case coreDefCtx: SurfaceElementCoreDefContext =>
        val envUpdate = visitSurfaceElementCoreDef(coreDefCtx)
        acc.copy(env = acc.env.merge(envUpdate))
      case topoNodeCtx: SurfaceElementTopoNodeContext =>
        val node = visitSurfaceElementTopoNode(topoNodeCtx)
        acc.copy(nodes = acc.nodes :+ node)
      case atomicPathCtx: SurfaceElementAtomicPathContext =>
        val path = visitSurfaceElementAtomicPath(atomicPathCtx)
        acc.copy(paths = acc.paths :+ path)
      case constraintCtx: SurfaceElementConstraintContext =>
        val constraint = visitSurfaceElementConstraint(constraintCtx)
        acc.copy(constraints = acc.constraints :+ constraint)
      case arrowCtx: SurfaceElementArrowContext =>
        ??? // TODO: to-be-updated for implementation
      case _ => acc // Ignore other elements for now
    }
  }

  override def visitSurfaceDefTransportExpr(ctx: SurfaceDefTransportExprContext): TransportExpr = {
    val name = ctx.ID().getText
    val transportType = ctx.expr().getText

    if (transportType != "Elevator" && transportType != "Escalator" && transportType != "Stairs") {
      throw new RuntimeException(s"Unsupported transport type '$transportType'.")
    }

    // TODO: Need more consideration on this, so ditch this feat for now
    val transportData: Expr.Record = Expr.Record(Map.empty) // Why does explicit type required here? (otherwise "requires Data but found Expr.Record", wtf)

    ctx.surfaceBody().surfaceBodyElement().asScala.foldLeft(
      TransportExpr(
        name = name,
        surfaceType = transportType,
        stations = List.empty,
        data = transportData
      )
    ) { (acc, element) => element match
      case coreDefCtx: SurfaceElementCoreDefContext =>
        val envUpdate = visitSurfaceElementCoreDef(coreDefCtx)
        acc.copy(env = acc.env.merge(envUpdate))
      case stationCtx: SurfaceElementStationContext =>
        acc.copy(stations = acc.stations :+ parseStationDef(stationCtx))
      case constraintCtx: SurfaceElementConstraintContext =>
        val constraint = visitSurfaceElementConstraint(constraintCtx)
        acc.copy(constraints = acc.constraints :+ constraint)
      case _ => acc // Ignore other elements for now
    }
  }

  override def visitSurfaceDefGlobalConfigExpr(ctx: SurfaceDefGlobalConfigExprContext): GlobalConfigExpr = {
    Option(ctx.globalConfigBody)
      .flatMap(body => Option(body.globalConfigElement))
      .map(_.asScala.toList)
      .getOrElse(List.empty)
      .foldLeft(GlobalConfigExpr(List.empty, List.empty)) { (acc, elementCtx) =>
        elementCtx match {
          case submapCtx: GlobalConfigElementSubmapRefContext =>
            if (submapCtx.ID().size() == 2) {
              // 'submap X using Y': X has no own .tmap — it reuses Y's compiled graph.
              val userName = submapCtx.ID(0).getText
              val baseName = submapCtx.ID(1).getText
              val baseRef  = TopoMapRef(baseName)
              val existing = acc.submapUsages.getOrElse(baseRef, List.empty)
              acc.copy(
                submapUsages = acc.submapUsages + (baseRef -> (existing :+ userName)),
                orderedSubmapNames = acc.orderedSubmapNames :+ userName
              )
            } else {
              // Plain 'submap X': has its own .tmap file, add to submaps for parsing
              val ref = visitGlobalConfigElementSubmapRef(submapCtx)
              acc.copy(
                submaps = acc.submaps :+ ref,
                orderedSubmapNames = acc.orderedSubmapNames :+ ref.name
              )
            }
          case vehicleCtx: GlobalConfigElementVehicleRefContext =>
            acc.copy(vehicles = acc.vehicles :+ visitGlobalConfigElementVehicleRef(vehicleCtx))
          case _ => acc // Ignore other elements
        }
      }
  }

  override def visitGlobalConfigElementVehicleRef(ctx: GlobalConfigElementVehicleRefContext): VehicleRef = {
    VehicleRef(
      name = ctx.ID().getText
    )
  }

  override def visitGlobalConfigElementSubmapRef(ctx: GlobalConfigElementSubmapRefContext): TopoMapRef = {
    if (ctx.ID().size() == 2) {
      // register in SubmapRefRegistry as kv (TopoMapRef created using ID(1) -> ID(0))
      TopoMapRef(
        name = ctx.ID(1).getText
      )
    }
    else {
      // register in SubmapRefRegistry as kv (TopoMapRef created using using the only ID())
      TopoMapRef(
        name = ctx.ID(0).getText
      )
    }

  }

  override def visitSurfaceElementCoreDef(ctx: SurfaceElementCoreDefContext): Environment[Identifier, Type, Expr] = {
    ctx.coreDef match {
      case c: TypeDefContext => visitTypeDef(c)
      case c: FuncDefContext => visitFuncDef(c)
      case c: LetDefContext => visitLetDef(c)
      case c: ScriptExprContext => throw RuntimeException("ScriptExprContext should not appear here")
    }
  }

  override def visitSurfaceElementTopoNode(ctx: SurfaceElementTopoNodeContext): TopoNodeExpr = {
    val recordFields = Option(ctx.recordAssign()) match {
      case Some(recordCtx) =>
        val assignments = if (recordCtx.fieldAssign() == null) Seq.empty
        else recordCtx.fieldAssign().asScala

        assignments.map { fieldAssignment =>
          (fieldAssignment.ID().getText, fieldAssignment.expr.visit)
        }.toMap

      case None => Map.empty
    }

    TopoNodeExpr(
      name = ctx.ID().getText,
      data = Expr.Record(recordFields)
    )
  }

  override def visitSurfaceElementAtomicPath(ctx: SurfaceElementAtomicPathContext): AtomicPathExpr = {
    // 1. Parse Path Specification: [ A -> B ] or [ A <-> B ]
    val pathCtx = ctx.pathSpec()
    val from = pathCtx.ID(0).getText
    val to = pathCtx.ID(1).getText

    // Check for bidirectionality by checking if the '<' token text exists in the source
    // Grammar: '[' ID ('<'? '->' ID) ']'
    val isBidirectional = pathCtx.getText.contains("<->")

    // 2. Parse Data (Record Assign)
    val recordCtx = ctx.recordAssign()
    val assignments = if (recordCtx.fieldAssign() == null) Seq.empty
    else recordCtx.fieldAssign().asScala

    val fields = assignments.map { fieldAssignment =>
      (fieldAssignment.ID().getText, fieldAssignment.expr.visit)
    }.toMap

    // 3. Parse Requirements, TODO: Verify correctness
    val reqCtx = ctx.requirements()
    // The requirements rule contains IDs in both alternatives:
    // 'requires' ID
    // 'requires' '<' (ID ('&&' ID)*)? '>'
    // ANTLR collects all matching IDs into a list automatically.
    val constraintExprs = Option(reqCtx).map { rCtx =>
      rCtx.ID().asScala.map { idNode =>
        Expr.Var(idNode.getText)
      }.toList
    }.getOrElse(List.empty)

    AtomicPathExpr(
      from = from,
      to = to,
      bidirectional = isBidirectional,
      data = Expr.Record(fields),
      constraints = constraintExprs
    )
  }

  // Use as an intermediate function
  private def parseStationDef(ctx: SurfaceElementStationContext): StationDef = {
    val stationName = ctx.ID().getText

    val nodeRefExpr = ctx.identifier().visit
    val nodeRef = nodeRefExpr match {
      case Expr.Var(Identifier.Path(parts)) if parts.length == 2 =>
        TopoNodeRef(parts.head, parts.last)
      case _ =>
        throw new RuntimeException(s"Invalid station node reference '$nodeRefExpr'. Expected 'MapName::NodeName'")
    }

    val recordFields = Option(ctx.recordAssign())
      .flatMap(rc => Option(rc.fieldAssign()))
      .map(_.asScala.map(f => (f.ID().getText, f.expr().visit)).toMap)
      .getOrElse(Map.empty)

    val constraintExprs = Option(ctx.requirements()).map { rCtx =>
      rCtx.ID().asScala.map { idNode =>
        Expr.Var(idNode.getText)
      }.toList
    }.getOrElse(List.empty)

    StationDef(nodeRef, Expr.Record(recordFields), constraintExprs)
  }
  
  override def visitSurfaceElementConstraint(ctx: SurfaceElementConstraintContext): ConstraintExpr = {
    val name = ctx.ID().getText
    val predicates = ctx.constraintBody().requireClause().asScala.map { clause =>
      clause.expr().visit
    }.toList
    ConstraintExpr(name, predicates)
  }

  // TODO: to-be-updated for implementation
  override def visitSurfaceElementArrow(ctx: SurfaceElementArrowContext): TopoMapRef = {
    ???
  }
}