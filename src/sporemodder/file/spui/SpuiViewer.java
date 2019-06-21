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
package sporemodder.file.spui;

import java.util.ListIterator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import sporemodder.file.spui.components.IWindow;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.editors.spui.SpuiLayoutWindow;

/**
 * A canvas capable of displaying an {@link SporeUserInterface} object.
 */
public class SpuiViewer extends Canvas {
	
	public static class PaintEvent extends Event {
		private final SpuiViewer viewer;
		
		public PaintEvent(EventType<? extends Event> eventType, SpuiViewer viewer) {
			super(eventType);
			this.viewer = viewer;
		}
		
		public final SpuiViewer getViewer() {
			return viewer;
		}
	}

	public static final EventType<Event> LAYOUT_EVENT = new EventType<Event>("LAYOUT_EVENT");
	public static final EventType<PaintEvent> PAINT_EVENT = new EventType<PaintEvent>("PAINT_EVENT");
	
	private final SpuiEditor editor;
	
	private final BooleanProperty showInvisible = new SimpleBooleanProperty();
	private final BooleanProperty isPreview = new SimpleBooleanProperty();
	
	// Don't repaint if it's still handling an event
	private boolean isHandlingEvent;
	private boolean repaintRequested;
	
	private final SpuiLayoutWindow layoutWindow;
	// In preview we have to resize the root node, we should restore it when closing
	private final SPUIRectangle originalRootArea = new SPUIRectangle();
	
private DoubleProperty contentTranslateX;
    
    public final DoubleProperty contentTranslateXProperty() {
    	if (contentTranslateX == null) {
    		contentTranslateX = new SimpleDoubleProperty(this, "contentTranslateX", 0);
    	}
    	return contentTranslateX;
    }
    public final double getContentTranslateX() {
    	return contentTranslateX.get();
    }
    
    public final void setContentTranslateX(double value) {
    	contentTranslateX.set(value);
    }
    
	private DoubleProperty contentTranslateY;
    
    public final DoubleProperty contentTranslateYProperty() {
    	if (contentTranslateY == null) {
    		contentTranslateY = new SimpleDoubleProperty(this, "contentTranslateY", 0);
    	}
    	return contentTranslateY;
    }
    public final double getContentTranslateY() {
    	return contentTranslateY.get();
    }
    
    public final void setContentTranslateY(double value) {
    	contentTranslateY.set(value);
    }
	
	public SpuiViewer(SpuiEditor editor) {
		this(editor, new SpuiLayoutWindow());
	}
	
	public SpuiViewer(SpuiEditor editor, SpuiLayoutWindow layoutWindow) {
		super();
		this.editor = editor;
		this.layoutWindow = layoutWindow;
		
		widthProperty().addListener((obs, oldValue, newValue) -> {
			resizeForPreview();
			repaint();
		});
		heightProperty().addListener((obs, oldValue, newValue) -> {
			resizeForPreview();
			repaint();
		});
		

		contentTranslateXProperty().addListener((obs, oldValue, newValue) -> {
			resizeForPreview();
			repaint();
		});
		contentTranslateYProperty().addListener((obs, oldValue, newValue) -> {
			resizeForPreview();
			repaint();
		});
		
		showInvisible.addListener((obs, oldValue, newValue) -> repaint());
		isPreview.addListener((obs, oldValue, newValue) -> repaint());
		
		addEventFilter(MouseEvent.ANY, event -> {
			if (isPreview()) handleEvent(event);
		});
		
		if (layoutWindow.getChildren().size() == 1) {
			originalRootArea.copy(layoutWindow.getChildren().get(0).getArea());
		}
	}
	
	private void restoreOriginalFlags(IWindow window) {
		window.setState(0);
		for (IWindow child : window.getChildren()) restoreOriginalFlags(child);
	}
	
	public void restoreOriginal() {
		if (layoutWindow.getChildren().size() == 1) {
			layoutWindow.getChildren().get(0).getArea().copy(originalRootArea);
		}
		restoreOriginalFlags(layoutWindow);
	}
	
	public SpuiLayoutWindow getLayoutWindow() {
		return layoutWindow;
	}
	
	public boolean mustShowWindow(IWindow window) {
		if (showInvisible.get()) return true;
		
		boolean mustShow = (window.getFlags() & IWindow.FLAG_VISIBLE) != 0;
		
		if (!mustShow && editor != null) {
			IWindow selected = editor.getSelectedWindow();
			while (selected != null) {
				// We want to show the selected window and its parents
				if (window == selected) return true;
				selected = selected.getParent();
			}
		}
		return mustShow;
	}
	
	public SpuiEditor getEditor() {
		return editor;
	}
	
	public BooleanProperty showInvisibleProperty() {
		return showInvisible;
	}
	
	public void setShowInvisible(boolean value) {
		showInvisible.set(value);
	}
	
	public boolean getShowInvisible() {
		return showInvisible.get();
	}
	
	public BooleanProperty isPreviewProperty() {
		return isPreview;
	}
	
