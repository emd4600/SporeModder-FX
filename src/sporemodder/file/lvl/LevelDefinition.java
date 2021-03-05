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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.HashManager;
import sporemodder.MainApp;

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
			
			marker.getOffset().readBE(stream);
			marker.getOrientation().readBE(stream);
			stream.readUInts(marker.getIds());
			
			//if version == 2, do something
			//TODO, we ignored it as Spore only has version 3 files
			
			byte[] data = new byte[dataSize];
			stream.read(data);
			
			markers.add(marker);
		}
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		String path = "E:\\Eric\\SporeModder\\Projects\\Spore_Game.package.unpacked\\LevelEditor_Saves~\\bumpy.lvl";
		
		try (FileStream stream = new FileStream(path, "r")) {
			LevelDefinition lvl = new LevelDefinition();
			lvl.read(stream);
			
			for (GameplayMarker marker : lvl.markers) {
				System.out.println(HashManager.get().getFileName((int) marker.getIds()[0]));
			}
		}
	}
}
