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
import sporemodder.file.simulator.attributes.Int64Attribute;
import sporemodder.file.simulator.attributes.IntArrayAttribute;
import sporemodder.file.simulator.attributes.IntAttribute;
import sporemodder.file.simulator.attributes.ResourceKeyArrayAttribute;
import sporemodder.file.simulator.attributes.ResourceKeyAttribute;
import sporemodder.file.simulator.attributes.SimulatorAttribute;
import sporemodder.file.simulator.attributes.Vector3Attribute;
import sporemodder.file.simulator.attributes.Vector4Attribute;

public class cScenarioResourceAttributes extends SimulatorClass {
	
	public static final int CLASS_ID = 0x01A80D26;
	
	public cScenarioResourceAttributes() {
		super(CLASS_ID);
	}
	
	@Override public SimulatorAttribute createAttribute(String name) {
		switch (name) {
		case "avatarPosition":	return new Vector3Attribute();
		case "avatarHealthMultiplier":	return new FloatAttribute();
		case "avatarIsInvulnerable":	return new BooleanAttribute();
		case "avatarOrientation":	return new Vector4Attribute();
		case "avatarScale":	return new FloatAttribute();
		case "bAvatarLocked":	return new BooleanAttribute();
		case "initialPosseMembers":	return new ClassArrayAttribute<>(cScenarioPosseMember.class);
		case "numAllowedPosseMembers":	return new IntAttribute();
		case "classes":	return new ClassArrayAttribute<>(cScenarioClass.class, true);  // it's indexed
		case "acts":	return new ClassArrayAttribute<>(cScenarioAct.class);
		case "markers":	return new ClassArrayAttribute<>(cScenarioMarker.class);
		case "winText":	return new ClassAttribute<>(cScenarioString.class);
		case "loseText":	return new ClassAttribute<>(cScenarioString.class);
		case "introText":	return new ClassAttribute<>(cScenarioString.class);
		case "type":	return new IntAttribute();
		case "bIsMission":	return new BooleanAttribute();
		case "classIDCounter":	return new IntAttribute();
		case "mScreenshotTypes":	return new IntArrayAttribute();
		case "mAvatarAsset":	return new ClassAttribute<>(cScenarioAsset.class);
		case "markerPositioningVersion":	return new IntAttribute();
		case "usedAppPackIds":	return new IntArrayAttribute();
		case "cameraTarget":	return new Vector3Attribute();
		case "cameraOrientation":	return new Vector4Attribute();
		case "cameraDistance":	return new FloatAttribute();
		case "avatarAssetKeyDEPRECATED":	return new ResourceKeyAttribute();
		case "avatarServerIDDEPRECATED":	return new Int64Attribute();
		case "atmosphereScoreDEPRECATED":	return new FloatAttribute();
		case "temperatureScoreDEPRECATED":	return new FloatAttribute();
		//TODO classesOld (or not)
		case "initialPosseMemberKeysDEPRECATED":	return new ResourceKeyArrayAttribute();
		case "waterScoreDEPRECATED":	return new FloatAttribute();
		case "mbIsTimeLockedDEPRECATED":	return new BooleanAttribute();
		case "mTimeElapsedDEPRECATED":	return new FloatAttribute();
		case "mbCustomScreenshotThumbnailDEPRECATED":	return new BooleanAttribute();
		
		default:
			return new DefaultAttribute();
		}
	}
}
