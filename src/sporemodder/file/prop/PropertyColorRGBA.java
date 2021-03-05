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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGBA;

public class PropertyColorRGBA extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0034;
	public static final String KEYWORD = "colorRGBA";
	public static final int ARRAY_SIZE = 16;

	private ColorRGBA[] values;
	
	public PropertyColorRGBA() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyColorRGBA(ColorRGBA value) {
		super(TYPE_CODE, 0);
		this.values = new ColorRGBA[] {value};
	}
	
	public PropertyColorRGBA(ColorRGBA ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyColorRGBA(List<ColorRGBA> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new ColorRGBA[values.size()]);
	}
	
	public ColorRGBA[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new ColorRGBA[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			ColorRGBA value = new ColorRGBA();
			value.readLE(stream);
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (ColorRGBA value : values) {
			value.writeLE(stream);
		}
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (ColorRGBA value : values) {
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
			ColorRGBA value = new ColorRGBA();
			if (line.getArguments(args, 2) && stream.parseColorRGBA(args, 1, value)) {
				parser.getData().add(args.get(0), new PropertyColorRGBA(value));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<ColorRGBA> values = new ArrayList<ColorRGBA>();
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
				ColorRGBA dst = new ColorRGBA();
				line.fromLine(text, null);
				line.getSplitsAsArguments(args);
				
				stream.parseColorRGBA(args, 0, dst);
				values.add(dst);
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyColorRGBA(values));
				stream.endSpecialBlock();
			}
		});
	}
}
