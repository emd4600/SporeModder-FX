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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

/**
 * A special type of {@link InspectorList} that uses {@link InspectorValue} objects of a certain type T to display
 * and edit lists of that T type. When creating this object, the developer must provide a backup list that will contain the T
 * values and reflect the edition changes made by the user.
 * <p>
 * There are two ways of showing the inspector value components:
 * <li>By providing a generator function, that receives the value and must return the corresponding inspector component.
 * <li>By overriding the {@link #fillValuesList(List, List)} method, which must clear and fill the components list
 * to adapt to the new values provided.
 * <p>
 * The values of the backup list should not be modified manually; instead, they are updated by this class or by using the {@link #setValue(List)}.
 * For this reason, the {@link #getValue()} method only returns a copy, that you can freely modify.
 * <p>
 * Value listeners are called every time the list is changed. This includes reordering, deleting and creating values. They are also called
 * whenever a component calls its own value listeners, that is, whenever a value changes.
 * @param <T> The type of value managed by the components in the list.
 */
public class InspectorValueList<T> extends InspectorList<Node> implements InspectorValue<List<T>> {
	
	private final List<T> backupList;
	private final List<InspectorValue<?>> valuesList;
	private final Function<T, InspectorValue<T>> valuesGenerator;
	
	private final List<ChangeListener<List<T>>> listeners = new ArrayList<>();
	
	// This will allow us to get the inspector component (and therefore value) when the nodes change
	private final Map<Node, InspectorValue<?>> componentMap = new HashMap<>();
	
	private boolean isSettingValue;
	
	public InspectorValueList(List<T> backupList) {
		this(backupList, null);
	}
	
	@SuppressWarnings("unchecked")
	public InspectorValueList(List<T> backupList, Function<T, InspectorValue<T>> valuesGenerator) {
		super();
		this.backupList = backupList;
		this.valuesList = new ArrayList<>();
		this.valuesGenerator = valuesGenerator;
		
		loadValues();
		
		setOnEditFinish(nodeList -> {
			if (!isSettingValue) {
				List<T> oldList = new ArrayList<>(this.backupList);
				
				this.backupList.clear();
				for (Node node : nodeList) {
					this.backupList.add((T) componentMap.get(node).getValue());
				}
				
				callListeners(oldList);
			}
		});
	}

	@Override
	public List<T> getValue() {
		return new ArrayList<>(backupList);
	}

	@Override
	public void setValue(List<T> value) {
		isSettingValue = true;
		
		backupList.clear();
		backupList.addAll(value);
		
		loadValues();
		
		isSettingValue = false;
	}
	
	@SuppressWarnings("unchecked")
	private void loadValues() {
		fillValuesList(valuesList, backupList);
		
		componentMap.clear();
		// We only want it to trigger one change listener, so do it on a separated list
		List<Node> nodesList = new ArrayList<>();
		
		for (InspectorValue<?> component : valuesList) {
			Node node = component.getNode();
			componentMap.put(node, component);
			nodesList.add(node);
			
			component.addValueListener((obs, oldValue, newValue) -> {
				List<T> oldList = new ArrayList<>(backupList);
				this.backupList.clear();
				for (Node n : getItems()) {
					this.backupList.add((T) componentMap.get(n).getValue());
				}
				
				callListeners(oldList);
			});
		}
		
		getItems().setAll(nodesList);
	}
	
	protected void fillValuesList(List<InspectorValue<?>> dest, List<T> backupList) {
		dest.clear();
		for (T value : backupList) {
			dest.add(valuesGenerator.apply(value));
		}
	}

	@Override
	public Node getNode() {
		return this;
	}
	
	private void callListeners(List<T> oldValue) {
		List<T> newList = new ArrayList<>(backupList);
		for (ChangeListener<List<T>> listener : listeners) {
			listener.changed(null, oldValue, newList);
		}
	}

	@Override
	public void addValueListener(ChangeListener<List<T>> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeValueListener(ChangeListener<List<T>> listener) {
		listeners.remove(listener);
	}

}
