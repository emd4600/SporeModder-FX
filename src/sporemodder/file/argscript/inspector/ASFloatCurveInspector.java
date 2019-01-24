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
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLexer;

public class ASFloatCurveInspector extends ASValueInspector {
	
	private float defaultValue;
	private boolean requiresValue;
	private float maxValue = Float.MAX_VALUE;
	private float minValue = -Float.MAX_VALUE;
	private float step = 0.1f;
	
	private boolean chartBeingEdited;
	private LineChart<Number, Number> chart;
	private final List<Spinner<Double>> spinners = new ArrayList<Spinner<Double>>();
	
	/** We use our own panel because all elements need to be removed and inserted again. */
	private VBox contentPanel;
	private VBox spinnersPanel;
	
	private boolean spinnerWasEmpty;
	private StringConverter<Double> stringConverter;

	/**
	 * 
	 * @param name
	 * @param descriptionCode
	 * @param defaultValue
	 * @param requiresValue Whether at least 1 value is required.
	 */
	public ASFloatCurveInspector(String name, String descriptionCode, float defaultValue, boolean requiresValue) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
		this.requiresValue = requiresValue;
		initStringConverter();
	}
	
	public ASFloatCurveInspector setRange(float min, float max) {
		this.maxValue = max;
		this.minValue = min;
		return this;
	}
	
	public ASFloatCurveInspector setStep(float step) {
		this.step = step;
		return this;
	}
	
	private void initStringConverter() {
		stringConverter = new StringConverter<Double>() {

			@Override
			public Double fromString(String arg0) {
				spinnerWasEmpty = false;
				
				if (arg0.trim().isEmpty()) {
					spinnerWasEmpty = true;
					// The change won't commit unless this value is different to the current one, so return the least possible value
					// We do have to return the default value if this is the last spinner and the user required it
					if (requiresValue && spinners.size() == 1) {
						return (double) defaultValue;
					}
					else {
						return Double.POSITIVE_INFINITY;
					}
				}
				
				ArgScriptLexer lexer = new ArgScriptLexer();
				lexer.setText(arg0);
				try {
					return lexer.parseFloat();
				} catch (DocumentException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String toString(Double value) {
				if (value == null) return "";
				return HashManager.get().floatToString(value.floatValue());
			}
			
		};
	}
	
	private void insertElements() {
		contentPanel.getChildren().clear();
		spinnersPanel.getChildren().clear();
		spinners.clear();
		
		int count = getRemoveableCount();
		ArgScriptArguments args = getArguments();
		List<Float> values = new ArrayList<Float>();
		
		for (int i = 0; i < count; i++) {
			values.add(lineInspector.getStream().parseFloat(args, argIndex + i));
		}
		
		// Generate spinners for every value
		
		for (int i = 0; i < count; i++) {
			Float value = values.get(i);
			Spinner<Double> spinner = new Spinner<Double>();
			spinners.add(spinner);
			spinner.setPrefWidth(Double.MAX_VALUE);
			
			if (value == null) {
				spinner.getEditor().setStyle("-fx-background-fill: red");
			}
			
			
			SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
			valueFactory.setAmountToStepBy(step);
			valueFactory.setConverter(stringConverter);
			valueFactory.setValue(value == null ? (double)defaultValue : value.doubleValue());
			
			spinner.setValueFactory(valueFactory);
			
			int index = i;
			
			spinner.setEditable(true);
			spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					spinner.increment(0); // won't change value, but will commit editor
				}
			});
			valueFactory.valueProperty().addListener((obs, oldValue, newValue) -> {
				
				// This might be called by the lose focus listener when the spinner is removed, so check if that happens
				if (!spinners.contains(spinner)) {
					return;
				}
				
				// If it's not an option, it will come after the keyword
				int splitIndex = argIndex + index + createIfNecessary();
				
				// If no value is input, remove the value
				if (spinnerWasEmpty) {
					
					if (requiresValue && spinners.size() == 1) {
						lineInspector.getSplits().set(splitIndex, stringConverter.toString((double) defaultValue));
						
						if (!chartBeingEdited) {
							chart.getData().get(0).getData().set(index, new XYChart.Data<Number, Number>(index, defaultValue));
						}
						
						this.submitChanges();
					}
					else {
						lineInspector.getSplits().remove(splitIndex);
						this.submitChanges();
						
						// Since we have to update the indices, we will just remove all and insert them again
						insertElements();
						// contentPanel.updateUI();
					}
				}
				else {
					
					lineInspector.getSplits().set(splitIndex, stringConverter.toString(newValue));
					
					if (!chartBeingEdited) {
						chart.getData().get(0).getData().set(index, new XYChart.Data<Number, Number>(index, newValue));
					}
					
					this.submitChanges();
				}
			});
			
			spinnersPanel.getChildren().add(spinner);
		}
		
		spinnersPanel.setCursor(Cursor.HAND);
		spinnersPanel.setOnMouseClicked((event) -> {
			if (event.getClickCount() != 2) {
				return;
			}
			
			double y = event.getY();
			
			int index = 0;
			
			for (int i = 0; i < spinners.size(); i++) {
				Bounds bounds = spinners.get(i).getBoundsInParent();
				
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
			
			lineInspector.getSplits().add(splitIndex, stringConverter.toString((double) defaultValue));
			
			addChartValue(index, defaultValue);
			
			submitChanges();
			insertElements();
			// contentPanel.updateUI();
			
			// Set the focus on the new spinner and select all text
			Spinner<Double> spinner = spinners.get(index);
			spinner.requestFocus();
			spinner.getEditor().selectAll();
			spinner.getEditor().requestFocus();
		});
		
		contentPanel.getChildren().add(spinnersPanel);
		
		// Add value button
		Button addButton = new Button("Add value");
		addButton.setPrefWidth(Double.MAX_VALUE);
		addButton.setOnAction((event) -> {
			// Get the index that comes after the last value
			int lastIndex = argIndex + getRemoveableCount() + createIfNecessary();
			List<String> splits = lineInspector.getSplits();
			
			// Add a new value in the correct position, update the text and update the UI so another spinner is shown.
			if (lastIndex == splits.size()) {
				splits.add(stringConverter.toString((double) defaultValue));
			} else {
				splits.add(lastIndex, stringConverter.toString((double) defaultValue));
			}
			
			submitChanges();
			insertElements();
			// contentPanel.updateUI();
			
			// Set the focus on the new spinner and select all text
			Spinner<Double> spinner = spinners.get(spinners.size() - 1);
			spinner.requestFocus();
			spinner.getEditor().selectAll();
			spinner.getEditor().requestFocus();
		});
		contentPanel.getChildren().add(addButton);
		
		// Generate the chart
		final NumberAxis xAxis = new NumberAxis();
	    final NumberAxis yAxis = new NumberAxis();
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		
		chart = new LineChart<Number, Number>(xAxis, yAxis);
		chart.setPrefHeight(220);
		xAxis.setTickLabelsVisible(false);
		chart.setLegendVisible(false);
		chart.getData().add(series);
		
		for (int i = 0; i < count; i++) {
			addChartValue(i, values.get(i) == null ? 0 : values.get(i));
		}
		
		
		chart.setAnimated(false);
		xAxis.setAutoRanging(false);
		xAxis.setUpperBound(count - 1);
		
		contentPanel.getChildren().add(chart);
	}
	
	private void addChartValue(int index, Number value) {
		final XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(index, value);
		chart.getData().get(0).getData().add(index, data);
		
		data.getNode().setCursor(Cursor.HAND);
		data.getNode().setOnMouseClicked((event) -> {
			Spinner<Double> spinner = spinners.get(index);
			spinner.requestFocus();
			spinner.getEditor().selectAll();
			spinner.getEditor().requestFocus();
		});
		data.getNode().setOnMousePressed((event) -> {
			chart.getYAxis().setAutoRanging(false);
			chartBeingEdited = true;
		});
		data.getNode().setOnMouseReleased((event) -> {
			chart.getYAxis().setAutoRanging(true);
			chartBeingEdited = false;
		});
		data.getNode().setOnMouseDragged((event) -> {
			Point2D pointInScene = new Point2D(event.getSceneX(), event.getSceneY());
            double yAxisLoc = chart.getYAxis().sceneToLocal(pointInScene).getY();
            Number y = chart.getYAxis().getValueForDisplay(yAxisLoc);
            data.setYValue(y);
            
            spinners.get(index).getValueFactory().setValue(y.doubleValue());
		});
	}

	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		contentPanel = new VBox(5);
		
		spinnersPanel = new VBox(10);
		
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
			splits.add(stringConverter.toString((double) defaultValue));
		}
	}

	@Override
	int getRemoveableCount() {
		return getArguments().get().size() - argIndex;
	}

	@Override
	boolean isDefault() {
		if (requiresValue) {
			return getRemoveableCount() == 1 && stringConverter.fromString(getArgument(0)).equals((double) defaultValue);
		} else {
			return getRemoveableCount() == 0;
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		for (Spinner<Double> spinner : spinners) {
			spinner.setDisable(!isEnabled);
		}
		chart.setDisable(!isEnabled);
	}
}
