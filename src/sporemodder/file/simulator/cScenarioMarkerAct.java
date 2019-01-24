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

import sporemodder.file.simulator.attributes.DefaultAttribute;
import sporemodder.file.simulator.attributes.FloatAttribute;
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.SimulatorAttribute;
import sporemodder.file.simulator.attributes.Vector3ArrayAttribute;
import sporemodder.file.simulator.attributes.Vector3Attribute;

public class cScenarioMarkerAct extends SimulatorClass {
	public static final int CLASS_ID = 0x01A80D26;
	
	public cScenarioMarkerAct() {
		super(CLASS_ID);
	}

	@Override public SimulatorAttribute createAttribute(String name) {
		switch(name) {
		case "wanderRadius":	return new FloatAttribute();
		case "patrolPositionDEPRECATED":	return new Vector3Attribute();
		case "teleportPositionDEPRECATED":	return new Vector3Attribute();
		case "patrolPath":	return new Vector3ArrayAttribute();
		case "patrolStyle":	return new IntAttribute();
		default:
			return new DefaultAttribute();
		}
	}
}
