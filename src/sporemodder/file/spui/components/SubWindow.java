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
package sporemodder.file.spui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.paint.Color;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.file.spui.SpuiViewer.PaintEvent;
import sporemodder.file.spui.StyleSheetInstance;

public class SubWindow<T extends IWindow> implements IWindow {
	
	private final SPUIRectangle area = new SPUIRectangle();
	private int flags;
	private int state;
	private T parent;
	
	public SubWindow(T parent) {
		super();
		this.parent = parent;
	}
	
	public void layout(SPUIRectangle area, IWindow parent) {}
	public void paintComponent(PaintEvent event) {}

	@Override
	public boolean handleEvent(SpuiViewer viewer, Event event) {
		int oldState = state;
		state = SpuiViewer.getStateFromEvent(this, event);
		// Request a repaint if the state changed
		if (state != oldState) {
			viewer.repaint();
		}
		
		if (event.getEventType() == SpuiViewer.PAINT_EVENT) {
			if (parent != null) {
				paintComponent((PaintEvent) event);
			}
		}
		else if (event.getEventType() == SpuiViewer.LAYOUT_EVENT) {
			if (parent != null) {
				layout(area, parent);
				SPUIRectangle parentArea = parent.getRealArea();
				area.translate(parentArea.x1, parentArea.y1);
			}
		}
		
		return false;
	}

	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public void setFlag(int flag, boolean value) {
		flags &= ~flag;
		if (value) flags |= flag;
	}

	@Override
	public SPUIRectangle getArea() {
		return area;
	}

	@Override
	public SPUIRectangle getRealArea() {
		return area;
	}

	@Override
	public LocalizedText getCaption() {
		return null;
	}

	@Override
	public void setCaption(LocalizedText text) {
	}

	@Override
	public Color getFillColor() {
		return null;
	}

	@Override
	public void setFillColor(Color color) {
	}

	@Override
	public IDrawable getFillDrawable() {
		return null;
	}

	@Override
	public void setFillDrawable(IDrawable drawable) {
	}

	@Override
	public Color getShadeColor() {
		return null;
	}

	@Override
	public void setShadeColor(Color color) {
	}

	@Override
	public StyleSheetInstance getTextFont() {
		return null;
	}

	@Override
	public void setTextFont(StyleSheetInstance textFont) {
	}

	@Override
	public ObservableList<IWinProc> getWinProcs() {
		return FXCollections.emptyObservableList();
	}

	@Override
	public ObservableList<IWindow> getChildren() {
		return FXCollections.emptyObservableList();
	}

	@Override
	public T getParent() {
		return parent;
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public void setState(int state) {
		this.state = state;
	}

}
