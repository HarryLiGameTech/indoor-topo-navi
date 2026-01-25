package syntax

import corelang.{Environment, Expr, Identifier, OpKind, Type}
import org.antlr.v4.runtime.tree.ParseTree
import surfacelang.{AtomicPathExpr, RootExpr, SubTopoMapExpr, SurfaceSyntax, TopoMapRef, TopoNodeExpr, TransportExpr, VehicleRef}
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
        val stationNodeTri = visitSurfaceElementStation(stationCtx)
        acc.copy(
          stationNodes = acc.stationNodes + (stationNodeTri._1 -> stationNodeTri._2),
          stationLocations = acc.stationLocations + (stationNodeTri._1 -> stationNodeTri._3)
        )
      case _ => acc // Ignore other elements for now
    })
    
    
    val stationNodes = mutable.Map[TopoMapRef, TopoNodeExpr]()
    val stationLocations = mutable.Map[TopoMapRef, Double]()

    if (ctx.surfaceBody() != null) {
      for (element <- ctx.surfaceBody().surfaceBodyElement().asScala) {
        element match {
          case stationCtx: SurfaceElementStationContext =>
            val stationNodeTri = visitSurfaceElementStation(stationCtx)
            // extract and add this pair into the stationNodes
            stationNodes += (stationNodeTri._1 -> stationNodeTri._2)
            stationLocations += (stationNodeTri._1 -> stationNodeTri._3)
          case _ => // Ignore other elements for now
        }
      }
    }
    
    TransportExpr(
      name = name,
      stationNodes = stationNodes, // TODO: Deal with the type mismatch
      stationLocations = stationLocations, // TODO: Deal with the type mismatch
      data = Expr.Record(Map.empty)
    )
  }
  

  override def visitSurfaceElementCoreDef(ctx: SurfaceElementCoreDefContext): Environment[Identifier, Type, Expr] = {
    ctx.coreDef match {
      case c: TypeDefContext => visitTypeDef(c)
      case c: FuncDefContext => visitFuncDef(c)
      case c: ScriptExprContext => Environment.empty
    }
  }

  override def visitSurfaceElementTopoNode(ctx: SurfaceElementTopoNodeContext): TopoNodeExpr = {
    val recordCtx = ctx.recordAssign()
    val assignments = if (recordCtx.fieldAssign() == null) Seq.empty
    else recordCtx.fieldAssign().asScala
    val fields = assignments.map { fieldAssignment =>
      (fieldAssignment.ID().getText, fieldAssignment.expr.visit)
    }.toMap
    TopoNodeExpr(
      name = ctx.ID().getText,
      data = Expr.Record(fields)
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
    val constraintExprs = reqCtx.ID().asScala.map { idNode =>
      Expr.Var(idNode.getText)
    }.toList

    AtomicPathExpr(
      from = from,
      to = to,
      bidirectional = isBidirectional,
      data = Expr.Record(fields),
      constraints = constraintExprs
    )
  }

  // Use as an intermediate function
  override def visitSurfaceElementStation(ctx: SurfaceElementStationContext): (TopoMapRef, TopoNodeExpr, Double) = {
    val mapRef = TopoMapRef(
      name = ctx.ID().getText
    )
    
    val nodeExpr = ??? // TODO: get the node expression from the context

    (mapRef, nodeExpr, ctx.expr(1).visit.asInstanceOf[Expr.FloatLit].value) // TODO: Correct?
  }
  
  override def visitSurfaceElementArrow(ctx: SurfaceElementArrowContext): TopoMapRef = {
    ???
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
}