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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLexer;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PropertyInt16 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0007;
	public static final String KEYWORD = "int16";
	public static final int ARRAY_SIZE = 2;
	
	public static final long MIN_VALUE = Short.MIN_VALUE;
	public static final long MAX_VALUE = Short.MAX_VALUE;

	private short[] values;
	
	public PropertyInt16() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyInt16(short value) {
		super(TYPE_CODE, 0);
		this.values = new short[] {value};
	}
	
	public PropertyInt16(short ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyInt16(List<Short> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new short[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public short[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new short[itemCount];
		stream.readShorts(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (short value : values) {
			stream.writeShort(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeShort(HashManager.get().int16(text));
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (short value : values) {
				writer.indentNewline();
				writer.arguments(Short.toString(value));
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName, Short.toString(values[0]));
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			Number value = null;
			
			if (line.getArguments(args, 2) && (value = stream.parseInt(args, 1, MIN_VALUE, MAX_VALUE)) != null) {
				parser.getData().add(args.get(0), new PropertyInt16(value.shortValue()));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Short> values = new ArrayList<Short>();
			final ArgScriptLexer lexer = new ArgScriptLexer();
			
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
				lexer.setText(line.trim());
				
				try {
					long value = lexer.parseInteger();
					
					if (value > MAX_VALUE || value < MIN_VALUE) {
						stream.addError(new DocumentError(String.format("16-bit Integer out of the range (%d, %d).", MIN_VALUE, MAX_VALUE), 0, line.length()));
					}
					else {
						values.add((short) value);
					}
				}
				catch (DocumentException e) {
					stream.addError(e.getError());
				}
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyInt16(values));
				stream.endSpecialBlock();
			}
		});
	}
}
