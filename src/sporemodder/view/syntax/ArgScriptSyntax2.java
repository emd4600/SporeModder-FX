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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import sporemodder.file.argscript.ArgScriptSyntaxHighlighting;

public abstract class ArgScriptSyntax2 implements SyntaxFormatFactory {
	
	/*
	 * (?<Comment>	 	- the capturing group Comment
	 *   (				- start block comment group
	 *     #<			- the characters #<
	 *     .*			- any characters
	 *     #>			- the characters #>
	 *   )				- end block comment group
	 *   |				- or
	 *   (				- start line comment group
	 *     #			- the character #
	 *     .*			- any characters
	 *     $			- the end of the line
	 *   )				- end line comment group
	 * ) 				- end Attribute
	 */
	private static final String TAG_COMMENT = "(?<Comment>(#<.*#>)|(#(?![><]).*$))";
	//private static final String TAG_COMMENT = "(?<Comment>(#<.*))";
	
	private final Pattern pattern;
	
	public ArgScriptSyntax2(ArgScriptSyntaxHighlighting syntax) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("(?m)(?s)");
		
		sb.append('(');
		sb.append(TAG_COMMENT);
		sb.append(')');
		sb.append('!');
		
		sb.append('(');
		
		sb.append("(?<Block>");
		List<String> blocks = syntax.getBlocks();
		
		sb.append("asd");
		
//		for (int i = 0; i < blocks.size(); i++) {
//			sb.append('(');
//			sb.append("^\\s*");
//			sb.append(blocks.get(i));
//			sb.append("\\s+");
//			sb.append(')');
//			
//			if (i + 1 != blocks.size()) {
//				sb.append('!');
//			}
//		}
		
		sb.append(')');
		
		sb.append(')');
		
		System.out.println(sb.toString());
		
		pattern = Pattern.compile(sb.toString());
	}
    
    @Override
    public String getStylesheetPath() {
    	//TODO first try to get from 'SporeModder\Syntax\', otherwise get a failsafe from inside the .jar
    	return XmlSyntax.class.getResource("/sporemodder/resources/styles/ArgScriptSyntax.css").toExternalForm();
    }

	@Override
	public StyleSpans<Collection<String>> generateStyle(String text) {
		
		Matcher matcher = pattern.matcher(text);
		int lastEnd = 0;
		
		StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<Collection<String>>();
		
		while (matcher.find()) {
			
			String styleClass = null;
			
			if (matcher.group("Comment") != null) {
				styleClass = "argscript-comment";
			}
			else if (matcher.group("Block") != null) {
				styleClass = "argscript-block";
			}
			
			
			builder.add(Collections.emptyList(), matcher.start() - lastEnd);
			builder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			
			lastEnd = matcher.end();
		}
		
		// Add the remaining text
		builder.add(Collections.emptyList(), text.length() - lastEnd);
		
		return builder.create();
	}
}
