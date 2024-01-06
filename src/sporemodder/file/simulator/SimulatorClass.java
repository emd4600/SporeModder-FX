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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.simulator.attributes.SimulatorAttribute;
import sporemodder.util.IntPair;

import javax.management.Attribute;

public abstract class SimulatorClass {

	public static int GENERIC_SERIALIZER_ID = 0x1A80D26;

	public class AttributeHeader {
		public int id;
		public int size;

		public AttributeHeader(int id, int size) {
			this.id = id;
			this.size = size;
		}
	}

	protected final LinkedHashMap<String, SimulatorAttribute> attributes = new LinkedHashMap<String, SimulatorAttribute>();
	protected int classID;
	protected int classSize;
	
	public SimulatorClass(int classID) {
		this.classID = classID;
	}
	
	/**
	 * Returns the simulator class ID used in this object.
	 * @return
	 */
	public int getClassID() {
		return classID;
	}
	public int getClassSize() {
		return classSize;
	}
	
	/**
	 * Returns the correct attribute type for the given attribute name. This must be implemented in subclasses to 
	 * give the appropriate types to each of the class attributes.
	 * @param name
	 * @return
	 */
	public abstract SimulatorAttribute createAttribute(String name);
	
	public int calculateSize() {
		int size = 3*4 + attributes.size()*8;
		
		for (SimulatorAttribute value : attributes.values()) {
			size += value.getSize();
		}
		
		return size;
	}

	public List<AttributeHeader> readHeader(StreamReader stream) throws IOException {
		classID = stream.readInt();
		classSize = stream.readInt();

		int count = stream.readInt();
		List<AttributeHeader> headers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			headers.add(new AttributeHeader(stream.readInt(), stream.readInt()));
		}
		return headers;
	}
	
	public void read(StreamReader stream) throws Exception {
		List<AttributeHeader> headers = readHeader(stream);

		HashManager hasher = HashManager.get();
        for (AttributeHeader header : headers) {
            String name = hasher.getSimulatorName(header.id);

            SimulatorAttribute attribute = createAttribute(name);
            attribute.read(stream, header.size);

            setAttribute(name, attribute);
        }
	}
	
	public void write(StreamWriter stream) throws Exception {
		
		HashManager hasher = HashManager.get();
		
		// We will rewrite the size later, keep the position
		long baseOffset = stream.getFilePointer();
						
		stream.writeInt(classID);
		stream.writeInt(0);
		stream.writeInt(attributes.size());
		
		for (Map.Entry<String, SimulatorAttribute> entry : attributes.entrySet()) {
			stream.writeInt(hasher.getSimulatorHash(entry.getKey()));
			stream.writeInt(entry.getValue().getSize());
		}
		
		for (SimulatorAttribute attribute : attributes.values()) {
			attribute.write(stream);
		}
		
		long endOffset = stream.getFilePointer();
		long size = endOffset - baseOffset;
		
		stream.seek(baseOffset + 4);
		stream.writeUInt(size);
		
		stream.seek(endOffset);
	}
	
	public SimulatorAttribute getAttribute(String name) {
		return attributes.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SimulatorAttribute> T getAttribute(String name, Class<T> clazz) {
		return (T) attributes.get(name);
	}
	
	public void setAttribute(String name, SimulatorAttribute attribute) {
		attributes.put(name, attribute);
	}
	
	public void print(StringBuilder sb, String tabulation) {
		for (Map.Entry<String, SimulatorAttribute> entry : attributes.entrySet()) {
			sb.append(tabulation);
			sb.append('"');
			sb.append(entry.getKey());
			sb.append("\":\t");
			sb.append(entry.getValue().toString(tabulation));
			sb.append("\n");
		}
	}
	
	public void printXML(StringBuilder sb, String tabulation) {
		int i = 0;
		for (Map.Entry<String, SimulatorAttribute> entry : attributes.entrySet()) {
			sb.append(tabulation);
			sb.append('<');
			sb.append(entry.getKey());
			sb.append('>');
			sb.append(entry.getValue().toXmlString(tabulation));
			sb.append("</");
			sb.append(entry.getKey());
			sb.append('>');
			
			if (i + 1 < attributes.size()) {
				sb.append('\n');
			}
			
			i++;
		}
	}
	
//	public static SimulatorClass getClass(int classID) {
//		switch (classID) {
//		case Scenario.CLASS_ID:	return new Scenario();
//		default:	return null;
//		}
//	}

	public static class UnknownSimulatorClass extends SimulatorClass {
		public UnknownSimulatorClass(int classID) {
			super(classID);
		}

		@Override
		public SimulatorAttribute createAttribute(String name) {
			return null;
		}
	}

	public static void scanClasses(StreamReader stream) throws IOException {
		while (stream.getFilePointer() + 4 < stream.length()) {
			int testValue = stream.readInt();
			if (testValue == GENERIC_SERIALIZER_ID) {
				stream.seek(stream.getFilePointer() - 4);
				SimulatorClass simulatorClass = new UnknownSimulatorClass(GENERIC_SERIALIZER_ID);
				long classOffset = stream.getFilePointer();
				List<AttributeHeader> headers = simulatorClass.readHeader(stream);
				long offset = stream.getFilePointer();

				System.out.println("Found class at offset " + classOffset + ", attributes:");
				for (AttributeHeader header : headers) {
					StringBuilder sb = new StringBuilder();
					sb.append("  ");
					sb.append(HashManager.get().hexToString(header.id));
					String attributeName = HashManager.get().getSimulatorName(header.id);
					if (attributeName != null) {
						sb.append(" ");
						sb.append(attributeName);
					}
					sb.append(": offset = ");
					sb.append(offset);
					sb.append("    size = ");
					sb.append(header.size);
					System.out.println(sb.toString());

					offset += header.size;
				}
				System.out.println();
				stream.seek(offset);
			}
		}
	}
}
