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

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class WinSlider extends WindowBase {

	public static final int IMAGE_UNKNOWN = 0;
	public static final int IMAGE_THUMB = 1;
	public static final int IMAGE_BACKGROUND = 2;
	
	public int orientation = SporeUserInterface.HORIZONTAL;
	public int value;
	public int minValue;
	public int maxValue = 1000;

	private float mouseX = 0;
	private float mouseY = 0;
	boolean mouseCoordsSet = false;
	
	private final SubWindow<WinSlider> thumb = new SubWindow<WinSlider>(this) {
		//private float translation;
		
		/*@Override public boolean handleEvent(SpuiViewer viewer, Event event) {
			boolean result = super.handleEvent(viewer, event);
			if (!result) {
				if ((getState() & STATE_FLAG_CLICKED) != 0 && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					translate(viewer, (MouseEvent) event);
					result = true;
				}
			}
			return result;
		}
		
		private float getMaxX() {
			return WinSlider.this.getRealArea().getWidth() - getRealArea().getWidth();
		}
		
		private float getMaxY() {
			return WinSlider.this.getRealArea().getHeight() - getRealArea().getHeight();
		}
		
		public void translate(SpuiViewer viewer, MouseEvent event) {
			float dx = (float) (event.getX() - mouseX);
			float dy = (float) (event.getY() - mouseY);
			mouseX += dx;
			mouseY += dy;
			
			if (orientation == SporeUserInterface.HORIZONTAL) {
				if (dx < 0 && translation < getMaxX()) {
					horizontalTranslate(viewer, dx);
				}
				else if (dx > 0 && translation > 0) {
					horizontalTranslate(viewer, dx);
				}
			}
			else {
				if (dy < 0 && translation < getMaxY()) {
					verticalTranslate(viewer, dy);
				}
				else if (dy > 0 && translation > 0) {
					verticalTranslate(viewer, dy);
				}
			}
		}
		
		private void verticalTranslate(SpuiViewer viewer, float dy) {
			translation += dy;
			if (translation < 0) {
				translation = 0;
				value = maxValue;
			}
			else if (translation > getMaxY()) {
				translation = getMaxY();
				value = minValue;
			}
			else {
				setValue(Math.round((1 - translation / getMaxY()) * (maxValue - minValue) + minValue));
			}
			
			viewer.repaint();
		}
		
		private void horizontalTranslate(SpuiViewer viewer, float dx) {
			translation += dx;
			if (translation < 0) {
				translation = 0;
				value = minValue;
			}
			else if (translation > getMaxX()) {
				translation = getMaxX();
				value = maxValue;
			}
			else {
				setValue(Math.round(translation / getMaxX() * (maxValue - minValue) + minValue));
			}
			
			viewer.repaint();
		}

		public void setValue(int value) {
			WinSlider.this.value = value;
			
			float thumbWidth = getRealArea().getWidth();
			float thumbHeight = getRealArea().getHeight();
			
			float pos = (value - minValue) / (float) (maxValue - minValue);
			
			if (orientation == SporeUserInterface.HORIZONTAL) {
				translation = pos * (getParent().getRealArea().getWidth() - thumbWidth);
			}
			else {
				translation = (1 - pos) * (getParent().getRealArea().getHeight() - thumbHeight);
			}
			
		}
		
		@Override public void layout(SPUIRectangle area, IWindow parent) {
			if (parent.getFillDrawable() != null) {
				Vector2 dimensions = parent.getFillDrawable().getDimensions(IMAGE_THUMB);
				area.setWidth(dimensions.getX());
				area.setHeight(dimensions.getY());
			} else {
				if (orientation == SporeUserInterface.HORIZONTAL) {
					area.setWidth(24);
					area.setHeight(parent.getRealArea().getHeight());
				} else {
					area.setWidth(parent.getRealArea().getWidth());
					area.setHeight(24);
				}
			}
			
			if (orientation == SporeUserInterface.HORIZONTAL) {
				area.translateX(translation);
			} else {
				area.translateY(translation);
			}
		}
		@Override public int getFlags() {
			return WinSlider.this.getFlags();
		}*/
		
		/*@Override
		public boolean handleEvent(SpuiViewer viewer, Event event) {
			if ((event.getEventType() == MouseEvent.MOUSE_DRAGGED) && ((getState() & STATE_FLAG_CLICKED) != 0)) {
					MouseEvent ev = (MouseEvent)event;
					float dx = (float) (ev.getX() - mouseX);
					float dy = (float) (ev.getY() - mouseY);
					setOffsetX(dx);
					setOffsetY(dy);
					mouseX += dx;
					mouseY += dy;
					//return true;
				}
			return super.handleEvent(viewer, event);*/
				/*else if (event.getEventType() == SpuiViewer.LAYOUT_EVENT) {
					if (viewer.mustShowWindow(WinSlider.this)) {
						realArea.copy(area);
						SPUIRectangle parentArea = WinSlider.this.getRealArea();
						realArea.translate(parentArea.x1, parentArea.y1);
					}
					/*realArea.translateX(getOffsetX());
					realArea.translateY(getOffsetY());
					if (orientation == SporeUserInterface.HORIZONTAL)
						realArea.translateX(mouseX);
					else
						realArea.translateY(mouseY);*
					//return true;
				}*/
			/*else
				return super.handleEvent(viewer, event);*/
		//}
		
		/*@Override
		public void setState(int state) {
			//if ((state == (getState() & IWindow.STATE_FLAG_SELECTED)) && (state == (getState() & ~IWindow.STATE_FLAG_SELECTED)))
			super.setState(state & ~IWindow.STATE_FLAG_SELECTED);
		}*/
		
		@Override
		public boolean handleEvent(SpuiViewer viewer, Event event) {
			boolean retVal = super.handleEvent(viewer, event);
			if (event.getEventType() == SpuiViewer.LAYOUT_EVENT) {
				//if (!(WinSlider.this.getFillDrawable() instanceof SliderDrawable)) {
				if (orientation == SporeUserInterface.HORIZONTAL) {
					getRealArea().setWidth(WinSlider.this.getRealArea().getHeight() / 3);
					//getRealArea().setHeight(WinSlider.this.getRealArea().getHeight());
					getRealArea().y1 = WinSlider.this.getRealArea().y1;
					getRealArea().y2 = WinSlider.this.getRealArea().y2;
				}
				else {
					//getRealArea().setWidth(WinSlider.this.getRealArea().getWidth());
					getRealArea().setHeight(WinSlider.this.getRealArea().getWidth() / 3);
					getRealArea().x1 = WinSlider.this.getRealArea().x1;
					getRealArea().x2 = WinSlider.this.getRealArea().x2;
				}	
				//} 
			}
			return retVal;
		}
	};
	
	public WinSlider() {
		super();
		setFlag(8, true);  // IS_EDITABLE
	}
	
	@Override public boolean handleEvent(SpuiViewer viewer, Event event) {
		boolean val = super.handleEvent(viewer, event);
		if ((event.getEventType() == MouseEvent.MOUSE_DRAGGED) && ((getState() & STATE_FLAG_CLICKED) != 0)) {
			MouseEvent ev = (MouseEvent)event;
			if (!mouseCoordsSet) {
				mouseX += ev.getX();
				mouseY += ev.getY();
				mouseCoordsSet = true;
			}
			else {
				float dx = (float) (ev.getX() - mouseX);
				float dy = (float) (ev.getY() - mouseY);
				if (orientation == SporeUserInterface.HORIZONTAL)
					thumb.setOffsetX(thumb.getOffsetX() + dx);
				else
					thumb.setOffsetY(thumb.getOffsetY() + dy);
				
				mouseX += dx;
				mouseY += dy;
			}
		}
		else if (event.getEventType() == SpuiViewer.LAYOUT_EVENT)// || ((event.getEventType() == MouseEvent.MOUSE_DRAGGED) && ((getState() & STATE_FLAG_CLICKED) != 0)))
			thumb.handleEvent(viewer, event);
		return val;
	}
	
	public SubWindow<WinSlider> getThumb() {
		return thumb;
	}
}
