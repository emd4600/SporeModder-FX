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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sporemodder.file.LocalizedText;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.file.spui.SpuiViewer.PaintEvent;
import sporemodder.file.spui.StyleSheetInstance;

// We don't make it abstract so we can create it with reflection, as many windows components don't have defined classes
public class WindowBase extends SpuiElement implements IWindow {
	
	protected IWindow parent;
	
	/** The combination of flags that define the behavior of this window. */
	protected int windowFlags = FLAG_VISIBLE;
	protected int stateFlags;

	/** The unique ID of this object. */
	protected String controlID;
	/** The message ID sent by this window. */
	protected String commandID;
	
	/** The rectangular extent of the window. */
	protected final SPUIRectangle area = new SPUIRectangle(10, 10, 50, 50);
	protected final SPUIRectangle realArea = new SPUIRectangle(10, 10, 50, 50);
	
	/** The text contents or title of the window. */
	protected final LocalizedText caption = new LocalizedText();
	/** Window background fill color. */
	protected Color fillColor = Color.rgb(0x7F, 0x7F, 0x7F);
	protected IDrawable fillDrawable;
	/** Color modulation value. */
	protected Color shadeColor = Color.WHITE;
	/** Default Text Style for this window. */
	protected StyleSheetInstance textFont;
	
	protected final List<IWinProc> winProcs = new ArrayList<>();
	/** All the children windows. */
	protected final ObservableList<IWindow> children = FXCollections.observableArrayList();
	
	public WindowBase() {
		super();
		
		children.addListener((ListChangeListener<? super IWindow>) c -> {
			while (c.next()) {
				for (IWindow window : c.getAddedSubList()) {
					((WindowBase)window).parent = this;
				}
				for (IWindow window : c.getRemoved()) {
					((WindowBase)window).parent = null;
				}
			}
		}); 
	}
	
	@Override public String toString() {
		String str = getDesignerClass().getName();
		if (controlID != null && !controlID.isEmpty()) {
			str = str + ": " + controlID; 
		}
		if (getEditorTag() != null && !getEditorTag().isEmpty()) {
			str += " [" + getEditorTag() + ']';
		}
		if (caption.getText() != null) {
			str += " - \"" + caption.getText() + "\"";
		} 
		else if (caption.getTableID() != 0) {
			str += " - \"" + new ResourceID(caption.getTableID(), caption.getInstanceID()).toString() + "\"";
		}
		return str;
	}

	@Override
	public int getFlags() {
		return windowFlags;
	}

	@Override
	public void setFlag(int flag, boolean value) {
		if (value) windowFlags |= flag;
		else windowFlags &= ~flag;
	}

	@Override
	public SPUIRectangle getArea() {
		return area;
	}

	@Override
	public SPUIRectangle getRealArea() {
		return realArea;
	}

	@Override
	public LocalizedText getCaption() {
		return caption;
	}

	@Override
	public void setCaption(LocalizedText text) {
		this.caption.copy(text);
	}

	@Override
	public Color getFillColor() {
		return fillColor;
	}

	@Override
	public void setFillColor(Color color) {
		this.fillColor = color;
	}

	@Override
	public IDrawable getFillDrawable() {
		return fillDrawable;
	}

	@Override
	public void setFillDrawable(IDrawable drawable) {
		this.fillDrawable = drawable;
	}

	@Override
	public Color getShadeColor() {
		return shadeColor;
	}

	@Override
	public void setShadeColor(Color color) {
		this.shadeColor = color;
	}

	@Override
	public StyleSheetInstance getTextFont() {
		return textFont;
	}

	@Override
	public void setTextFont(StyleSheetInstance textFont) {
		this.textFont = textFont;
	}

	@Override public List<IWinProc> getWinProcs() {
		return winProcs;
	}

	@Override public ObservableList<IWindow> getChildren() {
		return children;
	}

	@Override
	public IWindow getParent() {
		return parent;
	}

	@Override
	public boolean handleEvent(SpuiViewer viewer, Event event) {
		int oldState = stateFlags;
		stateFlags = SpuiViewer.getStateFromEvent(this, event);
		// Request a repaint if the state changed
		if (stateFlags != oldState) {
			viewer.repaint();
		}
		
		if (event.getEventType() == SpuiViewer.PAINT_EVENT) {
			// We won't show the children neither, so don't propagate the event to them
			if (!viewer.mustShowWindow(this)) return false;
			
			paintComponent((PaintEvent) event);
		}
		else if (event.getEventType() == SpuiViewer.LAYOUT_EVENT) {
			// We won't show the children neither, so don't propagate the event to them
			if (!viewer.mustShowWindow(this)) return false;
						
			realArea.copy(area);
			if (parent != null) {
				SPUIRectangle parentArea = parent.getRealArea();
				realArea.translate(parentArea.x1, parentArea.y1);
			}
		}
		
		for (IWinProc proc : winProcs) {
			if (proc.handleEvent(viewer, this, event)) return true;
		}

		// Paint events are propagated in reverse (so first node is painted last)
		if (event.getEventType() == SpuiViewer.PAINT_EVENT) {
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
	
	protected boolean shouldUseFillColor() {
		// Only generic windows use fill color
		// Subwindows don't have designer class
		if (getDesignerClass() != null && getDesignerClass().getProxyID() == 0x4ec1b8d8) {
			return true;
		} else {
			return false;
		}
	}
	
	protected void paintComponent(PaintEvent event) {
		if (shouldUseFillColor() && fillColor.getOpacity() != 0) {
			GraphicsContext g = event.getViewer().getGraphicsContext2D();
			g.setFill(new Color(
					fillColor.getRed() * shadeColor.getRed(),
					fillColor.getGreen() * shadeColor.getGreen(),
					fillColor.getBlue() * shadeColor.getBlue(),
					fillColor.getOpacity() * shadeColor.getOpacity()));
			g.fillRect(realArea.x1, realArea.y1, realArea.getWidth(), realArea.getHeight());
		}
		
		if (fillDrawable != null) {
			fillDrawable.paint(this, event.getViewer());
		}
	}

	@Override
	public int getState() {
		return stateFlags;
	}
	
	@Override public boolean isRoot() {
		return parent == null || parent.getParent() == null;
	}

	public void setParent(IWindow parent) {
		this.parent = parent;
	}

	@Override
	public void setState(int state) {
		this.stateFlags = state;
	}

	public String getControlID() {
		return controlID;
	}

	public String getCommandID() {
		return commandID;
	}

	public void setControlID(String controlID) {
		this.controlID = controlID;
	}

	public void setCommandID(String commandID) {
		this.commandID = commandID;
	}
}
