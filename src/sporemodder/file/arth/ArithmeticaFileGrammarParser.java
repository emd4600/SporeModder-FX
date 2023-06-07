// Generated from E:\Eric\Eclipse Projects\SporeModder FX\src\sporemodder\file\arth\ArithmeticaFileGrammar.g4 by ANTLR 4.13.0
package sporemodder.file.arth;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ArithmeticaFileGrammarParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, IF=4, ELSE=5, ENDIF=6, AND=7, OR=8, DEF=9, ENDDEF=10, 
		RAND=11, POW=12, ADD=13, SUB=14, MULT=15, DIV=16, EQ=17, NEQ=18, LESS=19, 
		GREATER=20, LEQ=21, GEQ=22, IDENTIFIER=23, FLOATING_LITERAL=24, NEWLINE=25, 
		LINE_COMMENT=26, WS=27;
	public static final int
		RULE_program = 0, RULE_function = 1, RULE_statement = 2, RULE_ifFragment = 3, 
		RULE_elseFragment = 4, RULE_endifFragment = 5, RULE_conditionalExpr = 6, 
		RULE_expr = 7;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "function", "statement", "ifFragment", "elseFragment", "endifFragment", 
			"conditionalExpr", "expr"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'='", "'if'", "'else'", "'endif'", "'and'", "'or'", 
			"'def'", "'enddef'", "'RAND'", "'^'", "'+'", "'-'", "'*'", "'/'", null, 
			"'!='", "'<'", "'>'", "'<='", "'>='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "IF", "ELSE", "ENDIF", "AND", "OR", "DEF", "ENDDEF", 
			"RAND", "POW", "ADD", "SUB", "MULT", "DIV", "EQ", "NEQ", "LESS", "GREATER", 
			"LEQ", "GEQ", "IDENTIFIER", "FLOATING_LITERAL", "NEWLINE", "LINE_COMMENT", 
			"WS"
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
	public String getGrammarFileName() { return "ArithmeticaFileGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ArithmeticaFileGrammarParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public List<FunctionContext> function() {
			return getRuleContexts(FunctionContext.class);
		}
		public FunctionContext function(int i) {
			return getRuleContext(FunctionContext.class,i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitProgram(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 41943568L) != 0)) {
				{
				{
				setState(19);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(16);
					match(NEWLINE);
					}
					}
					setState(21);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(24);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case DEF:
					{
					setState(22);
					function();
					}
					break;
				case IF:
				case IDENTIFIER:
					{
					setState(23);
					statement();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(30);
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
	public static class FunctionContext extends ParserRuleContext {
		public TerminalNode DEF() { return getToken(ArithmeticaFileGrammarParser.DEF, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ArithmeticaFileGrammarParser.IDENTIFIER, 0); }
		public TerminalNode ENDDEF() { return getToken(ArithmeticaFileGrammarParser.ENDDEF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_function);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(31);
			match(DEF);
			setState(32);
			match(IDENTIFIER);
			setState(33);
			match(T__0);
			setState(34);
			match(T__1);
			setState(36); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(35);
				match(NEWLINE);
				}
				}
				setState(38); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IF || _la==IDENTIFIER) {
				{
				{
				setState(40);
				statement();
				}
				}
				setState(45);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(46);
			match(ENDDEF);
			setState(48); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(47);
					match(NEWLINE);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(50); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class StatementContext extends ParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionalStatementContext extends StatementContext {
		public IfFragmentContext ifFragment() {
			return getRuleContext(IfFragmentContext.class,0);
		}
		public EndifFragmentContext endifFragment() {
			return getRuleContext(EndifFragmentContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public ElseFragmentContext elseFragment() {
			return getRuleContext(ElseFragmentContext.class,0);
		}
		public ConditionalStatementContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterConditionalStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitConditionalStatement(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignStatementContext extends StatementContext {
		public TerminalNode IDENTIFIER() { return getToken(ArithmeticaFileGrammarParser.IDENTIFIER, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArithmeticaFileGrammarParser.EOF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public AssignStatementContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterAssignStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitAssignStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		int _la;
		try {
			int _alt;
			setState(81);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				_localctx = new ConditionalStatementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(52);
				ifFragment();
				setState(56);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==IF || _la==IDENTIFIER) {
					{
					{
					setState(53);
					statement();
					}
					}
					setState(58);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(66);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(59);
					elseFragment();
					setState(63);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==IF || _la==IDENTIFIER) {
						{
						{
						setState(60);
						statement();
						}
						}
						setState(65);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(68);
				endifFragment();
				}
				break;
			case IDENTIFIER:
				_localctx = new AssignStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(70);
				match(IDENTIFIER);
				setState(71);
				match(T__2);
				setState(72);
				expr(0);
				setState(79);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case NEWLINE:
					{
					setState(74); 
					_errHandler.sync(this);
					_alt = 1;
					do {
						switch (_alt) {
						case 1:
							{
							{
							setState(73);
							match(NEWLINE);
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(76); 
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
					} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
					}
					break;
				case EOF:
					{
					setState(78);
					match(EOF);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
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
	public static class IfFragmentContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(ArithmeticaFileGrammarParser.IF, 0); }
		public ConditionalExprContext conditionalExpr() {
			return getRuleContext(ConditionalExprContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public IfFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterIfFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitIfFragment(this);
		}
	}

	public final IfFragmentContext ifFragment() throws RecognitionException {
		IfFragmentContext _localctx = new IfFragmentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_ifFragment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(IF);
			setState(84);
			match(T__0);
			setState(85);
			conditionalExpr();
			setState(86);
			match(T__1);
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(87);
				match(NEWLINE);
				}
				}
				setState(92);
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
	public static class ElseFragmentContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(ArithmeticaFileGrammarParser.ELSE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public ElseFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterElseFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitElseFragment(this);
		}
	}

	public final ElseFragmentContext elseFragment() throws RecognitionException {
		ElseFragmentContext _localctx = new ElseFragmentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_elseFragment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(ELSE);
			setState(97);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(94);
				match(NEWLINE);
				}
				}
				setState(99);
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
	public static class EndifFragmentContext extends ParserRuleContext {
		public TerminalNode ENDIF() { return getToken(ArithmeticaFileGrammarParser.ENDIF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(ArithmeticaFileGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArithmeticaFileGrammarParser.NEWLINE, i);
		}
		public EndifFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_endifFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterEndifFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitEndifFragment(this);
		}
	}

	public final EndifFragmentContext endifFragment() throws RecognitionException {
		EndifFragmentContext _localctx = new EndifFragmentContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_endifFragment);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(ENDIF);
			setState(104);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(101);
					match(NEWLINE);
					}
					} 
				}
				setState(106);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
	public static class ConditionalExprContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode AND() { return getToken(ArithmeticaFileGrammarParser.AND, 0); }
		public TerminalNode OR() { return getToken(ArithmeticaFileGrammarParser.OR, 0); }
		public ConditionalExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterConditionalExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitConditionalExpr(this);
		}
	}

	public final ConditionalExprContext conditionalExpr() throws RecognitionException {
		ConditionalExprContext _localctx = new ConditionalExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_conditionalExpr);
		int _la;
		try {
			setState(112);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				expr(0);
				setState(108);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(109);
				expr(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
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
	public static class CallContext extends ExprContext {
		public TerminalNode IDENTIFIER() { return getToken(ArithmeticaFileGrammarParser.IDENTIFIER, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CallContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitCall(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesisContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArithmeticaFileGrammarParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(ArithmeticaFileGrammarParser.SUB, 0); }
		public ParenthesisContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitParenthesis(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparativeOpContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode EQ() { return getToken(ArithmeticaFileGrammarParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(ArithmeticaFileGrammarParser.NEQ, 0); }
		public TerminalNode LESS() { return getToken(ArithmeticaFileGrammarParser.LESS, 0); }
		public TerminalNode GREATER() { return getToken(ArithmeticaFileGrammarParser.GREATER, 0); }
		public TerminalNode LEQ() { return getToken(ArithmeticaFileGrammarParser.LEQ, 0); }
		public TerminalNode GEQ() { return getToken(ArithmeticaFileGrammarParser.GEQ, 0); }
		public ComparativeOpContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterComparativeOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitComparativeOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VariableContext extends ExprContext {
		public TerminalNode IDENTIFIER() { return getToken(ArithmeticaFileGrammarParser.IDENTIFIER, 0); }
		public TerminalNode ADD() { return getToken(ArithmeticaFileGrammarParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(ArithmeticaFileGrammarParser.SUB, 0); }
		public VariableContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitVariable(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RandomContext extends ExprContext {
		public TerminalNode RAND() { return getToken(ArithmeticaFileGrammarParser.RAND, 0); }
		public RandomContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterRandom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitRandom(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberContext extends ExprContext {
		public TerminalNode FLOATING_LITERAL() { return getToken(ArithmeticaFileGrammarParser.FLOATING_LITERAL, 0); }
		public NumberContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitNumber(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeOpContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode MULT() { return getToken(ArithmeticaFileGrammarParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(ArithmeticaFileGrammarParser.DIV, 0); }
		public MultiplicativeOpContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterMultiplicativeOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitMultiplicativeOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveOpContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ADD() { return getToken(ArithmeticaFileGrammarParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(ArithmeticaFileGrammarParser.SUB, 0); }
		public AdditiveOpContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterAdditiveOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitAdditiveOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PowerOpContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode POW() { return getToken(ArithmeticaFileGrammarParser.POW, 0); }
		public PowerOpContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).enterPowerOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArithmeticaFileGrammarListener ) ((ArithmeticaFileGrammarListener)listener).exitPowerOp(this);
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
		int _startState = 14;
		enterRecursionRule(_localctx, 14, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				_localctx = new RandomContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(115);
				match(RAND);
				}
				break;
			case 2:
				{
				_localctx = new VariableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==SUB) {
					{
					setState(116);
					_la = _input.LA(1);
					if ( !(_la==ADD || _la==SUB) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(119);
				match(IDENTIFIER);
				}
				break;
			case 3:
				{
				_localctx = new NumberContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(120);
				match(FLOATING_LITERAL);
				}
				break;
			case 4:
				{
				_localctx = new CallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(121);
				match(IDENTIFIER);
				setState(122);
				match(T__0);
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 25192450L) != 0)) {
					{
					setState(123);
					expr(0);
					}
				}

				setState(126);
				match(T__1);
				}
				break;
			case 5:
				{
				_localctx = new ParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(128);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==SUB) {
					{
					setState(127);
					_la = _input.LA(1);
					if ( !(_la==ADD || _la==SUB) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(130);
				match(T__0);
				setState(131);
				expr(0);
				setState(132);
				match(T__1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(150);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(148);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
					case 1:
						{
						_localctx = new PowerOpContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(136);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(137);
						match(POW);
						setState(138);
						expr(6);
						}
						break;
					case 2:
						{
						_localctx = new MultiplicativeOpContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(139);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(140);
						_la = _input.LA(1);
						if ( !(_la==MULT || _la==DIV) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(141);
						expr(5);
						}
						break;
					case 3:
						{
						_localctx = new AdditiveOpContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(142);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(143);
						_la = _input.LA(1);
						if ( !(_la==ADD || _la==SUB) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(144);
						expr(4);
						}
						break;
					case 4:
						{
						_localctx = new ComparativeOpContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(145);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(146);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8257536L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(147);
						expr(3);
						}
						break;
					}
					} 
				}
				setState(152);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 7:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		case 3:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001b\u009a\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0001\u0000\u0005\u0000\u0012\b\u0000\n\u0000\f\u0000\u0015\t\u0000\u0001"+
		"\u0000\u0001\u0000\u0003\u0000\u0019\b\u0000\u0005\u0000\u001b\b\u0000"+
		"\n\u0000\f\u0000\u001e\t\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0004\u0001%\b\u0001\u000b\u0001\f\u0001&\u0001\u0001"+
		"\u0005\u0001*\b\u0001\n\u0001\f\u0001-\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0004\u00011\b\u0001\u000b\u0001\f\u00012\u0001\u0002\u0001\u0002\u0005"+
		"\u00027\b\u0002\n\u0002\f\u0002:\t\u0002\u0001\u0002\u0001\u0002\u0005"+
		"\u0002>\b\u0002\n\u0002\f\u0002A\t\u0002\u0003\u0002C\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0004\u0002"+
		"K\b\u0002\u000b\u0002\f\u0002L\u0001\u0002\u0003\u0002P\b\u0002\u0003"+
		"\u0002R\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003Y\b\u0003\n\u0003\f\u0003\\\t\u0003\u0001\u0004\u0001"+
		"\u0004\u0005\u0004`\b\u0004\n\u0004\f\u0004c\t\u0004\u0001\u0005\u0001"+
		"\u0005\u0005\u0005g\b\u0005\n\u0005\f\u0005j\t\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006q\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007v\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007}\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0081\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0087\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0095\b\u0007\n"+
		"\u0007\f\u0007\u0098\t\u0007\u0001\u0007\u0000\u0001\u000e\b\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0000\u0004\u0001\u0000\u0007\b\u0001\u0000\r"+
		"\u000e\u0001\u0000\u000f\u0010\u0001\u0000\u0011\u0016\u00ac\u0000\u001c"+
		"\u0001\u0000\u0000\u0000\u0002\u001f\u0001\u0000\u0000\u0000\u0004Q\u0001"+
		"\u0000\u0000\u0000\u0006S\u0001\u0000\u0000\u0000\b]\u0001\u0000\u0000"+
		"\u0000\nd\u0001\u0000\u0000\u0000\fp\u0001\u0000\u0000\u0000\u000e\u0086"+
		"\u0001\u0000\u0000\u0000\u0010\u0012\u0005\u0019\u0000\u0000\u0011\u0010"+
		"\u0001\u0000\u0000\u0000\u0012\u0015\u0001\u0000\u0000\u0000\u0013\u0011"+
		"\u0001\u0000\u0000\u0000\u0013\u0014\u0001\u0000\u0000\u0000\u0014\u0018"+
		"\u0001\u0000\u0000\u0000\u0015\u0013\u0001\u0000\u0000\u0000\u0016\u0019"+
		"\u0003\u0002\u0001\u0000\u0017\u0019\u0003\u0004\u0002\u0000\u0018\u0016"+
		"\u0001\u0000\u0000\u0000\u0018\u0017\u0001\u0000\u0000\u0000\u0019\u001b"+
		"\u0001\u0000\u0000\u0000\u001a\u0013\u0001\u0000\u0000\u0000\u001b\u001e"+
		"\u0001\u0000\u0000\u0000\u001c\u001a\u0001\u0000\u0000\u0000\u001c\u001d"+
		"\u0001\u0000\u0000\u0000\u001d\u0001\u0001\u0000\u0000\u0000\u001e\u001c"+
		"\u0001\u0000\u0000\u0000\u001f \u0005\t\u0000\u0000 !\u0005\u0017\u0000"+
		"\u0000!\"\u0005\u0001\u0000\u0000\"$\u0005\u0002\u0000\u0000#%\u0005\u0019"+
		"\u0000\u0000$#\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000&$\u0001"+
		"\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000\'+\u0001\u0000\u0000\u0000"+
		"(*\u0003\u0004\u0002\u0000)(\u0001\u0000\u0000\u0000*-\u0001\u0000\u0000"+
		"\u0000+)\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000,.\u0001\u0000"+
		"\u0000\u0000-+\u0001\u0000\u0000\u0000.0\u0005\n\u0000\u0000/1\u0005\u0019"+
		"\u0000\u00000/\u0001\u0000\u0000\u000012\u0001\u0000\u0000\u000020\u0001"+
		"\u0000\u0000\u000023\u0001\u0000\u0000\u00003\u0003\u0001\u0000\u0000"+
		"\u000048\u0003\u0006\u0003\u000057\u0003\u0004\u0002\u000065\u0001\u0000"+
		"\u0000\u00007:\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u000089\u0001"+
		"\u0000\u0000\u00009B\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000"+
		";?\u0003\b\u0004\u0000<>\u0003\u0004\u0002\u0000=<\u0001\u0000\u0000\u0000"+
		">A\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000"+
		"\u0000@C\u0001\u0000\u0000\u0000A?\u0001\u0000\u0000\u0000B;\u0001\u0000"+
		"\u0000\u0000BC\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DE\u0003"+
		"\n\u0005\u0000ER\u0001\u0000\u0000\u0000FG\u0005\u0017\u0000\u0000GH\u0005"+
		"\u0003\u0000\u0000HO\u0003\u000e\u0007\u0000IK\u0005\u0019\u0000\u0000"+
		"JI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LJ\u0001\u0000\u0000"+
		"\u0000LM\u0001\u0000\u0000\u0000MP\u0001\u0000\u0000\u0000NP\u0005\u0000"+
		"\u0000\u0001OJ\u0001\u0000\u0000\u0000ON\u0001\u0000\u0000\u0000PR\u0001"+
		"\u0000\u0000\u0000Q4\u0001\u0000\u0000\u0000QF\u0001\u0000\u0000\u0000"+
		"R\u0005\u0001\u0000\u0000\u0000ST\u0005\u0004\u0000\u0000TU\u0005\u0001"+
		"\u0000\u0000UV\u0003\f\u0006\u0000VZ\u0005\u0002\u0000\u0000WY\u0005\u0019"+
		"\u0000\u0000XW\u0001\u0000\u0000\u0000Y\\\u0001\u0000\u0000\u0000ZX\u0001"+
		"\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\u0007\u0001\u0000\u0000"+
		"\u0000\\Z\u0001\u0000\u0000\u0000]a\u0005\u0005\u0000\u0000^`\u0005\u0019"+
		"\u0000\u0000_^\u0001\u0000\u0000\u0000`c\u0001\u0000\u0000\u0000a_\u0001"+
		"\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b\t\u0001\u0000\u0000\u0000"+
		"ca\u0001\u0000\u0000\u0000dh\u0005\u0006\u0000\u0000eg\u0005\u0019\u0000"+
		"\u0000fe\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000\u0000hf\u0001\u0000"+
		"\u0000\u0000hi\u0001\u0000\u0000\u0000i\u000b\u0001\u0000\u0000\u0000"+
		"jh\u0001\u0000\u0000\u0000kl\u0003\u000e\u0007\u0000lm\u0007\u0000\u0000"+
		"\u0000mn\u0003\u000e\u0007\u0000nq\u0001\u0000\u0000\u0000oq\u0003\u000e"+
		"\u0007\u0000pk\u0001\u0000\u0000\u0000po\u0001\u0000\u0000\u0000q\r\u0001"+
		"\u0000\u0000\u0000rs\u0006\u0007\uffff\uffff\u0000s\u0087\u0005\u000b"+
		"\u0000\u0000tv\u0007\u0001\u0000\u0000ut\u0001\u0000\u0000\u0000uv\u0001"+
		"\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000w\u0087\u0005\u0017\u0000"+
		"\u0000x\u0087\u0005\u0018\u0000\u0000yz\u0005\u0017\u0000\u0000z|\u0005"+
		"\u0001\u0000\u0000{}\u0003\u000e\u0007\u0000|{\u0001\u0000\u0000\u0000"+
		"|}\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~\u0087\u0005\u0002"+
		"\u0000\u0000\u007f\u0081\u0007\u0001\u0000\u0000\u0080\u007f\u0001\u0000"+
		"\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000"+
		"\u0000\u0000\u0082\u0083\u0005\u0001\u0000\u0000\u0083\u0084\u0003\u000e"+
		"\u0007\u0000\u0084\u0085\u0005\u0002\u0000\u0000\u0085\u0087\u0001\u0000"+
		"\u0000\u0000\u0086r\u0001\u0000\u0000\u0000\u0086u\u0001\u0000\u0000\u0000"+
		"\u0086x\u0001\u0000\u0000\u0000\u0086y\u0001\u0000\u0000\u0000\u0086\u0080"+
		"\u0001\u0000\u0000\u0000\u0087\u0096\u0001\u0000\u0000\u0000\u0088\u0089"+
		"\n\u0005\u0000\u0000\u0089\u008a\u0005\f\u0000\u0000\u008a\u0095\u0003"+
		"\u000e\u0007\u0006\u008b\u008c\n\u0004\u0000\u0000\u008c\u008d\u0007\u0002"+
		"\u0000\u0000\u008d\u0095\u0003\u000e\u0007\u0005\u008e\u008f\n\u0003\u0000"+
		"\u0000\u008f\u0090\u0007\u0001\u0000\u0000\u0090\u0095\u0003\u000e\u0007"+
		"\u0004\u0091\u0092\n\u0002\u0000\u0000\u0092\u0093\u0007\u0003\u0000\u0000"+
		"\u0093\u0095\u0003\u000e\u0007\u0003\u0094\u0088\u0001\u0000\u0000\u0000"+
		"\u0094\u008b\u0001\u0000\u0000\u0000\u0094\u008e\u0001\u0000\u0000\u0000"+
		"\u0094\u0091\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000\u0000\u0000"+
		"\u0096\u0094\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000\u0000\u0000"+
		"\u0097\u000f\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000"+
		"\u0016\u0013\u0018\u001c&+28?BLOQZahpu|\u0080\u0086\u0094\u0096";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}