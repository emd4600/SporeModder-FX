package sporemodder.extras.spuieditor;

import sporemodder.extras.spuieditor.components.WinComponent;

public class WinComponentAction implements CommandAction {
	
	private WinComponent component;
	private DraggableType type;
	private int dx;
	private int dy;
	

	public WinComponentAction(WinComponent component, DraggableType type, int dx, int dy) {
		this.component = component;
		this.type = type;
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void undo() {
		if (type != null) {
			type.process(component, -dx, -dy);
		}
	}

	@Override
	public void redo() {
		if (type != null) {
			type.process(component, dx, dy);
		}
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
}
