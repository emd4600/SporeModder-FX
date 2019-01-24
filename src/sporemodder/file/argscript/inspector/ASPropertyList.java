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

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;
import sporemodder.file.DocumentStructure.DocumentFragment;
import sporemodder.util.inspector.InspectorElement;
import sporemodder.util.inspector.InspectorUnit;
import sporemodder.util.inspector.InspectorValue;

public class ASPropertyList extends InspectorElement {
	
	private InspectorUnit<?> inspector;
	private DocumentFragment parentFragment;
	private String title;
	private String descriptionCode;
	private String keyword;
	
	private final List<DocumentFragment> fragments = new ArrayList<DocumentFragment>();
	
	private VBox valuesPanel;
	
	public ASPropertyList(InspectorUnit<?> inspector, DocumentFragment parentFragment, String title, String descriptionCode, String keyword) {
		this.inspector = inspector;
		this.title = title;
		this.descriptionCode = descriptionCode;
		this.keyword = keyword;
		this.parentFragment = parentFragment;
	}
	
	private void updateValues() {
		fragments.clear();
		
		for (DocumentFragment f : parentFragment.children) {
			if (ArgScriptInspector.checkKeyword(f, keyword)) {
				fragments.add(f);
			}
		}
	}
	
	private void generateValuesUI() {
		
		valuesPanel.getChildren().clear();
		
		// Add a value button for every element
		for (DocumentFragment f : fragments) {
			
			Button button = new Button(f.description);
			button.setPrefWidth(Double.MAX_VALUE);
			button.setOnAction((event) -> {
				inspector.setSelectedFragment(f);
			});
			
			Button deleteButton = new Button("X");
			deleteButton.setOnAction((event) -> {
				ArgScriptInspector.removeLine(inspector, f);
			});
			
			BorderPane pane = new BorderPane();
			pane.setRight(deleteButton);
			pane.setCenter(button);
			
			valuesPanel.getChildren().add(pane);
		}
	}

	@Override
	public void generateUI(Pane pane) {
		VBox internalPane = new VBox(5);
		
		TitledPane titledPane = new TitledPane();
		titledPane.setContent(internalPane);
		if (title != null) {
			titledPane.setText(title);
		}
		
		if (descriptionCode != null) {
			new InspectorValue(null, DocumentationManager.get().getDocumentation(descriptionCode)).generateUI(internalPane);
		}
		
		inspector.add(titledPane);
		
		valuesPanel = new VBox(10);
		
		// Find all values
		updateValues();
		
		// Add a value button for every value
		generateValuesUI();
		
		internalPane.getChildren().add(valuesPanel);
		
		Button addButton = new Button("Add value");
		addButton.setPrefWidth(Double.MAX_VALUE);
		
		internalPane.getChildren().add(addButton);
		
		// Allow the user to add values in the middle
		valuesPanel.setCursor(Cursor.HAND);
		//TODO
	}

}
