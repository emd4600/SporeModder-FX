/****************************************************************************
* Copyright (C) 2018 Eric Mor
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlSyntax implements SyntaxFormatFactory {
	
	/*
	 * (?<Pattern>  - the capturing group Pattern
	 *   (			- start 1st case: tag start
	 *     </?		- < and optionally /
	 *     \\w+		- a word (int16, proP_what5ever)
	 *   )			- end 1st case
	 *   |			- another case
	 *   (			- start 2nd case: tag end
	 *     (?<=		- ensure it is preceded by the following characters (positive lookbehind)
	 *       </?\\w{1,100}.{1,100}	- <, optionally /, a name of [1,100] characters and then any characters, between [1,100]; this way we ensure we are closing a tag
	 *     )		- end lookbehind
	 *     /?>		- optionally / and necessary >
	 *   )			- end 2nd case
	 * ) 			- end Pattern
	 */
	private static final String TAG_PATTERN = "(?<Pattern>(</?\\w+)|((?<=<\\w{1,100}.{1,100})/?>))";
	
	/*
	 * (?<Attribute> 	- the capturing group Attribute
	 *   \\s			- we need a whitespace before it
	 *   \\w+			- one or more word characters
	 *   (?=			- ensure it is followed by = (positive lookahead)
	 *     \\=			- the character =
	 *   )				- end lookahead
	 * ) 				- end Attribute
	 */
	private static final String TAG_ATTRIBUTE = "(?<Attribute>\\s\\w+(?=\\=))";
	
	/*
	 * (?<AttributeValue> 	- the capturing group AttributeValue
	 *   (?<=			- ensure it is preceded by the following characters (positive lookbehind)
	 *     \\w+			- one or more word characters (the attribute name)
	 *     \\=			- the character =
	 *   )				- end lookbehind
	 *   \"				- the " character
	 *   [~#-\\.\\w]+			- one or more word characters
	 *   \"				- the " character
	 * ) 				- end AttributeValue
	 */
	private static final String TAG_ATTRIBUTE_VALUE = "(?<AttributeValue>(?<=\\w+\\=)\"[~#-\\.\\w]+\")";
	
	/*
	 * (?<Comment>	 	- the capturing group Comment
	 *   <\\!--			- the characters <!--
	 *   .*				- any characters
	 *   -->			- the characters -->
	 * ) 				- end Attribute
	 */
	private static final String TAG_COMMENT = "(?<Comment><\\!--.*-->)";
	
	private static final String TAG_CDATA = "(?<CData><\\!\\[CDATA\\[.*\\]\\]>)";
	
//	private static final String TAG_PATTERN_END = "(>)?";
//	private static final String TAG_CLOSING_PATTERN = "(/>)";
//	private static final String TAG_ATTRIBUTE_PATTERN = "\\s(\\w*)\\=";
//	//private static final String TAG_ATTRIBUTE_VALUE = "[a-z-]*\\=(\"[^\"]*\")"; // [a-z-]
//    //private static final String TAG_COMMENT = "(<!--.*-->)";
//    private static final String TAG_CDATA_START = "(\\<!\\[CDATA\\[).*";
//    private static final String TAG_CDATA_END = ".*(]]>)";
    
	private static final Pattern PATTERN = Pattern.compile(
			"(" + TAG_PATTERN + ")" + 
			"|(" + TAG_ATTRIBUTE + ")" + 
			"|(" + TAG_ATTRIBUTE_VALUE + ")" +
			"|(" + TAG_COMMENT + ")" +
			"|(" + TAG_CDATA + ")"
	);
    

	@Override
	public boolean isSupportedFile(File file) {
		try {
			// Unfortunately, this syntax highlighter is too slow for "big" files
			return (
				(file.getName().endsWith(".xml") || file.getName().endsWith(".eapdPixie")) &&
				Files.size(file.toPath()) < 10240
			);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public void generateStyle(String text, SyntaxHighlighter syntax) {
		Matcher matcher = PATTERN.matcher(text);
		
		while (matcher.find()) {
			
			String styleClass = null;
			
			if (matcher.group("CData") != null) {
				styleClass = "xml-cdata";
			}
			else if (matcher.group("Comment") != null) {
				styleClass = "xml-comment";
			}
			else if (matcher.group("Attribute") != null) {
				styleClass = "xml-attribute";
			}
			else if (matcher.group("AttributeValue") != null) {
				styleClass = "xml-attribute_value";
			} 
			else if (matcher.group("Pattern") != null) {
				styleClass = "xml-pattern";
			}
			
			syntax.add(matcher.start(), matcher.end() - matcher.start(), Collections.singleton(styleClass));
		}
	}

//	@Override
//	public StyleSpans<Collection<String>> generateStyle(String text) {
//		
//		Matcher matcher = PATTERN.matcher(text);
//		int lastEnd = 0;
//		
//		StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<Collection<String>>();
//		
//		while (matcher.find()) {
//			
//			String styleClass = null;
//			
//			if (matcher.group("CData") != null) {
//				styleClass = "xml-cdata";
//			}
//			else if (matcher.group("Comment") != null) {
//				styleClass = "xml-comment";
//			}
//			else if (matcher.group("Attribute") != null) {
//				styleClass = "xml-attribute";
//			}
//			else if (matcher.group("AttributeValue") != null) {
//				styleClass = "xml-attribute_value";
//			} 
//			else if (matcher.group("Pattern") != null) {
//				styleClass = "xml-pattern";
//			}
//			
//			
//			builder.add(Collections.emptyList(), matcher.start() - lastEnd);
//			builder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
//			
//			lastEnd = matcher.end();
//		}
//		
//		// Add the remaining text
//		builder.add(Collections.emptyList(), text.length() - lastEnd);
//		
//		return builder.create();
//	}
}
