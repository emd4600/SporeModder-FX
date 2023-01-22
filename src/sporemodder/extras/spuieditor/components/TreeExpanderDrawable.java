package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIObject;

public class TreeExpanderDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final int TYPE = 0xF02C7C44;

	// apparently, it uses no properties
	
	public TreeExpanderDrawable(SPUIObject object) throws InvalidBlockException {
		super(object);
	}

	private TreeExpanderDrawable() {
		super();
	}

	public TreeExpanderDrawable(SPUIViewer viewer) {
		super(viewer);
	}

	@Override
	public TreeExpanderDrawable copyComponent(boolean propagate) {
		TreeExpanderDrawable other = new TreeExpanderDrawable();
		copyComponent(other, propagate);
		return other;
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public Dimension getDimensions(int imageIndex) {
		return null;
	}

	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
