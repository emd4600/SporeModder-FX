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

import javafx.scene.Cursor;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.components.IWindow;

public enum SpuiDraggableType {
	COMPONENT {
		@Override
		public void process(SPUIRectangle bounds, float dx, float dy) {
			bounds.translate(dx, dy);
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			activeComponent.getArea().translate(dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return null;
		}
	},
	RESIZE_TOP_LEFT {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.x1 += dx;
			rect.y1 += dy;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x1, bounds.y1, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.NW_RESIZE;
		}
	},
	RESIZE_TOP_RIGHT {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.y1 += dy;
			rect.x2 += dx;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x2, bounds.y1, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.NE_RESIZE;
		}
	},
	RESIZE_BOTTOM_LEFT {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.x1 += dx;
			rect.y2 += dy;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x1, bounds.y2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.SW_RESIZE;
		}
	},
	RESIZE_BOTTOM_RIGHT {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.x2 += dx;
			rect.y2 += dy;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x2, bounds.y2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.SE_RESIZE;
		}
	},
	RESIZE_TOP {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.y1 += dy;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x1 + bounds.getWidth() / 2, bounds.y1, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.N_RESIZE;
		}
	},
	RESIZE_BOTTOM {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.y2 += dy;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x1 + bounds.getWidth() / 2, bounds.y2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.S_RESIZE;
		}
	},
	RESIZE_LEFT {
		@Override
		public void process(SPUIRectangle rect, float dx, float dy) {
			rect.x1 += dx;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x1, bounds.y1 + bounds.getHeight() / 2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.W_RESIZE;
		}
	},
	RESIZE_RIGHT {
		@Override
		public void process(SPUIRectangle bounds, float dx, float dy) {
			bounds.x2 += dx;
		}
		@Override
		public void process(IWindow activeComponent, float dx, float dy) {
			process(activeComponent.getArea(), dx, dy);
		}
		@Override
		public SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize) {
			return getPointRect(bounds.x2, bounds.y1 + bounds.getHeight() / 2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.E_RESIZE;
		}
	};

	public abstract void process(IWindow activeComponent, float dx, float dy);
	public abstract void process(SPUIRectangle bounds, float dx, float dy);
	public abstract SPUIRectangle getPointRect(SPUIRectangle bounds, float pointSize);
	public Cursor getCursor() {
		return null;
	}
	
	private static SPUIRectangle getPointRect(float x, float y, float pointSize) {
		float radius = pointSize / 2.0f;
		return new SPUIRectangle(x - radius, y - radius, x + radius, y + radius);
	}
}
