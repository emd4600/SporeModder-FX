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
package sporemodder.file.lvl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

/**
 * A level definition is the representation of a .lvl file, which contains gameplay markers. Gameplay markers are used by Spore to determine gameplay aspects of planets,
 * such as the position of cities, tribes, migration points, etc
 */
public class LevelDefinition {
	private final List<GameplayMarker> markers = new ArrayList<GameplayMarker>();
	
	public void read(StreamReader stream) throws IOException {
		
		int version = stream.readInt();
		int count = stream.readInt();
		// All objects have the same size, despite being different in essence; they
		// fill the rest with 0s
		// Apparently, it's always 0x88
		int dataSize = stream.readUShort();
		
		for (int i = 0; i < count; i++) {
			GameplayMarker marker = new GameplayMarker();
			marker.read(stream, version, dataSize);
			markers.add(marker);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(3);
		stream.writeInt(markers.size());
		stream.writeUShort(GameplayMarker.DATA_SIZE);
		for (GameplayMarker marker : markers) {
			marker.write(stream);
		}
	}
	
	public void clear() {
		markers.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (GameplayMarker marker : markers) {
			marker.toArgScript(writer);
			writer.blankLine();
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<LevelDefinition> generateStream() {
		ArgScriptStream<LevelDefinition> stream = new ArgScriptStream<LevelDefinition>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("marker", new ArgScriptBlock<LevelDefinition>() {
			GameplayMarker marker;
			
			@Override
			public void parse(ArgScriptLine line) {
				final ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				
				marker = new GameplayMarker();
				markers.add(marker);
				
				if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
					marker.type = value.intValue();
					
					marker.data = marker.createData();
					
					if (marker.data != null) {
						marker.data.addParsers(this, stream);
					}
				}
				
				if (line.getOptionArguments(args, "id", 2) && (value = stream.parseFileID(args, 0)) != null) {
					marker.id = value.intValue();
					
					if ((value = stream.parseFileID(args, 1)) != null) marker.definitionID = value.intValue();
				}
				
				stream.startBlock(this);
			}
			
			@Override
			public void setData(ArgScriptStream<LevelDefinition> stream_, LevelDefinition data) {
				super.setData(stream_, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				addParser("position", ArgScriptParser.create((parser, line) -> {
					float[] value = new float[3];
					if (line.getArguments(args, 1) && stream.parseVector3(args, 0, value)) {
						marker.offset.set(new Vector3(value));
					}
				}));
				addParser("orientation", ArgScriptParser.create((parser, line) -> {
					float[] value = new float[4];
					if (line.getArguments(args, 1) && stream.parseVector4(args, 0, value)) {
						marker.orientation.set(new Vector4(value));
					}
				}));
				addParser("group", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (marker.data != null && line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						marker.data.group = value.intValue();
					}
				}));
				addParser("propertyCount", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (marker.data != null && line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						marker.data.propertyCount = value.intValue();
					}
				}));
			}
		});
		
		return stream;
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Spore (Game & Graphics)\\LevelEditor_Saves~\\";
		String outputPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Levels\\LevelEditor_Saves~";
		
		File outputFolder = new File(outputPath);
		File folder = new File(path);
		for (File file : folder.listFiles())
		{
			if (file.getName().endsWith(".lvl"))
			{
				System.out.println(file.getName());
				try (FileStream stream = new FileStream(file, "r")) {
					LevelDefinition lvl = new LevelDefinition();
					lvl.read(stream);
					
					lvl.toArgScript().write(new File(outputFolder, file.getName() + ".lvl_t"));
					
					for (GameplayMarker marker : lvl.markers) {
						System.out.println(marker.pos + 
								"     type: " + HashManager.get().getFileName(marker.type) + 
								"     id: " + HashManager.get().getFileName(marker.id) + 
								"     definitionID: " + HashManager.get().getFileName(marker.definitionID));
					}
				}
			}
		}
	}
}
