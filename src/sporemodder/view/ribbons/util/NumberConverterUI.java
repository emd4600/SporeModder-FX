/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

package sporemodder.view.ribbons.util;

import emord.javafx.ribbon.RibbonMenuButton;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import sporemodder.UIManager;
import sporemodder.view.Controller;

public class NumberConverterUI implements Controller {
	
	@FXML
	private Node mainNode;
	
	@FXML
	private RibbonMenuButton menuButton;
	
	@FXML
	private ToggleGroup toggleGroup;
	
	@FXML
	private TextField tfHexadecimal;
	@FXML
	private TextField tfDecimal;
	
	/** When we change the text of a field, it will raise an event that would change the other field, and so on.
	 * This field is used to avoid that circular calling. */
	private boolean isInEvent;

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	@FXML
	protected void initialize() {
		toggleGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
			if (!tfHexadecimal.getText().trim().isEmpty()) {
				// Ensure that we don't trigger another event when changing the text.
				isInEvent = true;
				
				tfDecimal.setText(convertToDecimal(tfHexadecimal.getText(), ((RadioMenuItem) newValue).getText()));
				
				// Restore
				isInEvent = false;
			}
		});
		
		tfHexadecimal.textProperty().addListener((obs, oldValue, newValue) -> {
			
			if (!isInEvent) {
				// Ensure that we don't trigger another event when changing the text.
				isInEvent = true;
				
				try {
					if (newValue.isEmpty()) {
						tfDecimal.setText("");
					}
					else {
						tfDecimal.setText(convertToDecimal(newValue, ((RadioMenuItem) toggleGroup.getSelectedToggle()).getText()));
					}
					
					// Restore the white background in case there had been an error before.
					tfHexadecimal.setStyle("-fx-control-inner-background: white");
				}
				catch (Exception e) {
					// If the format is incorrect, make the background red so the user notices.
					tfHexadecimal.setStyle("-fx-control-inner-background: red");
				}
				
				// Once we are finished, allow  firing events again
				isInEvent = false;
			}
		});
		
		
		tfDecimal.textProperty().addListener((obs, oldValue, newValue) -> {
			
			if (!isInEvent) {
				// Ensure that we don't trigger another event when changing the text.
				isInEvent = true;
				
				try {
					
					if (newValue.isEmpty()) {
						tfHexadecimal.setText("");
					}
					else {
						tfHexadecimal.setText(convertToHex(newValue, ((RadioMenuItem) toggleGroup.getSelectedToggle()).getText()));
					}
					
					// Restore the white background in case there had been an error before.
					tfDecimal.setStyle("-fx-control-inner-background: white");
				}
				catch (Exception e) {
					// If the format is incorrect, make the background red so the user notices.
					tfDecimal.setStyle("-fx-control-inner-background: red");
				}
				
				// Once we are finished, allow  firing events again
				isInEvent = false;
			}
		});
		
		menuButton.setGraphic(UIManager.get().loadIcon("number-converter-type.png", 38, 38, true));
	}
	
	private static String convertToHex(String input, String mode) {
		if (mode.equals("int8")) {
			return Integer.toHexString(((int)Byte.parseByte(input)) & 0xFF);
		}
		else if (mode.equals("uint8")) {
			return Integer.toHexString(Byte.toUnsignedInt(Byte.parseByte(input)));
		}
		else if (mode.equals("int16")) {
			return Integer.toHexString(((int)Short.parseShort(input)) & 0xFFFF);
		}
		else if (mode.equals("uint16")) {
			return Integer.toHexString(Short.toUnsignedInt(Short.parseShort(input)));
		}
		else if (mode.equals("int32")) {
			return Integer.toHexString(Integer.parseInt(input));
		}
		else if (mode.equals("uint32")) {
			return Long.toHexString(Long.parseLong(input));
		}
		else if (mode.equals("int64")) {
			return Long.toHexString(Long.parseLong(input));
		}
		else if (mode.equals("float")) {
			return Integer.toHexString(Float.floatToRawIntBits(Float.parseFloat(input)));
		}
		else if (mode.equals("double")) {
			return Long.toHexString(Double.doubleToRawLongBits(Double.parseDouble(input)));
		}
		
		return null;
	}
	
	private static String convertToDecimal(String input, String mode) {
		if (mode.equals("int8")) {
			return Byte.toString((byte) (Short.parseShort(input, 16) & 0xFF));
		}
		else if (mode.equals("uint8")) {
			short num = Short.parseShort(input, 16);
			if (num < 0 || num > 255) return null;
			return Short.toString(num);
		}
		else if (mode.equals("int16")) {
			short num = (short) (Integer.parseInt(input.substring(2), 16) & 0xFFFF);;
			return Short.toString(num);
		}
		else if (mode.equals("uint16")) {
			int num = Integer.parseInt(input, 16);
			if (num < 0 || num > 65535) return null;
			return Integer.toString(num);
		}
		else if (mode.equals("int32")) {
			return Integer.toString(Integer.parseUnsignedInt(input, 16));
		}
		else if (mode.equals("uint32")) {
			long num = Long.parseLong(input, 16);
			if (num < 0 || num > 4294967295L) return null;
			return Long.toString(num);
		}
		else if (mode.equals("int64")) {
			return Long.toString(Long.parseLong(input, 16));
		}
		else if (mode.equals("float")) {
			return Float.toString(Float.intBitsToFloat(Integer.parseUnsignedInt(input, 16)));
		}
		else if (mode.equals("double")) {
			return Double.toString(Double.longBitsToDouble(Long.parseUnsignedLong(input, 16)));
		}
		
		return null;
	}

}
