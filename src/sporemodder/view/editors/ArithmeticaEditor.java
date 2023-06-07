package sporemodder.view.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import sporemodder.file.DocumentError;
import sporemodder.file.arth.ArithmeticaFile;
import sporemodder.file.arth.ArithmeticaFileGrammarLexer;
import sporemodder.file.arth.ArithmeticaFileGrammarParser;
import sporemodder.file.arth.ArithmeticaFileListener;
import sporemodder.view.UserInterface;
import sporemodder.view.syntax.SyntaxHighlighter;

public class ArithmeticaEditor extends TextEditorWithErrors {
	
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\R");
	
	private static final List<Integer> KEYWORD_TOKENS = new ArrayList<>();
	static
	{
		KEYWORD_TOKENS.add(ArithmeticaFileGrammarLexer.IF);
		KEYWORD_TOKENS.add(ArithmeticaFileGrammarLexer.ELSE);
		KEYWORD_TOKENS.add(ArithmeticaFileGrammarLexer.ENDIF);
		KEYWORD_TOKENS.add(ArithmeticaFileGrammarLexer.DEF);
		KEYWORD_TOKENS.add(ArithmeticaFileGrammarLexer.ENDDEF);
	}
	private static final List<String> FUNCTION_KEYWORDS = new ArrayList<>();
	static
	{
		FUNCTION_KEYWORDS.add("sin");
		FUNCTION_KEYWORDS.add("cos");
		FUNCTION_KEYWORDS.add("tan");
		FUNCTION_KEYWORDS.add("asin");
		FUNCTION_KEYWORDS.add("acos");
		FUNCTION_KEYWORDS.add("atan");
		FUNCTION_KEYWORDS.add("floor");
		FUNCTION_KEYWORDS.add("ceil");
	}
	
	private final List<DocumentError> lastErrors = new ArrayList<>();
	
	public ArithmeticaEditor() {
		super();
		
		setSyntaxHighlighting(this::doSyntaxHighlighting);
	}
	
	@Override protected List<DocumentError> getErrors() {
		return lastErrors;
	}
	@Override protected List<DocumentError> getWarnings() {
		return new ArrayList<DocumentError>();
	}

	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Arithmetica File (ARTH)", "ARTH", null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
	
	protected void doSyntaxHighlighting(String text, SyntaxHighlighter syntax) {
		Matcher matcher = NEWLINE_PATTERN.matcher(text);
		List<Integer> linePositions = new ArrayList<Integer>();
		List<Integer> lineEnds = new ArrayList<Integer>();
		linePositions.add(0);
		
		while (matcher.find()) {
			linePositions.add(matcher.end());
			lineEnds.add(matcher.start());
		}
		lineEnds.add(text.length());
		
		lastErrors.clear();
		ArithmeticaFileGrammarLexer lexer = new ArithmeticaFileGrammarLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ArithmeticaFileGrammarParser parser = new ArithmeticaFileGrammarParser(tokens);
		parser.addErrorListener(new BaseErrorListener() {
			@Override 
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				int lineLength = lineEnds.get(line - 1) - linePositions.get(line - 1);
				int numChars = 1;
				if (charPositionInLine >= lineLength) {
					charPositionInLine = lineLength - 2;
					numChars = 2;
				}
				lastErrors.add(new DocumentError(msg, charPositionInLine, charPositionInLine + numChars, line-1));
			}
		});
		ParseTree tree = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		ArithmeticaFile arth = new ArithmeticaFile();
		ArithmeticaFileListener listener = new ArithmeticaFileListener(arth, parser);
		walker.walk(listener, tree);
		
		syntax.setText(text, linePositions);
		for (Token token : tokens.getTokens()) {
			String style = null;
			if (KEYWORD_TOKENS.contains(token.getType())) {
				style = "arth-keyword";
			}
			else if (token.getType() == ArithmeticaFileGrammarLexer.FLOATING_LITERAL) {
				style = "arth-number";
			}
			else if (token.getType() == ArithmeticaFileGrammarLexer.RAND ||
					(token.getType() == ArithmeticaFileGrammarLexer.IDENTIFIER &&
					 FUNCTION_KEYWORDS.contains(token.getText()))) {
				style = "arth-functions";
			}
			
			if (style != null) {
				syntax.add(token.getLine() - 1, token.getCharPositionInLine(), token.getText().length(), Collections.singleton(style));
			}
		}
		
		SyntaxHighlighter errorsHighlighter = new SyntaxHighlighter();
		errorsHighlighter.setText(text, linePositions);
		
		for (DocumentError error : lastErrors) {
			int start = error.getStartPosition() + errorsHighlighter.getLinePosition(error.getLine());
			int end = error.getEndPosition() + errorsHighlighter.getLinePosition(error.getLine());
			errorsHighlighter.addExtra(start, end - start, DocumentError.STYLE_ERROR, false);
		}
		
		syntax.addExtras(errorsHighlighter, false);
		setErrorInfo(errorsHighlighter);
	}
}
