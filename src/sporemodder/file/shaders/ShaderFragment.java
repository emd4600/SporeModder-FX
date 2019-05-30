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
package sporemodder.file.shaders;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.argscript.TextPositionMap;
import sporemodder.file.argscript.WordSplitLexer;
import sporemodder.view.syntax.HlslSyntax;
import sporemodder.view.syntax.SyntaxHighlighter;

public abstract class ShaderFragment {
	
	private static final HlslSyntax HLSL_SYNTAX = new HlslSyntax();
	
	public static final int FLAG_DEFINED = 1;
	public static final int FLAG_NAME = 2;

	public String mainCode;
	public String declareCode;
	public final List<ShaderDataUniform> shaderData = new ArrayList<>();
	
	public int input;  // 20h
	public int output;  // 24h
	public int flags;  // -18h
	public String shaderName;
	
	public short numRegisters;  // 28h, 29h in pixel shaders
	
	public String getName() {
		return shaderName;
	}
	
	// Writes the declare (including shader data) and main code
	protected void codeToArgScript(ArgScriptWriter writer) {
		boolean needsBlankLine = false;
		if (!declareCode.isEmpty() || !shaderData.isEmpty()) {
			writer.command("declareCode").startBlock();
			for (ShaderDataUniform variable : shaderData) {
				
				writer.command("extern").arguments("uniform ");
				writer.tabulatedText(variable.name + ";", false);
				//writer.arguments(":", "register(c" + startRegister + ");");
			}
			if (!declareCode.trim().isEmpty()) {
				if (!shaderData.isEmpty()) {
					writer.blankLine();
				}
				writer.tabulatedText(declareCode, true);
			}
			writer.endBlock().command("endCode");
			needsBlankLine = true;
		}
		
		if (!mainCode.isEmpty()) {
			if (needsBlankLine) writer.blankLine();
			writer.command("code").startBlock();
			writer.tabulatedText(mainCode, true);
			writer.endBlock().command("endCode");
		}
	}
	
	protected static class CodeParser extends ArgScriptSpecialBlock<ShaderFragmentUnit> {
		final StringBuilder sb = new StringBuilder();
		final TextPositionMap posMap = new TextPositionMap();
		int lastLineNumber;
		int startLineNumber;
		public ShaderFragment fragment;
		public boolean isDeclareCode;
		
		public CodeParser(boolean isDeclareCode) {
			this.isDeclareCode = isDeclareCode;
		}

		@Override public void parse(ArgScriptLine line) {
			sb.setLength(0);
			posMap.clear();
			if (isDeclareCode && fragment.declareCode != null) {
				stream.addError(line.createError("Only one declareCode block per fragment is supported"));
			}
			else if (!isDeclareCode && fragment.mainCode != null) {
				stream.addError(line.createError("Only one main code block per fragment is supported"));
			}
			stream.startSpecialBlock(this, "endCode");
			lastLineNumber = stream.getCurrentLine();
			startLineNumber = lastLineNumber + 1;
		}
		
		@Override public void onBlockEnd() {
			String code = sb.toString();
			if (isDeclareCode) fragment.declareCode = code;
			else fragment.mainCode = code;
			stream.endSpecialBlock();
			
			if (!stream.isFastParsing()) {
				SyntaxHighlighter syntax = new SyntaxHighlighter();
				HLSL_SYNTAX.generateStyle(code, syntax);
				stream.getSyntaxHighlighter().addExtras(syntax, posMap, false);
			}
			
			if (isDeclareCode) {
				try {
					processUniforms();
				} catch (DocumentException e) {
					stream.addError(e.getError());
				}
			}
		}
		
