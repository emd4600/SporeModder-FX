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
package sporemodder.file;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.prop.PropConverter;

public class LocalizedText {

	private String text;
	private int tableID;
	private int instanceID;
	
	public LocalizedText(String text) {
		this.text = text;
	}
	
	public LocalizedText(int tableID, int instanceID) {
		this.tableID = tableID;
		this.instanceID = instanceID;
	}
	
	public LocalizedText() {
	}
	
	public LocalizedText(LocalizedText other) {
		this.text = other.text;
		this.tableID = other.tableID;
		this.instanceID = other.instanceID;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getTableID() {
		return tableID;
	}
	public void setTableID(int tableID) {
		this.tableID = tableID;
	}
	public int getInstanceID() {
		return instanceID;
	}
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	
	public void write(ArgScriptWriter writer) {
		HashManager hasher = HashManager.get();
		
		if (tableID != 0) {
			StringBuilder sb = new StringBuilder();
			
			sb.append(hasher.getFileName(tableID));
			sb.append('!');
			sb.append(hasher.hexToString(instanceID));
			
			writer.parenthesis(sb.toString());
		}
		
		if (text != null) {
			writer.literal(PropConverter.intoValidText(text));
		}
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		HashManager hasher = HashManager.get();
		
		if (tableID != 0) {
			sb.append('(');
			sb.append(hasher.getFileName(tableID));
			sb.append('!');
			sb.append(hasher.hexToString(instanceID));
			sb.append(')');
		} else if (text != null) {
			sb.append(text);
		}
		
		return sb.toString();
	}

	public void copy(LocalizedText other) {
		this.text = other.text;
		this.instanceID = other.instanceID;
		this.tableID = other.tableID;
	}
}
