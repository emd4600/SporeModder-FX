// Generated from E:\Eric\Eclipse Projects\SporeModder FX\src\sporemodder\file\arth\ArithmeticaFileGrammar.g4 by ANTLR 4.13.0
package sporemodder.file.arth;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ArithmeticaFileGrammarParser}.
 */
public interface ArithmeticaFileGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(ArithmeticaFileGrammarParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(ArithmeticaFileGrammarParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(ArithmeticaFileGrammarParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(ArithmeticaFileGrammarParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ConditionalStatement}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterConditionalStatement(ArithmeticaFileGrammarParser.ConditionalStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ConditionalStatement}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitConditionalStatement(ArithmeticaFileGrammarParser.ConditionalStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignStatement}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAssignStatement(ArithmeticaFileGrammarParser.AssignStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignStatement}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAssignStatement(ArithmeticaFileGrammarParser.AssignStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#ifFragment}.
	 * @param ctx the parse tree
	 */
	void enterIfFragment(ArithmeticaFileGrammarParser.IfFragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#ifFragment}.
	 * @param ctx the parse tree
	 */
	void exitIfFragment(ArithmeticaFileGrammarParser.IfFragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#elseFragment}.
	 * @param ctx the parse tree
	 */
	void enterElseFragment(ArithmeticaFileGrammarParser.ElseFragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#elseFragment}.
	 * @param ctx the parse tree
	 */
	void exitElseFragment(ArithmeticaFileGrammarParser.ElseFragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#endifFragment}.
	 * @param ctx the parse tree
	 */
	void enterEndifFragment(ArithmeticaFileGrammarParser.EndifFragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#endifFragment}.
	 * @param ctx the parse tree
	 */
	void exitEndifFragment(ArithmeticaFileGrammarParser.EndifFragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ArithmeticaFileGrammarParser#conditionalExpr}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpr(ArithmeticaFileGrammarParser.ConditionalExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ArithmeticaFileGrammarParser#conditionalExpr}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpr(ArithmeticaFileGrammarParser.ConditionalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Call}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCall(ArithmeticaFileGrammarParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Call}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCall(ArithmeticaFileGrammarParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parenthesis}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenthesis(ArithmeticaFileGrammarParser.ParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parenthesis}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenthesis(ArithmeticaFileGrammarParser.ParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparativeOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterComparativeOp(ArithmeticaFileGrammarParser.ComparativeOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparativeOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitComparativeOp(ArithmeticaFileGrammarParser.ComparativeOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVariable(ArithmeticaFileGrammarParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVariable(ArithmeticaFileGrammarParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Random}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRandom(ArithmeticaFileGrammarParser.RandomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Random}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRandom(ArithmeticaFileGrammarParser.RandomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Number}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNumber(ArithmeticaFileGrammarParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Number}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNumber(ArithmeticaFileGrammarParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MultiplicativeOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeOp(ArithmeticaFileGrammarParser.MultiplicativeOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MultiplicativeOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeOp(ArithmeticaFileGrammarParser.MultiplicativeOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AdditiveOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveOp(ArithmeticaFileGrammarParser.AdditiveOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AdditiveOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveOp(ArithmeticaFileGrammarParser.AdditiveOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PowerOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPowerOp(ArithmeticaFileGrammarParser.PowerOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PowerOp}
	 * labeled alternative in {@link ArithmeticaFileGrammarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPowerOp(ArithmeticaFileGrammarParser.PowerOpContext ctx);
}