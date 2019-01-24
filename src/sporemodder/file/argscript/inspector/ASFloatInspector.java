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

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptLexer;

public class ASFloatInspector extends ASValueInspector {
	
	
	private float defaultValue = 0.0f;
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;
	private double step = 0.1f;
	
	private Spinner<Double> spinner;
	private StringConverter<Double> stringConverter;
	
	public ASFloatInspector(String name, String descriptionCode, float defaultValue) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
		initStringConverter();
	}
	
	public ASFloatInspector setRange(float min, float max) {
		this.maxValue = max;
		this.minValue = min;
		return this;
	}
	
	public ASFloatInspector setStep(float step) {
		this.step = step;
		return this;
	}
	
	private void initStringConverter() {
		stringConverter = new StringConverter<Double>() {

			@Override
			public Double fromString(String arg0) {
				if (arg0.trim().isEmpty()) {
					return (double) defaultValue;
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
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		// If true, the thing will be shown in red.
		boolean hasError = false;
		
		double initialValue = defaultValue;
		
		// Only do this if the option exists
		if (getArguments() != null) {
			Number value = null;
			if ((value = lineInspector.getStream().parseFloat(getArguments(), argIndex)) == null) {
				hasError = true;
			} else {
				initialValue = value.doubleValue();
			}
		}
		
		spinner = new Spinner<Double>();
		spinner.setPrefWidth(Double.MAX_VALUE);
		
		if (hasError) {
			spinner.getEditor().setStyle("-fx-background-fill: red");
		}
		
		SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
		spinner.setValueFactory(valueFactory);
		
		valueFactory.setAmountToStepBy(step);
		valueFactory.setConverter(stringConverter);
		valueFactory.setValue(initialValue);
		
		spinner.setEditable(true);
		spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				spinner.increment(0); // won't change value, but will commit editor
			}
		});
		spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			
			// If it's not an option, it will come after the keyword
			int splitIndex = argIndex + createIfNecessary();
			
			if (newValue == null) {
				newValue = (double) defaultValue;
			}
			lineInspector.getSplits().set(splitIndex, Float.toString(newValue.floatValue()));
			
			this.submitChanges();
		});
		
		panel.getChildren().add(spinner);
	}

	@Override
	void addDefaultValue(List<String> splits) {
		splits.add(stringConverter.toString((double) defaultValue));
	}
	
	@Override
	int getRemoveableCount() {
		//TOOD might change if it's optional
		return 1;
	}
	
	@Override
	boolean isDefault() {
		return getArgument(0) == null || stringConverter.fromString(getArgument(0)).equals((double) defaultValue);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		spinner.setDisable(!isEnabled);
	}
}
