// Generated from MapFile.g4 by ANTLR 4.13.2
package topomap.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MapFileParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MapFileVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by the {@code RootExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpr(MapFileParser.RootExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TopoMapExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopoMapExpr(MapFileParser.TopoMapExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TransportExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransportExpr(MapFileParser.TransportExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#surfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSurfaceBody(MapFileParser.SurfaceBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CoreExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCoreExpr(MapFileParser.CoreExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TopoNodeExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopoNodeExpr(MapFileParser.TopoNodeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AtomicPathExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomicPathExpr(MapFileParser.AtomicPathExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArrowExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrowExpr(MapFileParser.ArrowExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VehicleExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVehicleExpr(MapFileParser.VehicleExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SubmapExpr}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubmapExpr(MapFileParser.SubmapExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TypeDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDef(MapFileParser.TypeDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncDef(MapFileParser.FuncDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ScriptExpr}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScriptExpr(MapFileParser.ScriptExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(MapFileParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(MapFileParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#typeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeExpr(MapFileParser.TypeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#typeAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAtom(MapFileParser.TypeAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#recordType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordType(MapFileParser.RecordTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#fieldDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDecl(MapFileParser.FieldDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AppMlExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAppMlExpr(MapFileParser.AppMlExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AppCExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAppCExpr(MapFileParser.AppCExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExpr(MapFileParser.IfExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LetExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetExpr(MapFileParser.LetExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LetRecExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetRecExpr(MapFileParser.LetRecExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegExpr(MapFileParser.NegExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CompExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompExpr(MapFileParser.CompExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(MapFileParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LamExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLamExpr(MapFileParser.LamExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MulDivExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivExpr(MapFileParser.MulDivExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FixExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixExpr(MapFileParser.FixExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ProjExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjExpr(MapFileParser.ProjExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSubExpr(MapFileParser.AddSubExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(MapFileParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#fieldAssign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAssign(MapFileParser.FieldAssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MapFileParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LetStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetStmt(MapFileParser.LetStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(MapFileParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#pathSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathSpec(MapFileParser.PathSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#arrowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrowSpec(MapFileParser.ArrowSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#arrowHeading}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrowHeading(MapFileParser.ArrowHeadingContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#requirements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRequirements(MapFileParser.RequirementsContext ctx);
}