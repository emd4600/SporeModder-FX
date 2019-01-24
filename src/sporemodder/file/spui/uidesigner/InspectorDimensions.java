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
import sporemodder.view.inspector.InspectorIntSpinner;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class InspectorDimensions implements InspectorValue<int[]> {

	private final InspectorIntSpinner[] spinners = new InspectorIntSpinner[2];
	private final PropertyPane pane = new PropertyPane();
	
	private final int[] oldValue = new int[2];
	private final int[] value;
	
	private final List<ChangeListener<int[]>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorDimensions(int[] dimensions) {
		if (dimensions == null) {
			throw new NullPointerException("Must specify a int[2] for the inspector to modify.");
		}
		this.value = dimensions;
		oldValue[0] = value[0];
		oldValue[1] = value[1];
		
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i] = new InspectorIntSpinner();
			spinners[i].setRange(0, Integer.MAX_VALUE);
			spinners[i].setValue((long) dimensions[i]);
			final int index = i;
			spinners[i].addValueListener((obs, oldValue, newValue) -> {
				this.oldValue[0] = this.value[0];
				this.oldValue[1] = this.value[1];
				dimensions[index] = newValue.intValue();

				if (!settingValue) updateListeners();
			});
 		}
		
		pane.add("Width:", null, spinners[0]);
		pane.add("Height:", null, spinners[1]);
	}

	@Override
	public int[] getValue() {
		return value;
	}

	@Override
	public void setValue(int[] value) {
		oldValue[0] = this.value[0];
		oldValue[1] = this.value[1];
		this.value[0] = value[0];
		this.value[1] = value[1];
		
		settingValue = true;
		loadValue();
		settingValue = false;
	}

	@Override
	public Node getNode() {
		return pane.getNode();
	}
	
	private void updateListeners() {
		int[] oldValue = new int[] {this.oldValue[0], this.oldValue[1]};
		int[] newValue = new int[] {this.value[0], this.value[1]};
		for (ChangeListener<int[]> listener : listeners) {
			listener.changed(null, oldValue, newValue);
		}
	}
	
	public void addValueListener(ChangeListener<int[]> listener) {
		listeners.add(listener);
	}
	
	public void removeValueListener(ChangeListener<int[]> listener) {
		listeners.remove(listener);
	}
	
	private void loadValue() {
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i].setValue((long) value[i]);
		}
	}

	public void setStep(long stepSize) {
		for (int i = 0; i < spinners.length; ++i) {
			spinners[i].setStep(stepSize);
		}
	}
}
