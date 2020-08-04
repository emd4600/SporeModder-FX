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
package sporemodder.view.syntax;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sporemodder.file.TextUtils;
import sporemodder.view.editors.TextEditor;

public class HlslSyntax implements SyntaxFormatFactory {

	private static final String[] TAGS_ENUMS = new String[] {
			// Vertex Shader input semantics
			"BINORMAL", "BINORMAL0", "BINORMAL1", "BINORMAL2", "BINORMAL3", "BINORMAL4", 
			"BLENDINDICES", "BLENDINDICES0", "BLENDINDICES1", "BLENDINDICES2", "BLENDINDICES3", "BLENDINDICES4", 
			"BLENDWEIGHT", "BLENDWEIGHT0", "BLENDWEIGHT1", "BLENDWEIGHT2", "BLENDWEIGHT3", "BLENDWEIGHT4", 
			"COLOR", "COLOR0", "COLOR1", "COLOR2", "COLOR3", "COLOR4", 
			"NORMAL", "NORMAL0", "NORMAL1", "NORMAL2", "NORMAL3", "NORMAL4", 
			"POSITION", "POSITION0", "POSITION1", "POSITION2", "POSITION3", "POSITION4", "POSITIONT",
			"PSIZE", "PSIZE0", "PSIZE1", "PSIZE2", "PSIZE3", "PSIZE4", 
			"TANGENT", "TANGENT0", "TANGENT1", "TANGENT2", "TANGENT3", "TANGENT4", 
			"TESSFACTOR", "TESSFACTOR0", "TESSFACTOR1", "TESSFACTOR2", "TESSFACTOR3", "TESSFACTOR4", 
			"TEXCOORD", "TEXCOORD0", "TEXCOORD1", "TEXCOORD2", "TEXCOORD3", "TEXCOORD4", "TEXCOORD5", "TEXCOORD6", "TEXCOORD7", "TEXCOORD8",
			// Vertex Shader output Symantics
			"FOG", "PSIZE",
			// Pixel Shader input Symantics
			"VFACE", "VPOS",
			// Pixel Shader output Symantics
			"DEPTH", "DEPTH0", "DEPTH1", "DEPTH2", "DEPTH3", "DEPTH4", "DEPTH5"
			};
	
	private static final String[] TAGS_TYPES = new String[] {
			// Basic types
			"BOOL", "bool", "int", "half", "float", "double", "sampler", "string",
			"sampler1D", "sampler2D", "sampler3D", "samplerCUBE",
			// Vector and matrix types
			"bool1", "bool2", "bool3", "bool4", 
			"BOOL1", "BOOL2", "BOOL3", "BOOL4",
			"int1", "int2", "int3", "int4", 
			"half1", "half2", "half3", "half4", 
			"float1", "float2", "float3", "float4", 
			"double1", "double2", "double3", "double4", 
			"vector", 
			"bool1x1", "bool1x2", "bool1x3", "bool1x4", 
			"bool2x1", "bool2x2", "bool2x3", "bool2x4", 
			"bool3x1", "bool3x2", "bool3x3", "bool3x4", 
			"bool4x1", "bool4x2", "bool4x3", "bool4x4", 
			"BOOL1x1", "BOOL1x2", "BOOL1x3", "BOOL1x4", 
			"BOOL2x1", "BOOL2x2", "BOOL2x3", "BOOL2x4", 
			"BOOL3x1", "BOOL3x2", "BOOL3x3", "BOOL3x4", 
			"BOOL4x1", "BOOL4x2", "BOOL4x3", "BOOL4x4", 
			"half1x1", "half1x2", "half1x3", "half1x4", 
			"half2x1", "half2x2", "half2x3", "half2x4", 
			"half3x1", "half3x2", "half3x3", "half3x4", 
			"half4x1", "half4x2", "half4x3", "half4x4", 
			"int1x1", "int1x2", "int1x3", "int1x4", 
			"int2x1", "int2x2", "int2x3", "int2x4", 
			"int3x1", "int3x2", "int3x3", "int3x4", 
			"int4x1", "int4x2", "int4x3", "int4x4", 
			"float1x1", "float1x2", "float1x3", "float1x4", 
			"float2x1", "float2x2", "float2x3", "float2x4", 
			"float3x1", "float3x2", "float3x3", "float3x4", 
			"float4x1", "float4x2", "float4x3", "float4x4", 
			"double1x1", "double1x2", "double1x3", "double1x4", 
			"double2x1", "double2x2", "double2x3", "double2x4", 
			"double3x1", "double3x2", "double3x3", "double3x4", 
			"double4x1", "double4x2", "double4x3", "double4x4", 
			"matrix", "vertexshader", "pixelshader"
	};
	
