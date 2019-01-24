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

import sporemodder.file.simulator.attributes.BooleanAttribute;
import sporemodder.file.simulator.attributes.ClassArrayAttribute;
import sporemodder.file.simulator.attributes.ClassAttribute;
import sporemodder.file.simulator.attributes.DefaultAttribute;
import sporemodder.file.simulator.attributes.FloatAttribute;
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.SimulatorAttribute;

public class cScenarioClassAct extends SimulatorClass {
	public static final int CLASS_ID = 0x01A80D26;
	
	public cScenarioClassAct() {
		super(CLASS_ID);
	}

	@Override public SimulatorAttribute createAttribute(String name) {
		switch(name) {
		case "teamBehavior":	return new IntAttribute();
		case "stanceBehavior":	return new IntAttribute();
		case "pickupBehavior":	return new IntAttribute();
		case "giveBehavior":	return new IntAttribute();
		case "movementBehavior":	return new IntAttribute();
		case "pickupTargetClassIndex":	return new IntAttribute();
		case "giveTargetClassIndex":	return new IntAttribute();
		case "trackTargetClassIndex":	return new IntAttribute();
		case "awareness":	return new FloatAttribute();
		case "damageMultiplier":	return new FloatAttribute();
		case "speedMultiplier":	return new FloatAttribute();
		case "health_DEPRECATED":	return new FloatAttribute();
		case "healthMultiplier":	return new FloatAttribute();
		case "damageTuning":	return new FloatAttribute();
		case "radiusTuning":	return new FloatAttribute();
		case "jumpTuning":	return new FloatAttribute();
		case "spawnDelay_":	return new IntAttribute();
		case "invulnerable":	return new BooleanAttribute();
		case "isDead":	return new BooleanAttribute();
		case "visible":	return new BooleanAttribute();
		case "dialogs_chatter":	return new ClassArrayAttribute<>(cScenarioDialog.class);
		case "dialogs_inspect":	return new ClassArrayAttribute<>(cScenarioDialog.class);
		case "descriptionDEPRECATED":	return new ClassAttribute<>(cScenarioString.class);
		case "customBehavior":	return new BooleanAttribute();
		case "customBehaviorEntries":	return new ClassArrayAttribute<>(cBehaviorEntry.class);
		default:
			return new DefaultAttribute();
		}
	}
}
