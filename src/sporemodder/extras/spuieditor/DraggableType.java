package sporemodder.extras.spuieditor;

import java.awt.Cursor;
import java.awt.Rectangle;

import sporemodder.extras.spuieditor.components.WinComponent;

public enum DraggableType {
	
	COMPONENT {
		@Override
		public void process(Rectangle bounds, int dx, int dy) {
			bounds.translate(dx, dy);
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			activeComponent.translate(dx, dy);
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return null;
		}
	},
	RESIZE_TOP_LEFT {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.x += dx;
			rect.y += dy;
			rect.width -= dx;
			rect.height -= dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x, bounds.y, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
		}
	},
	RESIZE_TOP_RIGHT {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.y += dy;
			rect.width += dx;
			rect.height -= dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x + bounds.width, bounds.y, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
		}
	},
	RESIZE_BOTTOM_LEFT {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.x += dx;
			rect.width -= dx;
			rect.height += dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x, bounds.y + bounds.height, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
		}
	},
	RESIZE_BOTTOM_RIGHT {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.width += dx;
			rect.height += dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x + bounds.width, bounds.y + bounds.height, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
		}
	},
	RESIZE_TOP {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.y += dy;
			rect.height -= dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x + bounds.width / 2, bounds.y, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
		}
	},
	RESIZE_BOTTOM {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.height += dy;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x + bounds.width / 2, bounds.y + bounds.height, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
		}
	},
	RESIZE_LEFT {
		@Override
		public void process(Rectangle rect, int dx, int dy) {
			rect.x += dx;
			rect.width -= dx;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x, bounds.y + bounds.height / 2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		}
	},
	RESIZE_RIGHT {
		@Override
		public void process(Rectangle bounds, int dx, int dy) {
			bounds.width += dx;
		}
		@Override
		public void process(WinComponent activeComponent, int dx, int dy) {
			process(activeComponent.getBounds(), dx, dy);
			
			activeComponent.setBounds(activeComponent.getBounds());
			activeComponent.revalidate();
		}
		@Override
		public Rectangle getPointRect(Rectangle bounds, int pointSize) {
			return getPointRect(bounds.x + bounds.width, bounds.y + bounds.height / 2, pointSize);
		}
		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		}
	};

	public abstract void process(WinComponent activeComponent, int dx, int dy);
	public abstract void process(Rectangle bounds, int dx, int dy);
	public abstract Rectangle getPointRect(Rectangle bounds, int pointSize);
	public Cursor getCursor() {
		return null;
	}
	
	private static Rectangle getPointRect(int x, int y, int pointSize) {
		float radius = pointSize / 2.0f;
		return new Rectangle((int) (x - radius), (int) (y - radius), pointSize, pointSize);
	}
}