		private void processUniforms() throws DocumentException {
			// 1st: remove comments
			String text = fragment.declareCode;
			StringBuilder realText = new StringBuilder();
			
			// Process block comments
			int endIndex = 0;
			int indexOf = text.indexOf("/*");
			while (indexOf != -1) {
				realText.append(text.substring(endIndex, indexOf));
				endIndex = text.indexOf("*/", indexOf + 2);
				
				if (endIndex == -1) {
					break;
				}
				endIndex += 2;
				
				indexOf = text.indexOf("/*", endIndex);
			}
			// Add remaining text
			realText.append(text.substring(endIndex));
			
			// Process line comments
			text = realText.toString();
			realText.setLength(0);
			endIndex = 0;
			indexOf = text.indexOf("//");
			while (indexOf != -1) {
				realText.append(text.substring(endIndex, indexOf));
				endIndex = text.indexOf("\n", indexOf + 2);
				
				if (endIndex == -1) {
					break;
				}
				// Don't increase endIndex because we actually want to include the line break
				
				indexOf = text.indexOf("//", endIndex + 1);
			}
			// Add remaining text
			realText.append(text.substring(endIndex));
			text = realText.toString();
			
			WordSplitLexer lexer = new WordSplitLexer(text);
			StringBuilder sbText = new StringBuilder();
			int pos = lexer.getPosition();
			
			while (!lexer.isEOF()) {
				lexer.skipUnreadable();
				
				int tempPos = lexer.getPosition();
				String word = lexer.nextReadableWord();
				
				if ("extern".equals(word) && "uniform".equals(lexer.nextReadableWord())) {
					sbText.append(text.substring(pos, tempPos));
					
					// Two possiblities: type is struct (complex) or simple
					String type = lexer.nextReadableWord();
					if (type == null) {
						stream.addError(new DocumentError("Expected a type after 'extern uniform'", getPos(lexer.getPosition() - 10), getPos(lexer.getPosition()), startLineNumber));
						break;
					}
					
					StringBuilder sbUniform = new StringBuilder();
					
					int registerSize = 0;
					
					sbUniform.append(type);
					if ("struct".equals(type)) {
						lexer.skipWhitespaces();
						if (lexer.isEOF() || lexer.nextChar() != '{') {
							stream.addError(new DocumentError("Expected a '{' after struct declaration", getPos(lexer.getPosition() - 6), getPos(lexer.getPosition()), startLineNumber));
							break;
						}
						sbUniform.append(" {\n");
						
						// We must calculate the register size
						String structType;
						while ((structType = lexer.nextReadableWord()) != null) {
							// The member name
							sbUniform.append(structType);
							sbUniform.append(' ');
							sbUniform.append(lexer.nextReadableWord());
							
							int startPos = lexer.getPosition();
							
							int arrayLength = parseArray(lexer);
							if (arrayLength == -1) return;
							if (arrayLength == 0) arrayLength = 1;
							registerSize += ShaderDataUniform.calculateRegisterSize(structType) * arrayLength;
							
							sbUniform.append(text.substring(startPos, lexer.getPosition()));
							
							sbUniform.append(";\n");
							
							if (lexer.isEOF() || lexer.peekChar() != ';') {
								stream.addError(new DocumentError("Missing ; after struct member declaration", 
										getPos(lexer.getPosition() - 1), getPos(lexer.getPosition()), startLineNumber));
								return;
							} else {
								// Eat the ;
								lexer.nextChar();
							}
						}
						
						sbUniform.append('}');
						if (lexer.isEOF() || lexer.peekChar() != '}') {
							stream.addError(new DocumentError("Missing '}' closing struct declaration", getPos(lexer.getPosition() - 6), getPos(lexer.getPosition()), startLineNumber));
							return;
						}
						// Eat the }
						lexer.nextChar();
					} else {
						if (type.startsWith("sampler")) {
							pos = tempPos;
							break;
						}
						registerSize = ShaderDataUniform.calculateRegisterSize(type);
					}
					sbUniform.append(' ');
					
					word = lexer.nextReadableWord();
					if (word == null) {
						stream.addError(new DocumentError("Missing shader data name", getPos(lexer.getPosition() - 5), getPos(lexer.getPosition()-1), startLineNumber));
						return;
					}
					else if (!ShaderData.hasIndex(word)) {
						stream.addError(new DocumentError("'" + word + "' is not a recognized shader data name",
								getPos(lexer.getPosition() - word.length()), getPos(lexer.getPosition()), startLineNumber));
						return;
					}
					else {
						sbUniform.append(word);
						
						ShaderDataUniform uniform = new ShaderDataUniform();
						fragment.shaderData.add(uniform);
						
						int startPos = lexer.getPosition();
						int arrayLength = parseArray(lexer);
						if (arrayLength == -1) return;
						uniform.dataIndex = uniform.field_2 = ShaderData.getIndex(word, arrayLength != 0);
						uniform.flags = ShaderData.getFlags(uniform.dataIndex);
						
						sbUniform.append(text.substring(startPos, lexer.getPosition()));
						uniform.name = sbUniform.toString();
						
						if (arrayLength == 0) arrayLength = 1;
						uniform.registerSize = registerSize * arrayLength;
						
						fragment.numRegisters += uniform.registerSize;
						
						lexer.skipWhitespaces();
						if (lexer.isEOF() || lexer.peekChar() != ';') {
							stream.addError(new DocumentError("Missing ; after uniform declaration", 
									getPos(lexer.getPosition() - 1), getPos(lexer.getPosition()), startLineNumber));
							return;
						} else {
							// Eat the ;
							lexer.nextChar();
						}
					}
					
					pos = lexer.getPosition();
				}
			}
			
			sbText.append(text.substring(pos, text.length()));
			
			// Don't need to include initial blank lines
			int i = 0;
			while (i < sbText.length() && (sbText.charAt(i) == '\n' || sbText.charAt(i) == '\r')) ++i;
			fragment.declareCode = sbText.toString().substring(i);
		}
		
