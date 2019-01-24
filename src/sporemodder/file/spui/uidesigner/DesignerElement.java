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
package sporemodder.file.spui.uidesigner;

import sporemodder.file.spui.SpuiElement;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.inspector.PropertyPane;

/** 
 * A part of a designer class that can generate a user interface to edit it.
 */
public abstract class DesignerElement implements DesignerNode {
	protected String name;
	/** The class that defines this element. */
	protected final DesignerClass parentClass;
	
	public DesignerElement(DesignerClass parentClass) {
		this.parentClass = parentClass;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the class that defines this element.
	 * @return
	 */
	public final DesignerClass getParentClass() {
		return parentClass;
	}
	
	public abstract void generateUI(DesignerUIBuilder builder);
	public abstract void generateUI(SpuiEditor editor, PropertyPane parentPane, SpuiElement element);
}
