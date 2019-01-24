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
package sporemodder.file.argscript.inspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a group of options that are mutually exclusive. Options inside a group can keep some of 
 * their arguments when switching values.
 */
public class ASOptionGroup {

	private final List<ASOptionInspector> options = new ArrayList<ASOptionInspector>();
	private int firstCommonArgument;
	private int numCommonArguments;
	
	public ASOptionGroup(int numCommonArguments, ASOptionInspector ... options) {
		this.numCommonArguments = numCommonArguments;
		this.options.addAll(Arrays.asList(options));
	}
	
	public ASOptionGroup(int firstCommonArgument, int numCommonArguments, ASOptionInspector ... options) {
		this.numCommonArguments = numCommonArguments;
		this.firstCommonArgument = firstCommonArgument;
		this.options.addAll(Arrays.asList(options));
	}
	
	public int getNumberOfCommonArguments() {
		return numCommonArguments;	
	}
	
	public int getFirstCommonArgument() {
		return firstCommonArgument;
	}
	
	public ASOptionInspector getOption(String name) {
		for (ASOptionInspector option : options) {
			if (option.getName().equals(name)) {
				return option;
			}
		}
		return null;
	}

	public List<ASOptionInspector> getOptions() {
		return options;
	}
}
