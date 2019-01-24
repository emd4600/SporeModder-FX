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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.Vector4;

public class PropertyVector4 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0033;
	public static final String KEYWORD = "vector4";
	public static final int ARRAY_SIZE = 16;

	private Vector4[] values;
	
	public PropertyVector4() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyVector4(Vector4 value) {
		super(TYPE_CODE, 0);
		this.values = new Vector4[] {value};
	}
	
	public PropertyVector4(Vector4 ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyVector4(List<Vector4> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Vector4[values.size()]);
	}
	
	public Vector4[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Vector4[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			Vector4 value = new Vector4();
			value.readLE(stream);
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Vector4 value : values) {
			value.writeLE(stream);
		}
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (Vector4 value : values) {
				writer.indentNewline();
				writer.arguments(value.toString());
			}
			writer.endBlock();
			writer.commandEND();
		} 
		else {
			writer.command(KEYWORD).arguments(propertyName, values[0].toString());
		}
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			float[] value = new float[4];
			if (line.getArguments(args, 2) && stream.parseVector4(args, 1, value)) {
				parser.getData().add(args.get(0), new PropertyVector4(new Vector4(value)));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Vector4> values = new ArrayList<Vector4>();
			final ArgScriptLine line = new ArgScriptLine(stream);
			final ArgScriptArguments args = new ArgScriptArguments();
			
			@Override
			public void parse(ArgScriptLine line) {
				values.clear();
				stream.startSpecialBlock(this, "end");
				
				if (line.getArguments(args, 1)) {
					propertyName = args.get(0);
				}
			}
			
			@Override
			public boolean processLine(String text) {
				float[] dst = new float[4];
				line.fromLine(text, null);
				line.getSplitsAsArguments(args);
				
				stream.parseVector4(args, 0, dst);
				values.add(new Vector4(dst));
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyVector4(values));
				stream.endSpecialBlock();
			}
		});
	}
}
