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
import sporemodder.util.ColorRGB;

public class PropertyColorRGB extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0032;
	public static final String KEYWORD = "colorRGB";
	public static final int ARRAY_SIZE = 12;

	private ColorRGB[] values;
	
	public PropertyColorRGB() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyColorRGB(ColorRGB value) {
		super(TYPE_CODE, 0);
		this.values = new ColorRGB[] {value};
	}
	
	public PropertyColorRGB(ColorRGB ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyColorRGB(List<ColorRGB> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new ColorRGB[values.size()]);
	}
	
	public ColorRGB[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new ColorRGB[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			ColorRGB value = new ColorRGB();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(4);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (ColorRGB value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(4);
		}
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (ColorRGB value : values) {
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
			ColorRGB value = new ColorRGB();
			if (line.getArguments(args, 2) && stream.parseColorRGB(args, 1, value)) {
				parser.getData().add(args.get(0), new PropertyColorRGB(value));
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<ColorRGB> values = new ArrayList<ColorRGB>();
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
				ColorRGB dst = new ColorRGB();
				line.fromLine(text, null);
				line.getSplitsAsArguments(args);
				
				stream.parseColorRGB(args, 0, dst);
				values.add(dst);
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyColorRGB(values));
				stream.endSpecialBlock();
			}
		});
	}
}
