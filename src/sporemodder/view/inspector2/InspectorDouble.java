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
package sporemodder.view.inspector2;

import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.argscript.ArgScriptLexer;

public class InspectorDouble extends Spinner<Double> implements InspectorValue<Double> {
	
	private double defaultValue = 0.0f;
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;
	private double step = 0.1f;
	
	private final SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory;
	
	public InspectorDouble() {
		this(0.0);
	}
	
	public InspectorDouble(double defaultValue) {
		super();
		
		getStyleClass().add(DEFAULT_STYLE_CLASS);
		setPrefWidth(Double.MAX_VALUE);
		
		this.defaultValue = defaultValue;
		
		valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
		setValueFactory(valueFactory);
		
		valueFactory.setAmountToStepBy(step);
		valueFactory.setConverter(new StringConverter<Double>() {
			
			private String lastString;
			private Double lastDouble;

			@Override
			public Double fromString(String arg0) {
				if (arg0.trim().isEmpty()) {
					return defaultValue;
				}
				
				ArgScriptLexer lexer = new ArgScriptLexer();
				lexer.setText(arg0);
				try {
					lastDouble = lexer.parseFloat();
					lastString = arg0;
					return lastDouble;
				} catch (DocumentException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String toString(Double value) {
				if (value == null) return "";
				if (value == lastDouble) return lastString;
				return HashManager.get().doubleToString(value);
			}
			
		});
		valueFactory.setValue(defaultValue);
		
		setEditable(true);
		
		// Ensure the value is saved when we exit focus
		focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				increment(0); // won't change value, but will commit editor
			}
		});
	}
	
	/**
	 * Sets the minimum and maximum values allowed for this inspector value. If this method is not used,
	 * there is no range by default.
	 * @param min
	 * @param max
	 * @return
	 */
	public InspectorDouble setRange(float min, float max) {
		this.maxValue = max;
		this.minValue = min;
		
		valueFactory.setMax(max);
		valueFactory.setMin(min);
		
		return this;
	}
	
	/**
	 * Sets the quantity that is increased/decreased every time the value spinner buttons are used.
	 * @param step
	 * @return
	 */
	public InspectorDouble setStep(float step) {
		this.step = step;
		valueFactory.setAmountToStepBy(step);
		return this;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getStep() {
		return step;
	}
	
	public double getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setValue(Double value) {
		valueFactory.setValue(value);
	}
	
	public void setValue(String value) {
		valueFactory.setValue(valueFactory.getConverter().fromString(value));
	}

	@Override
	public Node getNode() {
		return this;
	}

}
