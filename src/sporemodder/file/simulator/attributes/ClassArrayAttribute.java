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
package sporemodder.file.simulator.attributes;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.simulator.SimulatorClass;

public class ClassArrayAttribute<T extends SimulatorClass> implements SimulatorAttribute {
	private final Class<T> clazz;
	public final List<T> value = new ArrayList<T>();
	public final List<Integer> indices = new ArrayList<Integer>();
	private boolean isIndexed;
	
	public ClassArrayAttribute(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public ClassArrayAttribute(Class<T> clazz, boolean isIndexed) {
		this.clazz = clazz;
		this.isIndexed = isIndexed;
	}
	
	public boolean isIndexed() {
		return isIndexed;
	}
	
	@Override
	public void read(StreamReader stream, int size) throws Exception {
		Constructor<T> ctor = clazz.getConstructor();

		int count = stream.readInt();
		
		for (int i = 0; i < count; i++) {
			if (isIndexed) {
				indices.add(stream.readInt());
			}
			
			T object = ctor.newInstance();
			object.read(stream);
			value.add(object);
		}
	}

	@Override
	public void write(StreamWriter stream) throws Exception {
		stream.writeInt(value.size());
		
		for (int i = 0; i < value.size(); i++) {
			
			if (isIndexed) {
				stream.writeInt(indices.get(i));
			}
			
			value.get(i).write(stream);
		}
	}

	@Override
	public int getSize() {
		int size = 4;
		
		for (T object : value) {
			size += object.calculateSize();
		}
		
		if (isIndexed) {
			size += 4 * value.size();
		}
		
		return size;
	}
	
	@Override
	public String toString(String tabulation) {
		if (value.isEmpty()) {
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for (int i = 0; i < value.size(); i++) {
			sb.append(tabulation + "\t{\n");
			
			if (isIndexed) {
				sb.append(tabulation);
				sb.append("\t\t\"index\": ");
				sb.append(indices.get(i));
				sb.append('\n');
			}
			
			value.get(i).print(sb, tabulation + "\t\t");
			
			sb.append(tabulation);
			sb.append("\t}");
			
			if (i + 1 < value.size()) {
				sb.append(',');
			}
			
			sb.append('\n');
		}
		
		sb.append(tabulation);
		sb.append(']');
		
		return sb.toString();
	}
	
	@Override
	public String toXmlString(String tabulation) {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		if (!value.isEmpty()) {
			
			for (int i = 0; i < value.size(); i++) {
				sb.append(tabulation + "\t<" + clazz.getSimpleName() + ">\n");
				
				value.get(i).printXML(sb, tabulation + "\t\t");
				sb.append('\n');
				
				sb.append(tabulation + "\t</" + clazz.getSimpleName() + ">");
				
				if (i + 1 < value.size()) {
					sb.append('\n');
				}
			}
		}
		sb.append(tabulation);
		return sb.toString();
	}
}
