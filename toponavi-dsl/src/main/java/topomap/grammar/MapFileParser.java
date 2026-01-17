// Generated from MapFile.g4 by ANTLR 4.13.2
package topomap.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MapFileParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		ID=53, FLOAT=54, INT=55, STRING=56, NL=57, WS=58, COMMENT=59, BLOCK_COMMENT=60;
	public static final int
		RULE_surfaceDef = 0, RULE_surfaceBody = 1, RULE_surfaceBodyElement = 2, 
		RULE_coreDef = 3, RULE_paramList = 4, RULE_param = 5, RULE_typeExpr = 6, 
		RULE_typeAtom = 7, RULE_recordType = 8, RULE_fieldDecl = 9, RULE_expr = 10, 
		RULE_atom = 11, RULE_fieldAssign = 12, RULE_block = 13, RULE_stmt = 14, 
		RULE_pathSpec = 15, RULE_arrowSpec = 16, RULE_arrowHeading = 17, RULE_requirements = 18, 
		RULE_recordAssign = 19;
	private static String[] makeRuleNames() {
		return new String[] {
			"surfaceDef", "surfaceBody", "surfaceBodyElement", "coreDef", "paramList", 
			"param", "typeExpr", "typeAtom", "recordType", "fieldDecl", "expr", "atom", 
			"fieldAssign", "block", "stmt", "pathSpec", "arrowSpec", "arrowHeading", 
			"requirements", "recordAssign"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'root'", "'('", "')'", "'topo-map'", "'transport'", "'is'", "'{'", 
			"'}'", "'topo-node'", "'atomic-path'", "'arrow'", "'>>'", "'vehicle'", 
			"'submap'", "'type'", "'='", "'def'", "':'", "','", "'->'", "'Int'", 
			"'Float'", "'Bool'", "'String'", "'.'", "'-'", "'*'", "'/'", "'+'", "'=='", 
			"'<'", "'>'", "'<='", "'>='", "'if'", "'then'", "'else'", "'let'", "'in'", 
			"'rec'", "'fix'", "'\\'", "'fn'", "'=>'", "'true'", "'false'", "';'", 
			"'['", "']'", "'^^'", "'requires'", "'&&'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "ID", "FLOAT", "INT", "STRING", "NL", "WS", 
			"COMMENT", "BLOCK_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "MapFile.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MapFileParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceDefContext extends ParserRuleContext {
		public SurfaceDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_surfaceDef; }
	 
		public SurfaceDefContext() { }
		public void copyFrom(SurfaceDefContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TopoMapExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public TopoMapExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTopoMapExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTopoMapExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTopoMapExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public RootExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRootExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRootExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRootExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TransportExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public TransportExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTransportExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTransportExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTransportExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SurfaceDefContext surfaceDef() throws RecognitionException {
		SurfaceDefContext _localctx = new SurfaceDefContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_surfaceDef);
		int _la;
		try {
			setState(62);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				_localctx = new RootExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(40);
				match(T__0);
				setState(41);
				match(ID);
				setState(42);
				match(T__1);
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(43);
					paramList();
					}
				}

				setState(46);
				match(T__2);
				setState(47);
				surfaceBody();
				}
				break;
			case T__3:
				_localctx = new TopoMapExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(48);
				match(T__3);
				setState(49);
				match(ID);
				setState(50);
				match(T__1);
				setState(52);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(51);
					paramList();
					}
				}

				setState(54);
				match(T__2);
				setState(55);
				surfaceBody();
				}
				break;
			case T__4:
				_localctx = new TransportExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(56);
				match(T__4);
				setState(57);
				match(ID);
				setState(58);
				match(T__5);
				setState(59);
				expr(0);
				setState(60);
				surfaceBody();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceBodyContext extends ParserRuleContext {
		public List<SurfaceBodyElementContext> surfaceBodyElement() {
			return getRuleContexts(SurfaceBodyElementContext.class);
		}
		public SurfaceBodyElementContext surfaceBodyElement(int i) {
			return getRuleContext(SurfaceBodyElementContext.class,i);
		}
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
		public SurfaceBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_surfaceBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SurfaceBodyContext surfaceBody() throws RecognitionException {
		SurfaceBodyContext _localctx = new SurfaceBodyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_surfaceBody);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			match(T__6);
			setState(70);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(65);
					surfaceBodyElement();
					setState(66);
					match(NL);
					}
					} 
				}
				setState(72);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(73);
			surfaceBodyElement();
			setState(74);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceBodyElementContext extends ParserRuleContext {
		public SurfaceBodyElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_surfaceBodyElement; }
	 
		public SurfaceBodyElementContext() { }
		public void copyFrom(SurfaceBodyElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementCoreDefContext extends SurfaceBodyElementContext {
		public CoreDefContext coreDef() {
			return getRuleContext(CoreDefContext.class,0);
		}
		public SurfaceElementCoreDefContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementCoreDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementCoreDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementCoreDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementArrowContext extends SurfaceBodyElementContext {
		public ArrowSpecContext arrowSpec() {
			return getRuleContext(ArrowSpecContext.class,0);
		}
		public ArrowHeadingContext arrowHeading() {
			return getRuleContext(ArrowHeadingContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public SurfaceElementArrowContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementArrow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementArrow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementArrow(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementSubmapExprContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceElementSubmapExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementSubmapExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementSubmapExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementSubmapExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementTopoNodeContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public RecordAssignContext recordAssign() {
			return getRuleContext(RecordAssignContext.class,0);
		}
		public SurfaceElementTopoNodeContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementTopoNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementTopoNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementTopoNode(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementVehicleExprContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceElementVehicleExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementVehicleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementVehicleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementVehicleExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceElementAtomicPathContext extends SurfaceBodyElementContext {
		public PathSpecContext pathSpec() {
			return getRuleContext(PathSpecContext.class,0);
		}
		public RecordAssignContext recordAssign() {
			return getRuleContext(RecordAssignContext.class,0);
		}
		public RequirementsContext requirements() {
			return getRuleContext(RequirementsContext.class,0);
		}
		public SurfaceElementAtomicPathContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementAtomicPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementAtomicPath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementAtomicPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SurfaceBodyElementContext surfaceBodyElement() throws RecognitionException {
		SurfaceBodyElementContext _localctx = new SurfaceBodyElementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_surfaceBodyElement);
		try {
			setState(95);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__6:
			case T__14:
			case T__16:
			case T__25:
			case T__34:
			case T__37:
			case T__40:
			case T__41:
			case T__42:
			case T__44:
			case T__45:
			case ID:
			case FLOAT:
			case INT:
			case STRING:
				_localctx = new SurfaceElementCoreDefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				coreDef();
				}
				break;
			case T__8:
				_localctx = new SurfaceElementTopoNodeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(77);
				match(T__8);
				setState(78);
				match(ID);
				setState(79);
				recordAssign();
				}
				break;
			case T__9:
				_localctx = new SurfaceElementAtomicPathContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(80);
				match(T__9);
				setState(81);
				pathSpec();
				setState(82);
				recordAssign();
				setState(83);
				requirements();
				}
				break;
			case T__10:
				_localctx = new SurfaceElementArrowContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(85);
				match(T__10);
				setState(86);
				arrowSpec();
				setState(87);
				arrowHeading();
				setState(88);
				match(T__11);
				setState(89);
				expr(0);
				}
				break;
			case T__12:
				_localctx = new SurfaceElementVehicleExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(91);
				match(T__12);
				setState(92);
				match(ID);
				}
				break;
			case T__13:
				_localctx = new SurfaceElementSubmapExprContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(93);
				match(T__13);
				setState(94);
				match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CoreDefContext extends ParserRuleContext {
		public CoreDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coreDef; }
	 
		public CoreDefContext() { }
		public void copyFrom(CoreDefContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeDefContext extends CoreDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public TypeDefContext(CoreDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTypeDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTypeDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTypeDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncDefContext extends CoreDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public FuncDefContext(CoreDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterFuncDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitFuncDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitFuncDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ScriptExprContext extends CoreDefContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ScriptExprContext(CoreDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterScriptExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitScriptExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitScriptExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoreDefContext coreDef() throws RecognitionException {
		CoreDefContext _localctx = new CoreDefContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_coreDef);
		int _la;
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__14:
				_localctx = new TypeDefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				match(T__14);
				setState(98);
				match(ID);
				setState(99);
				match(T__15);
				setState(100);
				typeExpr();
				}
				break;
			case T__16:
				_localctx = new FuncDefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(101);
				match(T__16);
				setState(102);
				match(ID);
				setState(103);
				match(T__1);
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(104);
					paramList();
					}
				}

				setState(107);
				match(T__2);
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(108);
					match(T__17);
					setState(109);
					typeExpr();
					}
				}

				setState(112);
				match(T__15);
				setState(113);
				expr(0);
				}
				break;
			case T__1:
			case T__6:
			case T__25:
			case T__34:
			case T__37:
			case T__40:
			case T__41:
			case T__42:
			case T__44:
			case T__45:
			case ID:
			case FLOAT:
			case INT:
			case STRING:
				_localctx = new ScriptExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(114);
				expr(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamListContext extends ParserRuleContext {
		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}
		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitParamList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitParamList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			param();
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__18) {
				{
				{
				setState(118);
				match(T__18);
				setState(119);
				param();
				}
				}
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitParam(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			match(ID);
			setState(126);
			match(T__17);
			setState(127);
			typeExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeExprContext extends ParserRuleContext {
		public TypeAtomContext typeAtom() {
			return getRuleContext(TypeAtomContext.class,0);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public TypeExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTypeExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTypeExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTypeExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeExprContext typeExpr() throws RecognitionException {
		TypeExprContext _localctx = new TypeExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_typeExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			typeAtom();
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__19) {
				{
				setState(130);
				match(T__19);
				setState(131);
				typeExpr();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeAtomContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public RecordTypeContext recordType() {
			return getRuleContext(RecordTypeContext.class,0);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public TypeAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeAtom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTypeAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTypeAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTypeAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeAtomContext typeAtom() throws RecognitionException {
		TypeAtomContext _localctx = new TypeAtomContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_typeAtom);
		try {
			setState(144);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
				enterOuterAlt(_localctx, 1);
				{
				setState(134);
				match(T__20);
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 2);
				{
				setState(135);
				match(T__21);
				}
				break;
			case T__22:
				enterOuterAlt(_localctx, 3);
				{
				setState(136);
				match(T__22);
				}
				break;
			case T__23:
				enterOuterAlt(_localctx, 4);
				{
				setState(137);
				match(T__23);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 5);
				{
				setState(138);
				match(ID);
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 6);
				{
				setState(139);
				recordType();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 7);
				{
				setState(140);
				match(T__1);
				setState(141);
				typeExpr();
				setState(142);
				match(T__2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordTypeContext extends ParserRuleContext {
		public List<FieldDeclContext> fieldDecl() {
			return getRuleContexts(FieldDeclContext.class);
		}
		public FieldDeclContext fieldDecl(int i) {
			return getRuleContext(FieldDeclContext.class,i);
		}
		public RecordTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRecordType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRecordType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRecordType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordTypeContext recordType() throws RecognitionException {
		RecordTypeContext _localctx = new RecordTypeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_recordType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(T__6);
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(147);
				fieldDecl();
				setState(152);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__18) {
					{
					{
					setState(148);
					match(T__18);
					setState(149);
					fieldDecl();
					}
					}
					setState(154);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(157);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldDeclContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public FieldDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterFieldDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitFieldDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitFieldDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDeclContext fieldDecl() throws RecognitionException {
		FieldDeclContext _localctx = new FieldDeclContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_fieldDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			match(ID);
			setState(160);
			match(T__17);
			setState(161);
			typeExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AppMlExprContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AppMlExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAppMlExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAppMlExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAppMlExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AppCExprContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public AppCExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAppCExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAppCExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAppCExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IfExprContext extends ExprContext {
		public ExprContext cond;
		public ExprContext ifExpr;
		public ExprContext elseExpr;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public IfExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterIfExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitIfExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitIfExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetExprContext extends ExprContext {
		public TypeExprContext letType;
		public ExprContext assignValue;
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public LetExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterLetExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitLetExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitLetExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetRecExprContext extends ExprContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public LetRecExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterLetRecExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitLetRecExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitLetRecExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegExprContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NegExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterNegExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitNegExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitNegExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public CompExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterCompExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitCompExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitCompExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AtomExprContext extends ExprContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAtomExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAtomExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAtomExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LamExprContext extends ExprContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public LamExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterLamExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitLamExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitLamExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MulDivExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public MulDivExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterMulDivExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitMulDivExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitMulDivExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FixExprContext extends ExprContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FixExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterFixExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitFixExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitFixExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ProjExprContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ProjExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterProjExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitProjExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitProjExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AddSubExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public AddSubExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAddSubExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAddSubExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAddSubExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 20;
		enterRecursionRule(_localctx, 20, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(164);
				atom();
				}
				break;
			case 2:
				{
				_localctx = new NegExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(165);
				match(T__25);
				setState(166);
				expr(9);
				}
				break;
			case 3:
				{
				_localctx = new IfExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(167);
				match(T__34);
				setState(168);
				((IfExprContext)_localctx).cond = expr(0);
				setState(169);
				match(T__35);
				setState(170);
				((IfExprContext)_localctx).ifExpr = expr(0);
				setState(171);
				match(T__36);
				setState(172);
				((IfExprContext)_localctx).elseExpr = expr(5);
				}
				break;
			case 4:
				{
				_localctx = new LetExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(174);
				match(T__37);
				setState(175);
				match(ID);
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(176);
					match(T__17);
					setState(177);
					((LetExprContext)_localctx).letType = typeExpr();
					}
				}

				setState(180);
				match(T__15);
				setState(181);
				((LetExprContext)_localctx).assignValue = expr(0);
				setState(182);
				match(T__38);
				setState(183);
				expr(4);
				}
				break;
			case 5:
				{
				_localctx = new LetRecExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(185);
				match(T__37);
				setState(186);
				match(T__39);
				setState(187);
				match(ID);
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(188);
					match(T__17);
					setState(189);
					typeExpr();
					}
				}

				setState(192);
				match(T__15);
				setState(193);
				expr(0);
				setState(194);
				match(T__38);
				setState(195);
				expr(3);
				}
				break;
			case 6:
				{
				_localctx = new FixExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(197);
				match(T__40);
				setState(198);
				match(ID);
				setState(199);
				match(T__17);
				setState(200);
				typeExpr();
				setState(201);
				match(T__24);
				setState(202);
				expr(2);
				}
				break;
			case 7:
				{
				_localctx = new LamExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(204);
				_la = _input.LA(1);
				if ( !(_la==T__41 || _la==T__42) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(205);
				match(ID);
				setState(206);
				match(T__17);
				setState(207);
				typeExpr();
				setState(208);
				_la = _input.LA(1);
				if ( !(_la==T__24 || _la==T__43) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(209);
				expr(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(242);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(240);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
					case 1:
						{
						_localctx = new MulDivExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(213);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(214);
						((MulDivExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__26 || _la==T__27) ) {
							((MulDivExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(215);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new AddSubExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(216);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(217);
						((AddSubExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__25 || _la==T__28) ) {
							((AddSubExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(218);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(219);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(220);
						((CompExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 33285996544L) != 0)) ) {
							((CompExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(221);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new ProjExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(222);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(223);
						match(T__24);
						setState(224);
						match(ID);
						}
						break;
					case 5:
						{
						_localctx = new AppMlExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(225);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(226);
						atom();
						}
						break;
					case 6:
						{
						_localctx = new AppCExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(227);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(228);
						match(T__1);
						setState(237);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 135229244404924548L) != 0)) {
							{
							setState(229);
							expr(0);
							setState(234);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__18) {
								{
								{
								setState(230);
								match(T__18);
								setState(231);
								expr(0);
								}
								}
								setState(236);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(239);
						match(T__2);
						}
						break;
					}
					} 
				}
				setState(244);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(MapFileParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(MapFileParser.FLOAT, 0); }
		public TerminalNode STRING() { return getToken(MapFileParser.STRING, 0); }
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public List<FieldAssignContext> fieldAssign() {
			return getRuleContexts(FieldAssignContext.class);
		}
		public FieldAssignContext fieldAssign(int i) {
			return getRuleContext(FieldAssignContext.class,i);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_atom);
		int _la;
		try {
			setState(268);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(245);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(246);
				match(FLOAT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(247);
				match(STRING);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(248);
				match(T__44);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(249);
				match(T__45);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(250);
				match(ID);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(251);
				match(T__6);
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(252);
					fieldAssign();
					setState(257);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__18) {
						{
						{
						setState(253);
						match(T__18);
						setState(254);
						fieldAssign();
						}
						}
						setState(259);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(262);
				match(T__7);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(263);
				block();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(264);
				match(T__1);
				setState(265);
				expr(0);
				setState(266);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldAssignContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FieldAssignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldAssign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterFieldAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitFieldAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitFieldAssign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldAssignContext fieldAssign() throws RecognitionException {
		FieldAssignContext _localctx = new FieldAssignContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_fieldAssign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			match(ID);
			setState(271);
			match(T__15);
			setState(272);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_block);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(T__6);
			setState(280);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(275);
					stmt();
					setState(276);
					match(T__46);
					}
					} 
				}
				setState(282);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 135229244404924548L) != 0)) {
				{
				setState(283);
				expr(0);
				}
			}

			setState(286);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StmtContext extends ParserRuleContext {
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
	 
		public StmtContext() { }
		public void copyFrom(StmtContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprStmtContext extends StmtContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprStmt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetStmtContext extends StmtContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public LetStmtContext(StmtContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterLetStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitLetStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitLetStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_stmt);
		int _la;
		try {
			setState(297);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				_localctx = new LetStmtContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(288);
				match(T__37);
				setState(289);
				match(ID);
				setState(292);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(290);
					match(T__17);
					setState(291);
					typeExpr();
					}
				}

				setState(294);
				match(T__15);
				setState(295);
				expr(0);
				}
				break;
			case 2:
				_localctx = new ExprStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(296);
				expr(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathSpecContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MapFileParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MapFileParser.ID, i);
		}
		public PathSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPathSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPathSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPathSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathSpecContext pathSpec() throws RecognitionException {
		PathSpecContext _localctx = new PathSpecContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_pathSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(T__47);
			setState(300);
			match(ID);
			{
			setState(302);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__30) {
				{
				setState(301);
				match(T__30);
				}
			}

			setState(304);
			match(T__19);
			setState(305);
			match(ID);
			}
			setState(307);
			match(T__48);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrowSpecContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MapFileParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MapFileParser.ID, i);
		}
		public ArrowSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrowSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterArrowSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitArrowSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitArrowSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrowSpecContext arrowSpec() throws RecognitionException {
		ArrowSpecContext _localctx = new ArrowSpecContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_arrowSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(309);
			match(T__47);
			setState(310);
			match(ID);
			setState(311);
			match(T__19);
			setState(312);
			match(ID);
			setState(313);
			match(T__48);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrowHeadingContext extends ParserRuleContext {
		public ArrowHeadingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrowHeading; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterArrowHeading(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitArrowHeading(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitArrowHeading(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrowHeadingContext arrowHeading() throws RecognitionException {
		ArrowHeadingContext _localctx = new ArrowHeadingContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_arrowHeading);
		try {
			setState(318);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__49:
				enterOuterAlt(_localctx, 1);
				{
				setState(315);
				match(T__49);
				}
				break;
			case T__41:
				enterOuterAlt(_localctx, 2);
				{
				setState(316);
				match(T__41);
				setState(317);
				match(T__27);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RequirementsContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MapFileParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MapFileParser.ID, i);
		}
		public RequirementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_requirements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRequirements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRequirements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRequirements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RequirementsContext requirements() throws RecognitionException {
		RequirementsContext _localctx = new RequirementsContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_requirements);
		int _la;
		try {
			setState(335);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				match(T__50);
				setState(321);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(322);
				match(T__50);
				setState(323);
				match(T__30);
				setState(332);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(324);
					match(ID);
					setState(329);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__51) {
						{
						{
						setState(325);
						match(T__51);
						setState(326);
						match(ID);
						}
						}
						setState(331);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(334);
				match(T__31);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordAssignContext extends ParserRuleContext {
		public List<FieldAssignContext> fieldAssign() {
			return getRuleContexts(FieldAssignContext.class);
		}
		public FieldAssignContext fieldAssign(int i) {
			return getRuleContext(FieldAssignContext.class,i);
		}
		public RecordAssignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordAssign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRecordAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRecordAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRecordAssign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordAssignContext recordAssign() throws RecognitionException {
		RecordAssignContext _localctx = new RecordAssignContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_recordAssign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			match(T__6);
			setState(346);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(338);
				fieldAssign();
				setState(343);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__18) {
					{
					{
					setState(339);
					match(T__18);
					setState(340);
					fieldAssign();
					}
					}
					setState(345);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(348);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 10:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 8);
		case 1:
			return precpred(_ctx, 7);
		case 2:
			return precpred(_ctx, 6);
		case 3:
			return precpred(_ctx, 12);
		case 4:
			return precpred(_ctx, 11);
		case 5:
			return precpred(_ctx, 10);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001<\u015f\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u0000-\b\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u00005\b\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u0000?\b\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0005\u0001E\b\u0001\n\u0001\f\u0001H\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002`\b\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003j\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"o\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003t\b\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0005\u0004y\b\u0004\n\u0004\f\u0004|\t"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0003\u0006\u0085\b\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0091\b\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0005\b\u0097\b\b\n\b\f\b\u009a\t\b\u0003\b\u009c\b\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0003\n\u00b3\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00bf\b\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00d4"+
		"\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0005\n\u00e9\b\n\n\n\f\n\u00ec\t\n\u0003\n\u00ee\b\n\u0001"+
		"\n\u0005\n\u00f1\b\n\n\n\f\n\u00f4\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0005\u000b\u0100\b\u000b\n\u000b\f\u000b\u0103\t\u000b\u0003"+
		"\u000b\u0105\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0003\u000b\u010d\b\u000b\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u0117\b\r\n\r\f\r\u011a\t\r"+
		"\u0001\r\u0003\r\u011d\b\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0003\u000e\u0125\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u012a\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u012f\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u013f\b\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0005\u0012\u0148\b\u0012\n\u0012\f\u0012\u014b\t\u0012\u0003\u0012"+
		"\u014d\b\u0012\u0001\u0012\u0003\u0012\u0150\b\u0012\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0156\b\u0013\n\u0013\f\u0013"+
		"\u0159\t\u0013\u0003\u0013\u015b\b\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0000\u0001\u0014\u0014\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&\u0000\u0005\u0001\u0000"+
		"*+\u0002\u0000\u0019\u0019,,\u0001\u0000\u001b\u001c\u0002\u0000\u001a"+
		"\u001a\u001d\u001d\u0001\u0000\u001e\"\u0187\u0000>\u0001\u0000\u0000"+
		"\u0000\u0002@\u0001\u0000\u0000\u0000\u0004_\u0001\u0000\u0000\u0000\u0006"+
		"s\u0001\u0000\u0000\u0000\bu\u0001\u0000\u0000\u0000\n}\u0001\u0000\u0000"+
		"\u0000\f\u0081\u0001\u0000\u0000\u0000\u000e\u0090\u0001\u0000\u0000\u0000"+
		"\u0010\u0092\u0001\u0000\u0000\u0000\u0012\u009f\u0001\u0000\u0000\u0000"+
		"\u0014\u00d3\u0001\u0000\u0000\u0000\u0016\u010c\u0001\u0000\u0000\u0000"+
		"\u0018\u010e\u0001\u0000\u0000\u0000\u001a\u0112\u0001\u0000\u0000\u0000"+
		"\u001c\u0129\u0001\u0000\u0000\u0000\u001e\u012b\u0001\u0000\u0000\u0000"+
		" \u0135\u0001\u0000\u0000\u0000\"\u013e\u0001\u0000\u0000\u0000$\u014f"+
		"\u0001\u0000\u0000\u0000&\u0151\u0001\u0000\u0000\u0000()\u0005\u0001"+
		"\u0000\u0000)*\u00055\u0000\u0000*,\u0005\u0002\u0000\u0000+-\u0003\b"+
		"\u0004\u0000,+\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-.\u0001"+
		"\u0000\u0000\u0000./\u0005\u0003\u0000\u0000/?\u0003\u0002\u0001\u0000"+
		"01\u0005\u0004\u0000\u000012\u00055\u0000\u000024\u0005\u0002\u0000\u0000"+
		"35\u0003\b\u0004\u000043\u0001\u0000\u0000\u000045\u0001\u0000\u0000\u0000"+
		"56\u0001\u0000\u0000\u000067\u0005\u0003\u0000\u00007?\u0003\u0002\u0001"+
		"\u000089\u0005\u0005\u0000\u00009:\u00055\u0000\u0000:;\u0005\u0006\u0000"+
		"\u0000;<\u0003\u0014\n\u0000<=\u0003\u0002\u0001\u0000=?\u0001\u0000\u0000"+
		"\u0000>(\u0001\u0000\u0000\u0000>0\u0001\u0000\u0000\u0000>8\u0001\u0000"+
		"\u0000\u0000?\u0001\u0001\u0000\u0000\u0000@F\u0005\u0007\u0000\u0000"+
		"AB\u0003\u0004\u0002\u0000BC\u00059\u0000\u0000CE\u0001\u0000\u0000\u0000"+
		"DA\u0001\u0000\u0000\u0000EH\u0001\u0000\u0000\u0000FD\u0001\u0000\u0000"+
		"\u0000FG\u0001\u0000\u0000\u0000GI\u0001\u0000\u0000\u0000HF\u0001\u0000"+
		"\u0000\u0000IJ\u0003\u0004\u0002\u0000JK\u0005\b\u0000\u0000K\u0003\u0001"+
		"\u0000\u0000\u0000L`\u0003\u0006\u0003\u0000MN\u0005\t\u0000\u0000NO\u0005"+
		"5\u0000\u0000O`\u0003&\u0013\u0000PQ\u0005\n\u0000\u0000QR\u0003\u001e"+
		"\u000f\u0000RS\u0003&\u0013\u0000ST\u0003$\u0012\u0000T`\u0001\u0000\u0000"+
		"\u0000UV\u0005\u000b\u0000\u0000VW\u0003 \u0010\u0000WX\u0003\"\u0011"+
		"\u0000XY\u0005\f\u0000\u0000YZ\u0003\u0014\n\u0000Z`\u0001\u0000\u0000"+
		"\u0000[\\\u0005\r\u0000\u0000\\`\u00055\u0000\u0000]^\u0005\u000e\u0000"+
		"\u0000^`\u00055\u0000\u0000_L\u0001\u0000\u0000\u0000_M\u0001\u0000\u0000"+
		"\u0000_P\u0001\u0000\u0000\u0000_U\u0001\u0000\u0000\u0000_[\u0001\u0000"+
		"\u0000\u0000_]\u0001\u0000\u0000\u0000`\u0005\u0001\u0000\u0000\u0000"+
		"ab\u0005\u000f\u0000\u0000bc\u00055\u0000\u0000cd\u0005\u0010\u0000\u0000"+
		"dt\u0003\f\u0006\u0000ef\u0005\u0011\u0000\u0000fg\u00055\u0000\u0000"+
		"gi\u0005\u0002\u0000\u0000hj\u0003\b\u0004\u0000ih\u0001\u0000\u0000\u0000"+
		"ij\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000kn\u0005\u0003\u0000"+
		"\u0000lm\u0005\u0012\u0000\u0000mo\u0003\f\u0006\u0000nl\u0001\u0000\u0000"+
		"\u0000no\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0005\u0010"+
		"\u0000\u0000qt\u0003\u0014\n\u0000rt\u0003\u0014\n\u0000sa\u0001\u0000"+
		"\u0000\u0000se\u0001\u0000\u0000\u0000sr\u0001\u0000\u0000\u0000t\u0007"+
		"\u0001\u0000\u0000\u0000uz\u0003\n\u0005\u0000vw\u0005\u0013\u0000\u0000"+
		"wy\u0003\n\u0005\u0000xv\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000\u0000"+
		"zx\u0001\u0000\u0000\u0000z{\u0001\u0000\u0000\u0000{\t\u0001\u0000\u0000"+
		"\u0000|z\u0001\u0000\u0000\u0000}~\u00055\u0000\u0000~\u007f\u0005\u0012"+
		"\u0000\u0000\u007f\u0080\u0003\f\u0006\u0000\u0080\u000b\u0001\u0000\u0000"+
		"\u0000\u0081\u0084\u0003\u000e\u0007\u0000\u0082\u0083\u0005\u0014\u0000"+
		"\u0000\u0083\u0085\u0003\f\u0006\u0000\u0084\u0082\u0001\u0000\u0000\u0000"+
		"\u0084\u0085\u0001\u0000\u0000\u0000\u0085\r\u0001\u0000\u0000\u0000\u0086"+
		"\u0091\u0005\u0015\u0000\u0000\u0087\u0091\u0005\u0016\u0000\u0000\u0088"+
		"\u0091\u0005\u0017\u0000\u0000\u0089\u0091\u0005\u0018\u0000\u0000\u008a"+
		"\u0091\u00055\u0000\u0000\u008b\u0091\u0003\u0010\b\u0000\u008c\u008d"+
		"\u0005\u0002\u0000\u0000\u008d\u008e\u0003\f\u0006\u0000\u008e\u008f\u0005"+
		"\u0003\u0000\u0000\u008f\u0091\u0001\u0000\u0000\u0000\u0090\u0086\u0001"+
		"\u0000\u0000\u0000\u0090\u0087\u0001\u0000\u0000\u0000\u0090\u0088\u0001"+
		"\u0000\u0000\u0000\u0090\u0089\u0001\u0000\u0000\u0000\u0090\u008a\u0001"+
		"\u0000\u0000\u0000\u0090\u008b\u0001\u0000\u0000\u0000\u0090\u008c\u0001"+
		"\u0000\u0000\u0000\u0091\u000f\u0001\u0000\u0000\u0000\u0092\u009b\u0005"+
		"\u0007\u0000\u0000\u0093\u0098\u0003\u0012\t\u0000\u0094\u0095\u0005\u0013"+
		"\u0000\u0000\u0095\u0097\u0003\u0012\t\u0000\u0096\u0094\u0001\u0000\u0000"+
		"\u0000\u0097\u009a\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000"+
		"\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u009c\u0001\u0000\u0000"+
		"\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009b\u0093\u0001\u0000\u0000"+
		"\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0005\b\u0000\u0000\u009e\u0011\u0001\u0000\u0000\u0000"+
		"\u009f\u00a0\u00055\u0000\u0000\u00a0\u00a1\u0005\u0012\u0000\u0000\u00a1"+
		"\u00a2\u0003\f\u0006\u0000\u00a2\u0013\u0001\u0000\u0000\u0000\u00a3\u00a4"+
		"\u0006\n\uffff\uffff\u0000\u00a4\u00d4\u0003\u0016\u000b\u0000\u00a5\u00a6"+
		"\u0005\u001a\u0000\u0000\u00a6\u00d4\u0003\u0014\n\t\u00a7\u00a8\u0005"+
		"#\u0000\u0000\u00a8\u00a9\u0003\u0014\n\u0000\u00a9\u00aa\u0005$\u0000"+
		"\u0000\u00aa\u00ab\u0003\u0014\n\u0000\u00ab\u00ac\u0005%\u0000\u0000"+
		"\u00ac\u00ad\u0003\u0014\n\u0005\u00ad\u00d4\u0001\u0000\u0000\u0000\u00ae"+
		"\u00af\u0005&\u0000\u0000\u00af\u00b2\u00055\u0000\u0000\u00b0\u00b1\u0005"+
		"\u0012\u0000\u0000\u00b1\u00b3\u0003\f\u0006\u0000\u00b2\u00b0\u0001\u0000"+
		"\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b4\u00b5\u0005\u0010\u0000\u0000\u00b5\u00b6\u0003\u0014"+
		"\n\u0000\u00b6\u00b7\u0005\'\u0000\u0000\u00b7\u00b8\u0003\u0014\n\u0004"+
		"\u00b8\u00d4\u0001\u0000\u0000\u0000\u00b9\u00ba\u0005&\u0000\u0000\u00ba"+
		"\u00bb\u0005(\u0000\u0000\u00bb\u00be\u00055\u0000\u0000\u00bc\u00bd\u0005"+
		"\u0012\u0000\u0000\u00bd\u00bf\u0003\f\u0006\u0000\u00be\u00bc\u0001\u0000"+
		"\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000"+
		"\u0000\u0000\u00c0\u00c1\u0005\u0010\u0000\u0000\u00c1\u00c2\u0003\u0014"+
		"\n\u0000\u00c2\u00c3\u0005\'\u0000\u0000\u00c3\u00c4\u0003\u0014\n\u0003"+
		"\u00c4\u00d4\u0001\u0000\u0000\u0000\u00c5\u00c6\u0005)\u0000\u0000\u00c6"+
		"\u00c7\u00055\u0000\u0000\u00c7\u00c8\u0005\u0012\u0000\u0000\u00c8\u00c9"+
		"\u0003\f\u0006\u0000\u00c9\u00ca\u0005\u0019\u0000\u0000\u00ca\u00cb\u0003"+
		"\u0014\n\u0002\u00cb\u00d4\u0001\u0000\u0000\u0000\u00cc\u00cd\u0007\u0000"+
		"\u0000\u0000\u00cd\u00ce\u00055\u0000\u0000\u00ce\u00cf\u0005\u0012\u0000"+
		"\u0000\u00cf\u00d0\u0003\f\u0006\u0000\u00d0\u00d1\u0007\u0001\u0000\u0000"+
		"\u00d1\u00d2\u0003\u0014\n\u0001\u00d2\u00d4\u0001\u0000\u0000\u0000\u00d3"+
		"\u00a3\u0001\u0000\u0000\u0000\u00d3\u00a5\u0001\u0000\u0000\u0000\u00d3"+
		"\u00a7\u0001\u0000\u0000\u0000\u00d3\u00ae\u0001\u0000\u0000\u0000\u00d3"+
		"\u00b9\u0001\u0000\u0000\u0000\u00d3\u00c5\u0001\u0000\u0000\u0000\u00d3"+
		"\u00cc\u0001\u0000\u0000\u0000\u00d4\u00f2\u0001\u0000\u0000\u0000\u00d5"+
		"\u00d6\n\b\u0000\u0000\u00d6\u00d7\u0007\u0002\u0000\u0000\u00d7\u00f1"+
		"\u0003\u0014\n\t\u00d8\u00d9\n\u0007\u0000\u0000\u00d9\u00da\u0007\u0003"+
		"\u0000\u0000\u00da\u00f1\u0003\u0014\n\b\u00db\u00dc\n\u0006\u0000\u0000"+
		"\u00dc\u00dd\u0007\u0004\u0000\u0000\u00dd\u00f1\u0003\u0014\n\u0007\u00de"+
		"\u00df\n\f\u0000\u0000\u00df\u00e0\u0005\u0019\u0000\u0000\u00e0\u00f1"+
		"\u00055\u0000\u0000\u00e1\u00e2\n\u000b\u0000\u0000\u00e2\u00f1\u0003"+
		"\u0016\u000b\u0000\u00e3\u00e4\n\n\u0000\u0000\u00e4\u00ed\u0005\u0002"+
		"\u0000\u0000\u00e5\u00ea\u0003\u0014\n\u0000\u00e6\u00e7\u0005\u0013\u0000"+
		"\u0000\u00e7\u00e9\u0003\u0014\n\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000"+
		"\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000"+
		"\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb\u00ee\u0001\u0000\u0000\u0000"+
		"\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ed\u00e5\u0001\u0000\u0000\u0000"+
		"\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000"+
		"\u00ef\u00f1\u0005\u0003\u0000\u0000\u00f0\u00d5\u0001\u0000\u0000\u0000"+
		"\u00f0\u00d8\u0001\u0000\u0000\u0000\u00f0\u00db\u0001\u0000\u0000\u0000"+
		"\u00f0\u00de\u0001\u0000\u0000\u0000\u00f0\u00e1\u0001\u0000\u0000\u0000"+
		"\u00f0\u00e3\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000\u0000"+
		"\u00f2\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f3\u0015\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000"+
		"\u00f5\u010d\u00057\u0000\u0000\u00f6\u010d\u00056\u0000\u0000\u00f7\u010d"+
		"\u00058\u0000\u0000\u00f8\u010d\u0005-\u0000\u0000\u00f9\u010d\u0005."+
		"\u0000\u0000\u00fa\u010d\u00055\u0000\u0000\u00fb\u0104\u0005\u0007\u0000"+
		"\u0000\u00fc\u0101\u0003\u0018\f\u0000\u00fd\u00fe\u0005\u0013\u0000\u0000"+
		"\u00fe\u0100\u0003\u0018\f\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u0100"+
		"\u0103\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0101"+
		"\u0102\u0001\u0000\u0000\u0000\u0102\u0105\u0001\u0000\u0000\u0000\u0103"+
		"\u0101\u0001\u0000\u0000\u0000\u0104\u00fc\u0001\u0000\u0000\u0000\u0104"+
		"\u0105\u0001\u0000\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000\u0106"+
		"\u010d\u0005\b\u0000\u0000\u0107\u010d\u0003\u001a\r\u0000\u0108\u0109"+
		"\u0005\u0002\u0000\u0000\u0109\u010a\u0003\u0014\n\u0000\u010a\u010b\u0005"+
		"\u0003\u0000\u0000\u010b\u010d\u0001\u0000\u0000\u0000\u010c\u00f5\u0001"+
		"\u0000\u0000\u0000\u010c\u00f6\u0001\u0000\u0000\u0000\u010c\u00f7\u0001"+
		"\u0000\u0000\u0000\u010c\u00f8\u0001\u0000\u0000\u0000\u010c\u00f9\u0001"+
		"\u0000\u0000\u0000\u010c\u00fa\u0001\u0000\u0000\u0000\u010c\u00fb\u0001"+
		"\u0000\u0000\u0000\u010c\u0107\u0001\u0000\u0000\u0000\u010c\u0108\u0001"+
		"\u0000\u0000\u0000\u010d\u0017\u0001\u0000\u0000\u0000\u010e\u010f\u0005"+
		"5\u0000\u0000\u010f\u0110\u0005\u0010\u0000\u0000\u0110\u0111\u0003\u0014"+
		"\n\u0000\u0111\u0019\u0001\u0000\u0000\u0000\u0112\u0118\u0005\u0007\u0000"+
		"\u0000\u0113\u0114\u0003\u001c\u000e\u0000\u0114\u0115\u0005/\u0000\u0000"+
		"\u0115\u0117\u0001\u0000\u0000\u0000\u0116\u0113\u0001\u0000\u0000\u0000"+
		"\u0117\u011a\u0001\u0000\u0000\u0000\u0118\u0116\u0001\u0000\u0000\u0000"+
		"\u0118\u0119\u0001\u0000\u0000\u0000\u0119\u011c\u0001\u0000\u0000\u0000"+
		"\u011a\u0118\u0001\u0000\u0000\u0000\u011b\u011d\u0003\u0014\n\u0000\u011c"+
		"\u011b\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d"+
		"\u011e\u0001\u0000\u0000\u0000\u011e\u011f\u0005\b\u0000\u0000\u011f\u001b"+
		"\u0001\u0000\u0000\u0000\u0120\u0121\u0005&\u0000\u0000\u0121\u0124\u0005"+
		"5\u0000\u0000\u0122\u0123\u0005\u0012\u0000\u0000\u0123\u0125\u0003\f"+
		"\u0006\u0000\u0124\u0122\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000"+
		"\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0127\u0005\u0010"+
		"\u0000\u0000\u0127\u012a\u0003\u0014\n\u0000\u0128\u012a\u0003\u0014\n"+
		"\u0000\u0129\u0120\u0001\u0000\u0000\u0000\u0129\u0128\u0001\u0000\u0000"+
		"\u0000\u012a\u001d\u0001\u0000\u0000\u0000\u012b\u012c\u00050\u0000\u0000"+
		"\u012c\u012e\u00055\u0000\u0000\u012d\u012f\u0005\u001f\u0000\u0000\u012e"+
		"\u012d\u0001\u0000\u0000\u0000\u012e\u012f\u0001\u0000\u0000\u0000\u012f"+
		"\u0130\u0001\u0000\u0000\u0000\u0130\u0131\u0005\u0014\u0000\u0000\u0131"+
		"\u0132\u00055\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u0134"+
		"\u00051\u0000\u0000\u0134\u001f\u0001\u0000\u0000\u0000\u0135\u0136\u0005"+
		"0\u0000\u0000\u0136\u0137\u00055\u0000\u0000\u0137\u0138\u0005\u0014\u0000"+
		"\u0000\u0138\u0139\u00055\u0000\u0000\u0139\u013a\u00051\u0000\u0000\u013a"+
		"!\u0001\u0000\u0000\u0000\u013b\u013f\u00052\u0000\u0000\u013c\u013d\u0005"+
		"*\u0000\u0000\u013d\u013f\u0005\u001c\u0000\u0000\u013e\u013b\u0001\u0000"+
		"\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013f#\u0001\u0000\u0000"+
		"\u0000\u0140\u0141\u00053\u0000\u0000\u0141\u0150\u00055\u0000\u0000\u0142"+
		"\u0143\u00053\u0000\u0000\u0143\u014c\u0005\u001f\u0000\u0000\u0144\u0149"+
		"\u00055\u0000\u0000\u0145\u0146\u00054\u0000\u0000\u0146\u0148\u00055"+
		"\u0000\u0000\u0147\u0145\u0001\u0000\u0000\u0000\u0148\u014b\u0001\u0000"+
		"\u0000\u0000\u0149\u0147\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000"+
		"\u0000\u0000\u014a\u014d\u0001\u0000\u0000\u0000\u014b\u0149\u0001\u0000"+
		"\u0000\u0000\u014c\u0144\u0001\u0000\u0000\u0000\u014c\u014d\u0001\u0000"+
		"\u0000\u0000\u014d\u014e\u0001\u0000\u0000\u0000\u014e\u0150\u0005 \u0000"+
		"\u0000\u014f\u0140\u0001\u0000\u0000\u0000\u014f\u0142\u0001\u0000\u0000"+
		"\u0000\u0150%\u0001\u0000\u0000\u0000\u0151\u015a\u0005\u0007\u0000\u0000"+
		"\u0152\u0157\u0003\u0018\f\u0000\u0153\u0154\u0005\u0013\u0000\u0000\u0154"+
		"\u0156\u0003\u0018\f\u0000\u0155\u0153\u0001\u0000\u0000\u0000\u0156\u0159"+
		"\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0157\u0158"+
		"\u0001\u0000\u0000\u0000\u0158\u015b\u0001\u0000\u0000\u0000\u0159\u0157"+
		"\u0001\u0000\u0000\u0000\u015a\u0152\u0001\u0000\u0000\u0000\u015a\u015b"+
		"\u0001\u0000\u0000\u0000\u015b\u015c\u0001\u0000\u0000\u0000\u015c\u015d"+
		"\u0005\b\u0000\u0000\u015d\'\u0001\u0000\u0000\u0000\",4>F_insz\u0084"+
		"\u0090\u0098\u009b\u00b2\u00be\u00d3\u00ea\u00ed\u00f0\u00f2\u0101\u0104"+
		"\u010c\u0118\u011c\u0124\u0129\u012e\u013e\u0149\u014c\u014f\u0157\u015a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}