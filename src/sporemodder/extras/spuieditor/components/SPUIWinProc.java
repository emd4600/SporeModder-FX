package sporemodder.extras.spuieditor.components;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public interface SPUIWinProc extends SPUIComponent {
	
	public static final String INTERFACE_NAME = "IWinProc";
	
	@Override
	public SPUIWinProc copyComponent(boolean propagate);

	public void modify(WinComponent component);
	public Graphics2D modifyPreRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds);
	public void modifyPostRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds);

	public WinComponent getParent();
	public void setParent(WinComponent parent);
	
	public static abstract class SPUIDefaultWinProc extends SPUIDefaultComponent implements SPUIWinProc {

		protected WinComponent parent;

		public SPUIDefaultWinProc(SPUIBlock block) throws InvalidBlockException {
			super(block);
		}

		protected SPUIDefaultWinProc() {
			super();
		}

		public SPUIDefaultWinProc(SPUIViewer viewer) {
			super(viewer);
		}

		@Override
		public void modify(WinComponent component) {
		}

		@Override
		public Graphics2D modifyPreRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds) {
			return realGraphics;
		}

		@Override
		public void modifyPostRender(WinComponent component, Graphics2D realGraphics, Rectangle drawBounds) {
		}

		@Override
		public WinComponent getParent() {
			return parent;
		}

		@Override
		public void setParent(WinComponent parent) {
			this.parent = parent;
		}
		
		@Override
		public boolean nodeIsMovable() {
			return true;
		}
		
		@Override
		public boolean nodeCanBeMovedAbove() {
			int indexOf = parent.getIndexOfChild(this);
			return indexOf > 0;
		}
		
		@Override
		public boolean nodeCanBeMovedBelow() {
			int indexOf = parent.getIndexOfChild(this);
			return indexOf < parent.getModifiers().size() - 1;
		}
		
		@Override
		public SPUIBlock saveComponent(SPUIBuilder builder) {
			SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
			
			if (parent != null && parent instanceof Window && ((Window) parent).isLayoutWindow()) {
				block.setIsRoot(true);
			}
			
			return block;
		}
	}
}
