import corelang.{Expr, OpKind, Type}
import topomap.grammar.MapFileParser.{AddSubExprContext, AppCExprContext, AppMlExprContext, AtomContext, AtomExprContext, BlockContext, CompExprContext, ExprStmtContext, FixExprContext, FuncDefContext, IfExprContext, LamExprContext, LetExprContext, LetRecExprContext, LetStmtContext, MulDivExprContext, NegExprContext, ProjExprContext, RecordTypeContext, TypeAtomContext, TypeDefContext, TypeExprContext}
import topomap.grammar.{MapFileBaseVisitor, MapFileParser}
import org.antlr.v4.runtime.tree.ParseTree

import scala.jdk.CollectionConverters.*



class TopoMapVisitor extends MapFileBaseVisitor[Any]{
  // --- Helpers for Currying ---

  def visitE(tree: ParseTree): Expr = visit(tree).asInstanceOf[Expr]

  private def curryLambda(params: Seq[(String, Type)], body: Expr): Expr = {
    params.foldRight(body) { case ((name, tpe), acc) =>
      Expr.Lam(name, tpe, acc)
    }
  }

  private def curryApply(func: Expr, args: Seq[Expr]): Expr = {
    args.foldLeft(func) { (acc, arg) =>
      Expr.App(acc, arg)
    }
  }

  // --- Top Level ---

  /**
   * Returns:
   * - (String, Type) for TypeDef
   * - (String, Expr) for FuncDef (desugared to let-rec style tuple, but without the 'in')
   * - Expr for ScriptExpr
   */
//  override def visitCoreDef(ctx: CoreDefContext): Any = {
//    if (ctx.typeDef() != null) visitTypeDef(ctx.typeDef())
//    else if (ctx.funcDef() != null) visitFuncDef(ctx.funcDef())
//    else visitExpr(ctx.expr())
//  }

  override def visitTypeDef(ctx: TypeDefContext): (String, Type) = {
    (ctx.ID().getText, visitTypeExpr(ctx.typeExpr()))
  }

  override def visitFuncDef(ctx: FuncDefContext): (String, Expr) = {
    // def f(x: Int, y: Int): Int = body
    // Becomes: ("f", Lam("x", Int, Lam("y", Int, body)))
    // Note: Recursive definitions usually need LetRec, but at top-level 
    // we just return the name and the value (the lambda chain).

    val name = ctx.ID().getText
    val bodyRaw = visitE(ctx.expr())

    val params = if (ctx.paramList() == null) Seq.empty else {
      ctx.paramList().param().asScala.map { p =>
        (p.ID().getText, visitTypeExpr(p.typeExpr()))
      }.toSeq
    }

    val bodyWithTypes = ctx.typeExpr() match {
      // If return type is explicit, we might assume the body satisfies it. 
      // In corelang, Lambda takes arg type, but return type is inferred.
      // We do not wrap body in a type check node as Expr doesn't have one (only Fix does).
      case null => bodyRaw
      case _ => bodyRaw // Return type ignored in AST construction, handled by checker
    }

    (name, curryLambda(params, bodyWithTypes))
  }

  // --- Types ---
  override def visitTypeExpr(ctx: TypeExprContext): Type = {
    if (ctx.typeExpr() != null) {
      // Int -> Int -> Int  =>  Arrow(Int, Arrow(Int, Int))
      Type.Arrow(visitTypeAtom(ctx.typeAtom()), visitTypeExpr(ctx.typeExpr()))
    } else {
      visitTypeAtom(ctx.typeAtom())
    }
  }

  override def visitTypeAtom(ctx: TypeAtomContext): Type = {
    if (ctx.typeExpr() != null) {
      // Case: '(' typeExpr ')'
      visitTypeExpr(ctx.typeExpr())
    }
    else if (ctx.recordType() != null) {
      // Case: recordType
      visitRecordType(ctx.recordType())
    }
    else if (ctx.ID() != null) {
      // Case: ID (named types like aliases or enums)
      val name = ctx.ID().getText
      // We throw here because corelang.Type requires resolved types, not just names
      throw new RuntimeException(s"Named types ($name) must be resolved or are not supported in this visitor yet.")
    }
    else {
      // Case: 'Int' | 'Float' | 'Bool' | 'String'
      // These are keywords, so they don't have helper methods like ID(),
      // but we can check the text directly.
      ctx.getText match {
        case "Int"    => Type.IntType
        case "Float"  => Type.FloatType
        case "Bool"   => Type.BoolType
        case "String" => Type.StringType
        case other    => throw new RuntimeException(s"Unknown type atom: $other")
      }
    }
  }

  override def visitRecordType(ctx: RecordTypeContext): Type = {
    val fields = ctx.fieldDecl().asScala.map { field =>
      (field.ID().getText, visitTypeExpr(field.typeExpr()))
    }.toMap
    Type.RecordType(fields)
  }

  // --- Expressions ---

  override def visitAtomExpr(ctx: AtomExprContext): Expr = visitAtom(ctx.atom())

