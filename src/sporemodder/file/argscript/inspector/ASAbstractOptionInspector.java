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

import javafx.scene.layout.VBox;
import sporemodder.file.argscript.ArgScriptArguments;

public abstract class ASAbstractOptionInspector {

	protected ASLineInspector<?> lineInspector;
	protected ArgScriptArguments args;
	
	public void setLineInspector(ASLineInspector<?> lineInspector, boolean updateArguments) {
		this.lineInspector = lineInspector;
	}
	
	public ArgScriptArguments getArguments() {
		return args;
	}
	
	public abstract void setEnabled(boolean isEnabled);
	
	public abstract void updateArguments();
	
	public abstract void generateUI(VBox pane);
	
	/**
	 * Writes this option if it doesn't exist yet into the splits, without updating the text editor.
	 * @return The index of the option.
	 */
	public abstract int createIfNecessary(ASValueInspector caller);
}
