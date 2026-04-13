// Generated from MapFile.g4 by ANTLR 4.13.2
package topomap.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MapFileParser}.
 */
public interface MapFileListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code SurfaceDefRootExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceDefRootExpr(MapFileParser.SurfaceDefRootExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceDefRootExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceDefRootExpr(MapFileParser.SurfaceDefRootExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceDefTopoMapExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceDefTopoMapExpr(MapFileParser.SurfaceDefTopoMapExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceDefTopoMapExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceDefTopoMapExpr(MapFileParser.SurfaceDefTopoMapExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceDefTransportExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceDefTransportExpr(MapFileParser.SurfaceDefTransportExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceDefTransportExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceDefTransportExpr(MapFileParser.SurfaceDefTransportExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceDefGlobalConfigExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceDefGlobalConfigExpr(MapFileParser.SurfaceDefGlobalConfigExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceDefGlobalConfigExpr}
	 * labeled alternative in {@link MapFileParser#surfaceDef}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceDefGlobalConfigExpr(MapFileParser.SurfaceDefGlobalConfigExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#globalConfigBody}.
	 * @param ctx the parse tree
	 */
	void enterGlobalConfigBody(MapFileParser.GlobalConfigBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#globalConfigBody}.
	 * @param ctx the parse tree
	 */
	void exitGlobalConfigBody(MapFileParser.GlobalConfigBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#surfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceBody(MapFileParser.SurfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#surfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceBody(MapFileParser.SurfaceBodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code GlobalConfigElementVehicleRef}
	 * labeled alternative in {@link MapFileParser#globalConfigElement}.
	 * @param ctx the parse tree
	 */
	void enterGlobalConfigElementVehicleRef(MapFileParser.GlobalConfigElementVehicleRefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code GlobalConfigElementVehicleRef}
	 * labeled alternative in {@link MapFileParser#globalConfigElement}.
	 * @param ctx the parse tree
	 */
	void exitGlobalConfigElementVehicleRef(MapFileParser.GlobalConfigElementVehicleRefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code GlobalConfigElementSubmapRef}
	 * labeled alternative in {@link MapFileParser#globalConfigElement}.
	 * @param ctx the parse tree
	 */
	void enterGlobalConfigElementSubmapRef(MapFileParser.GlobalConfigElementSubmapRefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code GlobalConfigElementSubmapRef}
	 * labeled alternative in {@link MapFileParser#globalConfigElement}.
	 * @param ctx the parse tree
	 */
	void exitGlobalConfigElementSubmapRef(MapFileParser.GlobalConfigElementSubmapRefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementCoreDef}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementCoreDef(MapFileParser.SurfaceElementCoreDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementCoreDef}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementCoreDef(MapFileParser.SurfaceElementCoreDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementTopoNode}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementTopoNode(MapFileParser.SurfaceElementTopoNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementTopoNode}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementTopoNode(MapFileParser.SurfaceElementTopoNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementAtomicPath}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementAtomicPath(MapFileParser.SurfaceElementAtomicPathContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementAtomicPath}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementAtomicPath(MapFileParser.SurfaceElementAtomicPathContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementStation}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementStation(MapFileParser.SurfaceElementStationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementStation}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementStation(MapFileParser.SurfaceElementStationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementArrow}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementArrow(MapFileParser.SurfaceElementArrowContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementArrow}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementArrow(MapFileParser.SurfaceElementArrowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementLinearPath}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementLinearPath(MapFileParser.SurfaceElementLinearPathContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementLinearPath}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementLinearPath(MapFileParser.SurfaceElementLinearPathContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SurfaceElementConstraint}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void enterSurfaceElementConstraint(MapFileParser.SurfaceElementConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SurfaceElementConstraint}
	 * labeled alternative in {@link MapFileParser#surfaceBodyElement}.
	 * @param ctx the parse tree
	 */
	void exitSurfaceElementConstraint(MapFileParser.SurfaceElementConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void enterTypeDef(MapFileParser.TypeDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void exitTypeDef(MapFileParser.TypeDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void enterFuncDef(MapFileParser.FuncDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void exitFuncDef(MapFileParser.FuncDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LetDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void enterLetDef(MapFileParser.LetDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LetDef}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void exitLetDef(MapFileParser.LetDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ScriptExpr}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void enterScriptExpr(MapFileParser.ScriptExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ScriptExpr}
	 * labeled alternative in {@link MapFileParser#coreDef}.
	 * @param ctx the parse tree
	 */
	void exitScriptExpr(MapFileParser.ScriptExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(MapFileParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(MapFileParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(MapFileParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(MapFileParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#typeExpr}.
	 * @param ctx the parse tree
	 */
	void enterTypeExpr(MapFileParser.TypeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#typeExpr}.
	 * @param ctx the parse tree
	 */
	void exitTypeExpr(MapFileParser.TypeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#typeAtom}.
	 * @param ctx the parse tree
	 */
	void enterTypeAtom(MapFileParser.TypeAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#typeAtom}.
	 * @param ctx the parse tree
	 */
	void exitTypeAtom(MapFileParser.TypeAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#recordType}.
	 * @param ctx the parse tree
	 */
	void enterRecordType(MapFileParser.RecordTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#recordType}.
	 * @param ctx the parse tree
	 */
	void exitRecordType(MapFileParser.RecordTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void enterFieldDecl(MapFileParser.FieldDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void exitFieldDecl(MapFileParser.FieldDeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AppMlExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAppMlExpr(MapFileParser.AppMlExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AppMlExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAppMlExpr(MapFileParser.AppMlExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AppCExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAppCExpr(MapFileParser.AppCExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AppCExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAppCExpr(MapFileParser.AppCExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIfExpr(MapFileParser.IfExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIfExpr(MapFileParser.IfExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LetExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLetExpr(MapFileParser.LetExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LetExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLetExpr(MapFileParser.LetExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LetRecExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLetRecExpr(MapFileParser.LetRecExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LetRecExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLetRecExpr(MapFileParser.LetRecExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NegExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNegExpr(MapFileParser.NegExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNegExpr(MapFileParser.NegExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CompExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(MapFileParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CompExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(MapFileParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(MapFileParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(MapFileParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LamExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLamExpr(MapFileParser.LamExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LamExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLamExpr(MapFileParser.LamExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDivExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDivExpr(MapFileParser.MulDivExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDivExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDivExpr(MapFileParser.MulDivExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FixExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFixExpr(MapFileParser.FixExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FixExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFixExpr(MapFileParser.FixExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ProjExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterProjExpr(MapFileParser.ProjExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ProjExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitProjExpr(MapFileParser.ProjExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSubExpr(MapFileParser.AddSubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link MapFileParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSubExpr(MapFileParser.AddSubExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(MapFileParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(MapFileParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#fieldAssign}.
	 * @param ctx the parse tree
	 */
	void enterFieldAssign(MapFileParser.FieldAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#fieldAssign}.
	 * @param ctx the parse tree
	 */
	void exitFieldAssign(MapFileParser.FieldAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(MapFileParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(MapFileParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LetStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterLetStmt(MapFileParser.LetStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LetStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitLetStmt(MapFileParser.LetStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(MapFileParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link MapFileParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(MapFileParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#pathSpec}.
	 * @param ctx the parse tree
	 */
	void enterPathSpec(MapFileParser.PathSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#pathSpec}.
	 * @param ctx the parse tree
	 */
	void exitPathSpec(MapFileParser.PathSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#arrowSpec}.
	 * @param ctx the parse tree
	 */
	void enterArrowSpec(MapFileParser.ArrowSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#arrowSpec}.
	 * @param ctx the parse tree
	 */
	void exitArrowSpec(MapFileParser.ArrowSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#linearPathSpec}.
	 * @param ctx the parse tree
	 */
	void enterLinearPathSpec(MapFileParser.LinearPathSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#linearPathSpec}.
	 * @param ctx the parse tree
	 */
	void exitLinearPathSpec(MapFileParser.LinearPathSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#requirements}.
	 * @param ctx the parse tree
	 */
	void enterRequirements(MapFileParser.RequirementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#requirements}.
	 * @param ctx the parse tree
	 */
	void exitRequirements(MapFileParser.RequirementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#constraintBody}.
	 * @param ctx the parse tree
	 */
	void enterConstraintBody(MapFileParser.ConstraintBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#constraintBody}.
	 * @param ctx the parse tree
	 */
	void exitConstraintBody(MapFileParser.ConstraintBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#requireClause}.
	 * @param ctx the parse tree
	 */
	void enterRequireClause(MapFileParser.RequireClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#requireClause}.
	 * @param ctx the parse tree
	 */
	void exitRequireClause(MapFileParser.RequireClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#recordAssign}.
	 * @param ctx the parse tree
	 */
	void enterRecordAssign(MapFileParser.RecordAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#recordAssign}.
	 * @param ctx the parse tree
	 */
	void exitRecordAssign(MapFileParser.RecordAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link MapFileParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(MapFileParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MapFileParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(MapFileParser.IdentifierContext ctx);
}