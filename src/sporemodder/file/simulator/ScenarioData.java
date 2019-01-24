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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.ResourceKey;
import sporemodder.file.simulator.attributes.ClassArrayAttribute;
import sporemodder.file.simulator.attributes.ClassAttribute;
import sporemodder.file.simulator.attributes.FloatAttribute;
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.ResourceKeyAttribute;
import sporemodder.file.simulator.attributes.Vector3Attribute;
import sporemodder.file.simulator.attributes.Vector4Attribute;
import sporemodder.util.Matrix;
import sporemodder.util.Transform;
import sporemodder.util.Vector4;

public class ScenarioData {
	
	private final cScenarioResourceAttributes scenario = new cScenarioResourceAttributes();

	public void read(StreamReader stream) throws Exception {
		
		scenario.read(stream);
		
		if (scenario.classID != cScenarioResourceAttributes.CLASS_ID) {
			throw new IOException("Incorrect magic number.");
		}
	}
	
	public void write(StreamWriter stream) throws Exception {
		scenario.write(stream);
	}
	
	@SuppressWarnings("unchecked")
	public void getObjects(final List<ResourceKey> modelKeys, final List<Transform> modelTransforms) {
		
		ClassArrayAttribute<cScenarioClass> classes = scenario.getAttribute("classes", ClassArrayAttribute.class);
		ClassArrayAttribute<cScenarioMarker> markers = scenario.getAttribute("markers", ClassArrayAttribute.class);
		
		// Store the classes in a map with their index
		HashMap<Integer, cScenarioClass> classesMap = new HashMap<Integer, cScenarioClass>();
		for (int i = 0; i < classes.value.size(); i++) {
			classesMap.put(classes.indices.get(i), classes.value.get(i));
		}
		
		for (cScenarioMarker marker : markers.value) {
			// Contains the transform
			cScenarioClass clazz = classesMap.get(marker.getAttribute("classIndex", IntAttribute.class).value);
			// Contains the model key
			
			ResourceKey key = clazz.getAttribute("mAsset", ClassAttribute.class).value.getAttribute("mKey", ResourceKeyAttribute.class).value;
			
			// building
			if (key.getTypeID() == 0x2399BE55) {  // .bld
				key.setGroupID(0x40636200);  // buildingModelsBLD~
				key.setTypeID(0x00B1B104);
			}
			
			if (key.getGroupID() == 0xD87454E6) {  // PaletteItems
				key.setGroupID(0x9430ADD7);  // CivicObjects
			}
			
			modelKeys.add(key);
			
			
			Vector4 quaternion = marker.getAttribute("orientation", Vector4Attribute.class).value;
			
			Transform transform = new Transform();
			transform.setOffset(marker.getAttribute("position", Vector3Attribute.class).value);
			transform.setScale(marker.getAttribute("scale", FloatAttribute.class).value);
			transform.setRotation(Matrix.fromQuaternion(quaternion).transposed());
			
			modelTransforms.add(transform);
		}
	}
	
	public String print() {
		StringBuilder sb = new StringBuilder();
		String tabulation = "\t";
		
		sb.append('{');
		sb.append('\n');
		
		scenario.print(sb, tabulation);
		
		sb.append('}');
		
		return sb.toString();
	}
	
	public String printXML() {
		StringBuilder sb = new StringBuilder();
		String tabulation = "\t";
		
		sb.append("<cScenarioResource>\n");
		
		scenario.printXML(sb, tabulation);
		
		sb.append("</cScenarioResource>\n");
		
		return sb.toString();
	}
}
