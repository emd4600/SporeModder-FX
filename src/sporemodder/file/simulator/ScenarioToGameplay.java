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
package sporemodder.file.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sporemodder.file.filestructures.FileStream;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.ResourceKey;
import sporemodder.file.prop.PropertyKey;
import sporemodder.file.prop.PropertyList;
import sporemodder.file.prop.PropertyTransform;
import sporemodder.util.Transform;

public class ScenarioToGameplay {
	private PropertyList createGameplayPlanetScript() {
		HashManager hasher = HashManager.get();
		PropertyList planetList = new PropertyList();
		
		// Add a parent
		planetList.getProperties().put(0x00b2cccb, new PropertyKey(
				new ResourceKey(hasher.getFileHash("planetTerrainThemes3~"), hasher.getFileHash("AllPlanets"), hasher.getTypeHash("prop"))));
		
//		planetList.getProperties().put(0x00b2cccb, new PropertyKey(
//				new ResourceKey(hasher.getFileHash("planetTerrainThemes2~"), 0x7AB1C9A7, hasher.getTypeHash("prop"))));
		
		planetList.addList(propertyList);
		
		// Now we must add the objects
		final List<ResourceKey> modelKeys = new ArrayList<ResourceKey>();
		final List<Transform> modelTransforms = new ArrayList<Transform>();
		
		PropertyKey propKeys = planetList.get("terrainScriptModels", PropertyKey.class);
		if (propKeys != null) modelKeys.addAll(Arrays.asList(propKeys.getValues()));
		
		PropertyTransform propTransforms = planetList.get("terrainScriptModelTransforms", PropertyTransform.class);
		if (propTransforms != null) modelTransforms.addAll(Arrays.asList(propTransforms.getValues()));
		
		data.getObjects(modelKeys, modelTransforms);
		
		planetList.add("terrainScriptModels", new PropertyKey(modelKeys));
		planetList.add("terrainScriptModelTransforms", new PropertyTransform(modelTransforms));
		
		return planetList;
	}
	
	public void toGameplayPlanet(File projectPath, String outputName) throws FileNotFoundException {
		// Create all the folders we will use
		File scriptsFolder = new File(projectPath, "planetTerrainScripts~");
		File themesFolder = new File(projectPath, "planetTerrainThemes2~");
		
		scriptsFolder.mkdir();
		themesFolder.mkdir();
		
		createGameplayPlanetScript().toArgScript().write(outputFile);
		try (PrintWriter writer = new PrintWriter(new File(scriptsFolder, outputName + ".prop.prop_t"))) {
			writer.write(createGameplayPlanetScript().toArgScript());
		}
	}
	
	public static void gameplayTest() throws Exception {
		MainApp.testInit();
		
		File projectPath = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Terrain Test");
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Terrain Test\\adventureImages_1~\\0x199C129D.0x366A930D";
		
		try (FileStream stream = new FileStream(path, "r")) {
			
			cScenarioResource resource = new cScenarioResource();
			resource.read(stream);
			
			resource.toGameplayPlanet(projectPath, "bumpy");
			resource.toGameplayPlanet(projectPath, "crystal");
			resource.toGameplayPlanet(projectPath, "cube");
			resource.toGameplayPlanet(projectPath, "domes");
			resource.toGameplayPlanet(projectPath, "layers");
			resource.toGameplayPlanet(projectPath, "mesas");
			resource.toGameplayPlanet(projectPath, "mountains");
			resource.toGameplayPlanet(projectPath, "mushroom");
			resource.toGameplayPlanet(projectPath, "rivers");
			resource.toGameplayPlanet(projectPath, "scallops");
			resource.toGameplayPlanet(projectPath, "scraped");
			resource.toGameplayPlanet(projectPath, "stack");
			resource.toGameplayPlanet(projectPath, "tentacle");
			resource.toGameplayPlanet(projectPath, "thorny");
		}
	}
	
	/*public static void main(String[] args) throws Exception {
//	extractTest();
//	compileTest();
//	
//	extractPropsTest();
	
	gameplayTest();
}*/
}
