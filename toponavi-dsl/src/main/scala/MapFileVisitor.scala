import corelang.{Expr, OpKind, Type}
import topomap.grammar.MapFileParser.{AddSubExprContext, AppCExprContext, AppMlExprContext, AtomContext, AtomExprContext, BlockContext, CompExprContext, CoreDefContext, ExprContext, ExprStmtContext, FixExprContext, FuncDefContext, IfExprContext, LamExprContext, LetExprContext, LetRecExprContext, LetStmtContext, MulDivExprContext, NegExprContext, ProjExprContext, RecordTypeContext, TypeAtomContext, TypeDefContext, TypeExprContext}
import topomap.grammar.{MapFileBaseVisitor, MapFileParser}

import scala.jdk.CollectionConverters.*



class MapFileVisitor extends MapFileBaseVisitor[Any]{
  // --- Helpers for Currying ---

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
  override def visitCoreDef(ctx: CoreDefContext): Any = {
    if (ctx.typeDef() != null) visitTypeDef(ctx.typeDef())
    else if (ctx.funcDef() != null) visitFuncDef(ctx.funcDef())
    else visitExpr(ctx.expr())
  }

  override def visitTypeDef(ctx: TypeDefContext): (String, Type) = {
    (ctx.ID().getText, visitTypeExpr(ctx.typeExpr()))
  }

  override def visitFuncDef(ctx: FuncDefContext): (String, Expr) = {
    // def f(x: Int, y: Int): Int = body
    // Becomes: ("f", Lam("x", Int, Lam("y", Int, body)))
    // Note: Recursive definitions usually need LetRec, but at top-level 
    // we just return the name and the value (the lambda chain).

    val name = ctx.ID().getText
    val bodyRaw = visitExpr(ctx.expr())

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
    if (ctx.arrow != null) {
      // Int -> Int -> Int  =>  Arrow(Int, Arrow(Int, Int))
      Type.Arrow(visitTypeAtom(ctx.typeAtom()), visitTypeExpr(ctx.typeExpr()))
    } else {
      visitTypeAtom(ctx.typeAtom())
    }
  }

  override def visitTypeAtom(ctx: TypeAtomContext): Type = {
    if (ctx.ID() != null) {
      // Usually Type.Var or Enum ref. 
      // Since corelang.Type doesn't have a generic "Ref", we check for primitives 
      // or assume it's a named type (which might need resolution or EnumType).
      ctx.ID().getText match {
        case "Int"    => Type.IntType
        case "Float"  => Type.FloatType
        case "Bool"   => Type.BoolType
        case "String" => Type.StringType
        case other    =>
          // If it's not a primitive, it might be an EnumType or Alias.
          // Since we can't look up definitions here easily, we might need a placeholder 
          // or assume it's an EnumType if it's uppercase? 
          // For now, let's treat unknown IDs as EnumType with empty variants (to be resolved later) 
          // OR throw if strict.
          // corelang.Type doesn't have a "TypeRef". 
          // We'll throw for now as corelang seems to require full type info.
          throw new RuntimeException(s"Named types ($other) must be resolved or are not supported in this visitor yet.")
      }
    } else if (ctx.recordType() != null) {
      visitRecordType(ctx.recordType())
    } else if (ctx.typeExpr() != null) {
      visitTypeExpr(ctx.typeExpr())
    } else {
      visitTypeAtom(ctx) // Fallback for literals if they existed in TypeAtom
    }
  }

  override def visitRecordType(ctx: RecordTypeContext): Type = {
    val fields = ctx.fieldDecl().asScala.map { field =>
      (field.ID().getText, visitTypeExpr(field.typeExpr()))
    }.toMap
    Type.RecordType(fields)
  }

  // --- Expressions ---

  override def visitExpr(ctx: ExprContext): Expr = {
    ctx match {
      case c: AtomExprContext => visitAtom(c.atom())
      case c: ProjExprContext => Expr.Proj(visitExpr(c.expr()), c.ID().getText)

      // f x (Left Associative)
      case c: AppMlExprContext =>
        Expr.App(visitExpr(c.expr()), visitAtom(c.atom()))

      // f(x, y)
      case c: AppCExprContext =>
        val func = visitExpr(c.expr(0))
        val args = if (c.expr().size() > 1) c.expr().asScala.tail.map(visitExpr).toSeq else Seq.empty
        curryApply(func, args)

      case c: NegExprContext =>
        // Desugar -x to (0 - x) or similar, since Expr doesn't have UnaryOp
        Expr.BinOp(OpKind.Sub, Expr.IntLit(0), visitExpr(c.expr()))

      case c: MulDivExprContext =>
        val op = c.op.getText match {
          case "*" => OpKind.Mul
          case "/" => throw new UnsupportedOperationException("Division not supported in OpKind yet")
        }
        Expr.BinOp(op, visitExpr(c.expr(0)), visitExpr(c.expr(1)))

      case c: AddSubExprContext =>
        val op = c.op.getText match {
          case "+" => OpKind.Add
          case "-" => OpKind.Sub
        }
        Expr.BinOp(op, visitExpr(c.expr(0)), visitExpr(c.expr(1)))

      case c: CompExprContext =>
        val op = c.op.getText match {
          case "==" => OpKind.Eq
          case "<"  => OpKind.Lt
          case ">"  => OpKind.Gt
          case "<=" => throw new UnsupportedOperationException("<= not supported in OpKind")
          case ">=" => throw new UnsupportedOperationException(">= not supported in OpKind")
        }
        Expr.BinOp(op, visitExpr(c.expr(0)), visitExpr(c.expr(1)))

      case c: IfExprContext =>
        Expr.If(visitExpr(c.cond), visitExpr(c.ifExpr), visitExpr(c.elseExpr))

      case c: LetExprContext =>
        // let x = val in body
        Expr.Let(c.ID().getText, visitExpr(c.assignValue), visitExpr(c.expr(1)))

      case c: LetRecExprContext =>
        // let rec f: T = val in body
        val name = c.ID().getText
        val tpe = if (c.typeExpr() != null) visitTypeExpr(c.typeExpr()) else throw new RuntimeException("LetRec requires explicit type")
        Expr.LetRec(name, tpe, visitExpr(c.expr(0)), visitExpr(c.expr(1)))

      case c: FixExprContext =>
        Expr.Fix(c.ID().getText, visitTypeExpr(c.typeExpr()), visitExpr(c.expr()))

      case c: LamExprContext =>
        // \x: Int. body  OR  fn x: Int => body
        val name = c.ID().getText
        val tpe = visitTypeExpr(c.typeExpr())
        Expr.Lam(name, tpe, visitExpr(c.expr()))
    }
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
    else visitExpr(ctx.expr()) // Parentheses
  }

  private def visitRecordLiteral(ctx: AtomContext): Expr = {
    val fields = ctx.fieldAssign().asScala.map { field =>
      (field.ID().getText, visitExpr(field.expr()))
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
    val finalExpr = if (ctx.expr() != null) visitExpr(ctx.expr()) else Expr.Record(Map.empty) // Unit/Empty record if no final expr

    stmts.foldRight(finalExpr) { (stmt, acc) =>
      stmt match {
        case s: LetStmtContext =>
          // let x = e
          Expr.Let(s.ID().getText, visitExpr(s.expr()), acc)
        case s: ExprStmtContext =>
          // e (side effect, discard result)
          Expr.Let("_", visitExpr(s.expr()), acc)
      }
    }
  }
}