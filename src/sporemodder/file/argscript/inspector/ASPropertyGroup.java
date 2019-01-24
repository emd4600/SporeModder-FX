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
 * Represents a group of properties that are mutually exclusive. Properties inside a group can keep some of 
 * their arguments and options when switching values.
 */
public class ASPropertyGroup {

	private int firstCommonArgument;
	/** How many common arguments are saved when changing the property. */
	private int numCommonArguments;
	/** The keywords of all the options that are saved when changing the property. */
	private final List<String> options = new ArrayList<String>();
	
	public ASPropertyGroup(int numCommonArguments, String ... options) {
		this.numCommonArguments = numCommonArguments;
		this.options.addAll(Arrays.asList(options));
	}
	
	public ASPropertyGroup(int firstCommonArgument, int numCommonArguments, String ... options) {
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

	public List<String> getOptions() {
		return options;
	}
}
