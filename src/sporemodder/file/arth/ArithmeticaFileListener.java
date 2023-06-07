package sporemodder.file.arth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sporemodder.file.arth.ArithmeticaFile.Function;
import sporemodder.file.arth.ArithmeticaFile.Operation;

public class ArithmeticaFileListener extends ArithmeticaFileGrammarBaseListener {
	
	private final static List<String> PREDEFINED_FUNCTIONS = new ArrayList<>();
	static {
		PREDEFINED_FUNCTIONS.add("sin");
		PREDEFINED_FUNCTIONS.add("cos");
		PREDEFINED_FUNCTIONS.add("tan");
		PREDEFINED_FUNCTIONS.add("asin");
		PREDEFINED_FUNCTIONS.add("acos");
		PREDEFINED_FUNCTIONS.add("atan");
		PREDEFINED_FUNCTIONS.add("floor");
		PREDEFINED_FUNCTIONS.add("ceil");
	}

	private ArithmeticaFile arth;
	private ArithmeticaFileGrammarParser parser;
	private final Map<String, Integer> stringsMap = new HashMap<>();
	private final Map<String, Function> functionsMap = new HashMap<>();
	private int currentLine;
	
	public ArithmeticaFileListener(ArithmeticaFile arth, ArithmeticaFileGrammarParser parser) {
		this.arth = arth;
		this.parser = parser;
	}
	
	private int getIndexForString(String str) {
		int index = stringsMap.getOrDefault(str, -1);
		if (index == -1) {
			index = arth.strings.size();
			arth.strings.add(str);
			stringsMap.put(str, index);
		}
		return index;
	}
	
	private Operation newOp(int opCode) {
		Operation op = new Operation();
		op.opCode = opCode;
		op.line = currentLine;
		arth.operations.add(op);
		return op;
	}
	private Operation newOp(int opCode, float value) {
		Operation op = newOp(opCode);
		op.value = value;
		return op;
	}
	private Operation newOp(int opCode, int indexValue) {
		Operation op = newOp(opCode);
		op.indexValue = indexValue;
		return op;
	}
	
	@Override public void enterAssignStatement(ArithmeticaFileGrammarParser.AssignStatementContext ctx) { 
		getIndexForString(ctx.IDENTIFIER().getText());
		currentLine = ctx.start.getLine();
	}

	@Override public void exitAssignStatement(ArithmeticaFileGrammarParser.AssignStatementContext ctx) { 
		newOp(ArithmeticaFile.OP_POP_INTO_VARIABLE, getIndexForString(ctx.IDENTIFIER().getText()));
	}
	
	@Override public void exitNumber(ArithmeticaFileGrammarParser.NumberContext ctx) { 
		getIndexForString(ctx.getText());
		newOp(ArithmeticaFile.OP_PUSH, Float.parseFloat(ctx.getText()));
	}
	
	@Override public void exitVariable(ArithmeticaFileGrammarParser.VariableContext ctx) { 
		newOp(ArithmeticaFile.OP_PUSH_VARIABLE, getIndexForString(ctx.getText()));
	}
	
	@Override public void exitRandom(ArithmeticaFileGrammarParser.RandomContext ctx) {
		getIndexForString(ctx.getText());
		newOp(ArithmeticaFile.OP_RANDOM);
	}
	
	@Override public void exitAdditiveOp(ArithmeticaFileGrammarParser.AdditiveOpContext ctx) { 
		newOp(ctx.SUB() != null ? ArithmeticaFile.OP_SUBTRACT : ArithmeticaFile.OP_ADD);
	}
	
	@Override public void exitMultiplicativeOp(ArithmeticaFileGrammarParser.MultiplicativeOpContext ctx) {
		newOp(ctx.MULT() != null ? ArithmeticaFile.OP_MULTIPLY : ArithmeticaFile.OP_DIVIDE);
	}
	
	@Override public void exitPowerOp(ArithmeticaFileGrammarParser.PowerOpContext ctx) { 
		newOp(ArithmeticaFile.OP_POW);  //TODO the order is not correct?
	}
	
