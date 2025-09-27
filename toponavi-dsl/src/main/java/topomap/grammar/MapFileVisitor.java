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
	 * Visit a parse tree produced by {@link MapFileParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MapFileParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#topoMap}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopoMap(MapFileParser.TopoMapContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopoMapContent(MapFileParser.TopoMapContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#topoNodeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopoNodeDeclaration(MapFileParser.TopoNodeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#relationshipDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationshipDeclaration(MapFileParser.RelationshipDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#pathDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathDeclaration(MapFileParser.PathDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#directionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectionDeclaration(MapFileParser.DirectionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#modifierDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifierDeclaration(MapFileParser.ModifierDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MapFileParser#modifierContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifierContent(MapFileParser.ModifierContentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprFnCall}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprFnCall(MapFileParser.ExprFnCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprMul}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprMul(MapFileParser.ExprMulContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprAdd}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprAdd(MapFileParser.ExprAddContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprIdentifier}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprIdentifier(MapFileParser.ExprIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprPow}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprPow(MapFileParser.ExprPowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprIf}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprIf(MapFileParser.ExprIfContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprPrimitive}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprPrimitive(MapFileParser.ExprPrimitiveContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprParan}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprParan(MapFileParser.ExprParanContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveInt}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveInt(MapFileParser.PrimitiveIntContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveBool}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveBool(MapFileParser.PrimitiveBoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveFloat}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveFloat(MapFileParser.PrimitiveFloatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveString}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveString(MapFileParser.PrimitiveStringContext ctx);
}