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

import sporemodder.file.simulator.attributes.BooleanArrayAttribute;
import sporemodder.file.simulator.attributes.ClassArrayAttribute;
import sporemodder.file.simulator.attributes.DefaultAttribute;
import sporemodder.file.simulator.attributes.FloatArrayAttribute;
import sporemodder.file.simulator.attributes.FloatAttribute;
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.SimulatorAttribute;
import sporemodder.file.simulator.attributes.Vector3Attribute;
import sporemodder.file.simulator.attributes.Vector4Attribute;

public class cScenarioMarker extends SimulatorClass {
	public static final int CLASS_ID = 0x01A80D26;
	
	public cScenarioMarker() {
		super(CLASS_ID);
	}

	@Override public SimulatorAttribute createAttribute(String name) {
		switch(name) {
		case "classIndex":	return new IntAttribute();
		case "position":	return new Vector3Attribute();
		case "orientation":	return new Vector4Attribute();
		case "elevation":	return new FloatAttribute();
		case "mFlags":	return new BooleanArrayAttribute();
		case "scale":	return new FloatAttribute();
		case "relativeScaleToDefault":	return new FloatAttribute();
		case "pitch":	return new FloatAttribute();
		case "gain":	return new FloatAttribute();
		case "teleportPosition":	return new Vector3Attribute();
		case "teleportOrientation":	return new Vector4Attribute();
		case "teleportScale":	return new FloatAttribute();
		case "oldDistances":	return new FloatArrayAttribute();
		case "acts":	return new ClassArrayAttribute<>(cScenarioMarkerAct.class);
		default:
			return new DefaultAttribute();
		}
	}
}
