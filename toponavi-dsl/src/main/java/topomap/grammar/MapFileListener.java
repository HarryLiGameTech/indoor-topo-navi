// Generated from MapFile.g4 by ANTLR 4.13.2
package topomap.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MapFileParser}.
 */
public interface MapFileListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MapFileParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MapFileParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MapFileParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#topoMap}.
	 * @param ctx the parse tree
	 */
	void enterTopoMap(MapFileParser.TopoMapContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#topoMap}.
	 * @param ctx the parse tree
	 */
	void exitTopoMap(MapFileParser.TopoMapContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nodeContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterNodeContent(MapFileParser.NodeContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nodeContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitNodeContent(MapFileParser.NodeContentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code relationshipContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterRelationshipContent(MapFileParser.RelationshipContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code relationshipContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitRelationshipContent(MapFileParser.RelationshipContentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code pathContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterPathContent(MapFileParser.PathContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code pathContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitPathContent(MapFileParser.PathContentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code directionContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterDirectionContent(MapFileParser.DirectionContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code directionContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitDirectionContent(MapFileParser.DirectionContentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code modifierContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterModifierContent(MapFileParser.ModifierContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code modifierContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitModifierContent(MapFileParser.ModifierContentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code commentContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void enterCommentContent(MapFileParser.CommentContentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code commentContent}
	 * labeled alternative in {@link MapFileParser#topoMapContent}.
	 * @param ctx the parse tree
	 */
	void exitCommentContent(MapFileParser.CommentContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#topoNodeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTopoNodeDeclaration(MapFileParser.TopoNodeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#topoNodeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTopoNodeDeclaration(MapFileParser.TopoNodeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#relationshipDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterRelationshipDeclaration(MapFileParser.RelationshipDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#relationshipDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitRelationshipDeclaration(MapFileParser.RelationshipDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#pathDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPathDeclaration(MapFileParser.PathDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#pathDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPathDeclaration(MapFileParser.PathDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#directionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterDirectionDeclaration(MapFileParser.DirectionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#directionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitDirectionDeclaration(MapFileParser.DirectionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#modifierDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterModifierDeclaration(MapFileParser.ModifierDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#modifierDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitModifierDeclaration(MapFileParser.ModifierDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#modifierText}.
	 * @param ctx the parse tree
	 */
	void enterModifierText(MapFileParser.ModifierTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#modifierText}.
	 * @param ctx the parse tree
	 */
	void exitModifierText(MapFileParser.ModifierTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprFnCall}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprFnCall(MapFileParser.ExprFnCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprFnCall}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprFnCall(MapFileParser.ExprFnCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprMul}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprMul(MapFileParser.ExprMulContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprMul}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprMul(MapFileParser.ExprMulContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprAdd}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprAdd(MapFileParser.ExprAddContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprAdd}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprAdd(MapFileParser.ExprAddContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprIdentifier}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprIdentifier(MapFileParser.ExprIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprIdentifier}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprIdentifier(MapFileParser.ExprIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprPow}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprPow(MapFileParser.ExprPowContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprPow}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprPow(MapFileParser.ExprPowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprIf}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprIf(MapFileParser.ExprIfContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprIf}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprIf(MapFileParser.ExprIfContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprPrimitive}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprPrimitive(MapFileParser.ExprPrimitiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprPrimitive}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprPrimitive(MapFileParser.ExprPrimitiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprParan}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprParan(MapFileParser.ExprParanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprParan}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprParan(MapFileParser.ExprParanContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveInt}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveInt(MapFileParser.PrimitiveIntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveInt}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveInt(MapFileParser.PrimitiveIntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveBool}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveBool(MapFileParser.PrimitiveBoolContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveBool}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveBool(MapFileParser.PrimitiveBoolContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveFloat}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveFloat(MapFileParser.PrimitiveFloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveFloat}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveFloat(MapFileParser.PrimitiveFloatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveString}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveString(MapFileParser.PrimitiveStringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveString}
	 * labeled alternative in {@link MapFileParser#primitive}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveString(MapFileParser.PrimitiveStringContext ctx);
}