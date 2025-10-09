import topomap.grammar.{MapFileBaseVisitor, MapFileParser}
import scala.jdk.CollectionConverters._

class MapFileVisitor extends MapFileBaseVisitor[Any]{
  override def visitProgram(ctx: MapFileParser.ProgramContext): Any = {
    visitTopoMap(ctx.topoMap(0)) // Why int as input
  }

  override def visitTopoMap(ctx: MapFileParser.TopoMapContext): Any = {
    val mapName = ctx.name.getText

    var topoNodes = List.empty[String]
    var paths = List.empty[(String, String, Any, String)]

    for (content <- ctx.topoMapContent().asScala) {
      content match {
        case nodeCtx: MapFileParser.NodeContentContext =>
          val node = nodeCtx.topoNodeDeclaration()
          val name = node.name.getText
        case pathCtx: MapFileParser.PathContentContext =>
          val path = pathCtx.pathDeclaration()
          val origin = path.from.getText
          val destination = path.to.getText
          val cost = visitExpr(path.cost)
//          val modifier = visitModifierContent(path.modifier)
        case directionCtx: MapFileParser.DirectionContentContext =>
        case _ => -1
      }
    }
  }

//  override def visitNode(ctx: MapFileParser.NodeContentContext): Any = {
//
//  }

  override def visitPathContent(ctx: MapFileParser.PathContentContext): Any = {

  }

  def visitExpr(ctx: MapFileParser.ExprContext): Any = {

  }

  override def visitExprIdentifier(ctx: MapFileParser.ExprIdentifierContext): Any = {

  }

  override def visitExprPrimitive(ctx: MapFileParser.ExprPrimitiveContext): Any = {

  }

  override def visitExprFnCall(ctx: MapFileParser.ExprFnCallContext): Any = {

  }

  override def visitExprParan(ctx: MapFileParser.ExprParanContext): Any = {

  }

  override def visitExprAdd(ctx: MapFileParser.ExprAddContext): Any = {

  }

  override def visitExprMul(ctx: MapFileParser.ExprMulContext): Any = {

  }

  override def visitExprPow(ctx: MapFileParser.ExprPowContext): Any = {

  }

  override def visitExprIf(ctx: MapFileParser.ExprIfContext): Any = {

  }

//  override def visitModifierDeclaration(ctx: MapFileParser.ModifierContentContext): Any = {
//
//  }
}