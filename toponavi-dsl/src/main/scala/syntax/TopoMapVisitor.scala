package syntax

import corelang.{Environment, Expr, Identifier, OpKind, Type}
import org.antlr.v4.runtime.tree.ParseTree
import surfacelang.{AtomicPathExpr, GlobalConfigExpr, RootExpr, SubTopoMapExpr, SurfaceSyntax, TopoMapRef, TopoNodeExpr, TransportExpr, VehicleRef}
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
    
    if (ctx.expr() != null) {
      // TODO: Should that one really be an expr?
//      if (ctx.expr().getText != "Elevator") // not "Elevator" or "Stairs" or "Escalator" or "Tram"{
//        throw RuntimeException(s"Unsupported transport type: ${ctx.expr().getText}")
//      }
    }

    ctx.surfaceBody().surfaceBodyElement().asScala.foldLeft(
      TransportExpr(
        name = name,
        stationNodes = Map.empty,
        stationLocations = Map.empty,
        data = Expr.Record(Map.empty)
      )
    ) { (acc, element) => element match
      case stationCtx: SurfaceElementStationContext =>
        val stationNodeTri = parseSurfaceElementStation(stationCtx)
        acc.copy(
          stationNodes = acc.stationNodes + (stationNodeTri._1 -> stationNodeTri._2),
          stationLocations = acc.stationLocations + (stationNodeTri._1 -> stationNodeTri._3)
        )
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
  private def parseSurfaceElementStation(ctx: SurfaceElementStationContext): (TopoMapRef, TopoNodeExpr, Double) = {
    val mapRef = TopoMapRef(
      name = ctx.ID().getText
    )

    val nodeExpr = ??? // TODO: get the node expression from the context

    (mapRef, nodeExpr, ctx.expr(1).visit.asInstanceOf[Expr.FloatLit].value) // TODO: Correct?
  }
  
  // TODO: to-be-updated for implementation
  override def visitSurfaceElementArrow(ctx: SurfaceElementArrowContext): TopoMapRef = {
    ???
  }
}