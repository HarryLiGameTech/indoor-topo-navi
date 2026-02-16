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
		T__52=53, T__53=54, T__54=55, T__55=56, T__56=57, T__57=58, T__58=59, 
		ID=60, FLOAT=61, INT=62, STRING=63, NL=64, WS=65, COMMENT=66, BLOCK_COMMENT=67;
	public static final int
		RULE_surfaceDef = 0, RULE_globalConfigBody = 1, RULE_surfaceBody = 2, 
		RULE_globalConfigElement = 3, RULE_surfaceBodyElement = 4, RULE_coreDef = 5, 
		RULE_paramList = 6, RULE_param = 7, RULE_typeExpr = 8, RULE_typeAtom = 9, 
		RULE_recordType = 10, RULE_fieldDecl = 11, RULE_expr = 12, RULE_atom = 13, 
		RULE_fieldAssign = 14, RULE_block = 15, RULE_stmt = 16, RULE_pathSpec = 17, 
		RULE_arrowSpec = 18, RULE_arrowHeading = 19, RULE_requirements = 20, RULE_recordAssign = 21, 
		RULE_identifier = 22;
	private static String[] makeRuleNames() {
		return new String[] {
			"surfaceDef", "globalConfigBody", "surfaceBody", "globalConfigElement", 
			"surfaceBodyElement", "coreDef", "paramList", "param", "typeExpr", "typeAtom", 
			"recordType", "fieldDecl", "expr", "atom", "fieldAssign", "block", "stmt", 
			"pathSpec", "arrowSpec", "arrowHeading", "requirements", "recordAssign", 
			"identifier"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'root'", "'('", "')'", "'topo-map'", "'transport'", "'is'", "'building-includes'", 
			"'{'", "'}'", "'vehicle'", "'submap'", "'topo-node'", "'atomic-path'", 
			"'station'", "'at'", "'on'", "'arrow'", "'>>'", "'type'", "'='", "'def'", 
			"':'", "'let'", "','", "'->'", "'Int'", "'Float'", "'Bool'", "'String'", 
			"'.'", "'-'", "'*'", "'/'", "'+'", "'=='", "'<'", "'>'", "'<='", "'>='", 
			"'if'", "'then'", "'else'", "'in'", "'rec'", "'fix'", "'\\'", "'fn'", 
			"'=>'", "'true'", "'false'", "';'", "'['", "'<->'", "']'", "'^^'", "'\\/'", 
			"'requires'", "'&&'", "'::'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"ID", "FLOAT", "INT", "STRING", "NL", "WS", "COMMENT", "BLOCK_COMMENT"
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
	public static class SurfaceDefTopoMapExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public SurfaceDefTopoMapExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceDefTopoMapExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceDefTopoMapExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceDefTopoMapExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceDefRootExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public SurfaceDefRootExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceDefRootExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceDefRootExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceDefRootExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceDefGlobalConfigExprContext extends SurfaceDefContext {
		public GlobalConfigBodyContext globalConfigBody() {
			return getRuleContext(GlobalConfigBodyContext.class,0);
		}
		public SurfaceDefGlobalConfigExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceDefGlobalConfigExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceDefGlobalConfigExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceDefGlobalConfigExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SurfaceDefTransportExprContext extends SurfaceDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public SurfaceBodyContext surfaceBody() {
			return getRuleContext(SurfaceBodyContext.class,0);
		}
		public SurfaceDefTransportExprContext(SurfaceDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceDefTransportExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceDefTransportExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceDefTransportExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SurfaceDefContext surfaceDef() throws RecognitionException {
		SurfaceDefContext _localctx = new SurfaceDefContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_surfaceDef);
		int _la;
		try {
			setState(70);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				_localctx = new SurfaceDefRootExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(46);
				match(T__0);
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
			case T__3:
				_localctx = new SurfaceDefTopoMapExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(54);
				match(T__3);
				setState(55);
				match(ID);
				setState(56);
				match(T__1);
				setState(58);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(57);
					paramList();
					}
				}

				setState(60);
				match(T__2);
				setState(61);
				surfaceBody();
				}
				break;
			case T__4:
				_localctx = new SurfaceDefTransportExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(62);
				match(T__4);
				setState(63);
				match(ID);
				setState(64);
				match(T__5);
				setState(65);
				expr(0);
				setState(66);
				surfaceBody();
				}
				break;
			case T__6:
				_localctx = new SurfaceDefGlobalConfigExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(68);
				match(T__6);
				setState(69);
				globalConfigBody();
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
	public static class GlobalConfigBodyContext extends ParserRuleContext {
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
		public List<GlobalConfigElementContext> globalConfigElement() {
			return getRuleContexts(GlobalConfigElementContext.class);
		}
		public GlobalConfigElementContext globalConfigElement(int i) {
			return getRuleContext(GlobalConfigElementContext.class,i);
		}
		public GlobalConfigBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globalConfigBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterGlobalConfigBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitGlobalConfigBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitGlobalConfigBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalConfigBodyContext globalConfigBody() throws RecognitionException {
		GlobalConfigBodyContext _localctx = new GlobalConfigBodyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_globalConfigBody);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(T__7);
			setState(76);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(73);
					match(NL);
					}
					} 
				}
				setState(78);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(87);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(79);
					globalConfigElement();
					setState(81); 
					_errHandler.sync(this);
					_alt = 1;
					do {
						switch (_alt) {
						case 1:
							{
							{
							setState(80);
							match(NL);
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(83); 
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
					} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
					}
					} 
				}
				setState(89);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==T__10) {
				{
				setState(90);
				globalConfigElement();
				}
			}

			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(93);
				match(NL);
				}
				}
				setState(98);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(99);
			match(T__8);
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
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
		public List<SurfaceBodyElementContext> surfaceBodyElement() {
			return getRuleContexts(SurfaceBodyElementContext.class);
		}
		public SurfaceBodyElementContext surfaceBodyElement(int i) {
			return getRuleContext(SurfaceBodyElementContext.class,i);
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
		enterRule(_localctx, 4, RULE_surfaceBody);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			match(T__7);
			setState(105);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(102);
					match(NL);
					}
					} 
				}
				setState(107);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			setState(116);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(108);
					surfaceBodyElement();
					setState(110); 
					_errHandler.sync(this);
					_alt = 1;
					do {
						switch (_alt) {
						case 1:
							{
							{
							setState(109);
							match(NL);
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(112); 
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
					} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
					}
					} 
				}
				setState(118);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -1150985262471679740L) != 0)) {
				{
				setState(119);
				surfaceBodyElement();
				}
			}

			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(122);
				match(NL);
				}
				}
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(128);
			match(T__8);
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
	public static class GlobalConfigElementContext extends ParserRuleContext {
		public GlobalConfigElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globalConfigElement; }
	 
		public GlobalConfigElementContext() { }
		public void copyFrom(GlobalConfigElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GlobalConfigElementVehicleRefContext extends GlobalConfigElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public GlobalConfigElementVehicleRefContext(GlobalConfigElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterGlobalConfigElementVehicleRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitGlobalConfigElementVehicleRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitGlobalConfigElementVehicleRef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GlobalConfigElementSubmapRefContext extends GlobalConfigElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public GlobalConfigElementSubmapRefContext(GlobalConfigElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterGlobalConfigElementSubmapRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitGlobalConfigElementSubmapRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitGlobalConfigElementSubmapRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalConfigElementContext globalConfigElement() throws RecognitionException {
		GlobalConfigElementContext _localctx = new GlobalConfigElementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_globalConfigElement);
		try {
			setState(134);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__9:
				_localctx = new GlobalConfigElementVehicleRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(130);
				match(T__9);
				setState(131);
				match(ID);
				}
				break;
			case T__10:
				_localctx = new GlobalConfigElementSubmapRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(132);
				match(T__10);
				setState(133);
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
	public static class SurfaceElementStationContext extends SurfaceBodyElementContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RecordAssignContext recordAssign() {
			return getRuleContext(RecordAssignContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public RequirementsContext requirements() {
			return getRuleContext(RequirementsContext.class,0);
		}
		public SurfaceElementStationContext(SurfaceBodyElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterSurfaceElementStation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitSurfaceElementStation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitSurfaceElementStation(this);
			else return visitor.visitChildren(this);
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
		enterRule(_localctx, 8, RULE_surfaceBodyElement);
		int _la;
		try {
			setState(173);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__7:
			case T__18:
			case T__20:
			case T__22:
			case T__30:
			case T__39:
			case T__44:
			case T__45:
			case T__46:
			case T__48:
			case T__49:
			case ID:
			case FLOAT:
			case INT:
			case STRING:
				_localctx = new SurfaceElementCoreDefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				coreDef();
				}
				break;
			case T__11:
				_localctx = new SurfaceElementTopoNodeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(137);
				match(T__11);
				setState(138);
				match(ID);
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__7) {
					{
					setState(139);
					recordAssign();
					}
				}

				}
				break;
			case T__12:
				_localctx = new SurfaceElementAtomicPathContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(142);
				match(T__12);
				setState(143);
				pathSpec();
				setState(144);
				recordAssign();
				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__56) {
					{
					setState(145);
					requirements();
					}
				}

				}
				break;
			case T__13:
				_localctx = new SurfaceElementStationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(148);
				match(T__13);
				setState(149);
				match(ID);
				setState(150);
				match(T__14);
				setState(151);
				identifier();
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__14) {
					{
					{
					setState(152);
					match(T__14);
					setState(153);
					expr(0);
					}
					}
					setState(158);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(159);
				recordAssign();
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__56) {
					{
					setState(160);
					requirements();
					setState(163);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__15) {
						{
						setState(161);
						match(T__15);
						setState(162);
						expr(0);
						}
					}

					}
				}

				}
				break;
			case T__16:
				_localctx = new SurfaceElementArrowContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(167);
				match(T__16);
				setState(168);
				arrowSpec();
				setState(169);
				arrowHeading();
				setState(170);
				match(T__17);
				setState(171);
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
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
	public static class LetDefContext extends CoreDefContext {
		public TerminalNode ID() { return getToken(MapFileParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TypeExprContext typeExpr() {
			return getRuleContext(TypeExprContext.class,0);
		}
		public LetDefContext(CoreDefContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterLetDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitLetDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitLetDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoreDefContext coreDef() throws RecognitionException {
		CoreDefContext _localctx = new CoreDefContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_coreDef);
		int _la;
		try {
			setState(203);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				_localctx = new TypeDefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				match(T__18);
				setState(176);
				match(ID);
				setState(177);
				match(T__19);
				setState(178);
				typeExpr();
				}
				break;
			case 2:
				_localctx = new FuncDefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				match(T__20);
				setState(180);
				match(ID);
				setState(186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(181);
					match(T__1);
					setState(183);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ID) {
						{
						setState(182);
						paramList();
						}
					}

					setState(185);
					match(T__2);
					}
				}

				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__21) {
					{
					setState(188);
					match(T__21);
					setState(189);
					typeExpr();
					}
				}

				setState(192);
				match(T__19);
				setState(193);
				expr(0);
				}
				break;
			case 3:
				_localctx = new LetDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(194);
				match(T__22);
				setState(195);
				match(ID);
				setState(198);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__21) {
					{
					setState(196);
					match(T__21);
					setState(197);
					typeExpr();
					}
				}

				setState(200);
				match(T__19);
				setState(201);
				expr(0);
				}
				break;
			case 4:
				_localctx = new ScriptExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(202);
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
		enterRule(_localctx, 12, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			param();
			setState(210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__23) {
				{
				{
				setState(206);
				match(T__23);
				setState(207);
				param();
				}
				}
				setState(212);
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
		enterRule(_localctx, 14, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
			match(ID);
			setState(214);
			match(T__21);
			setState(215);
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
		enterRule(_localctx, 16, RULE_typeExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			typeAtom();
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__24) {
				{
				setState(218);
				match(T__24);
				setState(219);
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
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
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
		enterRule(_localctx, 18, RULE_typeAtom);
		try {
			setState(232);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__25:
				enterOuterAlt(_localctx, 1);
				{
				setState(222);
				match(T__25);
				}
				break;
			case T__26:
				enterOuterAlt(_localctx, 2);
				{
				setState(223);
				match(T__26);
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 3);
				{
				setState(224);
				match(T__27);
				}
				break;
			case T__28:
				enterOuterAlt(_localctx, 4);
				{
				setState(225);
				match(T__28);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 5);
				{
				setState(226);
				identifier();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 6);
				{
				setState(227);
				recordType();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 7);
				{
				setState(228);
				match(T__1);
				setState(229);
				typeExpr();
				setState(230);
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
		enterRule(_localctx, 20, RULE_recordType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(T__7);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(235);
				fieldDecl();
				setState(240);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__23) {
					{
					{
					setState(236);
					match(T__23);
					setState(237);
					fieldDecl();
					}
					}
					setState(242);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(245);
			match(T__8);
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
		enterRule(_localctx, 22, RULE_fieldDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(ID);
			setState(248);
			match(T__21);
			setState(249);
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
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(252);
				atom();
				}
				break;
			case 2:
				{
				_localctx = new NegExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(253);
				match(T__30);
				setState(254);
				expr(9);
				}
				break;
			case 3:
				{
				_localctx = new IfExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(255);
				match(T__39);
				setState(256);
				((IfExprContext)_localctx).cond = expr(0);
				setState(257);
				match(T__40);
				setState(258);
				((IfExprContext)_localctx).ifExpr = expr(0);
				setState(259);
				match(T__41);
				setState(260);
				((IfExprContext)_localctx).elseExpr = expr(5);
				}
				break;
			case 4:
				{
				_localctx = new LetExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(262);
				match(T__22);
				setState(263);
				match(ID);
				setState(266);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__21) {
					{
					setState(264);
					match(T__21);
					setState(265);
					((LetExprContext)_localctx).letType = typeExpr();
					}
				}

				setState(268);
				match(T__19);
				setState(269);
				((LetExprContext)_localctx).assignValue = expr(0);
				setState(270);
				match(T__42);
				setState(271);
				expr(4);
				}
				break;
			case 5:
				{
				_localctx = new LetRecExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(273);
				match(T__22);
				setState(274);
				match(T__43);
				setState(275);
				match(ID);
				setState(278);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__21) {
					{
					setState(276);
					match(T__21);
					setState(277);
					typeExpr();
					}
				}

				setState(280);
				match(T__19);
				setState(281);
				expr(0);
				setState(282);
				match(T__42);
				setState(283);
				expr(3);
				}
				break;
			case 6:
				{
				_localctx = new FixExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(285);
				match(T__44);
				setState(286);
				match(ID);
				setState(287);
				match(T__21);
				setState(288);
				typeExpr();
				setState(289);
				match(T__29);
				setState(290);
				expr(2);
				}
				break;
			case 7:
				{
				_localctx = new LamExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(292);
				_la = _input.LA(1);
				if ( !(_la==T__45 || _la==T__46) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(293);
				match(ID);
				setState(294);
				match(T__21);
				setState(295);
				typeExpr();
				setState(296);
				_la = _input.LA(1);
				if ( !(_la==T__29 || _la==T__47) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(297);
				expr(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(330);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(328);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
					case 1:
						{
						_localctx = new MulDivExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(301);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(302);
						((MulDivExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__31 || _la==T__32) ) {
							((MulDivExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(303);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new AddSubExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(304);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(305);
						((AddSubExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__30 || _la==T__33) ) {
							((AddSubExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(306);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(307);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(308);
						((CompExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1065151889408L) != 0)) ) {
							((CompExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(309);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new ProjExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(310);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(311);
						match(T__29);
						setState(312);
						match(ID);
						}
						break;
					case 5:
						{
						_localctx = new AppMlExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(313);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(314);
						atom();
						}
						break;
					case 6:
						{
						_localctx = new AppCExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(315);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(316);
						match(T__1);
						setState(325);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -1150985262474460924L) != 0)) {
							{
							setState(317);
							expr(0);
							setState(322);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__23) {
								{
								{
								setState(318);
								match(T__23);
								setState(319);
								expr(0);
								}
								}
								setState(324);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(327);
						match(T__2);
						}
						break;
					}
					} 
				}
				setState(332);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
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
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
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
		enterRule(_localctx, 26, RULE_atom);
		int _la;
		try {
			int _alt;
			setState(374);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(333);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(334);
				match(FLOAT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(335);
				match(STRING);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(336);
				match(T__48);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(337);
				match(T__49);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(338);
				identifier();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(339);
				match(T__7);
				setState(343);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(340);
						match(NL);
						}
						} 
					}
					setState(345);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
				}
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(346);
					fieldAssign();
					setState(357);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__23) {
						{
						{
						setState(347);
						match(T__23);
						setState(351);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NL) {
							{
							{
							setState(348);
							match(NL);
							}
							}
							setState(353);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(354);
						fieldAssign();
						}
						}
						setState(359);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(365);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(362);
					match(NL);
					}
					}
					setState(367);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(368);
				match(T__8);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(369);
				block();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(370);
				match(T__1);
				setState(371);
				expr(0);
				setState(372);
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
		enterRule(_localctx, 28, RULE_fieldAssign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			match(ID);
			setState(377);
			match(T__19);
			setState(378);
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
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
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
		enterRule(_localctx, 30, RULE_block);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(380);
			match(T__7);
			setState(384);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(381);
				match(NL);
				}
				}
				setState(386);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(397);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(387);
					stmt();
					setState(388);
					match(T__50);
					setState(392);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==NL) {
						{
						{
						setState(389);
						match(NL);
						}
						}
						setState(394);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					} 
				}
				setState(399);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			}
			setState(407);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -1150985262474460924L) != 0)) {
				{
				setState(400);
				expr(0);
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(401);
					match(NL);
					}
					}
					setState(406);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(409);
			match(T__8);
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
		enterRule(_localctx, 32, RULE_stmt);
		int _la;
		try {
			setState(420);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				_localctx = new LetStmtContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(411);
				match(T__22);
				setState(412);
				match(ID);
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__21) {
					{
					setState(413);
					match(T__21);
					setState(414);
					typeExpr();
					}
				}

				setState(417);
				match(T__19);
				setState(418);
				expr(0);
				}
				break;
			case 2:
				_localctx = new ExprStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(419);
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
		public Token direction;
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
		enterRule(_localctx, 34, RULE_pathSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(422);
			match(T__51);
			setState(423);
			match(ID);
			{
			setState(424);
			((PathSpecContext)_localctx).direction = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==T__24 || _la==T__52) ) {
				((PathSpecContext)_localctx).direction = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(425);
			match(ID);
			}
			setState(427);
			match(T__53);
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
		enterRule(_localctx, 36, RULE_arrowSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(429);
			match(T__51);
			setState(430);
			match(ID);
			setState(431);
			match(T__24);
			setState(432);
			match(ID);
			setState(433);
			match(T__53);
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
		enterRule(_localctx, 38, RULE_arrowHeading);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(435);
			_la = _input.LA(1);
			if ( !(_la==T__54 || _la==T__55) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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
		enterRule(_localctx, 40, RULE_requirements);
		int _la;
		try {
			setState(452);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(437);
				match(T__56);
				setState(438);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(439);
				match(T__56);
				setState(440);
				match(T__35);
				setState(449);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(441);
					match(ID);
					setState(446);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__57) {
						{
						{
						setState(442);
						match(T__57);
						setState(443);
						match(ID);
						}
						}
						setState(448);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(451);
				match(T__36);
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
		public List<TerminalNode> NL() { return getTokens(MapFileParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(MapFileParser.NL, i);
		}
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
		enterRule(_localctx, 42, RULE_recordAssign);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(454);
			match(T__7);
			setState(458);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(455);
					match(NL);
					}
					} 
				}
				setState(460);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
			}
			setState(475);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(461);
				fieldAssign();
				setState(472);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__23) {
					{
					{
					setState(462);
					match(T__23);
					setState(466);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==NL) {
						{
						{
						setState(463);
						match(NL);
						}
						}
						setState(468);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(469);
					fieldAssign();
					}
					}
					setState(474);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(477);
				match(NL);
				}
				}
				setState(482);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(483);
			match(T__8);
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
	public static class IdentifierContext extends ParserRuleContext {
		public Token ID;
		public List<Token> path = new ArrayList<Token>();
		public List<TerminalNode> ID() { return getTokens(MapFileParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MapFileParser.ID, i);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_identifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
			((IdentifierContext)_localctx).ID = match(ID);
			((IdentifierContext)_localctx).path.add(((IdentifierContext)_localctx).ID);
			setState(490);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(486);
					match(T__58);
					setState(487);
					((IdentifierContext)_localctx).ID = match(ID);
					((IdentifierContext)_localctx).path.add(((IdentifierContext)_localctx).ID);
					}
					} 
				}
				setState(492);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 12:
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
		"\u0004\u0001C\u01ee\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u00003\b\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u0000;\b\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u0000G\b\u0000\u0001\u0001\u0001\u0001"+
		"\u0005\u0001K\b\u0001\n\u0001\f\u0001N\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0004\u0001R\b\u0001\u000b\u0001\f\u0001S\u0005\u0001V\b\u0001\n\u0001"+
		"\f\u0001Y\t\u0001\u0001\u0001\u0003\u0001\\\b\u0001\u0001\u0001\u0005"+
		"\u0001_\b\u0001\n\u0001\f\u0001b\t\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0002\u0001\u0002\u0005\u0002h\b\u0002\n\u0002\f\u0002k\t\u0002\u0001"+
		"\u0002\u0001\u0002\u0004\u0002o\b\u0002\u000b\u0002\f\u0002p\u0005\u0002"+
		"s\b\u0002\n\u0002\f\u0002v\t\u0002\u0001\u0002\u0003\u0002y\b\u0002\u0001"+
		"\u0002\u0005\u0002|\b\u0002\n\u0002\f\u0002\u007f\t\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"\u0087\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"\u008d\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"\u0093\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0005\u0004\u009b\b\u0004\n\u0004\f\u0004\u009e\t\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00a4\b\u0004\u0003"+
		"\u0004\u00a6\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u00ae\b\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005\u00b8\b\u0005\u0001\u0005\u0003\u0005\u00bb\b\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005\u00bf\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u00c7\b\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u00cc\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0005\u0006\u00d1\b\u0006\n\u0006\f\u0006\u00d4\t\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0003"+
		"\b\u00dd\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\t\u00e9\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0005"+
		"\n\u00ef\b\n\n\n\f\n\u00f2\t\n\u0003\n\u00f4\b\n\u0001\n\u0001\n\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u010b\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0117\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u012c\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0005\f\u0141\b\f\n\f\f\f\u0144\t\f\u0003\f\u0146\b"+
		"\f\u0001\f\u0005\f\u0149\b\f\n\f\f\f\u014c\t\f\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u0156\b\r\n\r\f\r\u0159"+
		"\t\r\u0001\r\u0001\r\u0001\r\u0005\r\u015e\b\r\n\r\f\r\u0161\t\r\u0001"+
		"\r\u0005\r\u0164\b\r\n\r\f\r\u0167\t\r\u0003\r\u0169\b\r\u0001\r\u0005"+
		"\r\u016c\b\r\n\r\f\r\u016f\t\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0003\r\u0177\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0005\u000f\u017f\b\u000f\n\u000f\f\u000f\u0182"+
		"\t\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0187\b\u000f"+
		"\n\u000f\f\u000f\u018a\t\u000f\u0005\u000f\u018c\b\u000f\n\u000f\f\u000f"+
		"\u018f\t\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0193\b\u000f\n\u000f"+
		"\f\u000f\u0196\t\u000f\u0003\u000f\u0198\b\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u01a0\b\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u01a5\b\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u01bd\b\u0014\n\u0014"+
		"\f\u0014\u01c0\t\u0014\u0003\u0014\u01c2\b\u0014\u0001\u0014\u0003\u0014"+
		"\u01c5\b\u0014\u0001\u0015\u0001\u0015\u0005\u0015\u01c9\b\u0015\n\u0015"+
		"\f\u0015\u01cc\t\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015"+
		"\u01d1\b\u0015\n\u0015\f\u0015\u01d4\t\u0015\u0001\u0015\u0005\u0015\u01d7"+
		"\b\u0015\n\u0015\f\u0015\u01da\t\u0015\u0003\u0015\u01dc\b\u0015\u0001"+
		"\u0015\u0005\u0015\u01df\b\u0015\n\u0015\f\u0015\u01e2\t\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u01e9\b\u0016"+
		"\n\u0016\f\u0016\u01ec\t\u0016\u0001\u0016\u0000\u0001\u0018\u0017\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,\u0000\u0007\u0001\u0000./\u0002\u0000\u001e\u001e00\u0001"+
		"\u0000 !\u0002\u0000\u001f\u001f\"\"\u0001\u0000#\'\u0002\u0000\u0019"+
		"\u001955\u0001\u000078\u022d\u0000F\u0001\u0000\u0000\u0000\u0002H\u0001"+
		"\u0000\u0000\u0000\u0004e\u0001\u0000\u0000\u0000\u0006\u0086\u0001\u0000"+
		"\u0000\u0000\b\u00ad\u0001\u0000\u0000\u0000\n\u00cb\u0001\u0000\u0000"+
		"\u0000\f\u00cd\u0001\u0000\u0000\u0000\u000e\u00d5\u0001\u0000\u0000\u0000"+
		"\u0010\u00d9\u0001\u0000\u0000\u0000\u0012\u00e8\u0001\u0000\u0000\u0000"+
		"\u0014\u00ea\u0001\u0000\u0000\u0000\u0016\u00f7\u0001\u0000\u0000\u0000"+
		"\u0018\u012b\u0001\u0000\u0000\u0000\u001a\u0176\u0001\u0000\u0000\u0000"+
		"\u001c\u0178\u0001\u0000\u0000\u0000\u001e\u017c\u0001\u0000\u0000\u0000"+
		" \u01a4\u0001\u0000\u0000\u0000\"\u01a6\u0001\u0000\u0000\u0000$\u01ad"+
		"\u0001\u0000\u0000\u0000&\u01b3\u0001\u0000\u0000\u0000(\u01c4\u0001\u0000"+
		"\u0000\u0000*\u01c6\u0001\u0000\u0000\u0000,\u01e5\u0001\u0000\u0000\u0000"+
		"./\u0005\u0001\u0000\u0000/0\u0005<\u0000\u000002\u0005\u0002\u0000\u0000"+
		"13\u0003\f\u0006\u000021\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u0000"+
		"34\u0001\u0000\u0000\u000045\u0005\u0003\u0000\u00005G\u0003\u0004\u0002"+
		"\u000067\u0005\u0004\u0000\u000078\u0005<\u0000\u00008:\u0005\u0002\u0000"+
		"\u00009;\u0003\f\u0006\u0000:9\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000"+
		"\u0000;<\u0001\u0000\u0000\u0000<=\u0005\u0003\u0000\u0000=G\u0003\u0004"+
		"\u0002\u0000>?\u0005\u0005\u0000\u0000?@\u0005<\u0000\u0000@A\u0005\u0006"+
		"\u0000\u0000AB\u0003\u0018\f\u0000BC\u0003\u0004\u0002\u0000CG\u0001\u0000"+
		"\u0000\u0000DE\u0005\u0007\u0000\u0000EG\u0003\u0002\u0001\u0000F.\u0001"+
		"\u0000\u0000\u0000F6\u0001\u0000\u0000\u0000F>\u0001\u0000\u0000\u0000"+
		"FD\u0001\u0000\u0000\u0000G\u0001\u0001\u0000\u0000\u0000HL\u0005\b\u0000"+
		"\u0000IK\u0005@\u0000\u0000JI\u0001\u0000\u0000\u0000KN\u0001\u0000\u0000"+
		"\u0000LJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000MW\u0001\u0000"+
		"\u0000\u0000NL\u0001\u0000\u0000\u0000OQ\u0003\u0006\u0003\u0000PR\u0005"+
		"@\u0000\u0000QP\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000SQ\u0001"+
		"\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000TV\u0001\u0000\u0000\u0000"+
		"UO\u0001\u0000\u0000\u0000VY\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000"+
		"\u0000WX\u0001\u0000\u0000\u0000X[\u0001\u0000\u0000\u0000YW\u0001\u0000"+
		"\u0000\u0000Z\\\u0003\u0006\u0003\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001"+
		"\u0000\u0000\u0000\\`\u0001\u0000\u0000\u0000]_\u0005@\u0000\u0000^]\u0001"+
		"\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000"+
		"`a\u0001\u0000\u0000\u0000ac\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000"+
		"\u0000cd\u0005\t\u0000\u0000d\u0003\u0001\u0000\u0000\u0000ei\u0005\b"+
		"\u0000\u0000fh\u0005@\u0000\u0000gf\u0001\u0000\u0000\u0000hk\u0001\u0000"+
		"\u0000\u0000ig\u0001\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000jt\u0001"+
		"\u0000\u0000\u0000ki\u0001\u0000\u0000\u0000ln\u0003\b\u0004\u0000mo\u0005"+
		"@\u0000\u0000nm\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pn\u0001"+
		"\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000qs\u0001\u0000\u0000\u0000"+
		"rl\u0001\u0000\u0000\u0000sv\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000"+
		"\u0000tu\u0001\u0000\u0000\u0000ux\u0001\u0000\u0000\u0000vt\u0001\u0000"+
		"\u0000\u0000wy\u0003\b\u0004\u0000xw\u0001\u0000\u0000\u0000xy\u0001\u0000"+
		"\u0000\u0000y}\u0001\u0000\u0000\u0000z|\u0005@\u0000\u0000{z\u0001\u0000"+
		"\u0000\u0000|\u007f\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000\u0000"+
		"}~\u0001\u0000\u0000\u0000~\u0080\u0001\u0000\u0000\u0000\u007f}\u0001"+
		"\u0000\u0000\u0000\u0080\u0081\u0005\t\u0000\u0000\u0081\u0005\u0001\u0000"+
		"\u0000\u0000\u0082\u0083\u0005\n\u0000\u0000\u0083\u0087\u0005<\u0000"+
		"\u0000\u0084\u0085\u0005\u000b\u0000\u0000\u0085\u0087\u0005<\u0000\u0000"+
		"\u0086\u0082\u0001\u0000\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000"+
		"\u0087\u0007\u0001\u0000\u0000\u0000\u0088\u00ae\u0003\n\u0005\u0000\u0089"+
		"\u008a\u0005\f\u0000\u0000\u008a\u008c\u0005<\u0000\u0000\u008b\u008d"+
		"\u0003*\u0015\u0000\u008c\u008b\u0001\u0000\u0000\u0000\u008c\u008d\u0001"+
		"\u0000\u0000\u0000\u008d\u00ae\u0001\u0000\u0000\u0000\u008e\u008f\u0005"+
		"\r\u0000\u0000\u008f\u0090\u0003\"\u0011\u0000\u0090\u0092\u0003*\u0015"+
		"\u0000\u0091\u0093\u0003(\u0014\u0000\u0092\u0091\u0001\u0000\u0000\u0000"+
		"\u0092\u0093\u0001\u0000\u0000\u0000\u0093\u00ae\u0001\u0000\u0000\u0000"+
		"\u0094\u0095\u0005\u000e\u0000\u0000\u0095\u0096\u0005<\u0000\u0000\u0096"+
		"\u0097\u0005\u000f\u0000\u0000\u0097\u009c\u0003,\u0016\u0000\u0098\u0099"+
		"\u0005\u000f\u0000\u0000\u0099\u009b\u0003\u0018\f\u0000\u009a\u0098\u0001"+
		"\u0000\u0000\u0000\u009b\u009e\u0001\u0000\u0000\u0000\u009c\u009a\u0001"+
		"\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009f\u0001"+
		"\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009f\u00a5\u0003"+
		"*\u0015\u0000\u00a0\u00a3\u0003(\u0014\u0000\u00a1\u00a2\u0005\u0010\u0000"+
		"\u0000\u00a2\u00a4\u0003\u0018\f\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a6\u0001\u0000\u0000\u0000"+
		"\u00a5\u00a0\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000\u0000\u0000"+
		"\u00a6\u00ae\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005\u0011\u0000\u0000"+
		"\u00a8\u00a9\u0003$\u0012\u0000\u00a9\u00aa\u0003&\u0013\u0000\u00aa\u00ab"+
		"\u0005\u0012\u0000\u0000\u00ab\u00ac\u0003\u0018\f\u0000\u00ac\u00ae\u0001"+
		"\u0000\u0000\u0000\u00ad\u0088\u0001\u0000\u0000\u0000\u00ad\u0089\u0001"+
		"\u0000\u0000\u0000\u00ad\u008e\u0001\u0000\u0000\u0000\u00ad\u0094\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a7\u0001\u0000\u0000\u0000\u00ae\t\u0001\u0000"+
		"\u0000\u0000\u00af\u00b0\u0005\u0013\u0000\u0000\u00b0\u00b1\u0005<\u0000"+
		"\u0000\u00b1\u00b2\u0005\u0014\u0000\u0000\u00b2\u00cc\u0003\u0010\b\u0000"+
		"\u00b3\u00b4\u0005\u0015\u0000\u0000\u00b4\u00ba\u0005<\u0000\u0000\u00b5"+
		"\u00b7\u0005\u0002\u0000\u0000\u00b6\u00b8\u0003\f\u0006\u0000\u00b7\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8\u00b9"+
		"\u0001\u0000\u0000\u0000\u00b9\u00bb\u0005\u0003\u0000\u0000\u00ba\u00b5"+
		"\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00be"+
		"\u0001\u0000\u0000\u0000\u00bc\u00bd\u0005\u0016\u0000\u0000\u00bd\u00bf"+
		"\u0003\u0010\b\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00be\u00bf\u0001"+
		"\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0005"+
		"\u0014\u0000\u0000\u00c1\u00cc\u0003\u0018\f\u0000\u00c2\u00c3\u0005\u0017"+
		"\u0000\u0000\u00c3\u00c6\u0005<\u0000\u0000\u00c4\u00c5\u0005\u0016\u0000"+
		"\u0000\u00c5\u00c7\u0003\u0010\b\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000"+
		"\u00c6\u00c7\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000"+
		"\u00c8\u00c9\u0005\u0014\u0000\u0000\u00c9\u00cc\u0003\u0018\f\u0000\u00ca"+
		"\u00cc\u0003\u0018\f\u0000\u00cb\u00af\u0001\u0000\u0000\u0000\u00cb\u00b3"+
		"\u0001\u0000\u0000\u0000\u00cb\u00c2\u0001\u0000\u0000\u0000\u00cb\u00ca"+
		"\u0001\u0000\u0000\u0000\u00cc\u000b\u0001\u0000\u0000\u0000\u00cd\u00d2"+
		"\u0003\u000e\u0007\u0000\u00ce\u00cf\u0005\u0018\u0000\u0000\u00cf\u00d1"+
		"\u0003\u000e\u0007\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d1\u00d4"+
		"\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000\u0000\u00d2\u00d3"+
		"\u0001\u0000\u0000\u0000\u00d3\r\u0001\u0000\u0000\u0000\u00d4\u00d2\u0001"+
		"\u0000\u0000\u0000\u00d5\u00d6\u0005<\u0000\u0000\u00d6\u00d7\u0005\u0016"+
		"\u0000\u0000\u00d7\u00d8\u0003\u0010\b\u0000\u00d8\u000f\u0001\u0000\u0000"+
		"\u0000\u00d9\u00dc\u0003\u0012\t\u0000\u00da\u00db\u0005\u0019\u0000\u0000"+
		"\u00db\u00dd\u0003\u0010\b\u0000\u00dc\u00da\u0001\u0000\u0000\u0000\u00dc"+
		"\u00dd\u0001\u0000\u0000\u0000\u00dd\u0011\u0001\u0000\u0000\u0000\u00de"+
		"\u00e9\u0005\u001a\u0000\u0000\u00df\u00e9\u0005\u001b\u0000\u0000\u00e0"+
		"\u00e9\u0005\u001c\u0000\u0000\u00e1\u00e9\u0005\u001d\u0000\u0000\u00e2"+
		"\u00e9\u0003,\u0016\u0000\u00e3\u00e9\u0003\u0014\n\u0000\u00e4\u00e5"+
		"\u0005\u0002\u0000\u0000\u00e5\u00e6\u0003\u0010\b\u0000\u00e6\u00e7\u0005"+
		"\u0003\u0000\u0000\u00e7\u00e9\u0001\u0000\u0000\u0000\u00e8\u00de\u0001"+
		"\u0000\u0000\u0000\u00e8\u00df\u0001\u0000\u0000\u0000\u00e8\u00e0\u0001"+
		"\u0000\u0000\u0000\u00e8\u00e1\u0001\u0000\u0000\u0000\u00e8\u00e2\u0001"+
		"\u0000\u0000\u0000\u00e8\u00e3\u0001\u0000\u0000\u0000\u00e8\u00e4\u0001"+
		"\u0000\u0000\u0000\u00e9\u0013\u0001\u0000\u0000\u0000\u00ea\u00f3\u0005"+
		"\b\u0000\u0000\u00eb\u00f0\u0003\u0016\u000b\u0000\u00ec\u00ed\u0005\u0018"+
		"\u0000\u0000\u00ed\u00ef\u0003\u0016\u000b\u0000\u00ee\u00ec\u0001\u0000"+
		"\u0000\u0000\u00ef\u00f2\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000"+
		"\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000"+
		"\u0000\u0000\u00f2\u00f0\u0001\u0000\u0000\u0000\u00f3\u00eb\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f4\u0001\u0000\u0000\u0000\u00f4\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f5\u00f6\u0005\t\u0000\u0000\u00f6\u0015\u0001\u0000\u0000"+
		"\u0000\u00f7\u00f8\u0005<\u0000\u0000\u00f8\u00f9\u0005\u0016\u0000\u0000"+
		"\u00f9\u00fa\u0003\u0010\b\u0000\u00fa\u0017\u0001\u0000\u0000\u0000\u00fb"+
		"\u00fc\u0006\f\uffff\uffff\u0000\u00fc\u012c\u0003\u001a\r\u0000\u00fd"+
		"\u00fe\u0005\u001f\u0000\u0000\u00fe\u012c\u0003\u0018\f\t\u00ff\u0100"+
		"\u0005(\u0000\u0000\u0100\u0101\u0003\u0018\f\u0000\u0101\u0102\u0005"+
		")\u0000\u0000\u0102\u0103\u0003\u0018\f\u0000\u0103\u0104\u0005*\u0000"+
		"\u0000\u0104\u0105\u0003\u0018\f\u0005\u0105\u012c\u0001\u0000\u0000\u0000"+
		"\u0106\u0107\u0005\u0017\u0000\u0000\u0107\u010a\u0005<\u0000\u0000\u0108"+
		"\u0109\u0005\u0016\u0000\u0000\u0109\u010b\u0003\u0010\b\u0000\u010a\u0108"+
		"\u0001\u0000\u0000\u0000\u010a\u010b\u0001\u0000\u0000\u0000\u010b\u010c"+
		"\u0001\u0000\u0000\u0000\u010c\u010d\u0005\u0014\u0000\u0000\u010d\u010e"+
		"\u0003\u0018\f\u0000\u010e\u010f\u0005+\u0000\u0000\u010f\u0110\u0003"+
		"\u0018\f\u0004\u0110\u012c\u0001\u0000\u0000\u0000\u0111\u0112\u0005\u0017"+
		"\u0000\u0000\u0112\u0113\u0005,\u0000\u0000\u0113\u0116\u0005<\u0000\u0000"+
		"\u0114\u0115\u0005\u0016\u0000\u0000\u0115\u0117\u0003\u0010\b\u0000\u0116"+
		"\u0114\u0001\u0000\u0000\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u0117"+
		"\u0118\u0001\u0000\u0000\u0000\u0118\u0119\u0005\u0014\u0000\u0000\u0119"+
		"\u011a\u0003\u0018\f\u0000\u011a\u011b\u0005+\u0000\u0000\u011b\u011c"+
		"\u0003\u0018\f\u0003\u011c\u012c\u0001\u0000\u0000\u0000\u011d\u011e\u0005"+
		"-\u0000\u0000\u011e\u011f\u0005<\u0000\u0000\u011f\u0120\u0005\u0016\u0000"+
		"\u0000\u0120\u0121\u0003\u0010\b\u0000\u0121\u0122\u0005\u001e\u0000\u0000"+
		"\u0122\u0123\u0003\u0018\f\u0002\u0123\u012c\u0001\u0000\u0000\u0000\u0124"+
		"\u0125\u0007\u0000\u0000\u0000\u0125\u0126\u0005<\u0000\u0000\u0126\u0127"+
		"\u0005\u0016\u0000\u0000\u0127\u0128\u0003\u0010\b\u0000\u0128\u0129\u0007"+
		"\u0001\u0000\u0000\u0129\u012a\u0003\u0018\f\u0001\u012a\u012c\u0001\u0000"+
		"\u0000\u0000\u012b\u00fb\u0001\u0000\u0000\u0000\u012b\u00fd\u0001\u0000"+
		"\u0000\u0000\u012b\u00ff\u0001\u0000\u0000\u0000\u012b\u0106\u0001\u0000"+
		"\u0000\u0000\u012b\u0111\u0001\u0000\u0000\u0000\u012b\u011d\u0001\u0000"+
		"\u0000\u0000\u012b\u0124\u0001\u0000\u0000\u0000\u012c\u014a\u0001\u0000"+
		"\u0000\u0000\u012d\u012e\n\b\u0000\u0000\u012e\u012f\u0007\u0002\u0000"+
		"\u0000\u012f\u0149\u0003\u0018\f\t\u0130\u0131\n\u0007\u0000\u0000\u0131"+
		"\u0132\u0007\u0003\u0000\u0000\u0132\u0149\u0003\u0018\f\b\u0133\u0134"+
		"\n\u0006\u0000\u0000\u0134\u0135\u0007\u0004\u0000\u0000\u0135\u0149\u0003"+
		"\u0018\f\u0007\u0136\u0137\n\f\u0000\u0000\u0137\u0138\u0005\u001e\u0000"+
		"\u0000\u0138\u0149\u0005<\u0000\u0000\u0139\u013a\n\u000b\u0000\u0000"+
		"\u013a\u0149\u0003\u001a\r\u0000\u013b\u013c\n\n\u0000\u0000\u013c\u0145"+
		"\u0005\u0002\u0000\u0000\u013d\u0142\u0003\u0018\f\u0000\u013e\u013f\u0005"+
		"\u0018\u0000\u0000\u013f\u0141\u0003\u0018\f\u0000\u0140\u013e\u0001\u0000"+
		"\u0000\u0000\u0141\u0144\u0001\u0000\u0000\u0000\u0142\u0140\u0001\u0000"+
		"\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u0143\u0146\u0001\u0000"+
		"\u0000\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0145\u013d\u0001\u0000"+
		"\u0000\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0147\u0001\u0000"+
		"\u0000\u0000\u0147\u0149\u0005\u0003\u0000\u0000\u0148\u012d\u0001\u0000"+
		"\u0000\u0000\u0148\u0130\u0001\u0000\u0000\u0000\u0148\u0133\u0001\u0000"+
		"\u0000\u0000\u0148\u0136\u0001\u0000\u0000\u0000\u0148\u0139\u0001\u0000"+
		"\u0000\u0000\u0148\u013b\u0001\u0000\u0000\u0000\u0149\u014c\u0001\u0000"+
		"\u0000\u0000\u014a\u0148\u0001\u0000\u0000\u0000\u014a\u014b\u0001\u0000"+
		"\u0000\u0000\u014b\u0019\u0001\u0000\u0000\u0000\u014c\u014a\u0001\u0000"+
		"\u0000\u0000\u014d\u0177\u0005>\u0000\u0000\u014e\u0177\u0005=\u0000\u0000"+
		"\u014f\u0177\u0005?\u0000\u0000\u0150\u0177\u00051\u0000\u0000\u0151\u0177"+
		"\u00052\u0000\u0000\u0152\u0177\u0003,\u0016\u0000\u0153\u0157\u0005\b"+
		"\u0000\u0000\u0154\u0156\u0005@\u0000\u0000\u0155\u0154\u0001\u0000\u0000"+
		"\u0000\u0156\u0159\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000"+
		"\u0000\u0157\u0158\u0001\u0000\u0000\u0000\u0158\u0168\u0001\u0000\u0000"+
		"\u0000\u0159\u0157\u0001\u0000\u0000\u0000\u015a\u0165\u0003\u001c\u000e"+
		"\u0000\u015b\u015f\u0005\u0018\u0000\u0000\u015c\u015e\u0005@\u0000\u0000"+
		"\u015d\u015c\u0001\u0000\u0000\u0000\u015e\u0161\u0001\u0000\u0000\u0000"+
		"\u015f\u015d\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000"+
		"\u0160\u0162\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000"+
		"\u0162\u0164\u0003\u001c\u000e\u0000\u0163\u015b\u0001\u0000\u0000\u0000"+
		"\u0164\u0167\u0001\u0000\u0000\u0000\u0165\u0163\u0001\u0000\u0000\u0000"+
		"\u0165\u0166\u0001\u0000\u0000\u0000\u0166\u0169\u0001\u0000\u0000\u0000"+
		"\u0167\u0165\u0001\u0000\u0000\u0000\u0168\u015a\u0001\u0000\u0000\u0000"+
		"\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u016d\u0001\u0000\u0000\u0000"+
		"\u016a\u016c\u0005@\u0000\u0000\u016b\u016a\u0001\u0000\u0000\u0000\u016c"+
		"\u016f\u0001\u0000\u0000\u0000\u016d\u016b\u0001\u0000\u0000\u0000\u016d"+
		"\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0001\u0000\u0000\u0000\u016f"+
		"\u016d\u0001\u0000\u0000\u0000\u0170\u0177\u0005\t\u0000\u0000\u0171\u0177"+
		"\u0003\u001e\u000f\u0000\u0172\u0173\u0005\u0002\u0000\u0000\u0173\u0174"+
		"\u0003\u0018\f\u0000\u0174\u0175\u0005\u0003\u0000\u0000\u0175\u0177\u0001"+
		"\u0000\u0000\u0000\u0176\u014d\u0001\u0000\u0000\u0000\u0176\u014e\u0001"+
		"\u0000\u0000\u0000\u0176\u014f\u0001\u0000\u0000\u0000\u0176\u0150\u0001"+
		"\u0000\u0000\u0000\u0176\u0151\u0001\u0000\u0000\u0000\u0176\u0152\u0001"+
		"\u0000\u0000\u0000\u0176\u0153\u0001\u0000\u0000\u0000\u0176\u0171\u0001"+
		"\u0000\u0000\u0000\u0176\u0172\u0001\u0000\u0000\u0000\u0177\u001b\u0001"+
		"\u0000\u0000\u0000\u0178\u0179\u0005<\u0000\u0000\u0179\u017a\u0005\u0014"+
		"\u0000\u0000\u017a\u017b\u0003\u0018\f\u0000\u017b\u001d\u0001\u0000\u0000"+
		"\u0000\u017c\u0180\u0005\b\u0000\u0000\u017d\u017f\u0005@\u0000\u0000"+
		"\u017e\u017d\u0001\u0000\u0000\u0000\u017f\u0182\u0001\u0000\u0000\u0000"+
		"\u0180\u017e\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000\u0000\u0000"+
		"\u0181\u018d\u0001\u0000\u0000\u0000\u0182\u0180\u0001\u0000\u0000\u0000"+
		"\u0183\u0184\u0003 \u0010\u0000\u0184\u0188\u00053\u0000\u0000\u0185\u0187"+
		"\u0005@\u0000\u0000\u0186\u0185\u0001\u0000\u0000\u0000\u0187\u018a\u0001"+
		"\u0000\u0000\u0000\u0188\u0186\u0001\u0000\u0000\u0000\u0188\u0189\u0001"+
		"\u0000\u0000\u0000\u0189\u018c\u0001\u0000\u0000\u0000\u018a\u0188\u0001"+
		"\u0000\u0000\u0000\u018b\u0183\u0001\u0000\u0000\u0000\u018c\u018f\u0001"+
		"\u0000\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000\u018d\u018e\u0001"+
		"\u0000\u0000\u0000\u018e\u0197\u0001\u0000\u0000\u0000\u018f\u018d\u0001"+
		"\u0000\u0000\u0000\u0190\u0194\u0003\u0018\f\u0000\u0191\u0193\u0005@"+
		"\u0000\u0000\u0192\u0191\u0001\u0000\u0000\u0000\u0193\u0196\u0001\u0000"+
		"\u0000\u0000\u0194\u0192\u0001\u0000\u0000\u0000\u0194\u0195\u0001\u0000"+
		"\u0000\u0000\u0195\u0198\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000"+
		"\u0000\u0000\u0197\u0190\u0001\u0000\u0000\u0000\u0197\u0198\u0001\u0000"+
		"\u0000\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u019a\u0005\t\u0000"+
		"\u0000\u019a\u001f\u0001\u0000\u0000\u0000\u019b\u019c\u0005\u0017\u0000"+
		"\u0000\u019c\u019f\u0005<\u0000\u0000\u019d\u019e\u0005\u0016\u0000\u0000"+
		"\u019e\u01a0\u0003\u0010\b\u0000\u019f\u019d\u0001\u0000\u0000\u0000\u019f"+
		"\u01a0\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1"+
		"\u01a2\u0005\u0014\u0000\u0000\u01a2\u01a5\u0003\u0018\f\u0000\u01a3\u01a5"+
		"\u0003\u0018\f\u0000\u01a4\u019b\u0001\u0000\u0000\u0000\u01a4\u01a3\u0001"+
		"\u0000\u0000\u0000\u01a5!\u0001\u0000\u0000\u0000\u01a6\u01a7\u00054\u0000"+
		"\u0000\u01a7\u01a8\u0005<\u0000\u0000\u01a8\u01a9\u0007\u0005\u0000\u0000"+
		"\u01a9\u01aa\u0005<\u0000\u0000\u01aa\u01ab\u0001\u0000\u0000\u0000\u01ab"+
		"\u01ac\u00056\u0000\u0000\u01ac#\u0001\u0000\u0000\u0000\u01ad\u01ae\u0005"+
		"4\u0000\u0000\u01ae\u01af\u0005<\u0000\u0000\u01af\u01b0\u0005\u0019\u0000"+
		"\u0000\u01b0\u01b1\u0005<\u0000\u0000\u01b1\u01b2\u00056\u0000\u0000\u01b2"+
		"%\u0001\u0000\u0000\u0000\u01b3\u01b4\u0007\u0006\u0000\u0000\u01b4\'"+
		"\u0001\u0000\u0000\u0000\u01b5\u01b6\u00059\u0000\u0000\u01b6\u01c5\u0005"+
		"<\u0000\u0000\u01b7\u01b8\u00059\u0000\u0000\u01b8\u01c1\u0005$\u0000"+
		"\u0000\u01b9\u01be\u0005<\u0000\u0000\u01ba\u01bb\u0005:\u0000\u0000\u01bb"+
		"\u01bd\u0005<\u0000\u0000\u01bc\u01ba\u0001\u0000\u0000\u0000\u01bd\u01c0"+
		"\u0001\u0000\u0000\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01be\u01bf"+
		"\u0001\u0000\u0000\u0000\u01bf\u01c2\u0001\u0000\u0000\u0000\u01c0\u01be"+
		"\u0001\u0000\u0000\u0000\u01c1\u01b9\u0001\u0000\u0000\u0000\u01c1\u01c2"+
		"\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001\u0000\u0000\u0000\u01c3\u01c5"+
		"\u0005%\u0000\u0000\u01c4\u01b5\u0001\u0000\u0000\u0000\u01c4\u01b7\u0001"+
		"\u0000\u0000\u0000\u01c5)\u0001\u0000\u0000\u0000\u01c6\u01ca\u0005\b"+
		"\u0000\u0000\u01c7\u01c9\u0005@\u0000\u0000\u01c8\u01c7\u0001\u0000\u0000"+
		"\u0000\u01c9\u01cc\u0001\u0000\u0000\u0000\u01ca\u01c8\u0001\u0000\u0000"+
		"\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cb\u01db\u0001\u0000\u0000"+
		"\u0000\u01cc\u01ca\u0001\u0000\u0000\u0000\u01cd\u01d8\u0003\u001c\u000e"+
		"\u0000\u01ce\u01d2\u0005\u0018\u0000\u0000\u01cf\u01d1\u0005@\u0000\u0000"+
		"\u01d0\u01cf\u0001\u0000\u0000\u0000\u01d1\u01d4\u0001\u0000\u0000\u0000"+
		"\u01d2\u01d0\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001\u0000\u0000\u0000"+
		"\u01d3\u01d5\u0001\u0000\u0000\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000"+
		"\u01d5\u01d7\u0003\u001c\u000e\u0000\u01d6\u01ce\u0001\u0000\u0000\u0000"+
		"\u01d7\u01da\u0001\u0000\u0000\u0000\u01d8\u01d6\u0001\u0000\u0000\u0000"+
		"\u01d8\u01d9\u0001\u0000\u0000\u0000\u01d9\u01dc\u0001\u0000\u0000\u0000"+
		"\u01da\u01d8\u0001\u0000\u0000\u0000\u01db\u01cd\u0001\u0000\u0000\u0000"+
		"\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01e0\u0001\u0000\u0000\u0000"+
		"\u01dd\u01df\u0005@\u0000\u0000\u01de\u01dd\u0001\u0000\u0000\u0000\u01df"+
		"\u01e2\u0001\u0000\u0000\u0000\u01e0\u01de\u0001\u0000\u0000\u0000\u01e0"+
		"\u01e1\u0001\u0000\u0000\u0000\u01e1\u01e3\u0001\u0000\u0000\u0000\u01e2"+
		"\u01e0\u0001\u0000\u0000\u0000\u01e3\u01e4\u0005\t\u0000\u0000\u01e4+"+
		"\u0001\u0000\u0000\u0000\u01e5\u01ea\u0005<\u0000\u0000\u01e6\u01e7\u0005"+
		";\u0000\u0000\u01e7\u01e9\u0005<\u0000\u0000\u01e8\u01e6\u0001\u0000\u0000"+
		"\u0000\u01e9\u01ec\u0001\u0000\u0000\u0000\u01ea\u01e8\u0001\u0000\u0000"+
		"\u0000\u01ea\u01eb\u0001\u0000\u0000\u0000\u01eb-\u0001\u0000\u0000\u0000"+
		"\u01ec\u01ea\u0001\u0000\u0000\u0000;2:FLSW[`iptx}\u0086\u008c\u0092\u009c"+
		"\u00a3\u00a5\u00ad\u00b7\u00ba\u00be\u00c6\u00cb\u00d2\u00dc\u00e8\u00f0"+
		"\u00f3\u010a\u0116\u012b\u0142\u0145\u0148\u014a\u0157\u015f\u0165\u0168"+
		"\u016d\u0176\u0180\u0188\u018d\u0194\u0197\u019f\u01a4\u01be\u01c1\u01c4"+
		"\u01ca\u01d2\u01d8\u01db\u01e0\u01ea";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}