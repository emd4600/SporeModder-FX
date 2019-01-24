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
package sporemodder.view.editors.spui;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.InspectableObject;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.file.spui.StyleSheetInstance;
import sporemodder.file.spui.components.IDrawable;
import sporemodder.file.spui.components.IWinProc;
import sporemodder.file.spui.components.IWindow;
import sporemodder.file.spui.components.WindowBase;
import sporemodder.view.editors.SpuiEditor;

public class SpuiLayoutWindow extends InspectableObject implements IWindow {
	
	private final ObservableList<IWindow> children = FXCollections.observableArrayList();
	private final SPUIRectangle area = new SPUIRectangle();
	
	public SpuiLayoutWindow() {
		super();
		children.addListener((ListChangeListener<? super IWindow>) c -> {
			while (c.next()) {
				for (IWindow window : c.getAddedSubList()) {
					((WindowBase)window).setParent(this);
				}
				for (IWindow window : c.getRemoved()) {
					((WindowBase)window).setParent(null);
				}
			}
		}); 
	}
	
	@Override public String toString() {
		return "Layout";
	}

	@Override
	public boolean handleEvent(SpuiViewer viewer, Event event) {
		
		// Paint events are propagated in reverse (so first node is painted last)
		if (event.getEventType() == SpuiViewer.PAINT_EVENT) {
			viewer.getGraphicsContext2D().clearRect(area.x1, area.y1, area.getWidth(), area.getHeight());
			
			ListIterator<IWindow> it = children.listIterator(children.size());
			while (it.hasPrevious()) {
				if (it.previous().handleEvent(viewer, event)) return true;
			}
		} else {
			for (IWindow child : children) {
				if (child.handleEvent(viewer, event)) return true;
			}
		}
		
		return false;
	}

	@Override public int getFlags() {
		return FLAG_VISIBLE | FLAG_ENABLED;
	}

	@Override public void setFlag(int flag, boolean value) {
	}

	@Override public SPUIRectangle getArea() {
		return area;
	}

	@Override public SPUIRectangle getRealArea() {
		return area;
	}

	@Override public LocalizedText getCaption() {
		return null;
	}

	@Override public void setCaption(LocalizedText text) {
	}

	@Override public Color getFillColor() {
		return null;
	}

	@Override public void setFillColor(Color color) {
	}

	@Override public IDrawable getFillDrawable() {
		return null;
	}

	@Override public void setFillDrawable(IDrawable drawable) {
	}

	@Override public Color getShadeColor() {
		return null;
	}

	@Override public void setShadeColor(Color color) {
	}

	@Override public StyleSheetInstance getTextFont() {
		return null;
	}

	@Override public void setTextFont(StyleSheetInstance textFont) {
	}

	@Override public List<IWinProc> getWinProcs() {
		return Collections.emptyList();
	}

	@Override public List<IWindow> getChildren() {
		return children;
	}

	@Override public IWindow getParent() {
		return null;
	}

	@Override public int getState() {
		return 0;
	}

	@Override public Node generateUI(SpuiEditor editor) {
		return new Pane();
	}

	@Override public void addComponents(SpuiWriter writer) {
	}

	@Override public void setState(int state) {
	}

}
