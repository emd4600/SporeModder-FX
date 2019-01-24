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

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.view.inspector.InspectorFloatSpinner;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class InspectorRectangle implements InspectorValue<SPUIRectangle> {

	private final InspectorFloatSpinner[] spinners = new InspectorFloatSpinner[4];
	private final PropertyPane pane = new PropertyPane();
	
	private final SPUIRectangle oldValue = new SPUIRectangle();
	private final SPUIRectangle value;
	
	private final List<ChangeListener<SPUIRectangle>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorRectangle(SPUIRectangle rect) {
		if (rect == null) {
			throw new NullPointerException("Must specify a SPUIRectangle for the inspector to modify.");
		}
		this.value = rect;
		oldValue.copy(rect);
		
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i] = new InspectorFloatSpinner();
			spinners[i].setValue((double) rect.get(i));
			final int index = i;
			spinners[i].addValueListener((obs, oldValue, newValue) -> {
				this.oldValue.copy(this.value);
				rect.set(index, newValue.floatValue());

				if (!settingValue) updateListeners();
			});
 		}
		
		pane.add("X1:", "Top-left X:", spinners[0]);
		pane.add("Y1:", "Top-left Y:", spinners[1]);
		pane.add("X2:", "Bottom-right X:", spinners[2]);
		pane.add("Y2:", "Bottom-right Y:", spinners[3]);
	}

	@Override
	public SPUIRectangle getValue() {
		return value;
	}

	@Override
	public void setValue(SPUIRectangle value) {
		oldValue.copy(this.value);
		this.value.copy(value);
		
		settingValue = true;
		loadValue();
		settingValue = false;
	}

	@Override
	public Node getNode() {
		return pane.getNode();
	}
	
	private void updateListeners() {
		SPUIRectangle oldValue = new SPUIRectangle(this.oldValue);
		SPUIRectangle newValue = new SPUIRectangle(this.value);
		for (ChangeListener<SPUIRectangle> listener : listeners) {
			listener.changed(null, oldValue, newValue);
		}
	}
	
	public void addValueListener(ChangeListener<SPUIRectangle> listener) {
		listeners.add(listener);
	}
	
	public void removeValueListener(ChangeListener<SPUIRectangle> listener) {
		listeners.remove(listener);
	}
	
	private void loadValue() {
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i].setValue((double) value.get(i));
		}
	}

	public void setStep(double stepSize) {
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i].setStep(stepSize);
		}
	}
	
	public void setRange(double min, double max) {
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i].setRange(min, max);
		}
	}
}
