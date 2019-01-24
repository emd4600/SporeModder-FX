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
import sporemodder.util.Vector2;

public class PropertyVector2 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0030;
	public static final String KEYWORD = "vector2";
	public static final int ARRAY_SIZE = 8;

	private Vector2[] values;
	
	public PropertyVector2() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyVector2(Vector2 value) {
		super(TYPE_CODE, 0);
		this.values = new Vector2[] {value};
	}
	
	public PropertyVector2(Vector2 ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyVector2(List<Vector2> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Vector2[values.size()]);
	}
	
	public Vector2[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Vector2[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			Vector2 value = new Vector2();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(8);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Vector2 value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(8);
		}
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (Vector2 value : values) {
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
			float[] value = new float[2];
			if (line.getArguments(args, 2) && stream.parseVector2(args, 1, value)) {
				parser.getData().add(args.get(0), new PropertyVector2(new Vector2(value)));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<Vector2> values = new ArrayList<Vector2>();
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
				float[] dst = new float[2];
				line.fromLine(text, null);
				line.getSplitsAsArguments(args);
				
				stream.parseVector2(args, 0, dst);
				values.add(new Vector2(dst));
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyVector2(values));
				stream.endSpecialBlock();
			}
		});
	}
}
