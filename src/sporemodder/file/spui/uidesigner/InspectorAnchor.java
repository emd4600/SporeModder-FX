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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sporemodder.UIManager;
import sporemodder.file.spui.components.SimpleLayout;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class InspectorAnchor implements InspectorValue<Integer> {
	
	private final ComboBox<String> cbHorizontal = new ComboBox<String>();
	private final ComboBox<String> cbVertical = new ComboBox<String>();

	private final PropertyPane pane = new PropertyPane();
	
	private int oldValue;
	private int value;
	
	private final List<ChangeListener<Integer>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorAnchor(int anchor) {
		this.value = anchor;
		oldValue = value;
		
		cbHorizontal.setButtonCell(new AnchorHorizontalCell());
		cbHorizontal.setCellFactory(c -> new AnchorHorizontalCell());
		cbHorizontal.getItems().addAll("Left", "Right", "Fill");
		
		cbVertical.setButtonCell(new AnchorVerticalCell());
		cbVertical.setCellFactory(c -> new AnchorVerticalCell());
		cbVertical.getItems().addAll("Top", "Bottom", "Fill");
		
		loadValue();
		
		pane.add("Horizontal", null, cbHorizontal);
		pane.add("Vertical", null, cbVertical);
		
		cbHorizontal.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
			oldValue = value;
			
			value &= ~(SimpleLayout.FLAG_LEFT | SimpleLayout.FLAG_RIGHT);
			
			if (newValue.equals("Right")) value |= SimpleLayout.FLAG_RIGHT;
			else if (newValue.equals("Fill")) value |= SimpleLayout.FLAG_RIGHT | SimpleLayout.FLAG_LEFT;
			
			if (!settingValue) updateListeners();
		});
		
		cbVertical.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
			oldValue = value;
			
			value &= ~(SimpleLayout.FLAG_TOP | SimpleLayout.FLAG_BOTTOM);
			
			if (newValue.equals("Bottom")) value |= SimpleLayout.FLAG_BOTTOM;
			else if (newValue.equals("Fill")) value |= SimpleLayout.FLAG_BOTTOM | SimpleLayout.FLAG_TOP;
			
			if (!settingValue) updateListeners();
		});
	}
	
	private void updateListeners() {
		for (ChangeListener<Integer> listener : listeners) {
			listener.changed(null, oldValue, value);
		}
	}
	
	public void addValueListener(ChangeListener<Integer> listener) {
		listeners.add(listener);
	}
	
	public void removeValueListener(ChangeListener<Integer> listener) {
		listeners.remove(listener);
	}
	
	private void loadValue() {
		String selectH;
		String selectV;
		
		if ((value & SimpleLayout.FLAG_RIGHT) != 0) {
			if ((value & SimpleLayout.FLAG_LEFT) != 0) selectH = "Fill";
			else selectH = "Right";
		} else selectH = "Left";
		
		if ((value & SimpleLayout.FLAG_BOTTOM) != 0) {
			if ((value & SimpleLayout.FLAG_TOP) != 0) selectV = "Fill";
			else selectV = "Bottom";
		} else selectV = "Top";
		
		cbHorizontal.getSelectionModel().select(selectH);
		cbVertical.getSelectionModel().select(selectV);
	}
	
	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public void setValue(Integer value) {
		oldValue = this.value;
		this.value = value;
		
		settingValue = true;
		loadValue();
		settingValue = false;
	}

	@Override
	public Node getNode() {
		return pane.getNode();
	}
	
	private static class AnchorHorizontalCell extends ListCell<String> {
		private static final Image imageLeft = UIManager.get().loadImage("anchor-left.png");
		private static final Image imageRight = UIManager.get().loadImage("anchor-right.png");
		private static final Image imageFill = UIManager.get().loadImage("anchor-fill-h.png");
		
		protected void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty);
	        setGraphic(null);
	        setText(null);
	        if (item != null) {
	        	Image image = null;
	        	if (item.equals("Left")) image = imageLeft;
	        	else if (item.equals("Right")) image = imageRight;
	        	else if (item.equals("Fill")) image = imageFill;
	        	
	            ImageView imageView = new ImageView(image);
	            setGraphic(imageView);
	            setText(item);
	        }
	    }
	}
	private static class AnchorVerticalCell extends ListCell<String> {
		private static final Image imageBottom = UIManager.get().loadImage("anchor-bottom.png");
		private static final Image imageTop = UIManager.get().loadImage("anchor-top.png");
		private static final Image imageFill = UIManager.get().loadImage("anchor-fill-v.png");
		
		protected void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty);
	        setGraphic(null);
	        setText(null);
	        if (item != null) {
	        	Image image = null;
	        	if (item.equals("Bottom")) image = imageBottom;
	        	else if (item.equals("Top")) image = imageTop;
	        	else if (item.equals("Fill")) image = imageFill;
	        	
	            ImageView imageView = new ImageView(image);
	            setGraphic(imageView);
	            setText(item);
	        }
	    }
	}
}
