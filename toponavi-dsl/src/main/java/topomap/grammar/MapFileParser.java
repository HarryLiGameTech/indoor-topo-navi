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
		ID=53, FLOAT=54, INT=55, STRING=56, WS=57, COMMENT=58, BLOCK_COMMENT=59, 
		NL=60;
	public static final int
		RULE_surfaceDef = 0, RULE_surfaceBody = 1, RULE_surfaceBodyElement = 2, 
		RULE_coreDef = 3, RULE_paramList = 4, RULE_param = 5, RULE_typeExpr = 6, 
		RULE_typeAtom = 7, RULE_recordType = 8, RULE_fieldDecl = 9, RULE_expr = 10, 
		RULE_atom = 11, RULE_fieldAssign = 12, RULE_block = 13, RULE_stmt = 14, 
		RULE_pathSpec = 15, RULE_arrowSpec = 16, RULE_arrowHeading = 17, RULE_requirements = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"surfaceDef", "surfaceBody", "surfaceBodyElement", "coreDef", "paramList", 
			"param", "typeExpr", "typeAtom", "recordType", "fieldDecl", "expr", "atom", 
			"fieldAssign", "block", "stmt", "pathSpec", "arrowSpec", "arrowHeading", 
			"requirements"
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
			null, null, null, null, null, "ID", "FLOAT", "INT", "STRING", "WS", "COMMENT", 
			"BLOCK_COMMENT", "NL"
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
			setState(60);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				_localctx = new RootExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(38);
				match(T__0);
				setState(39);
				match(ID);
				setState(40);
				match(T__1);
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(41);
					paramList();
					}
				}

				setState(44);
				match(T__2);
				setState(45);
				surfaceBody();
				}
				break;
			case T__3:
				_localctx = new TopoMapExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				match(T__3);
				setState(47);
				match(ID);
				setState(48);
				match(T__1);
				setState(50);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(49);
					paramList();
					}
				}

				setState(52);
				match(T__2);
				setState(53);
				surfaceBody();
				}
				break;
			case T__4:
				_localctx = new TransportExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(54);
				match(T__4);
				setState(55);
				match(ID);
				setState(56);
				match(T__5);
				setState(57);
				expr(0);
				setState(58);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			match(T__6);
			setState(66); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(63);
				surfaceBodyElement();
				setState(64);
				match(NL);
				}
				}
				setState(68); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 63171650367188612L) != 0) );
			setState(70);
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
	public static class SubmapExprContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SubmapExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSubmapExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSubmapExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSubmapExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TopoNodeExprContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public RecordTypeContext recordType() {
			return getRuleContext(RecordTypeContext.class,0);
		}
		public TopoNodeExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTopoNodeExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTopoNodeExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTopoNodeExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AtomicPathExprContext extends SurfaceBodyElementContext {
		public PathSpecContext pathSpec() {
			return getRuleContext(PathSpecContext.class,0);
		}
		public RecordTypeContext recordType() {
			return getRuleContext(RecordTypeContext.class,0);
		}
		public RequirementsContext requirements() {
			return getRuleContext(RequirementsContext.class,0);
		}
		public AtomicPathExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterAtomicPathExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitAtomicPathExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitAtomicPathExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrowExprContext extends SurfaceBodyElementContext {
		public ArrowSpecContext arrowSpec() {
			return getRuleContext(ArrowSpecContext.class,0);
		}
		public ArrowHeadingContext arrowHeading() {
			return getRuleContext(ArrowHeadingContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ArrowExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterArrowExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitArrowExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitArrowExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VehicleExprContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public VehicleExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterVehicleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitVehicleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitVehicleExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CoreExprContext extends SurfaceBodyElementContext {
		public CoreDefContext coreDef() {
			return getRuleContext(CoreDefContext.class,0);
		}
		public CoreExprContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterCoreExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitCoreExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitCoreExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SurfaceBodyElementContext surfaceBodyElement() throws RecognitionException {
		SurfaceBodyElementContext _localctx = new SurfaceBodyElementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_surfaceBodyElement);
		try {
			setState(91);
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
				_localctx = new CoreExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(72);
				coreDef();
				}
				break;
			case T__8:
				_localctx = new TopoNodeExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(73);
				match(T__8);
				setState(74);
				match(ID);
				setState(75);
				recordType();
				}
				break;
			case T__9:
				_localctx = new AtomicPathExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(76);
				match(T__9);
				setState(77);
				pathSpec();
				setState(78);
				recordType();
				setState(79);
				requirements();
				}
				break;
			case T__10:
				_localctx = new ArrowExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(81);
				match(T__10);
				setState(82);
				arrowSpec();
				setState(83);
				arrowHeading();
				setState(84);
				match(T__11);
				setState(85);
				expr(0);
				}
				break;
			case T__12:
				_localctx = new VehicleExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(87);
				match(T__12);
				setState(88);
				match(ID);
				}
				break;
			case T__13:
				_localctx = new SubmapExprContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(89);
				match(T__13);
				setState(90);
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
			setState(111);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__14:
				_localctx = new TypeDefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				match(T__14);
				setState(94);
				match(ID);
				setState(95);
				match(T__15);
				setState(96);
				typeExpr();
				}
				break;
			case T__16:
				_localctx = new FuncDefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(97);
				match(T__16);
				setState(98);
				match(ID);
				setState(99);
				match(T__1);
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(100);
					paramList();
					}
				}

				setState(103);
				match(T__2);
				setState(106);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(104);
					match(T__17);
					setState(105);
					typeExpr();
					}
				}

				setState(108);
				match(T__15);
				setState(109);
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
				_localctx = new ScriptExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(110);
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
			setState(113);
			param();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__18) {
				{
				{
				setState(114);
				match(T__18);
				setState(115);
				param();
				}
				}
				setState(120);
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
			setState(121);
			match(ID);
			setState(122);
			match(T__17);
			setState(123);
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
			setState(125);
			typeAtom();
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__19) {
				{
				setState(126);
				match(T__19);
				setState(127);
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
			setState(140);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
				enterOuterAlt(_localctx, 1);
				{
				setState(130);
				match(T__20);
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 2);
				{
				setState(131);
				match(T__21);
				}
				break;
			case T__22:
				enterOuterAlt(_localctx, 3);
				{
				setState(132);
				match(T__22);
				}
				break;
			case T__23:
				enterOuterAlt(_localctx, 4);
				{
				setState(133);
				match(T__23);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 5);
				{
				setState(134);
				match(ID);
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 6);
				{
				setState(135);
				recordType();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 7);
				{
				setState(136);
				match(T__1);
				setState(137);
				typeExpr();
				setState(138);
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
			setState(142);
			match(T__6);
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(143);
				fieldDecl();
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__18) {
					{
					{
					setState(144);
					match(T__18);
					setState(145);
					fieldDecl();
					}
					}
					setState(150);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(153);
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
			setState(155);
			match(ID);
			setState(156);
			match(T__17);
			setState(157);
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
			setState(207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(160);
				atom();
				}
				break;
			case 2:
				{
				_localctx = new NegExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(161);
				match(T__25);
				setState(162);
				expr(9);
				}
				break;
			case 3:
				{
				_localctx = new IfExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(163);
				match(T__34);
				setState(164);
				((IfExprContext)_localctx).cond = expr(0);
				setState(165);
				match(T__35);
				setState(166);
				((IfExprContext)_localctx).ifExpr = expr(0);
				setState(167);
				match(T__36);
				setState(168);
				((IfExprContext)_localctx).elseExpr = expr(5);
				}
				break;
			case 4:
				{
				_localctx = new LetExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(170);
				match(T__37);
				setState(171);
				match(ID);
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(172);
					match(T__17);
					setState(173);
					((LetExprContext)_localctx).letType = typeExpr();
					}
				}

				setState(176);
				match(T__15);
				setState(177);
				((LetExprContext)_localctx).assignValue = expr(0);
				setState(178);
				match(T__38);
				setState(179);
				expr(4);
				}
				break;
			case 5:
				{
				_localctx = new LetRecExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(181);
				match(T__37);
				setState(182);
				match(T__39);
				setState(183);
				match(ID);
				setState(186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(184);
					match(T__17);
					setState(185);
					typeExpr();
					}
				}

				setState(188);
				match(T__15);
				setState(189);
				expr(0);
				setState(190);
				match(T__38);
				setState(191);
				expr(3);
				}
				break;
			case 6:
				{
				_localctx = new FixExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(193);
				match(T__40);
				setState(194);
				match(ID);
				setState(195);
				match(T__17);
				setState(196);
				typeExpr();
				setState(197);
				match(T__24);
				setState(198);
				expr(2);
				}
				break;
			case 7:
				{
				_localctx = new LamExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(200);
				_la = _input.LA(1);
				if ( !(_la==T__41 || _la==T__42) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(201);
				match(ID);
				setState(202);
				match(T__17);
				setState(203);
				typeExpr();
				setState(204);
				_la = _input.LA(1);
				if ( !(_la==T__24 || _la==T__43) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(205);
				expr(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(238);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(236);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
					case 1:
						{
						_localctx = new MulDivExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(209);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(210);
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
						setState(211);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new AddSubExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(212);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(213);
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
						setState(214);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(215);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(216);
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
						setState(217);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new ProjExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(218);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(219);
						match(T__24);
						setState(220);
						match(ID);
						}
						break;
					case 5:
						{
						_localctx = new AppMlExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(221);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(222);
						atom();
						}
						break;
					case 6:
						{
						_localctx = new AppCExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(223);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(224);
						match(T__1);
						setState(233);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 63171650366996612L) != 0)) {
							{
							setState(225);
							expr(0);
							setState(230);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__18) {
								{
								{
								setState(226);
								match(T__18);
								setState(227);
								expr(0);
								}
								}
								setState(232);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(235);
						match(T__2);
						}
						break;
					}
					} 
				}
				setState(240);
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
			setState(263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(241);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
				match(FLOAT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(243);
				match(T__44);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(244);
				match(T__45);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(245);
				match(ID);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(246);
				match(T__6);
				setState(255);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(247);
					fieldAssign();
					setState(252);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__18) {
						{
						{
						setState(248);
						match(T__18);
						setState(249);
						fieldAssign();
						}
						}
						setState(254);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(257);
				match(T__7);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(258);
				block();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(259);
				match(T__1);
				setState(260);
				expr(0);
				setState(261);
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
			setState(265);
			match(ID);
			setState(266);
			match(T__15);
			setState(267);
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
			setState(269);
			match(T__6);
			setState(275);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(270);
					stmt();
					setState(271);
					match(T__46);
					}
					} 
				}
				setState(277);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 63171650366996612L) != 0)) {
				{
				setState(278);
				expr(0);
				}
			}

			setState(281);
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
			setState(292);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				_localctx = new LetStmtContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(283);
				match(T__37);
				setState(284);
				match(ID);
				setState(287);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__17) {
					{
					setState(285);
					match(T__17);
					setState(286);
					typeExpr();
					}
				}

				setState(289);
				match(T__15);
				setState(290);
				expr(0);
				}
				break;
			case 2:
				_localctx = new ExprStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(291);
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
			setState(294);
			match(T__47);
			setState(295);
			match(ID);
			{
			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__30) {
				{
				setState(296);
				match(T__30);
				}
			}

			setState(299);
			match(T__19);
			setState(300);
			match(ID);
			}
			setState(302);
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
			setState(304);
			match(T__47);
			setState(305);
			match(ID);
			setState(306);
			match(T__19);
			setState(307);
			match(ID);
			setState(308);
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
			setState(313);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__49:
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				match(T__49);
				}
				break;
			case T__41:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				match(T__41);
				setState(312);
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
			setState(330);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(315);
				match(T__50);
				setState(316);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(317);
				match(T__50);
				setState(318);
				match(T__30);
				setState(327);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(319);
					match(ID);
					setState(324);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__51) {
						{
						{
						setState(320);
						match(T__51);
						setState(321);
						match(ID);
						}
						}
						setState(326);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(329);
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
		"\u0004\u0001<\u014d\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000+\b\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u00003\b\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000=\b\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u0001C\b\u0001"+
		"\u000b\u0001\f\u0001D\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002"+
		"\\\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003f\b\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003k\b\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0003\u0003p\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0005\u0004u\b\u0004\n\u0004\f\u0004x\t\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006"+
		"\u0081\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u008d\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0005\b\u0093\b\b\n\b\f"+
		"\b\u0096\t\b\u0003\b\u0098\b\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00af"+
		"\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0003\n\u00bb\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00d0\b\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0005\n\u00e5"+
		"\b\n\n\n\f\n\u00e8\t\n\u0003\n\u00ea\b\n\u0001\n\u0005\n\u00ed\b\n\n\n"+
		"\f\n\u00f0\t\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00fb\b\u000b"+
		"\n\u000b\f\u000b\u00fe\t\u000b\u0003\u000b\u0100\b\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0108"+
		"\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0005\r\u0112\b\r\n\r\f\r\u0115\t\r\u0001\r\u0003\r\u0118\b\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u0120\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0125\b"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u012a\b\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0003\u0011\u013a\b\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0005\u0012\u0143"+
		"\b\u0012\n\u0012\f\u0012\u0146\t\u0012\u0003\u0012\u0148\b\u0012\u0001"+
		"\u0012\u0003\u0012\u014b\b\u0012\u0001\u0012\u0000\u0001\u0014\u0013\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$\u0000\u0005\u0001\u0000*+\u0002\u0000\u0019\u0019,,\u0001\u0000"+
		"\u001b\u001c\u0002\u0000\u001a\u001a\u001d\u001d\u0001\u0000\u001e\"\u0173"+
		"\u0000<\u0001\u0000\u0000\u0000\u0002>\u0001\u0000\u0000\u0000\u0004["+
		"\u0001\u0000\u0000\u0000\u0006o\u0001\u0000\u0000\u0000\bq\u0001\u0000"+
		"\u0000\u0000\ny\u0001\u0000\u0000\u0000\f}\u0001\u0000\u0000\u0000\u000e"+
		"\u008c\u0001\u0000\u0000\u0000\u0010\u008e\u0001\u0000\u0000\u0000\u0012"+
		"\u009b\u0001\u0000\u0000\u0000\u0014\u00cf\u0001\u0000\u0000\u0000\u0016"+
		"\u0107\u0001\u0000\u0000\u0000\u0018\u0109\u0001\u0000\u0000\u0000\u001a"+
		"\u010d\u0001\u0000\u0000\u0000\u001c\u0124\u0001\u0000\u0000\u0000\u001e"+
		"\u0126\u0001\u0000\u0000\u0000 \u0130\u0001\u0000\u0000\u0000\"\u0139"+
		"\u0001\u0000\u0000\u0000$\u014a\u0001\u0000\u0000\u0000&\'\u0005\u0001"+
		"\u0000\u0000\'(\u00055\u0000\u0000(*\u0005\u0002\u0000\u0000)+\u0003\b"+
		"\u0004\u0000*)\u0001\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+,\u0001"+
		"\u0000\u0000\u0000,-\u0005\u0003\u0000\u0000-=\u0003\u0002\u0001\u0000"+
		"./\u0005\u0004\u0000\u0000/0\u00055\u0000\u000002\u0005\u0002\u0000\u0000"+
		"13\u0003\b\u0004\u000021\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u0000"+
		"34\u0001\u0000\u0000\u000045\u0005\u0003\u0000\u00005=\u0003\u0002\u0001"+
		"\u000067\u0005\u0005\u0000\u000078\u00055\u0000\u000089\u0005\u0006\u0000"+
		"\u00009:\u0003\u0014\n\u0000:;\u0003\u0002\u0001\u0000;=\u0001\u0000\u0000"+
		"\u0000<&\u0001\u0000\u0000\u0000<.\u0001\u0000\u0000\u0000<6\u0001\u0000"+
		"\u0000\u0000=\u0001\u0001\u0000\u0000\u0000>B\u0005\u0007\u0000\u0000"+
		"?@\u0003\u0004\u0002\u0000@A\u0005<\u0000\u0000AC\u0001\u0000\u0000\u0000"+
		"B?\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000"+
		"\u0000DE\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000FG\u0005\b\u0000"+
		"\u0000G\u0003\u0001\u0000\u0000\u0000H\\\u0003\u0006\u0003\u0000IJ\u0005"+
		"\t\u0000\u0000JK\u00055\u0000\u0000K\\\u0003\u0010\b\u0000LM\u0005\n\u0000"+
		"\u0000MN\u0003\u001e\u000f\u0000NO\u0003\u0010\b\u0000OP\u0003$\u0012"+
		"\u0000P\\\u0001\u0000\u0000\u0000QR\u0005\u000b\u0000\u0000RS\u0003 \u0010"+
		"\u0000ST\u0003\"\u0011\u0000TU\u0005\f\u0000\u0000UV\u0003\u0014\n\u0000"+
		"V\\\u0001\u0000\u0000\u0000WX\u0005\r\u0000\u0000X\\\u00055\u0000\u0000"+
		"YZ\u0005\u000e\u0000\u0000Z\\\u00055\u0000\u0000[H\u0001\u0000\u0000\u0000"+
		"[I\u0001\u0000\u0000\u0000[L\u0001\u0000\u0000\u0000[Q\u0001\u0000\u0000"+
		"\u0000[W\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000\\\u0005\u0001"+
		"\u0000\u0000\u0000]^\u0005\u000f\u0000\u0000^_\u00055\u0000\u0000_`\u0005"+
		"\u0010\u0000\u0000`p\u0003\f\u0006\u0000ab\u0005\u0011\u0000\u0000bc\u0005"+
		"5\u0000\u0000ce\u0005\u0002\u0000\u0000df\u0003\b\u0004\u0000ed\u0001"+
		"\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000"+
		"gj\u0005\u0003\u0000\u0000hi\u0005\u0012\u0000\u0000ik\u0003\f\u0006\u0000"+
		"jh\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000"+
		"\u0000lm\u0005\u0010\u0000\u0000mp\u0003\u0014\n\u0000np\u0003\u0014\n"+
		"\u0000o]\u0001\u0000\u0000\u0000oa\u0001\u0000\u0000\u0000on\u0001\u0000"+
		"\u0000\u0000p\u0007\u0001\u0000\u0000\u0000qv\u0003\n\u0005\u0000rs\u0005"+
		"\u0013\u0000\u0000su\u0003\n\u0005\u0000tr\u0001\u0000\u0000\u0000ux\u0001"+
		"\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000"+
		"w\t\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000yz\u00055\u0000\u0000"+
		"z{\u0005\u0012\u0000\u0000{|\u0003\f\u0006\u0000|\u000b\u0001\u0000\u0000"+
		"\u0000}\u0080\u0003\u000e\u0007\u0000~\u007f\u0005\u0014\u0000\u0000\u007f"+
		"\u0081\u0003\f\u0006\u0000\u0080~\u0001\u0000\u0000\u0000\u0080\u0081"+
		"\u0001\u0000\u0000\u0000\u0081\r\u0001\u0000\u0000\u0000\u0082\u008d\u0005"+
		"\u0015\u0000\u0000\u0083\u008d\u0005\u0016\u0000\u0000\u0084\u008d\u0005"+
		"\u0017\u0000\u0000\u0085\u008d\u0005\u0018\u0000\u0000\u0086\u008d\u0005"+
		"5\u0000\u0000\u0087\u008d\u0003\u0010\b\u0000\u0088\u0089\u0005\u0002"+
		"\u0000\u0000\u0089\u008a\u0003\f\u0006\u0000\u008a\u008b\u0005\u0003\u0000"+
		"\u0000\u008b\u008d\u0001\u0000\u0000\u0000\u008c\u0082\u0001\u0000\u0000"+
		"\u0000\u008c\u0083\u0001\u0000\u0000\u0000\u008c\u0084\u0001\u0000\u0000"+
		"\u0000\u008c\u0085\u0001\u0000\u0000\u0000\u008c\u0086\u0001\u0000\u0000"+
		"\u0000\u008c\u0087\u0001\u0000\u0000\u0000\u008c\u0088\u0001\u0000\u0000"+
		"\u0000\u008d\u000f\u0001\u0000\u0000\u0000\u008e\u0097\u0005\u0007\u0000"+
		"\u0000\u008f\u0094\u0003\u0012\t\u0000\u0090\u0091\u0005\u0013\u0000\u0000"+
		"\u0091\u0093\u0003\u0012\t\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0093"+
		"\u0096\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0094"+
		"\u0095\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000\u0000\u0000\u0096"+
		"\u0094\u0001\u0000\u0000\u0000\u0097\u008f\u0001\u0000\u0000\u0000\u0097"+
		"\u0098\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099"+
		"\u009a\u0005\b\u0000\u0000\u009a\u0011\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u00055\u0000\u0000\u009c\u009d\u0005\u0012\u0000\u0000\u009d\u009e\u0003"+
		"\f\u0006\u0000\u009e\u0013\u0001\u0000\u0000\u0000\u009f\u00a0\u0006\n"+
		"\uffff\uffff\u0000\u00a0\u00d0\u0003\u0016\u000b\u0000\u00a1\u00a2\u0005"+
		"\u001a\u0000\u0000\u00a2\u00d0\u0003\u0014\n\t\u00a3\u00a4\u0005#\u0000"+
		"\u0000\u00a4\u00a5\u0003\u0014\n\u0000\u00a5\u00a6\u0005$\u0000\u0000"+
		"\u00a6\u00a7\u0003\u0014\n\u0000\u00a7\u00a8\u0005%\u0000\u0000\u00a8"+
		"\u00a9\u0003\u0014\n\u0005\u00a9\u00d0\u0001\u0000\u0000\u0000\u00aa\u00ab"+
		"\u0005&\u0000\u0000\u00ab\u00ae\u00055\u0000\u0000\u00ac\u00ad\u0005\u0012"+
		"\u0000\u0000\u00ad\u00af\u0003\f\u0006\u0000\u00ae\u00ac\u0001\u0000\u0000"+
		"\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b1\u0005\u0010\u0000\u0000\u00b1\u00b2\u0003\u0014\n\u0000"+
		"\u00b2\u00b3\u0005\'\u0000\u0000\u00b3\u00b4\u0003\u0014\n\u0004\u00b4"+
		"\u00d0\u0001\u0000\u0000\u0000\u00b5\u00b6\u0005&\u0000\u0000\u00b6\u00b7"+
		"\u0005(\u0000\u0000\u00b7\u00ba\u00055\u0000\u0000\u00b8\u00b9\u0005\u0012"+
		"\u0000\u0000\u00b9\u00bb\u0003\f\u0006\u0000\u00ba\u00b8\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000"+
		"\u0000\u00bc\u00bd\u0005\u0010\u0000\u0000\u00bd\u00be\u0003\u0014\n\u0000"+
		"\u00be\u00bf\u0005\'\u0000\u0000\u00bf\u00c0\u0003\u0014\n\u0003\u00c0"+
		"\u00d0\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005)\u0000\u0000\u00c2\u00c3"+
		"\u00055\u0000\u0000\u00c3\u00c4\u0005\u0012\u0000\u0000\u00c4\u00c5\u0003"+
		"\f\u0006\u0000\u00c5\u00c6\u0005\u0019\u0000\u0000\u00c6\u00c7\u0003\u0014"+
		"\n\u0002\u00c7\u00d0\u0001\u0000\u0000\u0000\u00c8\u00c9\u0007\u0000\u0000"+
		"\u0000\u00c9\u00ca\u00055\u0000\u0000\u00ca\u00cb\u0005\u0012\u0000\u0000"+
		"\u00cb\u00cc\u0003\f\u0006\u0000\u00cc\u00cd\u0007\u0001\u0000\u0000\u00cd"+
		"\u00ce\u0003\u0014\n\u0001\u00ce\u00d0\u0001\u0000\u0000\u0000\u00cf\u009f"+
		"\u0001\u0000\u0000\u0000\u00cf\u00a1\u0001\u0000\u0000\u0000\u00cf\u00a3"+
		"\u0001\u0000\u0000\u0000\u00cf\u00aa\u0001\u0000\u0000\u0000\u00cf\u00b5"+
		"\u0001\u0000\u0000\u0000\u00cf\u00c1\u0001\u0000\u0000\u0000\u00cf\u00c8"+
		"\u0001\u0000\u0000\u0000\u00d0\u00ee\u0001\u0000\u0000\u0000\u00d1\u00d2"+
		"\n\b\u0000\u0000\u00d2\u00d3\u0007\u0002\u0000\u0000\u00d3\u00ed\u0003"+
		"\u0014\n\t\u00d4\u00d5\n\u0007\u0000\u0000\u00d5\u00d6\u0007\u0003\u0000"+
		"\u0000\u00d6\u00ed\u0003\u0014\n\b\u00d7\u00d8\n\u0006\u0000\u0000\u00d8"+
		"\u00d9\u0007\u0004\u0000\u0000\u00d9\u00ed\u0003\u0014\n\u0007\u00da\u00db"+
		"\n\f\u0000\u0000\u00db\u00dc\u0005\u0019\u0000\u0000\u00dc\u00ed\u0005"+
		"5\u0000\u0000\u00dd\u00de\n\u000b\u0000\u0000\u00de\u00ed\u0003\u0016"+
		"\u000b\u0000\u00df\u00e0\n\n\u0000\u0000\u00e0\u00e9\u0005\u0002\u0000"+
		"\u0000\u00e1\u00e6\u0003\u0014\n\u0000\u00e2\u00e3\u0005\u0013\u0000\u0000"+
		"\u00e3\u00e5\u0003\u0014\n\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e8\u0001\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e7\u0001\u0000\u0000\u0000\u00e7\u00ea\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e6\u0001\u0000\u0000\u0000\u00e9\u00e1\u0001\u0000\u0000\u0000\u00e9"+
		"\u00ea\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb"+
		"\u00ed\u0005\u0003\u0000\u0000\u00ec\u00d1\u0001\u0000\u0000\u0000\u00ec"+
		"\u00d4\u0001\u0000\u0000\u0000\u00ec\u00d7\u0001\u0000\u0000\u0000\u00ec"+
		"\u00da\u0001\u0000\u0000\u0000\u00ec\u00dd\u0001\u0000\u0000\u0000\u00ec"+
		"\u00df\u0001\u0000\u0000\u0000\u00ed\u00f0\u0001\u0000\u0000\u0000\u00ee"+
		"\u00ec\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef"+
		"\u0015\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f1"+
		"\u0108\u00057\u0000\u0000\u00f2\u0108\u00056\u0000\u0000\u00f3\u0108\u0005"+
		"-\u0000\u0000\u00f4\u0108\u0005.\u0000\u0000\u00f5\u0108\u00055\u0000"+
		"\u0000\u00f6\u00ff\u0005\u0007\u0000\u0000\u00f7\u00fc\u0003\u0018\f\u0000"+
		"\u00f8\u00f9\u0005\u0013\u0000\u0000\u00f9\u00fb\u0003\u0018\f\u0000\u00fa"+
		"\u00f8\u0001\u0000\u0000\u0000\u00fb\u00fe\u0001\u0000\u0000\u0000\u00fc"+
		"\u00fa\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd"+
		"\u0100\u0001\u0000\u0000\u0000\u00fe\u00fc\u0001\u0000\u0000\u0000\u00ff"+
		"\u00f7\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100"+
		"\u0101\u0001\u0000\u0000\u0000\u0101\u0108\u0005\b\u0000\u0000\u0102\u0108"+
		"\u0003\u001a\r\u0000\u0103\u0104\u0005\u0002\u0000\u0000\u0104\u0105\u0003"+
		"\u0014\n\u0000\u0105\u0106\u0005\u0003\u0000\u0000\u0106\u0108\u0001\u0000"+
		"\u0000\u0000\u0107\u00f1\u0001\u0000\u0000\u0000\u0107\u00f2\u0001\u0000"+
		"\u0000\u0000\u0107\u00f3\u0001\u0000\u0000\u0000\u0107\u00f4\u0001\u0000"+
		"\u0000\u0000\u0107\u00f5\u0001\u0000\u0000\u0000\u0107\u00f6\u0001\u0000"+
		"\u0000\u0000\u0107\u0102\u0001\u0000\u0000\u0000\u0107\u0103\u0001\u0000"+
		"\u0000\u0000\u0108\u0017\u0001\u0000\u0000\u0000\u0109\u010a\u00055\u0000"+
		"\u0000\u010a\u010b\u0005\u0010\u0000\u0000\u010b\u010c\u0003\u0014\n\u0000"+
		"\u010c\u0019\u0001\u0000\u0000\u0000\u010d\u0113\u0005\u0007\u0000\u0000"+
		"\u010e\u010f\u0003\u001c\u000e\u0000\u010f\u0110\u0005/\u0000\u0000\u0110"+
		"\u0112\u0001\u0000\u0000\u0000\u0111\u010e\u0001\u0000\u0000\u0000\u0112"+
		"\u0115\u0001\u0000\u0000\u0000\u0113\u0111\u0001\u0000\u0000\u0000\u0113"+
		"\u0114\u0001\u0000\u0000\u0000\u0114\u0117\u0001\u0000\u0000\u0000\u0115"+
		"\u0113\u0001\u0000\u0000\u0000\u0116\u0118\u0003\u0014\n\u0000\u0117\u0116"+
		"\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u0119"+
		"\u0001\u0000\u0000\u0000\u0119\u011a\u0005\b\u0000\u0000\u011a\u001b\u0001"+
		"\u0000\u0000\u0000\u011b\u011c\u0005&\u0000\u0000\u011c\u011f\u00055\u0000"+
		"\u0000\u011d\u011e\u0005\u0012\u0000\u0000\u011e\u0120\u0003\f\u0006\u0000"+
		"\u011f\u011d\u0001\u0000\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000"+
		"\u0120\u0121\u0001\u0000\u0000\u0000\u0121\u0122\u0005\u0010\u0000\u0000"+
		"\u0122\u0125\u0003\u0014\n\u0000\u0123\u0125\u0003\u0014\n\u0000\u0124"+
		"\u011b\u0001\u0000\u0000\u0000\u0124\u0123\u0001\u0000\u0000\u0000\u0125"+
		"\u001d\u0001\u0000\u0000\u0000\u0126\u0127\u00050\u0000\u0000\u0127\u0129"+
		"\u00055\u0000\u0000\u0128\u012a\u0005\u001f\u0000\u0000\u0129\u0128\u0001"+
		"\u0000\u0000\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u012b\u0001"+
		"\u0000\u0000\u0000\u012b\u012c\u0005\u0014\u0000\u0000\u012c\u012d\u0005"+
		"5\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\u012f\u00051\u0000"+
		"\u0000\u012f\u001f\u0001\u0000\u0000\u0000\u0130\u0131\u00050\u0000\u0000"+
		"\u0131\u0132\u00055\u0000\u0000\u0132\u0133\u0005\u0014\u0000\u0000\u0133"+
		"\u0134\u00055\u0000\u0000\u0134\u0135\u00051\u0000\u0000\u0135!\u0001"+
		"\u0000\u0000\u0000\u0136\u013a\u00052\u0000\u0000\u0137\u0138\u0005*\u0000"+
		"\u0000\u0138\u013a\u0005\u001c\u0000\u0000\u0139\u0136\u0001\u0000\u0000"+
		"\u0000\u0139\u0137\u0001\u0000\u0000\u0000\u013a#\u0001\u0000\u0000\u0000"+
		"\u013b\u013c\u00053\u0000\u0000\u013c\u014b\u00055\u0000\u0000\u013d\u013e"+
		"\u00053\u0000\u0000\u013e\u0147\u0005\u001f\u0000\u0000\u013f\u0144\u0005"+
		"5\u0000\u0000\u0140\u0141\u00054\u0000\u0000\u0141\u0143\u00055\u0000"+
		"\u0000\u0142\u0140\u0001\u0000\u0000\u0000\u0143\u0146\u0001\u0000\u0000"+
		"\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0144\u0145\u0001\u0000\u0000"+
		"\u0000\u0145\u0148\u0001\u0000\u0000\u0000\u0146\u0144\u0001\u0000\u0000"+
		"\u0000\u0147\u013f\u0001\u0000\u0000\u0000\u0147\u0148\u0001\u0000\u0000"+
		"\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u014b\u0005 \u0000\u0000"+
		"\u014a\u013b\u0001\u0000\u0000\u0000\u014a\u013d\u0001\u0000\u0000\u0000"+
		"\u014b%\u0001\u0000\u0000\u0000 *2<D[ejov\u0080\u008c\u0094\u0097\u00ae"+
		"\u00ba\u00cf\u00e6\u00e9\u00ec\u00ee\u00fc\u00ff\u0107\u0113\u0117\u011f"+
		"\u0124\u0129\u0139\u0144\u0147\u014a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}