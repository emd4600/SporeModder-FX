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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.simulator.SimulatorClass;

public class ClassAttribute<T extends SimulatorClass> implements SimulatorAttribute {
	private final Class<T> clazz;
	public T value;
	
	public ClassAttribute(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void read(StreamReader stream, int size) throws Exception {
		Constructor<T> ctor = clazz.getConstructor();
		value = ctor.newInstance();
		
		value.read(stream);
	}

	@Override
	public void write(StreamWriter stream) throws Exception {
		value.write(stream);
	}

	@Override
	public int getSize() {
		return value.calculateSize();
	}
	
	@Override
	public String toString(String tabulation) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		
		value.print(sb, tabulation + "\t");
		
		sb.append(tabulation);
		sb.append('}');
		
		return sb.toString();
	}

	@Override
	public String toXmlString(String tabulation) {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		value.printXML(sb, tabulation + "\t");
		sb.append('\n');
		sb.append(tabulation);
		return sb.toString();
	}
}
