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

import java.util.List;

import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.util.inspector.InspectorValue;

public abstract class ASValueInspector {

	protected ASLineInspector<?> lineInspector;
	protected String title;
	/** The index of the first argument. */
	protected int argIndex;
	/** The option this inspector modifies, or null if it modifies the main arguments. */
	protected ASAbstractOptionInspector option;
	
	protected String descriptionCode;
	
	protected boolean isOptional;
	
	
	public ASValueInspector(String title, String descriptionCode) {
		this.title = title;
		this.descriptionCode = descriptionCode;
	}
	
	public abstract int getArgumentCount();
	

	public boolean isOptional() {
		return isOptional;
	}

	public ASValueInspector setOptional(boolean isOptional) {
		this.isOptional = isOptional;
		return this;
	}
	
	public abstract void setEnabled(boolean isEnabled);

	public void generateUI(VBox pane) {
		new InspectorValue(title, DocumentationManager.get().getDocumentation(descriptionCode)).generateUI(pane);
	}
	
	void setLineInspector(ASLineInspector<?> lineInspector, ASAbstractOptionInspector option) {
		this.lineInspector = lineInspector;
		this.option = option;
	}
	
	void setArgIndex(int argIndex) {
		this.argIndex = argIndex;
	}
	
	protected int createIfNecessary() {
		return option != null ? option.createIfNecessary(this) : lineInspector.createIfNecessary(this);
	}
	
	/**
	 * Overrides of this method must add the default value to the splits list. It is possible
	 * to add more than one value.
	 * @param splits
	 */
	abstract void addDefaultValue(List<String> splits);
	/**
	 * Tells how many arguments must be removed in order to completely remove this value.
	 * @return
	 */
	abstract int getRemoveableCount();
	abstract boolean isDefault();
	
	protected void submitChanges() {
		lineInspector.submitText();
	}
	
	protected boolean isAvailable() {
		ArgScriptArguments args = option != null ? option.getArguments() : lineInspector.getArguments();
		if (args == null) return false;
		if (!isOptional) {
			return true;
		}
		else {
			return argIndex < args.get().size(); 
		}
	}
	
	protected ArgScriptArguments getArguments() {
		if (!isAvailable()) return null;
		return option != null ? option.getArguments() : lineInspector.getArguments();
	}
	
	protected String getArgument(int index) {
		ArgScriptArguments args = option != null ? option.getArguments() : lineInspector.getArguments();
		
		index += argIndex;
		
		// If the option was not found
		if (args == null) {
			return null;
		}
		
		int count = args.get().size();
		
		if (count > index) {
			return args.get(index);
		}
		else {
			return null;
		}
	}

	public int getArgIndex() {
		return argIndex;
	}
}
