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
package sporemodder.file.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PropertyString8 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0012;
	public static final String KEYWORD = "string8";
	public static final int ARRAY_SIZE = 8;

	private String[] values;
	
	public PropertyString8() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyString8(String value) {
		super(TYPE_CODE, 0);
		this.values = new String[] {value};
	}
	
	public PropertyString8(String ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyString8(List<String> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new String[values.size()]);
	}
	
	public String[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new String[itemCount];
		for (int i = 0; i < itemCount; i++) {
			values[i] = stream.readString(StringEncoding.ASCII, stream.readInt());
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (String value : values) {
			stream.writeInt(value.length());
			stream.writeString(value, StringEncoding.ASCII);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeInt(text.length());
		stream.write(text.getBytes("US-ASCII"));
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (String value : values) {
				writer.indentNewline();
				writer.literal(PropConverter.intoValidText(value));
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName);
			writer.literal(values[0]);
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 2)) {
				parser.getData().add(args.get(0), new PropertyString8(PropConverter.intoOriginalText(args.get(1))));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<String> values = new ArrayList<String>();
			
			@Override
			public void parse(ArgScriptLine line) {
				values.clear();
				stream.startSpecialBlock(this, "end");
				
				if (line.getArguments(args, 1)) {
					propertyName = args.get(0);
				}
			}
			
			@Override
			public boolean processLine(String line) {
				String trimmedLine = line.trim();
				if (trimmedLine.charAt(0) != '"') {
					// We do 1, 2 as we assume the first character is a tabulation
					stream.addError(new DocumentError("String literals must start and end with \" symbols.", 1, 2));
					return true;
				}
				if (trimmedLine.charAt(trimmedLine.length()-1) != '"') {
					stream.addError(new DocumentError("String literals must start and end with \" symbols.", line.length() - 1, line.length()));
					return true;
				}
				
				values.add(PropConverter.intoOriginalText(trimmedLine.substring(1, trimmedLine.length() - 2)));
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyString8(values));
				stream.endSpecialBlock();
			}
		});
	}
}
