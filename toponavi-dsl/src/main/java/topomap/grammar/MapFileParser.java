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
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, RCC=23, TPCC=24, Int=25, 
		Dec=26, Hex=27, Oct=28, Bin=29, Float=30, RegularStringLiteral=31, Identifier=32, 
		LineComment=33, Whitespace=34;
	public static final int
		RULE_program = 0, RULE_topoMap = 1, RULE_topoMapContent = 2, RULE_topoNodeDeclaration = 3, 
		RULE_relationshipDeclaration = 4, RULE_pathDeclaration = 5, RULE_directionDeclaration = 6, 
		RULE_modifierDeclaration = 7, RULE_modifierText = 8, RULE_expr = 9, RULE_primitive = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "topoMap", "topoMapContent", "topoNodeDeclaration", "relationshipDeclaration", 
			"pathDeclaration", "directionDeclaration", "modifierDeclaration", "modifierText", 
			"expr", "primitive"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'TopoMap'", "'{'", "'}'", "'TopoNode'", "'('", "')'", "','", "'AtomicPath'", 
			"'Direction'", "'modifier'", "'TODO'", "'**'", "'*'", "'/'", "'%'", "'+'", 
			"'-'", "'if'", "'then'", "'else'", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "RCC", 
			"TPCC", "Int", "Dec", "Hex", "Oct", "Bin", "Float", "RegularStringLiteral", 
			"Identifier", "LineComment", "Whitespace"
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
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MapFileParser.EOF, 0); }
		public List<TopoMapContext> topoMap() {
			return getRuleContexts(TopoMapContext.class);
		}
		public TopoMapContext topoMap(int i) {
			return getRuleContext(TopoMapContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(22);
				topoMap();
				}
				}
				setState(27);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(28);
			match(EOF);
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
	public static class TopoMapContext extends ParserRuleContext {
		public Token name;
		public TerminalNode Identifier() { return getToken(MapFileParser.Identifier, 0); }
		public List<TopoMapContentContext> topoMapContent() {
			return getRuleContexts(TopoMapContentContext.class);
		}
		public TopoMapContentContext topoMapContent(int i) {
			return getRuleContext(TopoMapContentContext.class,i);
		}
		public TopoMapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topoMap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTopoMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTopoMap(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTopoMap(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopoMapContext topoMap() throws RecognitionException {
		TopoMapContext _localctx = new TopoMapContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_topoMap);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(T__0);
			setState(31);
			((TopoMapContext)_localctx).name = match(Identifier);
			setState(32);
			match(T__1);
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8598325008L) != 0)) {
				{
				{
				setState(33);
				topoMapContent();
				}
				}
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(39);
			match(T__2);
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
	public static class TopoMapContentContext extends ParserRuleContext {
		public TopoMapContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topoMapContent; }
	 
		public TopoMapContentContext() { }
		public void copyFrom(TopoMapContentContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ModifierContentContext extends TopoMapContentContext {
		public ModifierDeclarationContext modifierDeclaration() {
			return getRuleContext(ModifierDeclarationContext.class,0);
		}
		public ModifierContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterModifierContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitModifierContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitModifierContent(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DirectionContentContext extends TopoMapContentContext {
		public DirectionDeclarationContext directionDeclaration() {
			return getRuleContext(DirectionDeclarationContext.class,0);
		}
		public DirectionContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterDirectionContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitDirectionContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitDirectionContent(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NodeContentContext extends TopoMapContentContext {
		public TopoNodeDeclarationContext topoNodeDeclaration() {
			return getRuleContext(TopoNodeDeclarationContext.class,0);
		}
		public NodeContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterNodeContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitNodeContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitNodeContent(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathContentContext extends TopoMapContentContext {
		public PathDeclarationContext pathDeclaration() {
			return getRuleContext(PathDeclarationContext.class,0);
		}
		public PathContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPathContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPathContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPathContent(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CommentContentContext extends TopoMapContentContext {
		public TerminalNode LineComment() { return getToken(MapFileParser.LineComment, 0); }
		public CommentContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterCommentContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitCommentContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitCommentContent(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RelationshipContentContext extends TopoMapContentContext {
		public RelationshipDeclarationContext relationshipDeclaration() {
			return getRuleContext(RelationshipDeclarationContext.class,0);
		}
		public RelationshipContentContext(TopoMapContentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRelationshipContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRelationshipContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRelationshipContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopoMapContentContext topoMapContent() throws RecognitionException {
		TopoMapContentContext _localctx = new TopoMapContentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_topoMapContent);
		try {
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__3:
				_localctx = new NodeContentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(41);
				topoNodeDeclaration();
				}
				break;
			case RCC:
				_localctx = new RelationshipContentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(42);
				relationshipDeclaration();
				}
				break;
			case T__7:
				_localctx = new PathContentContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(43);
				pathDeclaration();
				}
				break;
			case T__8:
				_localctx = new DirectionContentContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(44);
				directionDeclaration();
				}
				break;
			case T__9:
				_localctx = new ModifierContentContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(45);
				modifierDeclaration();
				}
				break;
			case LineComment:
				_localctx = new CommentContentContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(46);
				match(LineComment);
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
	public static class TopoNodeDeclarationContext extends ParserRuleContext {
		public Token name;
		public TerminalNode Identifier() { return getToken(MapFileParser.Identifier, 0); }
		public TerminalNode LineComment() { return getToken(MapFileParser.LineComment, 0); }
		public TopoNodeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topoNodeDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterTopoNodeDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitTopoNodeDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitTopoNodeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopoNodeDeclarationContext topoNodeDeclaration() throws RecognitionException {
		TopoNodeDeclarationContext _localctx = new TopoNodeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_topoNodeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			match(T__3);
			setState(50);
			match(T__4);
			setState(51);
			((TopoNodeDeclarationContext)_localctx).name = match(Identifier);
			setState(52);
			match(T__5);
			setState(54);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(53);
				match(LineComment);
				}
				break;
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
	public static class RelationshipDeclarationContext extends ParserRuleContext {
		public Token relationType;
		public Token subject;
		public Token object;
		public TerminalNode RCC() { return getToken(MapFileParser.RCC, 0); }
		public List<TerminalNode> Identifier() { return getTokens(MapFileParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(MapFileParser.Identifier, i);
		}
		public TerminalNode LineComment() { return getToken(MapFileParser.LineComment, 0); }
		public RelationshipDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationshipDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterRelationshipDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitRelationshipDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitRelationshipDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationshipDeclarationContext relationshipDeclaration() throws RecognitionException {
		RelationshipDeclarationContext _localctx = new RelationshipDeclarationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_relationshipDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			((RelationshipDeclarationContext)_localctx).relationType = match(RCC);
			setState(57);
			match(T__4);
			setState(58);
			((RelationshipDeclarationContext)_localctx).subject = match(Identifier);
			setState(59);
			match(T__6);
			setState(60);
			((RelationshipDeclarationContext)_localctx).object = match(Identifier);
			setState(61);
			match(T__5);
			setState(63);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(62);
				match(LineComment);
				}
				break;
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
	public static class PathDeclarationContext extends ParserRuleContext {
		public Token from;
		public Token to;
		public ExprContext cost;
		public Token modifier;
		public List<TerminalNode> Identifier() { return getTokens(MapFileParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(MapFileParser.Identifier, i);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public PathDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPathDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPathDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPathDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathDeclarationContext pathDeclaration() throws RecognitionException {
		PathDeclarationContext _localctx = new PathDeclarationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_pathDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(T__7);
			setState(66);
			match(T__4);
			setState(67);
			((PathDeclarationContext)_localctx).from = match(Identifier);
			setState(68);
			match(T__6);
			setState(69);
			((PathDeclarationContext)_localctx).to = match(Identifier);
			setState(70);
			match(T__6);
			setState(71);
			((PathDeclarationContext)_localctx).cost = expr(0);
			setState(72);
			match(T__6);
			setState(73);
			((PathDeclarationContext)_localctx).modifier = match(Identifier);
			setState(74);
			match(T__5);
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
	public static class DirectionDeclarationContext extends ParserRuleContext {
		public Token from;
		public Token via;
		public Token to;
		public Token direction;
		public List<TerminalNode> Identifier() { return getTokens(MapFileParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(MapFileParser.Identifier, i);
		}
		public TerminalNode TPCC() { return getToken(MapFileParser.TPCC, 0); }
		public DirectionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterDirectionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitDirectionDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitDirectionDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectionDeclarationContext directionDeclaration() throws RecognitionException {
		DirectionDeclarationContext _localctx = new DirectionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_directionDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(T__8);
			setState(77);
			match(T__4);
			setState(78);
			((DirectionDeclarationContext)_localctx).from = match(Identifier);
			setState(79);
			match(T__6);
			setState(80);
			((DirectionDeclarationContext)_localctx).via = match(Identifier);
			setState(81);
			match(T__6);
			setState(82);
			((DirectionDeclarationContext)_localctx).to = match(Identifier);
			setState(83);
			match(T__6);
			setState(84);
			((DirectionDeclarationContext)_localctx).direction = match(TPCC);
			setState(85);
			match(T__5);
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
	public static class ModifierDeclarationContext extends ParserRuleContext {
		public Token name;
		public TerminalNode Identifier() { return getToken(MapFileParser.Identifier, 0); }
		public List<ModifierTextContext> modifierText() {
			return getRuleContexts(ModifierTextContext.class);
		}
		public ModifierTextContext modifierText(int i) {
			return getRuleContext(ModifierTextContext.class,i);
		}
		public ModifierDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifierDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterModifierDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitModifierDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitModifierDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierDeclarationContext modifierDeclaration() throws RecognitionException {
		ModifierDeclarationContext _localctx = new ModifierDeclarationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_modifierDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(T__9);
			setState(88);
			((ModifierDeclarationContext)_localctx).name = match(Identifier);
			setState(89);
			match(T__1);
			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10) {
				{
				{
				setState(90);
				modifierText();
				}
				}
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(96);
			match(T__2);
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
	public static class ModifierTextContext extends ParserRuleContext {
		public ModifierTextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifierText; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterModifierText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitModifierText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitModifierText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierTextContext modifierText() throws RecognitionException {
		ModifierTextContext _localctx = new ModifierTextContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_modifierText);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(T__10);
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
	public static class ExprFnCallContext extends ExprContext {
		public Token fn;
		public ExprContext expr;
		public List<ExprContext> args = new ArrayList<ExprContext>();
		public TerminalNode Identifier() { return getToken(MapFileParser.Identifier, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprFnCallContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprFnCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprFnCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprFnCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprMulContext extends ExprContext {
		public ExprContext lhs;
		public Token op;
		public ExprContext rhs;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprMulContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprMul(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprMul(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprMul(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprAddContext extends ExprContext {
		public ExprContext lhs;
		public Token op;
		public ExprContext rhs;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprAddContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprAdd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprAdd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprAdd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprIdentifierContext extends ExprContext {
		public Token ident;
		public TerminalNode Identifier() { return getToken(MapFileParser.Identifier, 0); }
		public ExprIdentifierContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprPowContext extends ExprContext {
		public ExprContext lhs;
		public ExprContext rhs;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprPowContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprPow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprPow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprPow(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprIfContext extends ExprContext {
		public ExprContext cond;
		public ExprContext thenExpr;
		public ExprContext elseExpr;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprIfContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprIf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprIf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprIf(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprPrimitiveContext extends ExprContext {
		public PrimitiveContext primitive() {
			return getRuleContext(PrimitiveContext.class,0);
		}
		public ExprPrimitiveContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprPrimitive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprPrimitive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprPrimitive(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprParanContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprParanContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterExprParan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitExprParan(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitExprParan(this);
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
		int _startState = 18;
		enterRecursionRule(_localctx, 18, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				_localctx = new ExprPrimitiveContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(101);
				primitive();
				}
				break;
			case 2:
				{
				_localctx = new ExprIdentifierContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(102);
				((ExprIdentifierContext)_localctx).ident = match(Identifier);
				}
				break;
			case 3:
				{
				_localctx = new ExprParanContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(103);
				match(T__4);
				setState(104);
				expr(0);
				setState(105);
				match(T__5);
				}
				break;
			case 4:
				{
				_localctx = new ExprFnCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(107);
				((ExprFnCallContext)_localctx).fn = match(Identifier);
				setState(108);
				match(T__4);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7556300832L) != 0)) {
					{
					setState(109);
					((ExprFnCallContext)_localctx).expr = expr(0);
					((ExprFnCallContext)_localctx).args.add(((ExprFnCallContext)_localctx).expr);
					setState(114);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__6) {
						{
						{
						setState(110);
						match(T__6);
						setState(111);
						((ExprFnCallContext)_localctx).expr = expr(0);
						((ExprFnCallContext)_localctx).args.add(((ExprFnCallContext)_localctx).expr);
						}
						}
						setState(116);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(119);
				match(T__5);
				}
				break;
			case 5:
				{
				_localctx = new ExprIfContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(120);
				match(T__17);
				setState(121);
				((ExprIfContext)_localctx).cond = expr(0);
				setState(122);
				match(T__18);
				setState(123);
				((ExprIfContext)_localctx).thenExpr = expr(0);
				setState(124);
				match(T__19);
				setState(125);
				((ExprIfContext)_localctx).elseExpr = expr(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(138);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new ExprPowContext(new ExprContext(_parentctx, _parentState));
						((ExprPowContext)_localctx).lhs = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(129);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						{
						setState(130);
						match(T__11);
						}
						setState(131);
						((ExprPowContext)_localctx).rhs = expr(5);
						}
						break;
					case 2:
						{
						_localctx = new ExprMulContext(new ExprContext(_parentctx, _parentState));
						((ExprMulContext)_localctx).lhs = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(132);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(133);
						((ExprMulContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 57344L) != 0)) ) {
							((ExprMulContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(134);
						((ExprMulContext)_localctx).rhs = expr(4);
						}
						break;
					case 3:
						{
						_localctx = new ExprAddContext(new ExprContext(_parentctx, _parentState));
						((ExprAddContext)_localctx).lhs = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(135);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(136);
						((ExprAddContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__15 || _la==T__16) ) {
							((ExprAddContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(137);
						((ExprAddContext)_localctx).rhs = expr(3);
						}
						break;
					}
					} 
				}
				setState(142);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
	public static class PrimitiveContext extends ParserRuleContext {
		public PrimitiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitive; }
	 
		public PrimitiveContext() { }
		public void copyFrom(PrimitiveContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveBoolContext extends PrimitiveContext {
		public PrimitiveBoolContext(PrimitiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPrimitiveBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPrimitiveBool(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPrimitiveBool(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveIntContext extends PrimitiveContext {
		public TerminalNode Int() { return getToken(MapFileParser.Int, 0); }
		public PrimitiveIntContext(PrimitiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPrimitiveInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPrimitiveInt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPrimitiveInt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveFloatContext extends PrimitiveContext {
		public TerminalNode Float() { return getToken(MapFileParser.Float, 0); }
		public PrimitiveFloatContext(PrimitiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPrimitiveFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPrimitiveFloat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPrimitiveFloat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveStringContext extends PrimitiveContext {
		public TerminalNode RegularStringLiteral() { return getToken(MapFileParser.RegularStringLiteral, 0); }
		public PrimitiveStringContext(PrimitiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).enterPrimitiveString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MapFileListener ) ((MapFileListener)listener).exitPrimitiveString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MapFileVisitor ) return ((MapFileVisitor<? extends T>)visitor).visitPrimitiveString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveContext primitive() throws RecognitionException {
		PrimitiveContext _localctx = new PrimitiveContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_primitive);
		int _la;
		try {
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Int:
				_localctx = new PrimitiveIntContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(143);
				match(Int);
				}
				break;
			case T__20:
			case T__21:
				_localctx = new PrimitiveBoolContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(144);
				_la = _input.LA(1);
				if ( !(_la==T__20 || _la==T__21) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case Float:
				_localctx = new PrimitiveFloatContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(145);
				match(Float);
				}
				break;
			case RegularStringLiteral:
				_localctx = new PrimitiveStringContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(146);
				match(RegularStringLiteral);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 9:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\"\u0096\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0005\u0000\u0018"+
		"\b\u0000\n\u0000\f\u0000\u001b\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001#\b\u0001\n\u0001\f\u0001"+
		"&\t\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u00020\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u00037\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u0004@\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007"+
		"\\\b\u0007\n\u0007\f\u0007_\t\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0005\tq\b\t\n\t\f\tt\t\t\u0003\tv\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0080"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0005\t\u008b\b\t\n\t\f\t\u008e\t\t\u0001\n\u0001\n\u0001\n\u0001\n"+
		"\u0003\n\u0094\b\n\u0001\n\u0000\u0001\u0012\u000b\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0000\u0003\u0001\u0000\r\u000f\u0001\u0000"+
		"\u0010\u0011\u0001\u0000\u0015\u0016\u00a0\u0000\u0019\u0001\u0000\u0000"+
		"\u0000\u0002\u001e\u0001\u0000\u0000\u0000\u0004/\u0001\u0000\u0000\u0000"+
		"\u00061\u0001\u0000\u0000\u0000\b8\u0001\u0000\u0000\u0000\nA\u0001\u0000"+
		"\u0000\u0000\fL\u0001\u0000\u0000\u0000\u000eW\u0001\u0000\u0000\u0000"+
		"\u0010b\u0001\u0000\u0000\u0000\u0012\u007f\u0001\u0000\u0000\u0000\u0014"+
		"\u0093\u0001\u0000\u0000\u0000\u0016\u0018\u0003\u0002\u0001\u0000\u0017"+
		"\u0016\u0001\u0000\u0000\u0000\u0018\u001b\u0001\u0000\u0000\u0000\u0019"+
		"\u0017\u0001\u0000\u0000\u0000\u0019\u001a\u0001\u0000\u0000\u0000\u001a"+
		"\u001c\u0001\u0000\u0000\u0000\u001b\u0019\u0001\u0000\u0000\u0000\u001c"+
		"\u001d\u0005\u0000\u0000\u0001\u001d\u0001\u0001\u0000\u0000\u0000\u001e"+
		"\u001f\u0005\u0001\u0000\u0000\u001f \u0005 \u0000\u0000 $\u0005\u0002"+
		"\u0000\u0000!#\u0003\u0004\u0002\u0000\"!\u0001\u0000\u0000\u0000#&\u0001"+
		"\u0000\u0000\u0000$\"\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000"+
		"%\'\u0001\u0000\u0000\u0000&$\u0001\u0000\u0000\u0000\'(\u0005\u0003\u0000"+
		"\u0000(\u0003\u0001\u0000\u0000\u0000)0\u0003\u0006\u0003\u0000*0\u0003"+
		"\b\u0004\u0000+0\u0003\n\u0005\u0000,0\u0003\f\u0006\u0000-0\u0003\u000e"+
		"\u0007\u0000.0\u0005!\u0000\u0000/)\u0001\u0000\u0000\u0000/*\u0001\u0000"+
		"\u0000\u0000/+\u0001\u0000\u0000\u0000/,\u0001\u0000\u0000\u0000/-\u0001"+
		"\u0000\u0000\u0000/.\u0001\u0000\u0000\u00000\u0005\u0001\u0000\u0000"+
		"\u000012\u0005\u0004\u0000\u000023\u0005\u0005\u0000\u000034\u0005 \u0000"+
		"\u000046\u0005\u0006\u0000\u000057\u0005!\u0000\u000065\u0001\u0000\u0000"+
		"\u000067\u0001\u0000\u0000\u00007\u0007\u0001\u0000\u0000\u000089\u0005"+
		"\u0017\u0000\u00009:\u0005\u0005\u0000\u0000:;\u0005 \u0000\u0000;<\u0005"+
		"\u0007\u0000\u0000<=\u0005 \u0000\u0000=?\u0005\u0006\u0000\u0000>@\u0005"+
		"!\u0000\u0000?>\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@\t\u0001"+
		"\u0000\u0000\u0000AB\u0005\b\u0000\u0000BC\u0005\u0005\u0000\u0000CD\u0005"+
		" \u0000\u0000DE\u0005\u0007\u0000\u0000EF\u0005 \u0000\u0000FG\u0005\u0007"+
		"\u0000\u0000GH\u0003\u0012\t\u0000HI\u0005\u0007\u0000\u0000IJ\u0005 "+
		"\u0000\u0000JK\u0005\u0006\u0000\u0000K\u000b\u0001\u0000\u0000\u0000"+
		"LM\u0005\t\u0000\u0000MN\u0005\u0005\u0000\u0000NO\u0005 \u0000\u0000"+
		"OP\u0005\u0007\u0000\u0000PQ\u0005 \u0000\u0000QR\u0005\u0007\u0000\u0000"+
		"RS\u0005 \u0000\u0000ST\u0005\u0007\u0000\u0000TU\u0005\u0018\u0000\u0000"+
		"UV\u0005\u0006\u0000\u0000V\r\u0001\u0000\u0000\u0000WX\u0005\n\u0000"+
		"\u0000XY\u0005 \u0000\u0000Y]\u0005\u0002\u0000\u0000Z\\\u0003\u0010\b"+
		"\u0000[Z\u0001\u0000\u0000\u0000\\_\u0001\u0000\u0000\u0000][\u0001\u0000"+
		"\u0000\u0000]^\u0001\u0000\u0000\u0000^`\u0001\u0000\u0000\u0000_]\u0001"+
		"\u0000\u0000\u0000`a\u0005\u0003\u0000\u0000a\u000f\u0001\u0000\u0000"+
		"\u0000bc\u0005\u000b\u0000\u0000c\u0011\u0001\u0000\u0000\u0000de\u0006"+
		"\t\uffff\uffff\u0000e\u0080\u0003\u0014\n\u0000f\u0080\u0005 \u0000\u0000"+
		"gh\u0005\u0005\u0000\u0000hi\u0003\u0012\t\u0000ij\u0005\u0006\u0000\u0000"+
		"j\u0080\u0001\u0000\u0000\u0000kl\u0005 \u0000\u0000lu\u0005\u0005\u0000"+
		"\u0000mr\u0003\u0012\t\u0000no\u0005\u0007\u0000\u0000oq\u0003\u0012\t"+
		"\u0000pn\u0001\u0000\u0000\u0000qt\u0001\u0000\u0000\u0000rp\u0001\u0000"+
		"\u0000\u0000rs\u0001\u0000\u0000\u0000sv\u0001\u0000\u0000\u0000tr\u0001"+
		"\u0000\u0000\u0000um\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000"+
		"vw\u0001\u0000\u0000\u0000w\u0080\u0005\u0006\u0000\u0000xy\u0005\u0012"+
		"\u0000\u0000yz\u0003\u0012\t\u0000z{\u0005\u0013\u0000\u0000{|\u0003\u0012"+
		"\t\u0000|}\u0005\u0014\u0000\u0000}~\u0003\u0012\t\u0001~\u0080\u0001"+
		"\u0000\u0000\u0000\u007fd\u0001\u0000\u0000\u0000\u007ff\u0001\u0000\u0000"+
		"\u0000\u007fg\u0001\u0000\u0000\u0000\u007fk\u0001\u0000\u0000\u0000\u007f"+
		"x\u0001\u0000\u0000\u0000\u0080\u008c\u0001\u0000\u0000\u0000\u0081\u0082"+
		"\n\u0004\u0000\u0000\u0082\u0083\u0005\f\u0000\u0000\u0083\u008b\u0003"+
		"\u0012\t\u0005\u0084\u0085\n\u0003\u0000\u0000\u0085\u0086\u0007\u0000"+
		"\u0000\u0000\u0086\u008b\u0003\u0012\t\u0004\u0087\u0088\n\u0002\u0000"+
		"\u0000\u0088\u0089\u0007\u0001\u0000\u0000\u0089\u008b\u0003\u0012\t\u0003"+
		"\u008a\u0081\u0001\u0000\u0000\u0000\u008a\u0084\u0001\u0000\u0000\u0000"+
		"\u008a\u0087\u0001\u0000\u0000\u0000\u008b\u008e\u0001\u0000\u0000\u0000"+
		"\u008c\u008a\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000"+
		"\u008d\u0013\u0001\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000\u0000"+
		"\u008f\u0094\u0005\u0019\u0000\u0000\u0090\u0094\u0007\u0002\u0000\u0000"+
		"\u0091\u0094\u0005\u001e\u0000\u0000\u0092\u0094\u0005\u001f\u0000\u0000"+
		"\u0093\u008f\u0001\u0000\u0000\u0000\u0093\u0090\u0001\u0000\u0000\u0000"+
		"\u0093\u0091\u0001\u0000\u0000\u0000\u0093\u0092\u0001\u0000\u0000\u0000"+
		"\u0094\u0015\u0001\u0000\u0000\u0000\f\u0019$/6?]ru\u007f\u008a\u008c"+
		"\u0093";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}