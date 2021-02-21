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
package sporemodder.file.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class SPAnimation {

	private static final int MAGIC = 0x4D494E41;
	private static final int VERSION = 0x19;
	private static final int MIN_VERSION = 0x14;
	private static final int MAX_PATH = 256;
	
	public final List<AnimationChannel> channels = new ArrayList<>();
	public float length;
	public final List<AnimationEvent> eventList = new ArrayList<>();
	public final AnimationPredicate predicate = new AnimationPredicate();
	
	// Used for parsing
	public final Map<String, AnimationEvent> eventMap = new HashMap<>();
	
	public void read(StreamReader stream) throws IOException {
		int magic = stream.readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported animation magic: 0x" + Integer.toHexString(magic));
		}
		
		stream.readLEInt();  // file size
		int version = stream.readLEInt();
		
		// System.out.println("version: " + version);
		if (version < MIN_VERSION) {
			throw new IOException("Only versions [" + MIN_VERSION + ", " + VERSION + "] are supported.");
		}
		
		DataStructure data = new DataStructure(stream);
		data.setPointer(0);
		
		int id = data.getInt(0x110);
		
		// 118h is some flags related to keyframe indices, check sub_99C860
		// 11Ch: always 0x3D088889 ?  1.0/30.0, keyframe length
		length = data.getFloat(0x120);
		
		// System.out.println("id: " + HashManager.get().getFileName(id) + "\tlength: " + length);
		// System.out.println();
		
		// 12Ch: number of animation loaded, doesn't matter
		// 130h: version
		
		int eventCount = data.getInt(0x13C);
		long eventPtr = data.getUInt(0x140);
		int channelCount = data.getInt(0x144);
		long channelPtr = data.getUInt(0x148);
		
		// 14Ch: number of end pointers
		// 150h: pointer to an array pointers, at the end of the file (but before vfx). They point to all the pointers?
		
		stream.seek(0x154);
		predicate.read(stream);
		
		for (int i = 0; i < channelCount; ++i) {
			data.setPointer(channelPtr);
			long ptr = data.getUInt(4 * i);
			
			data.setPointer(ptr);
			AnimationChannel channel = new AnimationChannel();
			channel.read(data);
			channels.add(channel);
		}
		
		for (int i = 0; i < eventCount; ++i) {
			stream.seek(eventPtr + i*0x60);
			
			AnimationEvent event = new AnimationEvent();
			event.read(stream);
			eventList.add(event);
		}
		
		fixVersion(version);
	}
	
	public void fixVersion(int version) {
		for (AnimationChannel c : channels) {
			c.fixVersion(version);
		}
	}
	
	public void write(StreamWriter stream, String path, int id) throws IOException {
		// They point to:
		// - eventPtr (0x140), if present
		// - channelPtr (0x148)
		// - the pointers at channelPtr 
		// - the pointers to Animation* at channel+04h
		// - the keyframePtr in channels
		// - the component metadata ptr in channels
		List<Long> offsets = new ArrayList<>();
		
		offsets.add(0x140L);
		offsets.add(0x148L);
		
		stream.writeLEInt(MAGIC);
		stream.writeLEInt(0);  // size, will fill later
		stream.writeLEInt(VERSION);
		
		if (path.length() > MAX_PATH) {
			throw new IOException("Path cannot be longer than 256 characters.");
		}
		stream.writeString(path, StringEncoding.ASCII);
		stream.writePadding(MAX_PATH - path.length());
		stream.writeLEInt(0);
		// We are at offset 110h
		stream.writeLEInt(id);
		stream.writePadding(8);
		stream.writeLEInt(0x3D088889);  // ?
		stream.writeLEFloat(length);
		stream.writePadding(0x13C - 0x124);
		
		stream.writeLEInt(eventList.size());
		stream.writeLEInt(0);  // event ptr,
		stream.writeLEInt(channels.size());
		stream.writeLEInt(0);  // channels ptr,
		stream.writeLEInt(0);  // offsets count,
		stream.writeLEInt(0);  // offsets ptr,
		
		predicate.write(stream);
		
		stream.writePadding(0x200 - 0x15C);
		
		long[] channelPtrs = new long[channels.size()];
		long channelPtrsOffset = stream.getFilePointer();
		// We will fill them later
		stream.writePadding(4 * channels.size());
		
		for (int i = 0; i < channels.size(); ++i) {
			channelPtrs[i] = stream.getFilePointer();
			
			channels.get(i).write(stream, offsets);
			
			// Add a pointer for each channel
			offsets.add(channelPtrsOffset + i*4);
		}
		
		long offsetsOffset = stream.getFilePointer();
		// One pointer per event
		long eventOffset = stream.getFilePointer() + 4 * (offsets.size() + eventList.size());
		for (int i = 0; i < eventList.size(); ++i) {
			// The pointer to the name is at 0x40; it's only written if ID != 0
			if (eventList.get(i).id != 0) {
				offsets.add(eventOffset + 0x60*i + 0x40);
			}
		}
		
		for (long offset : offsets) stream.writeLEUInt(offset);
		
		if (!eventList.isEmpty()) {
			long eventNamesOffset = eventOffset + eventList.size()*0x60;
			
			for (AnimationEvent event : eventList) {
				event.write(stream, eventNamesOffset);
				eventNamesOffset += event.name.length() + 1;
			}
			
			for (AnimationEvent event : eventList) {
				stream.writeCString(event.name, StringEncoding.ASCII);
			}
		}
		
		// Fix size
		stream.seek(4);
		stream.writeLEUInt(stream.length());
		
		// Fix channel pointers
		stream.seek(channelPtrsOffset);
		stream.writeLEUInts(channelPtrs);
		
		// Fix the header pointers
		stream.seek(0x13C);
		stream.writeLEInt(eventList.size());
		stream.writeLEUInt(eventOffset);
		stream.writeLEInt(channels.size());
		stream.writeLEUInt(channelPtrsOffset);
		stream.writeLEInt(offsets.size());
		stream.writeLEUInt(offsetsOffset);
	}
	
	public ArgScriptWriter toArgScript() throws IOException {
		ArgScriptWriter writer = new ArgScriptWriter();
		
		// We don't write it because it doesn't matter, Spore replaces it with the file ID
		//writer.command("id").arguments(HashManager.get().getFileName(id));
		writer.command("length").floats(length);
		if (!predicate.isDefault()) {
			writer.command("branchPredicate");
			predicate.toArgScript(writer);
		}
		writer.blankLine();
		
		if (!eventList.isEmpty()) {
			for (int i = 0; i < eventList.size(); ++i) {
				eventList.get(i).toArgScript(writer, "event" + i);
			}
			writer.blankLine();
		}
		
		for (AnimationChannel channel : channels) {
			channel.toArgScript(writer, this);
			writer.blankLine();
		}
		
		return writer;
	}
	
	public void clear() {
		length = 0;
		channels.clear();
		eventList.clear();
		eventMap.clear();
		predicate.flags1 = 0;
		predicate.flags2 = 0;
	}
	
	public ArgScriptStream<SPAnimation> generateStream() {
		ArgScriptStream<SPAnimation> stream = new ArgScriptStream<SPAnimation>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		AnimationEvent.addParser(stream);
		
		stream.addParser("length", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			Number value;
			
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				length = value.floatValue();
			}
		}));
		
		stream.addParser("branchPredicate", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 2, 6)) {
				if ((args.size() % 2) != 0) {
					stream.addError(line.createErrorForOption("branchPredicate", "Must specify an even number of arguments"));
				}
				else {
					predicate.parse(args, stream);
				}
			}
		}));
		
		stream.addParser(AnimationChannel.KEYWORD, new AnimChannelParser());
		
		return stream;
	}
}

