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
package sporemodder.view.skin;


import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import sporemodder.view.StatusBar;
import sporemodder.view.StatusBar.Status;

public class StatusBarSkin extends SkinBase<StatusBar> {
	
	private static final PseudoClass PSEUDO_CLASS_ERROR = PseudoClass.getPseudoClass("error");
	
	private final HBox leftBox = new HBox(5);
	private final HBox rightBox = new HBox(5);

	public StatusBarSkin(StatusBar control) {
		super(control);
		
		BorderPane pane = new BorderPane();
		
		updateLeftBox();
		updateRightBox();
		
		pane.setLeft(leftBox);
		pane.setRight(rightBox);
		
		getChildren().add(pane);
		
		
		control.getLeftNodes().addListener((ListChangeListener<Node>)c -> {
			updateLeftBox();
		});
		control.getRightNodes().addListener((ListChangeListener<Node>)c -> {
			updateRightBox();
		});
	}

	public Separator createSeparator() {
		Separator separator = new Separator(Orientation.VERTICAL);
		separator.getStyleClass().add("status-separator");
		getSkinnable().statusProperty().addListener((obs, oldValue, newValue) -> {
			separator.pseudoClassStateChanged(PSEUDO_CLASS_ERROR, newValue == Status.ERROR);
		});
		return separator;
	}
	
	public void updateLeftBox() {
		leftBox.getChildren().clear();
		boolean first = true;
		for (Node node : getSkinnable().getLeftNodes()) {
			if (!first) {
				leftBox.getChildren().add(createSeparator());
			}
			first = false;
			
			leftBox.getChildren().add(node);
		}
	}
	
	public void updateRightBox() {
		rightBox.getChildren().clear();
		boolean first = true;
		for (Node node : getSkinnable().getRightNodes()) {
			if (!first) {
				rightBox.getChildren().add(createSeparator());
			}
			first = false;
			
			rightBox.getChildren().add(node);
		}
	}
}