	@Override public void exitComparativeOp(ArithmeticaFileGrammarParser.ComparativeOpContext ctx) {
		int opCode = ArithmeticaFile.OP_NOP;
		if (ctx.EQ() != null) opCode = ArithmeticaFile.OP_EQUAL;
		else if (ctx.NEQ() != null) opCode = ArithmeticaFile.OP_NOT_EQUAL;
		else if (ctx.LESS() != null) opCode = ArithmeticaFile.OP_LESS;
		else if (ctx.GREATER() != null) opCode = ArithmeticaFile.OP_GREATER;
		else if (ctx.LEQ() != null) opCode = ArithmeticaFile.OP_LESS_EQUAL;
		else if (ctx.GEQ() != null) opCode = ArithmeticaFile.OP_GREATER_EQUAL;
		newOp(opCode);
	}
	
	@Override public void exitParenthesis(ArithmeticaFileGrammarParser.ParenthesisContext ctx) { 
		if (ctx.SUB() != null) {
			newOp(ArithmeticaFile.OP_PUSH, -1.0f);
			newOp(ArithmeticaFile.OP_MULTIPLY);
		}
	}
	
	@Override public void exitIfFragment(ArithmeticaFileGrammarParser.IfFragmentContext ctx) { 
		newOp(ArithmeticaFile.OP_BEGIN_IF);
	}
	
	@Override public void exitElseFragment(ArithmeticaFileGrammarParser.ElseFragmentContext ctx) { 
		newOp(ArithmeticaFile.OP_ELSE);
	}
	
	@Override public void exitEndifFragment(ArithmeticaFileGrammarParser.EndifFragmentContext ctx) { 
		newOp(ArithmeticaFile.OP_END_IF);
	}
	
	@Override public void enterFunction(ArithmeticaFileGrammarParser.FunctionContext ctx) { 
		String functionName = ctx.IDENTIFIER().getText();
		if (functionsMap.containsKey(functionName)) {
			throw new UnsupportedOperationException("Function " + functionName + " already defined");
		}
		if (PREDEFINED_FUNCTIONS.contains(functionName)) {
			throw new UnsupportedOperationException("Cannot name custom function " + functionName + ", name is protected");
		}
		
		Function f = new Function();
		f.stringIndex = getIndexForString(functionName);
		f.firstOpIndex = arth.operations.size();
		arth.functions.add(f);
	}
	
	@Override public void exitFunction(ArithmeticaFileGrammarParser.FunctionContext ctx) { 
		newOp(ArithmeticaFile.OP_RETURN);
	}
	
	@Override public void exitCall(ArithmeticaFileGrammarParser.CallContext ctx) { 
		String functionName = ctx.IDENTIFIER().getText();
		if (PREDEFINED_FUNCTIONS.contains(functionName)) {
			if (ctx.expr() == null) {
				parser.notifyErrorListeners(ctx.start, "Function " + functionName + "() needs a parameter", null);
				return;
			}
			int opCode = ArithmeticaFile.OP_NOP;
			switch (functionName) {
			case "sin": opCode = ArithmeticaFile.OP_SIN; break;
			case "cos": opCode = ArithmeticaFile.OP_COS; break;
			case "tan": opCode = ArithmeticaFile.OP_TAN; break;
			case "asin": opCode = ArithmeticaFile.OP_ASIN; break;
			case "acos": opCode = ArithmeticaFile.OP_ACOS; break;
			case "atan": opCode = ArithmeticaFile.OP_ATAN; break;
			case "floor": opCode = ArithmeticaFile.OP_FLOOR; break;
			case "ceil": opCode = ArithmeticaFile.OP_CEIL; break;
			}
			newOp(opCode);
		}
		else if (functionsMap.containsKey(functionName)) {
			if (ctx.expr() != null) {
				parser.notifyErrorListeners(ctx.start, "Function " + functionName + "() cannot have parameters", null);
				return;
			}
			newOp(ArithmeticaFile.OP_CALL, functionsMap.get(functionName).firstOpIndex);
		}
		else {
			parser.notifyErrorListeners(ctx.start, "Unrecognized function " + functionName + "()", null);
			return;
		}
	}
}
