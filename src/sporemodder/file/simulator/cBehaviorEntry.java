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
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.SimulatorAttribute;

public class cBehaviorEntry extends SimulatorClass {
	public static final int CLASS_ID = 0x01A80D26;
	
	public cBehaviorEntry() {
		super(CLASS_ID);
	}

	@Override public SimulatorAttribute createAttribute(String name) {
		switch(name) {
		case "condition.conditionType":	return new IntAttribute();
		case "condition.target.type":	return new IntAttribute();
		case "condition.target.value":	return new IntAttribute();
		case "action.actionType":	return new IntAttribute();
		case "action.target.type":	return new IntAttribute();
		case "action.target.value":	return new IntAttribute();
		case "action.secondaryTarget.type":	return new IntAttribute();
		case "action.secondaryTarget.value":	return new IntAttribute();
		default:
			return new DefaultAttribute();
		}
	}
}
