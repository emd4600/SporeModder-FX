/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.argscript;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.util.ColorRGBA;

class DefaultParsers {
	static <T> void addDefaultParsers(ArgScriptStream<T> stream) {
		
		stream.addParser("include", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				String path = args.getSingle();
				File file = null;
				
				// Is it an absolute path? C: ...
				if (path.length() > 2 && Character.isAlphabetic(path.charAt(0)) && path.charAt(1) == ':') {
					file = new File(path);
				}
				else {
					file = new File(stream.getFolder(), path);
				}
				
				if (file.exists()) {
					try {
						stream.includeFile(file);
					} catch (Exception e) {
						stream.addError(line.createError("Error reading file: " + e.getLocalizedMessage()));
					}
				}
				else {
					stream.addError(line.createError("The specified file does not exist."));
				}
			}
		}));
		
		// The only difference is that we just add warnings, but the file can still compile if the file does not exist
		stream.addParser("sinclude", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				String path = args.getSingle();
				File file = null;
				
				// Is it an absolute path? C: ...
				if (path.length() > 2 && Character.isAlphabetic(path.charAt(0)) && path.charAt(1) == ':') {
					file = new File(path);
				}
				else {
					file = new File(stream.getFolder(), path);
				}
				
				if (file.exists()) {
					try {
						stream.includeFile(file);
					} catch (Exception e) {
						stream.addWarning(line.createError("Error reading file: " + e.getLocalizedMessage()));
					}
				}
				else {
					stream.addWarning(line.createError("The specified file does not exist."));
				}
			}
		}));
		
		stream.addParser("version", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				Integer version = stream.parseInt(args, 0);
				
				if (version != null) {
					if (version < stream.getMinVersion()) {
						stream.addError(line.createError(String.format("Script version no longer supported: have %d, need at least %d.", version, stream.getMinVersion())));
					}
					
					if (version > stream.getMaxVersion()) {
						stream.addError(line.createError(String.format("Script version more recent than code: have %d, can only handle up to %d.", version, stream.getMaxVersion())));
					}
					
					stream.setVersion(version.intValue());
				}
			}
		}));
		
		stream.addParser("end", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			// Raise an error if it has any arguments
			line.getArguments(args, 0);
			
			if (!stream.insideBlock()) {
				stream.addError(line.createError("Not inside a block."));
			}
			else {
				stream.endBlock();
			}
		}));
		
		stream.addParser("eval", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				stream.processLine(args.getSingle());
			}
		}));
		
		stream.addParser("set", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				stream.setVariable(args.get(0), args.get(1));
			}
		}));
		
		stream.addParser("sete", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 3)) {
				String value = args.get(1);

				ArgScriptLine valuesLine = new ArgScriptLine(stream);
				valuesLine.fromLine(args.get(2), line.positionTracker);
				
				boolean found = false;
				List<String> enumValues = valuesLine.getSplits();
				for (String enumValue : enumValues) {
					if (value.equals(enumValue)) {
						found = true;
						break;
					}
				}
				
				if (found) {
					stream.setVariable(args.get(0), value);
				}
				else {
					stream.addError(line.createErrorForArgument("Unknown enum value.", 1));
				}
				
			}
		}));
		
		stream.addParser("setb", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				Boolean value = stream.parseBoolean(args, 1);
				if (value != null) {
					stream.setVariable(args.get(0), value ? "true" : "false");
				}
			}
		}));
		
		stream.addParser("seti", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				Integer value = stream.parseInt(args, 1);
				if (value != null) {
					stream.setVariable(args.get(0), Integer.toString(value));
				}
			}
		}));
		
		stream.addParser("setf", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				Float value = stream.parseFloat(args, 1);
				if (value != null) {
					stream.setVariable(args.get(0), Float.toString(value));
				}
			}
		}));
		
		stream.addParser("setc", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				ColorRGBA color = new ColorRGBA();
				if (stream.parseColorRGBA(args, 1, color)) {
					stream.setVariable(args.get(0), color.toString());
				}
			}
		}));
		
		stream.addParser("setv2", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				float[] array = new float[2];
				if (stream.parseVector2(args, 1, array)) {
					stream.setVariable(args.get(0), String.format("(%s, %s)", 
							HashManager.get().floatToString(array[0]),
							HashManager.get().floatToString(array[1])));
				}
			}
		}));
		
		stream.addParser("setv3", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				float[] array = new float[3];
				if (stream.parseVector3(args, 1, array)) {
					stream.setVariable(args.get(0), String.format("(%s, %s, %s)", 
							HashManager.get().floatToString(array[0]),
							HashManager.get().floatToString(array[1]),
							HashManager.get().floatToString(array[2])));
				}
			}
		}));
		
		stream.addParser("setv4", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				float[] array = new float[4];
				if (stream.parseVector4(args, 1, array)) {
					stream.setVariable(args.get(0), String.format("(%s, %s, %s, %s)", 
							HashManager.get().floatToString(array[0]),
							HashManager.get().floatToString(array[1]),
							HashManager.get().floatToString(array[2]),
							HashManager.get().floatToString(array[3])));
				}
			}
		}));
		
		stream.addParser("namespace", ArgScriptBlock.create(
		(block, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				stream.startScope(args.getSingle());
			}
			
			stream.startBlock(block);
		}, 
		(block) -> {
			stream.endScope();
		}));
		
		
		stream.addParser("purge", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				stream.purgeScope(args.getSingle());
			}
		}));
		
		stream.addParser("if", new ArgScriptSpecialBlock<T>() {
			
			private boolean meetsCondition = false;
			/** Have we written all the content needed? */
			private boolean ignoreTheRest = false;
			private ArgScriptArguments args = new ArgScriptArguments();

			@Override
			public void parse(ArgScriptLine line) {

				meetsCondition = false;
				ignoreTheRest = false;

				stream.startSpecialBlock(this, "endif");
				
				if (line.getArguments(args, 1)) {
					Boolean result = stream.parseBoolean(args, 0);
					if (result != null && result) {
						meetsCondition = true;
					}
				}
			}
			
			private String replaceVariables(String text) {
				StringBuilder dst = new StringBuilder();
				
				if (!stream.replaceVariables(text.toCharArray(), dst, null, null)) {
					return null;
				}
				
				return dst.toString();
			}
			
			@Override
			public boolean processLine(String line) {
				// We've already processed what we needed, the rest can be ignored
				if (ignoreTheRest) {
					return true;
				}
				
				// Get the keyword
				char[] chars = line.toCharArray();
				int index = 0;
				StringBuilder sb = new StringBuilder();
				
				while (index < chars.length && Character.isWhitespace(chars[index])) index++;
				int startIndex = index;
				while (index < chars.length && !Character.isWhitespace(chars[index])) sb.append(chars[index++]);
				
				String keyword = sb.toString();
				
				if (keyword.equals("elseif")) {
					if (stream.hasSyntaxHighlighting()) {
						stream.getSyntaxHighlighter().add(stream.getCurrentLine(), startIndex, keyword.length(), Collections.singleton(ArgScriptStream.SYNTAX_COMMAND));
					}
					// If the condition was met, now it isn't anymore; everything must be ignored from now
					if (meetsCondition) {
						meetsCondition = false;
						ignoreTheRest = true;
					}
					else {
						// Does it meet the condition now?
						String text = replaceVariables(args.get(0));
						if (text != null) {
							ArgScriptArguments newArgs = new ArgScriptArguments();
							newArgs.arguments = Arrays.asList(text);
							newArgs.positions = args.positions;
							newArgs.endPositions = args.endPositions;
							newArgs.originalToText = args.originalToText;
							newArgs.stream = args.stream;
							newArgs.numArguments = 1;
							newArgs.tracker = args.tracker;
							
							Boolean result = stream.parseBoolean(newArgs, 0);
							if (result != null && result) {
								meetsCondition = true;
							}
						}
					}
				}
				else if (keyword.equals("else")) {
					if (stream.hasSyntaxHighlighting()) {
						stream.getSyntaxHighlighter().add(stream.getCurrentLine(), startIndex, keyword.length(), Collections.singleton(ArgScriptStream.SYNTAX_BLOCK));
					}
					// If the condition was met, now it isn't anymore; everything must be ignored from now
					if (meetsCondition) {
						meetsCondition = false;
						ignoreTheRest = true;
					}
					else {
						meetsCondition = true;
					}
				}
				else {
					if (meetsCondition) {
						// Tell the stream we must process the line normally
						return false;
					}
				}
				
				return true;
			}

			@Override
			public void onBlockEnd() {
				stream.endSpecialBlock();
			}
		
		});
		
		stream.addParser("define", new ArgScriptSpecialBlock<T>() {
			
			private ArgScriptDefinition definition;
			
			@Override
			public void parse(ArgScriptLine line) {
				ArgScriptArguments args = new ArgScriptArguments();
				
				stream.startSpecialBlock(this, "enddef");
				
				if (line.getArguments(args, 1)) {
					ArgScriptLexer lexer = new ArgScriptLexer();
					lexer.setText(args.getSingle());
					
					try {
						String name = lexer.parseKeyword();
						definition = new ArgScriptDefinition(name, stream.getCurrentLine());
						
						if (stream.getDefinition(name) != null) {
							throw new DocumentException(new DocumentError(String.format("'%s' already defined.", name), 0, lexer.getIndex()));
						}
						
						lexer.expect('(');
						lexer.skipWhitespaces();
						
						if (!lexer.available()) {
							throw new DocumentException(new DocumentError("Expected parameter names or ')'.", lexer.getIndex() - 1, lexer.getIndex()));
						}
						
						// Get the parameters
						
						while (true) {
							lexer.skipWhitespaces();
							String parameter = lexer.parseKeyword();
							
							if (parameter.isEmpty()) {
								throw new DocumentException(new DocumentError("Empty parameter name.", lexer.getIndex() - 1, lexer.getIndex()));
							}
							
							definition.addParameter(parameter);
							
							lexer.skipWhitespaces();
							char c = lexer.nextCharater();
							
							if (c == ')') {
								break;
							}
							else if (c != ',') {
								throw new DocumentException(new DocumentError("Expected ',' after parameter name.", lexer.getIndex() - 1, lexer.getIndex()));
							}
						}
						
						stream.addDefinition(definition);
						
					} catch (DocumentException e) {
						DocumentError error = e.getError();
						error.setStartPosition(error.getStartPosition() + args.getPosition(0));
						error.setEndPosition(error.getEndPosition() + args.getPosition(0));
					}
				}
			}
			
			@Override
			public boolean processLine(String line) {
				if (definition != null) {
					definition.addLine(line);
				}
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.endSpecialBlock();
			}
		
		});

		stream.addParser("undefine", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				if (!stream.removeDefinition(args.getSingle())) {
					
					stream.addError(line.createErrorForArgument(String.format("Definition '%s' does not exist.", args.getSingle()), 1));
				}
			}
		}));
		
		stream.addParser("create", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				String text = args.getSingle();
				ArgScriptLexer lexer = parser.stream.getLexer();
				lexer.setText(text);
				
				// Here we will put all the arguments, which are separated by spaces
				List<String> arguments = new ArrayList<String>();
				String definitionName = null;
				
				try {
					
					definitionName = lexer.parseKeyword();
					
					lexer.expect('(');
					
					while (true) {
						lexer.skipWhitespaces();
						
						if (!lexer.available()) {
							stream.addError(line.createError("Missing closing ')' after definition arguments."));
							return;
						}
						
						if (lexer.peekNextCharater() == ')') {
							lexer.nextCharater();
							break;
						}
						
						WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
						String word = ws.nextParameter();
						
						// If the splitter is uncapable of generating a word it means we are reaching invalid characters
						// Is it the ) or another thing?
						if (word.isEmpty()) {
							if (lexer.peekNextCharater() == ')') {
								lexer.nextCharater();
								break;
							}
							else {
								stream.addError(line.createError("Missing closing ')' after definition arguments."));
								return;
							}
						}
						else {
							arguments.add(word);
						}
					}
				}
				catch (DocumentException e) {
					int startPosition = args.getPosition(0);
					DocumentError error = e.getError();
					error.setStartPosition(error.getStartPosition() + startPosition);
					error.setEndPosition(error.getEndPosition() + startPosition);
					stream.addError(error);
					return;
				}
				
				ArgScriptDefinition definition = stream.getDefinition(definitionName);
				if (definition == null) {
					stream.addError(line.createError(String.format("Unknown definition '%s'.", definitionName)));
					return;
				}
				
				try {
					definition.creteInstance(stream, arguments);
				}
				catch (DocumentException e) {
//					int startPosition = args.getPosition(0);
//					DocumentError error = e.getError();
//					error.setStartPosition(error.getStartPosition() + startPosition);
//					error.setEndPosition(error.getEndPosition() + startPosition);
					stream.addError(line.createError(e.getError().getMessage()));
					return;
				}
			}
		}));
		
		stream.addParser("create", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				String text = args.getSingle();
				ArgScriptLexer lexer = parser.stream.getLexer();
				lexer.setText(text);
				
				// Here we will put all the arguments, which are separated by spaces
				List<String> arguments = new ArrayList<String>();
				String definitionName = null;
				
				try {
					
					definitionName = lexer.parseKeyword();
					
					lexer.expect('(');
					
					while (true) {
						lexer.skipWhitespaces();
						
						if (!lexer.available()) {
							stream.addError(line.createError("Missing closing ')' after definition arguments."));
							return;
						}
						
						if (lexer.peekNextCharater() == ')') {
							lexer.nextCharater();
							break;
						}
						
						WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
						String word = ws.nextParameter();
						lexer.setIndex(ws.index);
						
						// If the splitter is uncapable of generating a word it means we are reaching invalid characters
						// Is it the ) or another thing?
						if (word.isEmpty()) {
							if (lexer.peekNextCharater() == ')') {
								lexer.nextCharater();
								break;
							}
							else {
								stream.addError(line.createError("Missing closing ')' after definition arguments."));
								return;
							}
						}
						else {
							arguments.add(word);
						}
						
						
					}
				}
				catch (DocumentException e) {
					int startPosition = args.getPosition(0);
					DocumentError error = e.getError();
					error.setStartPosition(error.getStartPosition() + startPosition);
					error.setEndPosition(error.getEndPosition() + startPosition);
					stream.addError(error);
					return;
				}
				
				ArgScriptDefinition definition = stream.getDefinition(definitionName);
				if (definition == null) {
					// The only difference with 'create' is that we just create a warning, instead of throwing an error
					stream.addWarning(line.createError(String.format("Unknown definition '%s'.", definitionName)));
					return;
				}
				
				try {
					definition.creteInstance(stream, arguments);
				}
				catch (DocumentException e) {
//					int startPosition = args.getPosition(0);
//					DocumentError error = e.getError();
//					error.setStartPosition(error.getStartPosition() + startPosition);
//					error.setEndPosition(error.getEndPosition() + startPosition);
//					stream.addError(error);
					stream.addError(line.createError(e.getMessage()));
					return;
				}
			}
		}));
		
		stream.addParser("arrayCreate", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 2)) {
				String definitionName = args.get(0);
				Integer quantity = stream.parseInt(args, 1);
				
				if (quantity != null) {
					ArgScriptDefinition definition = stream.getDefinition(definitionName);
					if (definition == null) {
						// The only difference with 'create' is that we just create a warning, instead of throwing an error
						stream.addWarning(line.createError(String.format("Unknown definition '%s'.", definitionName)));
						return;
					}
					
					List<String> arguments = new ArrayList<String>();
					
					for (int i = 0; i < quantity; i++) {
						
						try {
							arguments.clear();
							arguments.add(Integer.toString(i));
							arguments.add(Integer.toString(quantity));
							
							definition.creteInstance(stream, arguments);
						}
						catch (DocumentException e) {
							int startPosition = args.getPosition(0);
							DocumentError error = e.getError();
							error.setStartPosition(error.getStartPosition() + startPosition);
							error.setEndPosition(error.getEndPosition() + startPosition);
							stream.addError(error);
							return;
						}
					}
				}
			}
		}));
		
		
		// Functions:
		
		stream.getLexer().addDefaultFunctions(stream);
		
		
		// We have ignored:
		// help
		// trace
		// showArguments
	}
}