	private static final String[] TAGS_KEYWORDS = new String[] {
			"struct", "extern", "shared", "static", "uniform", "volatile", "const", "row_major", "column_major", 
			"pack_matrix", "warning", "def", "once", "default", "disable", "error", 
			"vs", "vs_1_1", "vs_2_0", "vs_2_a", "ps", "ps_1_1", "ps_1_2", "ps_1_3", "ps_1_4", "ps_2_0", "ps_2_a", 
			"__FILE__", "__LINE__", "asm", "asm_fragment", "compile", "compile_fragment", "discard", "decl", 
			"do", "else", "false", "for", "if", "in", "inline", "inout", "out", 
			"pass", /*"pixelfragment",*/ "return", "register", "sampler_state", "shared", "stateblock", "stateblock_state", 
			"technique", "true", "typedef", "uniform", /*"vertexfragment",*/ "void", "volatile", "while"
	};
	
	private static final String[] TAGS_FUNCTIONS = new String[] {
			"abs", "acos", "all", "any", "asin", "atan", "atan2", "ceil", "clamp", "clip", "cos", "cosh", "cross", 
			"D3DCOLORtoUBYTE4", "ddx", "ddy", "degrees", "determinant", "distance", "dot", "exp", "exp2", "faceforward", 
			"floor", "fmod", "frac", "frexp", "fwidth", "isfinite", "isinf", "isnan", "ldexp", "length", "lerp", "lit", 
			"log", "log10", "log2", "max", "min", "modf", "mul", "noise", "normalize", "pow", "radians", "reflect", 
			"refract", "round", "rsqrt", "saturate", "sign", "sin", "sincos", "sinh", "smoothstep", "sqrt", "step", 
			"tan", "tanh", "tex1D", "tex1D", "tex1Dbias", "tex1Dgrad", "tex1Dlod", "tex1Dproj", 
			"tex2D", "tex2D", "tex2Dbias", "tex2Dgrad", "tex2Dlod", "tex2Dproj", "tex3D", "tex3D", 
			"tex3Dbias", "tex3Dgrad", "tex3Dlod", "tex3Dproj", 
			"texCUBE", "texCUBE", "texCUBEbias", "texCUBEgrad", "texCUBElod", "texCUBEproj", "transpose"
	};
	
	private static final String HLSL_COMMENTS = "hlsl-comments";
	
	private static Map<Pattern, String> patterns = new LinkedHashMap<>();
	
	static {
	    // NOTE: the order is important!
	    
	    patterns.put(Pattern.compile("\\W+(\\d*\\.?\\d*)\\W"), "hlsl-numbers");
	    
	    for (String s : TAGS_ENUMS) {
	    	patterns.put(Pattern.compile("\\W+(" + s + ")\\W"), "hlsl-enums");
	    }
	    for (String s : TAGS_TYPES) {
	    	patterns.put(Pattern.compile("^(" + s + ")\\W"), "hlsl-types");
	    	patterns.put(Pattern.compile("\\W(" + s + ")\\W"), "hlsl-types");
	    }
	    for (String s : TAGS_KEYWORDS) {
	    	patterns.put(Pattern.compile("^(" + s + ")\\W"), "hlsl-keywords");
	    	patterns.put(Pattern.compile("\\W+(" + s + ")\\W"), "hlsl-keywords");
	    }
	    for (String s : TAGS_FUNCTIONS) {
	    	patterns.put(Pattern.compile("\\W(" + s + ")\\("), "hlsl-functions");
	    }
	}

	@Override public void generateStyle(String text, SyntaxHighlighter syntax) {
		
		SyntaxHighlighter commentsSyntax = new SyntaxHighlighter();
		
		// Process block comments
		int indexOf = text.indexOf("/*");
		while (indexOf != -1) {
			int endIndex = text.indexOf("*/", indexOf + 2);
			
			// It's an error, but we comment all the following text
			if (endIndex == -1) {
				commentsSyntax.add(indexOf, text.length() - indexOf, Collections.singleton(HLSL_COMMENTS));
				break;
			} else {
				// + 2 because we also include the ending */
				commentsSyntax.add(indexOf, endIndex - indexOf + 2, Collections.singleton(HLSL_COMMENTS));
			}
			
			indexOf = text.indexOf("/*", endIndex + 2);
		}
		
		// Process line comments
		indexOf = text.indexOf("//");
		while (indexOf != -1) {
			int endIndex = text.indexOf("\n", indexOf + 2);
			// End of the file
			if (endIndex == -1) {
				commentsSyntax.add(indexOf, text.length() - indexOf, Collections.singleton(HLSL_COMMENTS));
				break;
			} else {
				commentsSyntax.add(indexOf, endIndex - indexOf, Collections.singleton(HLSL_COMMENTS));
			}
			indexOf = text.indexOf("//", endIndex + 1);
		}
		
		for (Map.Entry<Pattern, String> entry : patterns.entrySet()) {
			Matcher matcher = entry.getKey().matcher(text);
			
			while (matcher.find()) {
				int start = matcher.start(1);
				int end = matcher.end(1);
				
				if (start != end) syntax.add(start, end - start, Collections.singleton(entry.getValue()));
			}
		}
		
		// We remove the existing syntax if it collides with comments
		syntax.addExtras(commentsSyntax, true);
	}

