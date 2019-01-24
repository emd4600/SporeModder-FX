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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import sporemodder.view.inspector2.skin.InspectorListSkin;

public class InspectorList extends Control {
	
	@FunctionalInterface
	public static interface ValueFactory {
		public InspectorValue<?> createValue(int index);
	}
	
	public static final String DEFAULT_STYLE_CLASS = "inspector-list";
	
	
	public InspectorList() {
		setPrefWidth(Double.MAX_VALUE);
	}
	
	
	private final ObservableList<InspectorValue<?>> values = FXCollections.observableArrayList();
	
	public final ObservableList<InspectorValue<?>> getValues() {
		return values;
	}
	
	
	private final ObjectProperty<ValueFactory> valueFactory = new SimpleObjectProperty<ValueFactory>(this, "valueFactory");
	
	public final ObjectProperty<ValueFactory> valueFactoryProperty() {
		return valueFactory;
	}
	
	public final ValueFactory getValueFactory() {
		return valueFactory.get();
	}
	
	public final void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory.set(valueFactory);
	}
	
	
//	public void add(InspectorValue<?> value) {
//		BorderPane pane = new BorderPane();
//		pane.setPrefWidth(Double.MAX_VALUE);
//		
//		Separator separator = new Separator(Orientation.VERTICAL);
//		separator.getStyleClass().add("inspector-list-item-handle");
//		separator.setOnDragDetected(event -> {
////			ClipboardContent content = new ClipboardContent();
////			content.putString("asd");
//			
//			System.out.println("ad");
//			
//			Dragboard dragboard = separator.startDragAndDrop(TransferMode.MOVE);
//			dragboard.setDragView(pane.snapshot(null, null));
//			
//			event.consume();
//		});
//		
//		separator.setOnDragOver(event -> {
//			if (event.getGestureSource() != pane) {
//				event.acceptTransferModes(TransferMode.MOVE);
//			}
//			event.consume();
//		});
//		value.getNode().setOnDragOver(event -> {
//			if (event.getGestureSource() != pane) {
//				event.acceptTransferModes(TransferMode.MOVE);
//			}
//			event.consume();
//		});
//		
//		separator.setOnDragEntered(event -> {
//			if (event.getGestureSource() != pane) {
//				pane.setOpacity(0.3);
//			}
//		});
//		value.getNode().setOnDragEntered(event -> {
//			if (event.getGestureSource() != pane) {
//				pane.setOpacity(0.3);
//			}
//		});
//		
//		pane.setOnDragExited(event -> {
//			if (event.getGestureSource() != this) {
//				pane.setOpacity(1);
//			}
//		});
//		
//		pane.setLeft(separator);
//		pane.setCenter(value.getNode());
//		
//		values.add(value);
//		panes.add(pane);
//		getChildren().add(pane);
//	}
	
	/** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new InspectorListSkin(this);
    }
}
