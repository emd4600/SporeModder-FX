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

public class PropertyInt8 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0005;
	public static final String KEYWORD = "int8";
	public static final int ARRAY_SIZE = 1;
	
	public static final long MIN_VALUE = Byte.MIN_VALUE;
	public static final long MAX_VALUE = Byte.MAX_VALUE;
	
	private byte[] values;
	
	public PropertyInt8() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyInt8(byte value) {
		super(TYPE_CODE, 0);
		this.values = new byte[] {value};
	}
	
	public PropertyInt8(byte ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyInt8(List<Byte> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new byte[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public byte[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new byte[itemCount];
		stream.readBytes(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (byte value : values) {
			stream.writeByte(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeByte(HashManager.get().int8(text));
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (byte value : values) {
				writer.indentNewline();
				writer.arguments(Byte.toString(value));
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName, Byte.toString(values[0]));
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			Number value = null;
			
			if (line.getArguments(args, 2) && (value = stream.parseByte(args, 1)) != null) {
				parser.getData().add(args.get(0), new PropertyInt8(value.byteValue()));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Byte> values = new ArrayList<Byte>();
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
						stream.addError(new DocumentError(String.format("8-bit Integer out of the range (%d, %d).", MIN_VALUE, MAX_VALUE), 0, line.length()));
					}
					else {
						values.add((byte) value);
					}
				}
				catch (DocumentException e) {
					stream.addError(e.getError());
				}
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyInt8(values));
				stream.endSpecialBlock();
			}
		});
	}
}
