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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InspectorValue extends InspectorElement {
	
	private String title;
	private String description;
	
	public InspectorValue(String title, String description) {
		super();
		this.title = title;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void generateUI(Pane pane) {
		
//		if (title != null) {
//			Label titleLabel = new Label(title);
//			titleLabel.setPadding(new Insets(0, 5, -1, 5));
//			titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
//			pane.getChildren().add(titleLabel);
//		}
//		
//		if (description != null) {
//			Label titleLabel = new Label(description);
//			titleLabel.setPadding(new Insets(0, 5, -1, 5));
//			titleLabel.setWrapText(true);
//			titleLabel.setFont(Font.font("Helvetica", 15));
//			pane.getChildren().add(titleLabel);
//		}
		
		if (title != null) {
			Label titleLabel = new Label(title);
			titleLabel.setPadding(new Insets(5, 5, -1, 5));
			titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 16));
			pane.getChildren().add(titleLabel);
			
			if (description != null && !description.isEmpty()) {
				Tooltip tt = new Tooltip(description);
				titleLabel.setTooltip(tt);
			}
		}
	}
	
}