	public void setIsPreview(boolean value) {
		isPreview.set(value);
	}
	
	public boolean isPreview() {
		return isPreview.get();
	}
	
	private void repaint_internal() {
		getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
		
		// First layout the elements, then paint them
		
		relayout();
		
		PaintEvent paintEvent = new PaintEvent(PAINT_EVENT, this);
		handleEvent(paintEvent, false);
		
		repaintRequested = false;
	}
	
	public void repaint() {
		if (isHandlingEvent) {
			repaintRequested = true;
		} else {
			repaint_internal();
		}
	}
	
	public void handleEvent(Event event) {
		handleEvent(event, true);
	}
	
	public void handleEvent(Event event, boolean acceptRepaint) {
		isHandlingEvent = true;
		
		layoutWindow.handleEvent(this, event);
		
		isHandlingEvent = false;
		
		if (acceptRepaint && repaintRequested) {
			repaint_internal();
		}
	}
	
	public IWindow getWindowInCoords(IWindow window, double x, double y) {
		return getWindowInCoords(window, x, y, null);
	}
	
	public IWindow getWindowInCoords(IWindow window, double x, double y, IWindow excludedWindow) {
		
		// we don't want to select invisible components,
		// but we do want to be able to select the root component
		if (!mustShowWindow(window)) {
			return null;
		}
		
		IWindow result = null;
		SPUIRectangle resultBounds = null;
		
		ListIterator<IWindow> iterator = window.getChildren().listIterator(window.getChildren().size());
		
		while (iterator.hasPrevious()) {
			IWindow child = iterator.previous();
			IWindow comp = getWindowInCoords(child, x, y);
			if (comp == null) {
				continue;
			}
			
			SPUIRectangle compBounds = comp.getRealArea();
			if (result == null || (
					compBounds.getWidth() <= resultBounds.getWidth() &&
							compBounds.getHeight() <= resultBounds.getHeight())) {
				result = comp;
				resultBounds = compBounds;
			}
		}
		
		if (result != null) {
			return result;
		}
		
		if (window != excludedWindow && window.getRealArea().contains(x, y)) {
			return window;
		} else {
			return null;
		}
	}
	
	public IWindow getWindowInCoords(double x, double y) {
		return getWindowInCoords(x, y, null);
	}
	
	public IWindow getWindowInCoords(double x, double y, IWindow excludedWindow) {
		for (IWindow window : layoutWindow.getChildren()) {
			IWindow result = getWindowInCoords(window, x, y, excludedWindow);
			if (result != null) return result;
		}
		return null;
	}
	
	public static int getStateFromEvent(IWindow window, Event event) {
		int state = window.getState();
		SPUIRectangle area = window.getRealArea();
		
		if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (area.contains(mouseEvent.getX(), mouseEvent.getY())) state |= IWindow.STATE_FLAG_HOVER;
			else state &= ~IWindow.STATE_FLAG_HOVER;
		}
		else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (area.contains(mouseEvent.getX(), mouseEvent.getY())) state |= IWindow.STATE_FLAG_CLICKED;
			else state &= ~IWindow.STATE_FLAG_CLICKED;
		}
		else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
			MouseEvent mouseEvent = (MouseEvent) event;
			state &= ~IWindow.STATE_FLAG_CLICKED;
			
			if (area.contains(mouseEvent.getX(), mouseEvent.getY())) {
				boolean wasSelected = (state & IWindow.STATE_FLAG_SELECTED) != 0;
				if (wasSelected) state &= ~IWindow.STATE_FLAG_SELECTED;
				else state |= IWindow.STATE_FLAG_SELECTED;
			}
		}
		
		return state;
	}
	
	public SPUIRectangle getMinimumArea() {
		SPUIRectangle rect = new SPUIRectangle();
		
		relayout();
		
		for (IWindow window : layoutWindow.getChildren()) {
			SPUIRectangle area = window.getRealArea();
			if (rect.x2 < area.x2) rect.x2 = area.x2;
			if (rect.y2 < area.y2) rect.y2 = area.y2;
		}
		
		return rect;
	}
	
	private void relayout() {
		handleEvent(new Event(LAYOUT_EVENT), false);
	}
	
	private void resizeForPreview() {
		if (isPreview()) {
			float width = (float)getWidth();
			float height = (float)getHeight();
			layoutWindow.getArea().x1 = 0;
			layoutWindow.getArea().y1 = 0;
			layoutWindow.getArea().setWidth(width);
			layoutWindow.getArea().setHeight(height);
			
			// if we don't do this, things like the creature editor won't get scaled
			if (layoutWindow.getChildren().size() == 1) {
				SPUIRectangle area = layoutWindow.getChildren().get(0).getArea();
				area.setWidth(width);
				area.setHeight(height);
			}
			
			relayout();
		}
		else {
			layoutWindow.getArea().x1 = (float)getContentTranslateX();
			layoutWindow.getArea().y1 = (float)getContentTranslateY();
		}
	}
}
