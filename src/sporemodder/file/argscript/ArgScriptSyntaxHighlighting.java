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
package sporemodder.file.argscript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgScriptSyntaxHighlighting {

	private final List<String> blocks = new ArrayList<String>();
	private final List<String> commands = new ArrayList<String>();
	private final List<String> options = new ArrayList<String>();
	private final List<String> enums = new ArrayList<String>();
	
	public List<String> getBlocks() {
		return blocks;
	}

	public List<String> getCommands() {
		return commands;
	}

	public List<String> getOptions() {
		return options;
	}

	public List<String> getEnums() {
		return enums;
	}

	public void addBlocks(String ... names) {
		blocks.addAll(Arrays.asList(names));
	}
	
	public void addCommands(String ... names) {
		commands.addAll(Arrays.asList(names));
	}
	
	public void addOptions(String ... names) {
		options.addAll(Arrays.asList(names));
	}
	
	public void addEnums(String ... names) {
		enums.addAll(Arrays.asList(names));
	}
}
