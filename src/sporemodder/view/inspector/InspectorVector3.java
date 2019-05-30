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
package sporemodder.view.inspector;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import sporemodder.util.Vector3;

public class InspectorVector3 implements InspectorValue<Vector3> {

	private final InspectorFloatSpinner[] spinners = new InspectorFloatSpinner[3];
	private final PropertyPane pane = new PropertyPane();
	
	private final Vector3 oldValue = new Vector3();
	private final Vector3 value;
	
	private final List<ChangeListener<Vector3>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorVector3(Vector3 vector) {
		if (vector == null) {
			throw new NullPointerException("Must specify a Vector3 for the inspector to modify.");
		}
		this.value = vector;
		oldValue.set(vector);
		
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i] = new InspectorFloatSpinner();
			spinners[i].setValue((double) vector.get(i));
			final int index = i;
			spinners[i].valueProperty().addListener((obs, oldValue, newValue) -> {
				this.oldValue.set(this.value);
				vector.set(index, newValue.floatValue());

				if (!settingValue) updateListeners();
			});
 		}
		
		pane.add("X:", null, spinners[0]);
		pane.add("Y:", null, spinners[1]);
		pane.add("Z:", null, spinners[2]);
	}

	@Override
	public Vector3 getValue() {
		return value;
	}

	@Override
	public void setValue(Vector3 value) {
		oldValue.set(this.value);
		this.value.set(value);
		
		settingValue = true;
		loadValue();
		settingValue = false;
	}

	@Override
	public Node getNode() {
		return pane.getNode();
	}
	
	private void updateListeners() {
		Vector3 oldValue = new Vector3(this.oldValue);
		Vector3 newValue = new Vector3(this.value);
		for (ChangeListener<Vector3> listener : listeners) {
			listener.changed(null, oldValue, newValue);
		}
	}
	
	public void addValueListener(ChangeListener<Vector3> listener) {
		listeners.add(listener);
	}
	
	public void removeValueListener(ChangeListener<Vector3> listener) {
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
}
