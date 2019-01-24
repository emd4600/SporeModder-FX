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
package sporemodder.util.inspector;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * An inspector panel that displays JavaFX nodes or inspector elements in a vertical layout. Optionally, it can include a title and a button to return
 * to the parent DocumentFragment. If no fragment is specified or the fragment does not have a parent, the button won't be displayed.
 * @author Eric
 *
 */
public class InspectorPanel extends InspectorElement {
	
	private String title;
	private DocumentFragment fragment;
	private InspectorUnit<?> inspector;
	
	public InspectorPanel(InspectorUnit<?> inspector) {
		this.inspector = inspector;
	}
	
	public InspectorPanel(InspectorUnit<?> inspector, String title) {
		this.inspector = inspector;
		this.title = title;
	}
	
	public InspectorPanel(InspectorUnit<?> inspector, String title, DocumentFragment fragment) {
		this.inspector = inspector;
		this.title = title;
		this.fragment = fragment;
	}
	@Override
	public void generateUI(Pane pane) {
		
		if (title != null || fragment != null) {
			BorderPane topPane = new BorderPane();
			pane.getChildren().add(topPane);
			VBox.setMargin(topPane, new Insets(5, 0, 5, 0));
			
			if (fragment != null && fragment.parent != null) {
				Button backButton = new Button("<");
				backButton.setOnAction((event) -> {
					inspector.setSelectedFragment(fragment.parent);
				});
				topPane.setLeft(backButton);
			}
			
			if (title != null) {
				Label titleLabel = new Label(title);
				titleLabel.setPadding(new Insets(0, 5, 0, 5));
				titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
				topPane.setCenter(titleLabel);
			}
		}
	}
}