	@Override public boolean isSupportedFile(File file) {
		return file.isFile() && file.getName().endsWith(".hlsl");
	}
	
	private void removeBlockComment(TextEditor editor, String text, int textStart, int textEnd) {
		// We use replaceText instead of multiple deleteText so that it goes into a single undoable action
		text = text.substring(0, textStart) + text.substring(textStart + 2, textEnd - 1) + text.substring(textEnd + 1);
		editor.getCodeArea().replaceText(text);
		editor.getCodeArea().selectRange(textStart, textEnd - 3);
	}
	
	@Override public boolean toggleBlockComment(TextEditor editor, int start, int end) {
		if (end - start <= 0) return true;
		String text = editor.getText();
		int textStart = TextUtils.scanNextWordStart(text, start);
		if (textStart != -1 && textStart + 1 < text.length() && 
				text.charAt(textStart) == '/')
		{
			if (text.charAt(textStart + 1) == '*') 
			{
				// If it's a block comment, uncomment
				int textEnd = TextUtils.scanPreviousWordEnd(text, end);
				if (textEnd > 1 && text.charAt(textEnd) == '/' && text.charAt(textEnd - 1) == '*') 
				{
					removeBlockComment(editor, text, textStart, textEnd);
					return true;
				}
			}
			else {
				// Special case: if there are multiple lines with '#', uncomment them
				List<Integer> lineComments = new ArrayList<>();
				lineComments.add(textStart);
				boolean multipleLineComment = true;
				int pos = textStart;
				while ((pos = TextUtils.scanNextWordStart(text, TextUtils.scanLineEnd(text, pos))) != -1 && pos < end) {
					if (!(text.charAt(pos) == '/' && pos + 1 < text.length() && text.charAt(pos + 1) == '/')) {
						multipleLineComment = false;
						break;
					}
					lineComments.add(pos);
				}
				
				if (multipleLineComment) {
					StringBuilder sb = new StringBuilder();
					int lastPos = 0;
					for (int p : lineComments) {
						sb.append(text.substring(lastPos, p));
						lastPos = p + 2;
					}
					sb.append(text.substring(lastPos));
					editor.getCodeArea().replaceText(sb.toString());
					editor.getCodeArea().selectRange(textStart, end - 2*lineComments.size());
					return true;
				}
				else {
					// If it's a block comment, uncomment
					int textEnd = TextUtils.scanPreviousWordEnd(text, end);
					if (textEnd > 1 && text.charAt(textEnd) == '/' && text.charAt(textEnd - 1) == '*')  {
						removeBlockComment(editor, text, textStart, textEnd);
						return true;
					}
				}
			}
		}
		
		text = text.substring(0, start) + "/*" + text.substring(start, end) + "*/" + text.substring(end);
		editor.getCodeArea().replaceText(text);
		editor.getCodeArea().selectRange(start, end + 4);
		
		return true;
	}
	
	
	@Override public boolean toggleLineComment(TextEditor editor, int position) {
		int originalPosition = position;
		String text = editor.getText();
		
		if (position >= text.length() || TextUtils.isNewLine(text, position)) position--;
		position = TextUtils.scanLineStart(text, position);
		
		boolean removeComment;

		int wordStart = TextUtils.scanNextWordStart(text, position);
		if (wordStart == -1) {
			// end of stream, no text in the line
			removeComment = false;
		}
		else if (wordStart < TextUtils.scanLineEnd(text, position) && wordStart < text.length()) {
			position = wordStart;
			removeComment = false;
			if (text.charAt(wordStart) == '/' && wordStart + 1 < text.length()) {
				// if it's the beginning of a block comment, comment after that
				if (text.charAt(wordStart + 1) == '*') {
					removeComment = false;
					position += 2;
				}
				else if (text.charAt(wordStart + 1) == '/') {
					removeComment = true;
				}
			}
		}
		else {
			// We are on an empty line
			removeComment = false;
		}
		
		int moveTo = originalPosition;
		if (removeComment) {
			editor.getCodeArea().deleteText(position, position + 2);
			
			if (originalPosition > wordStart) moveTo -= 2;
		}
		else {
			editor.getCodeArea().insertText(position, "//");

			if (originalPosition > wordStart) moveTo += 2;
		}
		
		editor.getCodeArea().moveTo(moveTo);
		
		return true;
	}
}
