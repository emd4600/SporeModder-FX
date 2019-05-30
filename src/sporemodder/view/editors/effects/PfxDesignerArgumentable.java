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
package sporemodder.view.editors.effects;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for any ArgScript object that can take arguments. 
 */
public abstract class PfxDesignerArgumentable {
	protected String name;
	protected final List<PfxDesignerArgument> arguments = new ArrayList<>();
	
	public List<PfxDesignerArgument> getArguments() {
		return arguments;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSingleArgument() {
		return arguments.size() == 1;
	}
	
	public int getArgumentCount() {
		return arguments.size();
	}
	
	/**
	 * Returns the index of the first optional argument. For any index greater or equal than this
	 * the argument is assumed to be optional. Returns 'arguments.size()' if no argument is optional.
	 * @return
	 */
	public int getOptionalIndex() {
		for (int i = 0; i < arguments.size(); ++i) {
			if (arguments.get(i).isOptional()) return i;
		}
		return arguments.size();
	}
}