  override def visitProjExpr(ctx: ProjExprContext): Expr = Expr.Proj(visitE(ctx.expr()), ctx.ID().getText)

  override def visitAppMlExpr(ctx: AppMlExprContext): Expr =
    Expr.App(visitE(ctx.expr()), visitAtom(ctx.atom()))

  override def visitAppCExpr(ctx: AppCExprContext): Expr = {
    val func = visitE(ctx.expr(0))
    val args = if (ctx.expr().size() > 1) ctx.expr().asScala.tail.map(visitE).toSeq else Seq.empty
    curryApply(func, args)
  }

  override def visitNegExpr(ctx: NegExprContext): Expr =
    Expr.BinOp(OpKind.Sub, Expr.IntLit(0), visitE(ctx.expr()))

  override def visitMulDivExpr(ctx: MulDivExprContext): Expr = {
    val op = ctx.op.getText match {
      case "*" => OpKind.Mul
      case "/" => throw new UnsupportedOperationException("Division not supported in OpKind yet")
    }
    Expr.BinOp(op, visitE(ctx.expr(0)), visitE(ctx.expr(1)))
  }

  override def visitAddSubExpr(ctx: AddSubExprContext): Expr = {
    val op = ctx.op.getText match {
      case "+" => OpKind.Add
      case "-" => OpKind.Sub
    }
    Expr.BinOp(op, visitE(ctx.expr(0)), visitE(ctx.expr(1)))
  }

  override def visitCompExpr(ctx: CompExprContext): Expr = {
    val op = ctx.op.getText match {
      case "==" => OpKind.Eq
      case "<"  => OpKind.Lt
      case ">"  => OpKind.Gt
      case "<=" => throw new UnsupportedOperationException("<= not supported in OpKind")
      case ">=" => throw new UnsupportedOperationException(">= not supported in OpKind")
    }
    Expr.BinOp(op, visitE(ctx.expr(0)), visitE(ctx.expr(1)))
  }

  override def visitIfExpr(ctx: IfExprContext): Expr =
    Expr.If(visitE(ctx.cond), visitE(ctx.ifExpr), visitE(ctx.elseExpr))

  override def visitLetExpr(ctx: LetExprContext): Expr =
    Expr.Let(ctx.ID().getText, visitE(ctx.assignValue), visitE(ctx.expr(1)))

  override def visitLetRecExpr(ctx: LetRecExprContext): Expr = {
    val name = ctx.ID().getText
    val tpe = if (ctx.typeExpr() != null) visitTypeExpr(ctx.typeExpr()) else throw new RuntimeException("LetRec requires explicit type")
    Expr.LetRec(name, tpe, visitE(ctx.expr(0)), visitE(ctx.expr(1)))
  }

  override def visitFixExpr(ctx: FixExprContext): Expr =
    Expr.Fix(ctx.ID().getText, visitTypeExpr(ctx.typeExpr()), visitE(ctx.expr()))

  override def visitLamExpr(ctx: LamExprContext): Expr = {
    val name = ctx.ID().getText
    val tpe = visitTypeExpr(ctx.typeExpr())
    Expr.Lam(name, tpe, visitE(ctx.expr()))
  }

  // --- Atoms ---

  override def visitAtom(ctx: AtomContext): Expr = {
    if (ctx.INT() != null) Expr.IntLit(ctx.INT().getText.toLong)
    else if (ctx.FLOAT() != null) Expr.FloatLit(ctx.FLOAT().getText.toDouble)
    else if (ctx.getText == "true") Expr.BoolLit(true)
    else if (ctx.getText == "false") Expr.BoolLit(false)
    else if (ctx.ID() != null) Expr.Var(ctx.ID().getText)
    else if (ctx.block() != null) visitBlock(ctx.block())
    else if (ctx.getChild(0).getText == "{") visitRecordLiteral(ctx)
    else visitE(ctx.expr()) // Parentheses
  }

  private def visitRecordLiteral(ctx: AtomContext): Expr = {
    val fields = ctx.fieldAssign().asScala.map { field =>
      (field.ID().getText, visitE(field.expr()))
    }.toMap
    Expr.Record(fields)
  }

  // --- Blocks ---

  override def visitBlock(ctx: BlockContext): Expr = {
    // { s1; s2; expr } -> Let(s1, Let(s2, expr))
    // Note: corelang.Expr.Let takes (name, value, body). 
    // This assumes statements are 'let x = ...'.
    // If statement is just an expression (side effect), we treat it as Let("_", expr, body)

    val stmts = ctx.stmt().asScala
    val finalExpr = if (ctx.expr() != null) visitE(ctx.expr()) else Expr.Record(Map.empty) // Unit/Empty record if no final expr

    stmts.foldRight(finalExpr) { (stmt, acc) =>
      stmt match {
        case s: LetStmtContext =>
          // let x = e
          Expr.Let(s.ID().getText, visitE(s.expr()), acc)
        case s: ExprStmtContext =>
          // e (side effect, discard result)
          Expr.Let("_", visitE(s.expr()), acc)
      }
    }
  }
}