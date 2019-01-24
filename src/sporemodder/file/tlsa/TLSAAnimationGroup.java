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
package sporemodder.file.tlsa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;

public class TLSAAnimationGroup {
	public static final String KEYWORD = "anim";
	
	public final List<TLSAAnimationChoice> choices = new ArrayList<TLSAAnimationChoice>();
	
	public int id;
	public String name;
	
	public float priorityOverride;
	public float blendInTime = -1.0f;
	public boolean idle;
	public boolean allowLocomotion;
	public boolean randomizeChoicePerLoop;
	public int matchVariantForToolMask;
	public int disableToolOverlayMask;
	public int endMode = 4;
	
	private void readOld(StreamReader stream, int version) throws IOException {
		id = stream.readInt();
		
		if (version == 8) {
			priorityOverride = stream.readFloat();
		}
		
		int numAnims = stream.readInt();
		
		TLSAAnimationChoice choice = new TLSAAnimationChoice();
		choices.add(choice);
		
		for (int i = 0; i < numAnims; i++) {
			TLSAAnimation anim = new TLSAAnimation();
			anim.description = stream.readString(StringEncoding.UTF16LE, stream.readInt());
			choice.animations.add(anim);
		}
		for (int i = 0; i < numAnims; i++) {
			choice.animations.get(i).id = stream.readInt();
		}
		
		float durationScale = stream.readFloat();
		float duration = stream.readFloat();
		for (TLSAAnimation anim : choice.animations) {
			anim.duration = duration;
			anim.durationScale = durationScale;
		}
		
		idle = stream.readBoolean();
		blendInTime = stream.readFloat();
		allowLocomotion = stream.readBoolean();
		disableToolOverlayMask = stream.readInt();
		matchVariantForToolMask = stream.readInt();
		endMode = stream.readInt();
	}
	
	private void writeOld(StreamWriter stream, int version) throws IOException {
		stream.writeInt(id);
		
		if (version == 8) {
			stream.writeFloat(priorityOverride);
		}
		
		float durationScale = 1.0f;
		float duration = -1.0f;
		
		if (choices.isEmpty()) {
			stream.writeInt(0);
		} else {
			List<TLSAAnimation> anims = choices.get(0).animations;
			stream.writeInt(anims.size());
			
			for (TLSAAnimation anim : anims) {
				stream.writeInt(anim.description.length());
				stream.writeString(anim.description, StringEncoding.UTF16LE);
			}
			for (TLSAAnimation anim : anims) {
				stream.writeInt(anim.id);
			}
			if (!anims.isEmpty()) {
				durationScale = anims.get(0).durationScale;
				duration = anims.get(0).duration;
			}
		}
		
		stream.writeFloat(durationScale);
		stream.writeFloat(duration);
		stream.writeBoolean(idle);
		stream.writeFloat(blendInTime);
		stream.writeBoolean(allowLocomotion);
		stream.writeInt(disableToolOverlayMask);
		stream.writeInt(matchVariantForToolMask);
		stream.writeInt(endMode);
	}
	
	private void readNew(StreamReader stream, int version) throws IOException {
		id = stream.readInt();
		
		name = stream.readString(StringEncoding.UTF16LE, stream.readInt());
		
		priorityOverride = stream.readFloat();  // priorityOverride ?
		blendInTime = stream.readFloat();
		
		idle = stream.readBoolean();
		allowLocomotion = stream.readBoolean();
		
		if (version == 10) {
			randomizeChoicePerLoop = stream.readBoolean();
		}
		
		matchVariantForToolMask = stream.readInt();
		disableToolOverlayMask = stream.readInt();
		endMode = stream.readInt();
		
		int choiceCount = stream.readInt();
		
		for (int i = 0; i < choiceCount; i++) {
			TLSAAnimationChoice choice = new TLSAAnimationChoice();
			choices.add(choice);
			
			choice.probabilityThreshold = stream.readFloat();
			
			int count = stream.readInt();
			for (int j = 0; j < count; ++j) {
				TLSAAnimation animation = new TLSAAnimation();
				choice.animations.add(animation);
				
				animation.durationScale = stream.readFloat();
				animation.duration = stream.readFloat();
				animation.id = stream.readInt();
				animation.description = stream.readString(StringEncoding.UTF16LE, stream.readInt());
			}
		}
	}
	
