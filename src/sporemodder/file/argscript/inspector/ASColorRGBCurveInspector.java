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
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.util.ColorRGB;

public class ASColorRGBCurveInspector extends ASValueInspector {
	
	private ColorRGB defaultValue = ColorRGB.white();
	private boolean requiresValue;
	private boolean is255;
	
	private Pane gradientPane;
	private final List<ColorRGB> colors = new ArrayList<ColorRGB>();
	private final List<ColorPicker> colorPickers = new ArrayList<ColorPicker>();
	
	/** We use our own panel because all elements need to be removed and inserted again. */
	private VBox contentPanel;
	private VBox valuesPanel;

	/**
	 * 
	 * @param name
	 * @param descriptionCode
	 * @param defaultValue
	 * @param requiresValue Whether at least 1 value is required.
	 */
	public ASColorRGBCurveInspector(String name, String descriptionCode, ColorRGB defaultValue, boolean requiresValue) {
		this(name, descriptionCode, false, defaultValue, requiresValue);
	}
	
	public ASColorRGBCurveInspector(String name, String descriptionCode, boolean is255, ColorRGB defaultValue, boolean requiresValue) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
		this.requiresValue = requiresValue;
		this.is255 = is255;
	}
	
	private String colorToString(ColorRGB color) {
		String str = color.toString();
		return str.substring(1, str.length() - 1);
	}
	
	private String colorToString255(ColorRGB color) {
		String str = color.toString255();
		return str.substring(1, str.length() - 1);
	}
	
	private void insertElements() {
		colors.clear();
		contentPanel.getChildren().clear();
		valuesPanel.getChildren().clear();
		colorPickers.clear();
		
		int count = getRemoveableCount();
		ArgScriptArguments args = getArguments();
		
		for (int i = 0; i < count; i++) {
			ColorRGB value = new ColorRGB();
			if (is255) lineInspector.getStream().parseColorRGB255(args, argIndex + i, value);
			else lineInspector.getStream().parseColorRGB(args, argIndex + i, value);
			colors.add(value);
		}
		
		// Generate color pickers for every value
		
		for (int i = 0; i < count; i++) {
			ColorRGB value = colors.get(i);
			
			ColorPicker colorPicker = new ColorPicker();
			colorPickers.add(colorPicker);
			colorPicker.setPrefWidth(Double.MAX_VALUE);
			colorPicker.setValue(value.toColor());
			colorPicker.setCursor(Cursor.DEFAULT);
			
			int index = i;
			
			colorPicker.setOnAction((event) -> {
				// If it's not an option, it will come after the keyword
				int splitIndex = argIndex + index + createIfNecessary();
				
				if (is255) lineInspector.getSplits().set(splitIndex, colorToString255(new ColorRGB(colorPicker.getValue())));
				else lineInspector.getSplits().set(splitIndex, colorToString(new ColorRGB(colorPicker.getValue())));
				
				colors.set(index, new ColorRGB(colorPicker.getValue()));
				updateGradient();
				
				submitChanges();
			});
			
			Button deleteButton = new Button("X");
			deleteButton.setOnAction((event) -> {
				// If it's not an option, it will come after the keyword
				int splitIndex = argIndex + index + createIfNecessary();
				
				if (requiresValue && colorPickers.size() == 1) {
					colorPickers.get(index).setValue(defaultValue.toColor());
					
					if (is255) lineInspector.getSplits().set(splitIndex, colorToString255(defaultValue));
					else lineInspector.getSplits().set(splitIndex, colorToString(defaultValue));
					
					colors.set(index, defaultValue);
					updateGradient();
					
					this.submitChanges();
				}
				else {
					colors.remove(index);
					updateGradient();
					
					lineInspector.getSplits().remove(splitIndex);
					this.submitChanges();
					
					// Since we have to update the indices, we will just remove all and insert them again
					insertElements();
					//lineInspector.getInspector().updateUI();
				}
			});
			
			BorderPane pane = new BorderPane();
			pane.setRight(deleteButton);
			pane.setCenter(colorPicker);
			
			valuesPanel.getChildren().add(pane);
		}
		
		valuesPanel.setCursor(Cursor.HAND);
		valuesPanel.setOnMouseClicked((event) -> {
			if (event.getClickCount() != 2) {
				return;
			}
			
			double y = event.getY();
			
			int index = 0;
			
			for (int i = 0; i < colorPickers.size(); i++) {
				// Color pickers are inside a panel, so we have to do this on their parent
				Bounds bounds = colorPickers.get(i).getParent().getBoundsInParent();
				
				if (y > bounds.getMaxY()) {
					index = i;
				}
				else {
					break;
				}
			}
			
			// We don't want to insert it at that position, but next to it
			index += 1;
			
			int splitIndex = argIndex + index + createIfNecessary();
			
			if (is255) lineInspector.getSplits().add(splitIndex, colorToString255(defaultValue));
			else lineInspector.getSplits().add(splitIndex, colorToString(defaultValue));
			
			colors.add(index, defaultValue);
			updateGradient();
			
			submitChanges();
			insertElements();
			//lineInspector.getInspector().updateUI();
		});
		
		contentPanel.getChildren().add(valuesPanel);
		
		// Add value button
		Button addButton = new Button("Add value");
		addButton.setPrefWidth(Double.MAX_VALUE);
		addButton.setOnAction((event) -> {
			// Get the index that comes after the last value
			int lastIndex = argIndex + getRemoveableCount() + createIfNecessary();
			List<String> splits = lineInspector.getSplits();
			
			// Add a new value in the correct position, update the text and update the UI so another spinner is shown.
			if (lastIndex == splits.size()) {
				if (is255) lineInspector.getSplits().add(colorToString255(defaultValue));
				else lineInspector.getSplits().add(colorToString(defaultValue));
			} else {
				if (is255) lineInspector.getSplits().add(lastIndex, colorToString255(defaultValue));
				else lineInspector.getSplits().add(lastIndex, colorToString(defaultValue));
			}
			
			colors.add(defaultValue);
			updateGradient();
			
			submitChanges();
			insertElements();
			//lineInspector.getInspector().updateUI();
		});
		contentPanel.getChildren().add(addButton);
		
		
		gradientPane = new Pane();
		gradientPane.setPrefHeight(100);
		gradientPane.setPrefWidth(Double.MAX_VALUE);
		
		updateGradient();

		contentPanel.getChildren().add(gradientPane);
	}
	
	private void updateGradient() {
		List<Stop> gradientStops = new ArrayList<Stop>();
		
		float max = (float) (colors.size() - 1);
		if (max == 0) max = 1;
		
		for (int i = 0; i < colors.size(); i++) {
			gradientStops.add(new Stop(i / max, colors.get(i).toColor()));
		}
		
		LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, gradientStops);
		gradientPane.setBackground(new Background(new BackgroundFill(gradient, null, null)));
	}

	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		contentPanel = new VBox(5);
		
		valuesPanel = new VBox(10);
		
		insertElements();
		
		panel.getChildren().add(contentPanel);
	}

	@Override
	public int getArgumentCount() {
		return -1;
	}

	@Override
	void addDefaultValue(List<String> splits) {
		if (requiresValue) {
			if (is255) lineInspector.getSplits().add(colorToString255(defaultValue));
			else lineInspector.getSplits().add(colorToString(defaultValue));
		}
	}

	@Override
	int getRemoveableCount() {
		return getArguments().get().size() - argIndex;
	}

	@Override
	boolean isDefault() {
		if (requiresValue) {
			if (getRemoveableCount() != 1) {
				return false;
			}
			
			ColorRGB test = new ColorRGB();
			
			if (is255) lineInspector.getStream().parseColorRGB255(getArguments(), argIndex, test);
			else lineInspector.getStream().parseColorRGB(getArguments(), argIndex, test);
			
			return defaultValue.equals(test);
		} else {
			return getRemoveableCount() == 0;
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		for (ColorPicker picker : colorPickers) {
			picker.setDisable(!isEnabled);
		}
		gradientPane.setDisable(!isEnabled);
	}
}
