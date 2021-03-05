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
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLexer;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PropertyFloat extends BaseProperty {
	
	public static final int TYPE_CODE = 0x000D;
	public static final String KEYWORD = "float";
	public static final int ARRAY_SIZE = 4;

	private float[] values;
	
	public PropertyFloat() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyFloat(float value) {
		super(TYPE_CODE, 0);
		this.values = new float[] {value};
	}
	
	public PropertyFloat(float ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyFloat(List<Float> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new float[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public float[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new float[itemCount];
		stream.readFloats(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (float value : values) {
			stream.writeFloats(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeFloat(Float.parseFloat(text));
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			HashManager hasher = HashManager.get();
			for (float value : values) {
				writer.indentNewline();
				writer.arguments(hasher.floatToString(value));
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName, HashManager.get().floatToString(values[0]));
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			Number value = null;
			
			if (line.getArguments(args, 2) && (value = stream.parseFloat(args, 1)) != null) {
				parser.getData().add(args.get(0), new PropertyFloat(value.floatValue()));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Float> values = new ArrayList<Float>();
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
					values.add((float) lexer.parseFloat());
				}
				catch (DocumentException e) {
					stream.addError(e.getError());
				}
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyFloat(values));
				stream.endSpecialBlock();
			}
		});
	}
}
