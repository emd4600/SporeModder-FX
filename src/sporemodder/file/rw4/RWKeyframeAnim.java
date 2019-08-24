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
package sporemodder.file.rw4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.rw4.RWKeyframe.BlendFactor;
import sporemodder.file.rw4.RWKeyframe.LocRot;
import sporemodder.file.rw4.RWKeyframe.LocRotScale;

public class RWKeyframeAnim extends RWObject {
	
	public static final int TYPE_CODE = 0x70001;
	public static final int ALIGNMENT = 16;
	
	public static class Channel <T extends RWKeyframe> {
		public int channelID;
		public final List<T> keyframes = new ArrayList<T>();
		
		@SuppressWarnings("unchecked")
		public T createKeyframe(int components) {
			T keyframe;
			
			switch (components) {
			case LocRotScale.COMPONENTS:
				keyframe = (T) new LocRotScale();
				keyframes.add(keyframe);
				return keyframe;
			case LocRot.COMPONENTS:
				keyframe = (T) new LocRot();
				keyframes.add(keyframe);
				return keyframe;
			case BlendFactor.COMPONENTS:
				keyframe = (T) new BlendFactor();
				keyframes.add(keyframe);
				return keyframe;
			default:
				return null;
			}
		}
	}
	
	public int skeletonID;
	public int field_C;
	public int field_1C;
	public float length;
	public int field_24 = 12;
	public int flags;
	public final List<Channel<? extends RWKeyframe>> channels = new ArrayList<Channel<? extends RWKeyframe>>();
	public int padding = -1;

	public RWKeyframeAnim(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		long pChannelNames = stream.readLEUInt();
		int channelCount = stream.readLEInt();
		skeletonID = stream.readLEInt();
		field_C = stream.readLEInt();
		
		// pChannelData
		stream.readLEUInt();		
		long pPaddingEnd = stream.readLEUInt();  // probably not just padding
		
		stream.skip(4);  // channel count again
		field_1C = stream.readLEInt();
		length = stream.readLEFloat();
		field_24 = stream.readLEInt();
		flags = stream.readLEInt();
		
		long pChannelInfo = stream.readLEUInt();
		
		int[] channelNames = new int[channelCount];
		long[] channelPositions = new long[channelCount];
		int[] channelPoseSizes = new int[channelCount];
		int[] channelComponents = new int[channelCount];
		
		stream.seek(pChannelNames);
		for (int i = 0; i < channelCount; i++) {
			channelNames[i] = stream.readLEInt();
		}
		
		stream.seek(pChannelInfo);
		for (int i = 0; i < channelCount; i++) {
			channelPositions[i] = stream.readLEUInt();
			channelPoseSizes[i] = stream.readLEInt();
			channelComponents[i] = stream.readLEInt();
			
			channels.add(createChannel(channelComponents[i]));
			channels.get(i).channelID = channelNames[i];
		}
		
		stream.seek(pChannelInfo);
		// This approach works except for the last channel
		for (int i = 0; i < channelCount-1; i++) {
			int keyframeCount = (int) ((channelPositions[i+1] - channelPositions[i]) / channelPoseSizes[i]);
			stream.seek(baseOffset + channelPositions[i]);
			
			for (int k = 0; k < keyframeCount; k++) {
				
				RWKeyframe keyframe = channels.get(i).createKeyframe(channelComponents[i]);
				keyframe.read(stream);
			}
		}
		
		// Now do the last channel
		stream.seek(baseOffset + channelPositions[channelCount-1]);
		Channel<?> lastChannel = channels.get(channelCount-1);
		float lastTime = 0;
		while (true) {
			RWKeyframe keyframe = lastChannel.createKeyframe(channelComponents[channelCount-1]);
			keyframe.read(stream);
			
			if (keyframe.time < lastTime) {
				// Remove it, because it wasn't really a keyframe
				lastChannel.keyframes.remove(keyframe);
				break;
			} else {
				lastTime = keyframe.time;
			}
		}
		
		// The amount of padding might be important, keep it just in case
		padding = (int) (pPaddingEnd - (channelPositions[channelCount-1] + lastChannel.keyframes.size()*channelPoseSizes[channelCount-1]));
	}
	
	public static Channel<? extends RWKeyframe> createChannel(int components) {
		switch (components) {
		case LocRotScale.COMPONENTS:
			return new Channel<LocRotScale>();
		case LocRot.COMPONENTS:
			return new Channel<LocRot>();
		case BlendFactor.COMPONENTS:
			return new Channel<BlendFactor>();
		default:
			return null;
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		// Need to update the offsets, so will write everything after
		stream.writePadding(12*4);
		
		long pChannelNames = stream.getFilePointer();
		for (Channel<?> channel : channels) {
			stream.writeLEInt(channel.channelID);
		}
		
		long pChannelInfo = stream.getFilePointer();
		for (Channel<?> channel : channels) {
			stream.writeLEUInt(0);  // channel data position
			stream.writeLEInt(channel.keyframes.isEmpty() ? LocRotScale.SIZE : channel.keyframes.get(0).getSize());
			stream.writeLEInt(channel.keyframes.isEmpty() ? LocRotScale.COMPONENTS : channel.keyframes.get(0).getComponents());
		}
		
		long pChannelData = stream.getFilePointer();
		long[] channelOffsets = new long[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			channelOffsets[i] = stream.getFilePointer() - baseOffset;
			
			for (RWKeyframe keyframe : channels.get(i).keyframes) {
				keyframe.write(stream);
			}
		}
		
		if (channels.isEmpty()) {
			stream.writePadding(48);
		}
		else {
			if (padding == -1) {
				// Do some random calculation
				stream.writePadding(channels.size() * channels.get(0).keyframes.size() * 2 * channels.get(0).keyframes.get(0).getSize());
			}
			else {
				stream.writePadding(padding);
			}
		}
		long pPaddingEnd = stream.getFilePointer();
		
		// Now write all the information we didn't write earlier
		
		long endPosition = stream.getFilePointer();
		
		stream.seek(baseOffset);
		stream.writeLEUInt(pChannelNames);
		stream.writeLEInt(channels.size());
		stream.writeLEInt(skeletonID);
		stream.writeLEInt(field_C);
		stream.writeLEUInt(pChannelData);
		stream.writeLEUInt(pPaddingEnd);
		stream.writeLEInt(channels.size());
		stream.writeLEInt(field_1C);
		stream.writeLEFloat(length);
		stream.writeLEInt(field_24);
		stream.writeLEInt(flags);
		stream.writeLEUInt(pChannelInfo);
		
		// Now update the channel positions
		for (int i = 0; i < channels.size(); i++) {
			stream.seek(pChannelInfo + i*12);
			stream.writeLEUInt(channelOffsets[i]);
		}
		
		stream.seek(endPosition);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
	
	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}


}