		private int parseArray(WordSplitLexer lexer) {
			lexer.skipWhitespaces();
			if (!lexer.isEOF() && lexer.peekChar() == '[') {
				// An array
				lexer.nextChar();
				lexer.skipWhitespaces();

				StringBuilder sbNumber = new StringBuilder();
				int numberStart = lexer.getPosition();
				while (!lexer.isEOF() && lexer.peekChar() != ']') sbNumber.append(lexer.nextChar());
				
				String errorStr = sbNumber.toString().trim();
				if (errorStr.isEmpty()) {
					stream.addError(new DocumentError("Array length not specified", getPos(numberStart-1), getPos(lexer.getPosition()+1), startLineNumber));
					return -1;
				}
				
				// Eat the ]
				lexer.nextChar();
				
				try {
					return HashManager.get().int32(sbNumber.toString().trim());
				} 
				catch (Exception e) {
					stream.addError(new DocumentError("Wrong number: " + e.getLocalizedMessage(), getPos(numberStart), getPos(lexer.getPosition() - 1), startLineNumber));
					return -1;
				}
			}
			
			return 0;
		}
		
		private int getPos(int i) {
			return posMap.getRealPosition(i) - stream.getLinePositions().get(startLineNumber);
		}
		
		@Override public boolean processLine(String line) {
			// Try to keep empty lines for readability
			int lineNumber = stream.getCurrentLine();
			for (int i = lastLineNumber+1; i < lineNumber; ++i) sb.append('\n');
			lastLineNumber = lineNumber;
			
			int removedTabs = 0;
			while (removedTabs < 2 && removedTabs < line.length() && 
					Character.isWhitespace(line.charAt(removedTabs))) ++removedTabs;
			line = line.substring(removedTabs);
			
			if (!stream.isFastParsing()) {
				// For syntax highlighting
				posMap.addEntry(sb.length(), stream.getLinePositions().get(lineNumber) + removedTabs);
			}
			
			sb.append(line);
			sb.append('\n');
			
			return true;
		}
	}
	
	protected static void replaceCodeIndices(BufferedWriter out, String code, int texcoordIndex, int samplerIndex) throws IOException {
		int indexOf;
		int lastIndex = 0;
		while ((indexOf = code.indexOf("<", lastIndex)) != -1) {
			out.write(code.substring(lastIndex, indexOf));
			
			if (code.charAt(indexOf + 3) == '>' 
					&& Character.isAlphabetic(code.charAt(indexOf + 1))
					&& Character.isDigit(code.charAt(indexOf + 2))) {
				
				int index = Character.getNumericValue(code.charAt(indexOf + 2));
				
				switch (code.charAt(indexOf + 2)) {
				case 's':
					index += samplerIndex;
					break;
				case 't':
					index += texcoordIndex;
					break;
				}
				
				out.write(Integer.toString(index));
				
				lastIndex = indexOf+4;
			}
			else {
				// We didn't append the <
				out.append('<');
				lastIndex = indexOf+1;
			}
		}
		
		out.write(code.substring(lastIndex));
	}
	
	protected static List<ShaderDataUniform> generateDeclareCode(BufferedWriter out, List<? extends ShaderFragment> fragments) throws IOException {
		final List<ShaderDataUniform> uniforms = new ArrayList<>();
		
		int register = 0;
		Set<Integer> setData = new HashSet<>();
		for (ShaderFragment frag : fragments) {
			for (ShaderDataUniform uniform : frag.shaderData) {
				if (! setData.contains(uniform.dataIndex)) {
					out.write("extern uniform " + uniform.name + " : register(c" + register + ");");
					out.newLine();
					register += uniform.registerSize;
					setData.add(uniform.dataIndex);
					uniforms.add(uniform);
				}
			}
		}
		
		for (ShaderFragment frag : fragments) {
			if (frag.declareCode != null && !frag.declareCode.isEmpty()) {
				out.newLine();
				out.write(frag.declareCode);
			}
		}
		
		return uniforms;
	}
}
