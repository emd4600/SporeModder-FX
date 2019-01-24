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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PropertyKey extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0020;
	public static final String KEYWORD = "key";
	public static final int ARRAY_SIZE = 12;

	private ResourceKey[] values;
	
	public PropertyKey() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyKey(ResourceKey value) {
		super(TYPE_CODE, 0);
		this.values = new ResourceKey[] {value};
	}
	
	public PropertyKey(ResourceKey ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyKey(List<ResourceKey> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new ResourceKey[values.size()]);
	}
	
	public ResourceKey[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new ResourceKey[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			ResourceKey value = new ResourceKey();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(4);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (ResourceKey value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(4);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text, boolean bArray) throws IOException {
	
		HashManager hasher = HashManager.get();
		
		int[] values = new int[3];
		
		String str = attributes.getValue("groupid");
		if (str == null) str = attributes.getValue("groupID");
		if (str != null && str.length() > 0) {
			values[0] = hasher.getFileHash(str);
		}
		
		str = attributes.getValue("instanceid");
		if (str == null) str = attributes.getValue("instanceID");
		if (str != null && str.length() > 0) {
			values[1] = hasher.getFileHash(str);
		}
		
		str = attributes.getValue("typeid");
		if (str == null) str = attributes.getValue("typeID");
		if (str != null && str.length() > 0) {
			values[2] = hasher.getTypeHash(str);
		}
		
		stream.writeLEInt(values[1]);
		stream.writeLEInt(values[2]);
		stream.writeLEInt(values[0]);
		if (!bArray) {
			stream.writeLEInt(0);
		}
	}
	
	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		if (isArray) {
			writer.command(KEYWORD + "s").arguments(propertyName);
			writer.startBlock();
			for (ResourceKey value : values) {
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
			if (line.getArguments(args, 2)) {
				String[] originals = new String[3];
				ResourceKey key = new ResourceKey();
				key.parse(args, 1, originals);
				parser.getData().add(args.get(0), new PropertyKey(key));
				
				line.addHyperlinkForArgument("key", originals, 1);
			}
		}));
		
		stream.addParser(KEYWORD + "s", new ArgScriptSpecialBlock<PropertyList>() {
			String propertyName;
			final ArrayList<ResourceKey> values = new ArrayList<ResourceKey>();
			
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
				int start = PropertyList.getHyperlinkStart(stream, line);
				line = line.trim();
				
				String[] originals = new String[3];
				ResourceKey key = new ResourceKey();
				key.parse(line.trim(), originals);
				values.add(key);
				
				stream.addHyperlink("key", originals, start, start + line.length());
				
				return true;
			}
			
			@Override
			public void onBlockEnd() {
				stream.getData().add(propertyName, new PropertyKey(values));
				stream.endSpecialBlock();
			}
		});
	}
}