	private void writeNew(StreamWriter stream, int version) throws IOException {
		stream.writeInt(id);
		
		stream.writeInt(name.length());
		stream.writeString(name, StringEncoding.UTF16LE);
		
		stream.writeFloat(priorityOverride);
		stream.writeFloat(blendInTime);
		stream.writeBoolean(idle);
		stream.writeBoolean(allowLocomotion);
		
		if (version == 10) {
			stream.writeBoolean(randomizeChoicePerLoop);
		}
		
		stream.writeInt(matchVariantForToolMask);
		stream.writeInt(disableToolOverlayMask);
		stream.writeInt(endMode);
		stream.writeInt(choices.size());
		
		for (TLSAAnimationChoice choice : choices) {
			stream.writeFloat(choice.probabilityThreshold);
			stream.writeInt(choice.animations.size());
			for (TLSAAnimation anim : choice.animations) {
				stream.writeFloat(anim.durationScale);
				stream.writeFloat(anim.duration);
				stream.writeInt(anim.id);
				stream.writeInt(anim.description.length());
				stream.writeString(anim.description, StringEncoding.UTF16LE);
			}
		}
	}
	
	public void read(StreamReader stream, int version) throws IOException {
		if (version <= 8) {
			readOld(stream, version);
		} else {
			readNew(stream, version);
		}
	}
	
	public void write(StreamWriter stream, int version) throws IOException {
		if (version <= 8) {
			writeOld(stream, version);
		} else {
			writeNew(stream, version);
		}
	} 
	
	public void toArgScript(ArgScriptWriter writer, int version) {
		HashManager hasher = HashManager.get();
		
		writer.command(KEYWORD).arguments(hasher.getFileName(id));
		if (name != null) writer.literal(name);
		writer.startBlock();
		
		if (version == 10 && randomizeChoicePerLoop != false) writer.command("randomizeChoicePerLoop").arguments(Boolean.toString(randomizeChoicePerLoop));
		writer.command("endMode").ints(endMode);
		if (idle != false) writer.command("idle").arguments(Boolean.toString(idle));
		if (blendInTime != -1.0f) writer.command("blendInTime").floats(blendInTime);
		writer.command("allowLocomotion").arguments(Boolean.toString(allowLocomotion));
		
		if (disableToolOverlayMask != 0) {
			writer.command("disableToolOverlay");
			for (int i = 0; i < 32; ++i) {
				if ((disableToolOverlayMask & (1 << i)) != 0) writer.ints(i);
			}
		}
		if (matchVariantForToolMask != 0) {
			writer.command("matchVariantForTool");
			for (int i = 0; i < 32; ++i) {
				if ((matchVariantForToolMask & (1 << i)) != 0) writer.ints(i);
			}
		}
		
		if (version >= 8) writer.command("priorityOverride").floats(priorityOverride);
		
		float accumulatedProb = 0.0f;
		
		if (!choices.isEmpty()) {
			for (TLSAAnimationChoice choice : choices) {
				writer.blankLine();
				writer.command("choice");
				if (version > 8) writer.option("probability").floats(choice.probabilityThreshold - accumulatedProb);
				writer.startBlock();
				
				for (TLSAAnimation anim : choice.animations) {
					writer.command("animation").literal(anim.description);
					
					if (hasher.fnvHash(instanceFromDescription(anim.description)) != anim.id) {
						writer.option("instanceID").arguments(hasher.getFileName(anim.id));
					}
					
					if (anim.durationScale != 1.0f) writer.option("durationScale").floats(anim.durationScale);
					if (anim.duration != -1.0f) writer.option("duration").floats(anim.duration);
				}
				
				writer.endBlock().commandEND();
				
				accumulatedProb = choice.probabilityThreshold;
			}
		}
		
		writer.endBlock().commandEND();
	}
	
	public static String instanceFromDescription(String description) {
		int index = description.lastIndexOf('/');
		if (index == -1) index = 0;
		else index += 1;
		return description.substring(index);
	}
}
