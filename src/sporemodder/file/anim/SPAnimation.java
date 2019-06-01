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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.MainApp;
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
	public final List<AnimationVFX> vfxList = new ArrayList<>();
	
	// Used for parsing
	public final Map<String, AnimationVFX> vfxMap = new HashMap<>();
	
	public void read(StreamReader stream) throws IOException {
		int magic = stream.readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported animation magic: 0x" + Integer.toHexString(magic));
		}
		
		stream.readLEInt();  // file size
		int version = stream.readLEInt();
		
		System.out.println("version: " + version);
		if (version < MIN_VERSION) {
			System.err.println("Only versions [" + MIN_VERSION + ", " + VERSION + "] are supported.");
		}
		
		DataStructure data = new DataStructure(stream);
		data.setPointer(0);
		
		int id = data.getInt(0x110);
		
		// 11Ch: always 0x3D088889 ?
		length = data.getFloat(0x120);
		
		System.out.println("id: " + HashManager.get().getFileName(id) + "\tlength: " + length);
		System.out.println();
		
		// 12Ch: number of animation loaded, doesn't matter
		// 130h: version
		
		int vfxCount = data.getInt(0x13C);
		long vfxPtr = data.getUInt(0x140);
		int channelCount = data.getInt(0x144);
		long channelPtr = data.getUInt(0x148);
		
		// 14Ch: number of end pointers
		// 150h: pointer to an array pointers, at the end of the file (but before vfx). They point to all the pointers?
		
		for (int i = 0; i < channelCount; ++i) {
			data.setPointer(channelPtr);
			long ptr = data.getUInt(4 * i);
			
			data.setPointer(ptr);
			AnimationChannel channel = new AnimationChannel();
			channel.read(data);
			channels.add(channel);
		}
		
		for (int i = 0; i < vfxCount; ++i) {
			data.setPointer(vfxPtr + i*0x60);
			
			AnimationVFX vfx = new AnimationVFX();
			vfx.read(data);
			vfxList.add(vfx);
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
		// - vfxPtr (0x140), if present
		// - channelPtr (0x148)
		// - the pointers at channelPtr 
		// - the pointers to Animation* at channel+04h
		// - the keyframePtr in channels
		// - the component metadata ptr in channels
		List<Long> offsets = new ArrayList<>();
		
		if (!vfxList.isEmpty()) offsets.add(0x140L);
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
		
		stream.writeLEInt(vfxList.size());
		stream.writeLEInt(0);  // ptr, TODO
		stream.writeLEInt(channels.size());
		stream.writeLEInt(0);  // ptr, TODO
		stream.writeLEInt(0);  // offsets count, TODO
		stream.writeLEInt(0);  // offsets ptr, TODO
		
		stream.writePadding(0x200 - 0x154);
		
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
		for (long offset : offsets) stream.writeLEUInt(offset);
		
		long vfxOffset = stream.getFilePointer();
		offsets.add(vfxOffset);
		
		if (!vfxList.isEmpty()) {
			long vfxNamesOffset = vfxOffset + vfxList.size()*0x60;
			
			for (AnimationVFX vfx : vfxList) {
				offsets.add(vfxNamesOffset);
				vfx.write(stream, offsets, vfxNamesOffset);
				vfxNamesOffset += vfx.name.length() + 1;
			}
			
			for (AnimationVFX vfx : vfxList) {
				stream.writeCString(vfx.name, StringEncoding.ASCII);
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
		stream.writeLEInt(vfxList.size());
		stream.writeLEUInt(vfxOffset);
		stream.writeLEInt(channels.size());
		stream.writeLEUInt(channelPtrsOffset);
		stream.writeLEInt(offsets.size());
		stream.writeLEUInt(offsetsOffset);
	}
	
	public String toArgScript() throws IOException {
		ArgScriptWriter writer = new ArgScriptWriter();
		
		// We don't write it because it doesn't matter, Spore replaces it with the file ID
		//writer.command("id").arguments(HashManager.get().getFileName(id));
		writer.command("length").floats(length);
		writer.blankLine();
		
		if (!vfxList.isEmpty()) {
			for (int i = 0; i < vfxList.size(); ++i) {
				vfxList.get(i).toArgScript(writer, "vfx" + i);
			}
			writer.blankLine();
		}
		
		for (AnimationChannel channel : channels) {
			channel.toArgScript(writer, this);
			writer.blankLine();
		}
		
		return writer.toString();
	}
	
	public void clear() {
		length = 0;
		channels.clear();
		vfxList.clear();
	}
	
	public ArgScriptStream<SPAnimation> generateStream() {
		ArgScriptStream<SPAnimation> stream = new ArgScriptStream<SPAnimation>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		AnimationVFX.addParser(stream);
		
		stream.addParser("length", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			Number value;
			
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				length = value.floatValue();
			}
		}));
		
		stream.addParser(AnimationChannel.KEYWORD, new AnimChannelParser());
		
		return stream;
	}
	
	public static void versionFind(int version) throws IOException {
		//String path = "E:\\Eric\\SporeModder\\Projects\\Spore_Game.package.unpacked\\animations~";
		//String path = "E:\\Eric\\SporeModder\\Projects\\Spore_EP1_Data.package.unpacked\\animations~";
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Spore (Game & Graphics)\\animations~";
		
		MainApp.testInit();
		
		for (File file : new File(path).listFiles()) {
			if (file.getName().endsWith(".animation")) {
				try (FileStream stream = new FileStream(file, "r")) {
					stream.readInt();
					stream.readInt();
					
					if (stream.readLEInt() == version) {
						System.out.println(file.getName().substring(0, file.getName().indexOf(".")));
					}
				}
			}
		}
	}
	
	public static void unpackTest() throws IOException {
		//String path = "C:\\Users\\Eric\\Desktop\\0x30EF4216.animation";
		//String path = "C:\\Users\\Eric\\Desktop\\tree_idle.animation";
		//String path = "C:\\Users\\Eric\\Desktop\\com_punch.animation";
		//String path = "C:\\Users\\Eric\\Desktop\\ep1_trader_jumpjet_land.animation";
		//String path = "E:\\Eric\\SporeModder\\Projects\\Spore_Game.package.unpacked\\animations~\\csa_actn_jumphit.animation";
		//String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Spore (Game & Graphics)\\animations~\\0x30EF4216.animation";
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CustomAnimations\\animations~\\csa_actn_jumphit_COPY.animation";
		MainApp.testInit();
		
		try (MemoryStream stream = new MemoryStream(Files.readAllBytes(new File(path).toPath()))) {
			
			SPAnimation animation = new SPAnimation();
			animation.read(stream);
			
			System.out.println(animation.toArgScript());
		}
	}
	
	public static void packTest() throws IOException {
		MainApp.testInit();
		
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CustomAnimations\\animations~";
		
		String fileName = "csa_actn_jumphit.animation";
		
		File output = new File(path, fileName);
		
		SPAnimation anim = new SPAnimation();
		anim.generateStream().process(new File(path, fileName + ".anim_t"));
		
		try (FileStream stream = new FileStream(output, "rw")) {
			anim.write(stream, output.getAbsolutePath(), 
					HashManager.get().getFileHash("csa_actn_jumphit"));
		}
		
		
		try (MemoryStream stream = new MemoryStream(Files.readAllBytes(output.toPath()))) {
			
			SPAnimation animation = new SPAnimation();
			animation.read(stream);
			
			System.out.println(animation.toArgScript());
		}
	}
	
	public static void main(String[] args) throws IOException {
		unpackTest();
		//packTest();
		//versionFind(VERSION);
	}
}

