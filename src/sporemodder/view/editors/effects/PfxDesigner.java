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

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import sporemodder.view.editors.effects.PfxDesignerType;
import sporemodder.view.inspector.InspectorColorPicker;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class PfxDesigner {

	private final Map<String, PfxDesignerType<?>> types = new HashMap<>();
	
	public PfxDesigner() {
		types.put("colorRGB", new PfxDesignerType<Color>() {
			@Override public InspectorValue<Color> createNode() {
				InspectorColorPicker control = new InspectorColorPicker();
				
				return control;
			}

			@Override public void generateUI(PropertyPane pane, String name, String description) {
				pane.add(name, description, createNode().getNode());
			}
		});
	}
}
