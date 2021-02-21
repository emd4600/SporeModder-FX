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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PropertyUnknown extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0000;
	public static final String KEYWORD = "unknown";
	
	public PropertyUnknown() {
		super(TYPE_CODE, 0);
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		stream.skip(16);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writePadding(16);
	}

	@Override
	public void writeArgScript(String propertyName, ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(propertyName);
	}
	
	public static void addParser(ArgScriptStream<PropertyList> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				parser.getData().add(args.get(0), new PropertyUnknown());
			}
		}));
		
	}
}
