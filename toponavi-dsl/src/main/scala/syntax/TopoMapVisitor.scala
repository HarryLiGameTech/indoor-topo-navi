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

  override def visitSurfaceDefRootExpr(ctx: MapFileParser.SurfaceDefRootExprContext): RootExpr = {
    val name = ctx.ID().getText

    val params = if (ctx.paramList() == null) List.empty
    else ctx.paramList().param().asScala.map { paramAssign =>
      val paramName = paramAssign.ID().getText
      val paramType = visitTypeExpr(paramAssign.typeExpr())
      (paramName, paramType)
    }.toList

    val submapRefs = ListBuffer[String]()
    val vehicleRefs = ListBuffer[String]()
    if (ctx.surfaceBody() != null) {
      for (element <- ctx.surfaceBody().surfaceBodyElement().asScala) {
        element match {
          case coreDefCtx: MapFileParser.SurfaceElementCoreDefContext =>
            visitSurfaceElementCoreDef(coreDefCtx)
          case vehicleCtx: MapFileParser.SurfaceElementVehicleExprContext =>
            val vehicleRef = vehicleCtx.ID().getText
            vehicleRefs += vehicleRef // Append vehicle to list vehicles
          case submapCtx: MapFileParser.SurfaceElementSubmapExprContext =>
            val submapRef = submapCtx.ID().getText
            submapRefs += submapRef // Append submap to list submaps
          case _ => // Ignore other elements for now
        }
      }
    }

    RootExpr(
      name = name,
      params = params,
      types = List.empty, // TODO: Implement type aliases
      defns = List.empty, // TODO: Implement definitions
      data = List.empty, // TODO: Implement root-level data
      submaps = List.empty, // TODO: Implement submap definitions
      submapReferences = submapRefs.toList,
      transportReferences = vehicleRefs.toList
    )
  }

  // TODO
  override def visitSurfaceDefTopoMapExpr(ctx: MapFileParser.SurfaceDefTopoMapExprContext): SubTopoMapExpr = {
    val name = ctx.ID().getText

    val params = if (ctx.paramList() == null) List.empty
    else ctx.paramList().param().asScala.map { paramAssign =>
      val paramName = paramAssign.ID().getText
      val paramType = visitTypeExpr(paramAssign.typeExpr())
      (paramName, paramType)
    }.toList

    val nodes = ListBuffer[TopoNodeExpr]()
    val paths = ListBuffer[AtomicPathExpr]()

    if (ctx.surfaceBody() != null) {
      for (element <- ctx.surfaceBody().surfaceBodyElement().asScala) {
        element match {
          case nodeCtx: MapFileParser.SurfaceElementTopoNodeContext =>
            val node = visitSurfaceElementTopoNode(nodeCtx)
            nodes += node
          case pathCtx: MapFileParser.SurfaceElementAtomicPathContext =>
            val path = visitSurfaceElementAtomicPath(pathCtx)
            paths += path
          case arrowCtx: MapFileParser.SurfaceElementArrowContext =>
            // TODO: Ignore for now, to-be-updated
          case coreDef: MapFileParser.CoreDefContext => 
            ???
        }
      }
    }

    SubTopoMapExpr(
      name = name,
      params = params,
      types = List.empty, // TODO: Implement type aliases
      defns = List.empty, // TODO: Implement definitions
      data = List.empty, // TODO: Implement submap-level data
      nodes = nodes.toList,
      paths = paths.toList
    )
  }

  override def visitSurfaceDefTransportExpr(ctx: MapFileParser.SurfaceDefTransportExprContext): TransportExpr = {
    val name = ctx.ID().getText
    
    if (ctx.expr() != null) {
      // TODO: Should that one really be an expr?
//      if (ctx.expr().getText != "Elevator") // not "Elevator" or "Stairs" or "Escalator" or "Tram"{
//        throw RuntimeException(s"Unsupported transport type: ${ctx.expr().getText}")
//      }
    }
    
    val stationNodes = mutable.Map[TopoMapRef, TopoNodeExpr]()
    val stationLocations = mutable.Map[TopoMapRef, Double]()

    if (ctx.surfaceBody() != null) {
      for (element <- ctx.surfaceBody().surfaceBodyElement().asScala) {
        element match {
          case stationCtx: MapFileParser.SurfaceElementStationContext =>
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
  

  // TODO: Important one, but don't know what to do yet
  override def visitSurfaceElementCoreDef(ctx: MapFileParser.SurfaceElementCoreDefContext): SurfaceSyntax = {
    // 1. Get the abstract CoreDefContext child
    val coreCtx = ctx.coreDef()

    // 2. Dispatch based on the specific subclass (Alternative)
    coreCtx match {
      case c: MapFileParser.TypeDefContext =>
        val (name, tpe) = visitTypeDef(c)
        // Wrap the tuple in your AST case class
        SurfaceSyntax.TypeDef(name, tpe)

      case c: MapFileParser.FuncDefContext =>
        val (name, expr) = visitFuncDef(c)
        // Wrap the tuple in your AST case class
        SurfaceSyntax.FuncDef(name, expr)

      case c: MapFileParser.ExprContext =>
        val expr = visitExpr(c)
        // Wrap the expression in your AST case class
        SurfaceSyntax.ExprDef(expr)

      case c: MapFileParser.ScriptExprContext =>
        // Handle standalone expressions if allowed, or throw error
        throw new RuntimeException("Standalone expressions (ScriptExpr) are not allowed in this context.")

      case _ =>
        throw new RuntimeException(s"Unknown core definition type: ${coreCtx.getClass.getName}")
    }
  }

  override def visitSurfaceElementTopoNode(ctx: MapFileParser.SurfaceElementTopoNodeContext): TopoNodeExpr = {
    // 1. Get the wrapper node '{ x = 1, y = 2 }'
    val recordCtx = ctx.recordAssign()

    // 2. Get the list of assignments inside it
    // fieldAssign() returns a Java List, so .asScala works here
    val assignments = if (recordCtx.fieldAssign() == null) Seq.empty
    else recordCtx.fieldAssign().asScala

    // 3. Map them to (String, Expr) pairs
    val fields = assignments.map { fieldAssignment =>
      (fieldAssignment.ID().getText, fieldAssignment.expr.visit)
    }.toMap

    TopoNodeExpr(
      name = ctx.ID().getText,
      data = Expr.Record(fields)
    )
  }

  override def visitSurfaceElementAtomicPath(ctx: MapFileParser.SurfaceElementAtomicPathContext): AtomicPathExpr = {
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
  
  override def visitSurfaceElementArrow(ctx: MapFileParser.SurfaceElementArrowContext): TopoMapRef = {
    ???
  }
  
  override def visitSurfaceElementVehicleExpr(ctx: MapFileParser.SurfaceElementVehicleExprContext): VehicleRef = {
    VehicleRef(
      name = ctx.ID().getText
    )
  }
  
  override def visitSurfaceElementSubmapExpr(ctx: MapFileParser.SurfaceElementSubmapExprContext): TopoMapRef = {
    TopoMapRef(
      name = ctx.ID().getText
    )
  }
}