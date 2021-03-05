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
package sporemodder.file.pctp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class PCTPUnit {
	private static final int MAGIC = 0x70637470;
	
	public static class CapabilityName {
		public String name;
		public String identifier;
	}
	
	public static class CapabilityMapping {
		public String identifier;
		public int index;
	}
	
	public static class DeformSpec {
		public static final String KEYWORD = "deform";
		
		public int deformID;
		public final float[] range = {0.0f, 1.0f}; // probably not ranges, since one is not present in version 3
		public int flags;
	}
	
	public int version = 4;
	public float priority;
	public final List<CapabilityName> capabilityNames = new ArrayList<CapabilityName>();
	public final Map<Integer, CapabilityMapping> capabilitiesMap = new HashMap<Integer, CapabilityMapping>();
	public final Map<String, List<String>> aggregates = new HashMap<String, List<String>>();
	public final Map<String, List<DeformSpec>> deformSpecs = new HashMap<String, List<DeformSpec>>();
	
	protected static String getIdentifierString(int identifier) throws UnsupportedEncodingException {
		byte[] bytes = ByteBuffer.allocate(4).putInt(identifier).array();
		// convert 0s to spaces
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) bytes[i] = 0x20;
		}
		return new String(bytes, "US-ASCII");
	}
	
	protected static void writeIdentifier(StreamWriter out, String identifier) throws IOException {
		byte[] array = identifier.getBytes("US-ASCII");
		
		if (array.length > 4) throw new IOException("PCTP-001; Unexpected identifier length.");
		
		for (int i = 3; i >= 0; i--) {
			if (i >= array.length) out.writeByte(0);
			else out.writeByte(array[i]);
		}
	}
	
	public void clear() {
		priority = 0;
		capabilityNames.clear();
		capabilitiesMap.clear();
		aggregates.clear();
		deformSpecs.clear();
	}

	public void read(StreamReader stream) throws IOException {
		
		if (stream.readInt() != MAGIC) {
			throw new IOException("Input file is not a PCTP file! Position: " + stream.getFilePointer());
		}
		
		version = stream.readInt();
		if (version != 3 && version != 4) {
			throw new IOException("PCTP-H002; Unsupported version, position: " + stream.getFilePointer());
		}
		if (version > 3) {
			priority = stream.readFloat();
		}
		
		int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			CapabilityName object = new CapabilityName();
			capabilityNames.add(object);
			
			object.name = stream.readString(StringEncoding.ASCII, stream.readInt());
			object.identifier = PCTPUnit.getIdentifierString(stream.readLEInt());
		}
		
		int remapCount = stream.readInt();
		for (int i = 0; i < remapCount; i++) {
			CapabilityMapping object = new CapabilityMapping();
			capabilitiesMap.put(stream.readInt(), object);

			object.identifier = PCTPUnit.getIdentifierString(stream.readLEInt());
			object.index = stream.readInt();
		}
		
		int aggregateCount = stream.readInt();
		for (int i = 0; i < aggregateCount; i++) {
			String key = PCTPUnit.getIdentifierString(stream.readLEInt());
			
			int n = stream.readInt();
			List<String> list = new ArrayList<String>(n);
			for (int j = 0; j < n; ++j) {
				list.add(PCTPUnit.getIdentifierString(stream.readLEInt()));
			}
			
			aggregates.put(key, list);
		}
		
		int deformSpecCount = stream.readInt();
		for (int i = 0; i < deformSpecCount; i++) {
			String identifier = PCTPUnit.getIdentifierString(stream.readLEInt());
			
			int n = stream.readInt();
			List<DeformSpec> list = new ArrayList<DeformSpec>(n);
			for (int j = 0; j < n; ++j) {
				DeformSpec entry = new DeformSpec();
				list.add(entry);
				
				entry.deformID = stream.readInt();
				entry.range[0] = stream.readFloat();
				if (version >= 3) entry.range[1] = stream.readFloat();
				entry.flags = stream.readInt();
			}
			
			deformSpecs.put(identifier, list);
		}
		
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(MAGIC);
		stream.writeInt(version);
		if (version > 3) stream.writeFloat(priority);
		
		// Ensure names are ordered alphabetically
		Collections.sort(capabilityNames, (o1, o2) -> {
			return o1.name.compareTo(o2.name);
		});
		
		stream.writeInt(capabilityNames.size());
		for (CapabilityName item : capabilityNames) {
			stream.writeInt(item.name.length());
			stream.writeString(item.name, StringEncoding.ASCII);
			PCTPUnit.writeIdentifier(stream, item.identifier);
		}
		
		stream.writeInt(capabilitiesMap.size());
		for (Map.Entry<Integer, CapabilityMapping> entry : capabilitiesMap.entrySet()) {
			stream.writeInt(entry.getKey());
			PCTPUnit.writeIdentifier(stream, entry.getValue().identifier);
			stream.writeInt(entry.getValue().index);
		}
		
		stream.writeInt(aggregates.size());
		for (Map.Entry<String, List<String>> entry : aggregates.entrySet()) {
			
			PCTPUnit.writeIdentifier(stream, entry.getKey());
			stream.writeInt(entry.getValue().size());
			for (String identifier : entry.getValue()) PCTPUnit.writeIdentifier(stream, identifier);
		}
		
		stream.writeInt(deformSpecs.size());
		for (Map.Entry<String, List<DeformSpec>> entry : deformSpecs.entrySet()) {
			
			PCTPUnit.writeIdentifier(stream, entry.getKey());
			stream.writeInt(entry.getValue().size());
			
			for (DeformSpec spec : entry.getValue())  {
				stream.writeInt(spec.deformID);
				stream.writeFloat(spec.range[0]);
				if (version > 3) {
					stream.writeFloat(spec.range[1]);
				}
				stream.writeInt(spec.flags);
			}
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		HashManager hasher = HashManager.get();
		
		// We will remove those used in "cap", the rest will be remaps
		Map<Integer, CapabilityMapping> mapCopy = new HashMap<Integer, CapabilityMapping>();
		mapCopy.putAll(capabilitiesMap);
		
		// Recover the old compile order
		Map<Integer, CapabilityName> orderedNames = new TreeMap<Integer, CapabilityName>();
		
		writer.command("version").ints(version);
		if (version > 3) writer.command("priority").floats(priority);
		writer.blankLine();
		
		for (CapabilityName name : capabilityNames) {
			int hash = HashManager.get().fnvHash(name.name);
			orderedNames.put(mapCopy.get(hash).index, name);
			mapCopy.remove(hash);
		}
		for (CapabilityName name : orderedNames.values()) {
			writer.command("cap").arguments(name.name, name.identifier);
		}
		writer.blankLine();
		
		for (Map.Entry<String, List<String>> entry : aggregates.entrySet()) {
			writer.command("aggregate").arguments(entry.getKey()).arguments(entry.getValue());
		}
		writer.blankLine();
		
		for (Map.Entry<Integer, CapabilityMapping> entry : mapCopy.entrySet()) {
			writer.command("remap").arguments(hasher.getFileName(entry.getKey()), orderedNames.get(entry.getValue().index).name);
		}
		writer.blankLine();
		
		for (Map.Entry<String, List<DeformSpec>> entry : deformSpecs.entrySet()) {
			writer.command("deformSpec").arguments(entry.getKey()).startBlock();
			for (DeformSpec spec : entry.getValue()) {
				// deform longname default_value default_weight rendered wrap
				// not actually range
				writer.command("deform").arguments(HashManager.get().getFileName(spec.deformID));
				if (version > 3) {
					writer.floats(spec.range);
				} else {
					writer.floats(spec.range[0]);
				}
				
				// rendered - 0 for helper deforms, 1 for deforms sent to animation channels
				// wrap - 0 for regular curves, 1 for curves that wrap mod 1
				if ((spec.flags & 1) != 0) writer.ints(0);
				else writer.ints(1);
				
				if ((spec.flags & 2) != 0) writer.ints(1);
				else writer.ints(0);
			}
			writer.endBlock().commandEND();
			writer.blankLine();
		}
	}
	
	public String toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer.toString();
	}
	
	public ArgScriptStream<PCTPUnit> generateStream() {
		ArgScriptStream<PCTPUnit> stream = new ArgScriptStream<PCTPUnit>();
		stream.setData(this);
		stream.addDefaultParsers();
		stream.setVersionRange(3, 4);
		
		stream.setOnStartAction((asStream, data) -> {
			data.aggregates.clear();
			data.capabilitiesMap.clear();
			data.capabilityNames.clear();
			data.deformSpecs.clear();
			data.priority = 0.0f;
			data.version = 4;
		});
		
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser("priority", ArgScriptParser.create((parser, line) -> {
			Number n;
			if (line.getArguments(args, 1) && (n = stream.parseFloat(args, 0)) != null) {
				stream.getData().priority = n.floatValue();
			}
		}));
		
		stream.addParser("cap", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 2)) {
				CapabilityName object = new CapabilityName();
				object.name = args.get(0);
				object.identifier = args.get(1);
				stream.getData().capabilityNames.add(object);
				
				CapabilityMapping mapping = new CapabilityMapping();
				mapping.identifier = args.get(1);
				mapping.index = stream.getData().capabilityNames.size() - 1;
				stream.getData().capabilitiesMap.put(HashManager.get().getFileHash(object.name), mapping);
			}
		}));
		
		stream.addParser("aggregate", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
				List<String> list = new ArrayList<String>();
				
				for (int i = 1; i < args.size(); ++i) {
					list.add(args.get(i));
				}
				
				stream.getData().aggregates.put(args.get(0), list);
			}
		}));
		
		stream.addParser("remap", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 2)) {
				int hash = stream.parseFileID(args, 0);

				CapabilityMapping object = stream.getData().capabilitiesMap.get(HashManager.get().fnvHash(args.get(1)));
				if (object == null) {
					stream.addError(line.createErrorForArgument(args.get(1) + " is not a defined capability", 1));
				} else {
					stream.getData().capabilitiesMap.put(hash, object);
				}
			}
		}));
		
		stream.addParser("deformSpec", new ArgScriptBlock<PCTPUnit>() {
			private List<DeformSpec> specs;
			
			@Override public void parse(ArgScriptLine line) {
				specs = new ArrayList<DeformSpec>();

				if (line.getArguments(args, 1)) {
					stream.getData().deformSpecs.put(args.get(0), specs);
				}
				
				stream.startBlock(this);
			}
			
			@Override public void setData(ArgScriptStream<PCTPUnit> stream, PCTPUnit data) {
				super.setData(stream, data);
				
				this.addParser("deform", new ArgScriptParser<PCTPUnit>() {

					@Override public void parse(ArgScriptLine line) {
						DeformSpec entry = new DeformSpec();
						specs.add(entry);
						
						if (line.getArguments(args, stream.getVersion() > 3 ? 5 : 3)) {
							int index = 0;
							entry.deformID = HashManager.get().getFileHash(args.get(index++));
							entry.range[0] = stream.parseFloat(args, index++);
							
							if (stream.getVersion() > 3) entry.range[1] = stream.parseFloat(args, index++);
							
							Number value;
							if ((value = stream.parseInt(args, index++)) != null) {
								if (value.intValue() == 0) entry.flags |= 1;
							}
							
							if ((value = stream.parseInt(args, index++)) != null) {
								if (value.intValue() != 0) entry.flags |= 2;
							}
						}
					}
					
				});
			}
		});
		
		return stream;
	}
}
