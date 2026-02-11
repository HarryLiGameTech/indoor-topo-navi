package syntax

import corelang.{Environment, Expr, Identifier, OpKind, Type}
import org.antlr.v4.runtime.tree.ParseTree
import surfacelang.{AtomicPathExpr, GlobalConfigExpr, RootExpr, StationDef, SubTopoMapExpr, SurfaceSyntax, TopoMapRef, TopoNodeExpr, TopoNodeRef, TransportExpr, VehicleRef}
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
      case arrowCtx: SurfaceElementArrowContext =>
        ??? // TODO: to-be-updated for implementation
      case _ => acc // Ignore other elements for now
    }
  }

  override def visitSurfaceDefTransportExpr(ctx: SurfaceDefTransportExprContext): TransportExpr = {
    val name = ctx.ID().getText

    // TODO: Need more consideration on this, so ditch this feat for now
    val transportData: Expr.Record = Expr.Record(Map.empty) // Why does explicit type required here? (otherwise "requires Data but found Expr.Record", wtf)

    ctx.surfaceBody().surfaceBodyElement().asScala.foldLeft(
      TransportExpr(
        name = name,
        stations = List.empty,
        data = transportData
      )
    ) { (acc, element) => element match
      case coreDefCtx: SurfaceElementCoreDefContext =>
        val envUpdate = visitSurfaceElementCoreDef(coreDefCtx)
        acc.copy(env = acc.env.merge(envUpdate))
      case stationCtx: SurfaceElementStationContext =>
        acc.copy(stations = acc.stations :+ parseStationDef(stationCtx))
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
            acc.copy(submaps = acc.submaps :+ visitGlobalConfigElementSubmapRef(submapCtx))
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
    TopoMapRef(
      name = ctx.ID().getText
    )
  }

  override def visitSurfaceElementCoreDef(ctx: SurfaceElementCoreDefContext): Environment[Identifier, Type, Expr] = {
    ctx.coreDef match {
      case c: TypeDefContext => visitTypeDef(c)
      case c: FuncDefContext => visitFuncDef(c)
      // TODO: Possibly need refactoring, to move them into somewhere else
      case c: ScriptExprContext =>
        c.expr() match {
          case atomExpr: AtomExprContext if atomExpr.atom().block() != null =>
            val blockCtx = atomExpr.atom().block()
            blockCtx.stmt().asScala.foldLeft(Environment.empty[Identifier, Type, Expr]) { (env, stmt) =>
              stmt match {
                case letStmt: LetStmtContext =>
                  val name = letStmt.ID().getText
                  val expr = letStmt.expr().visit
                  val envWithVal = env.addValueVar(Identifier(name), expr)
                  if (letStmt.typeExpr() != null) {
                    envWithVal.addTypeVar(Identifier(name), visitTypeExpr(letStmt.typeExpr()))
                  } else {
                    envWithVal
                  }
                case _ => env
              }
            }
          case _ => Environment.empty
        }
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

    StationDef(nodeRef, Expr.Record(recordFields))
  }
  
  // TODO: to-be-updated for implementation
  override def visitSurfaceElementArrow(ctx: SurfaceElementArrowContext): TopoMapRef = {
    ???
  }
}