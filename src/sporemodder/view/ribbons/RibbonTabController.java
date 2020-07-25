package sporemodder.view.ribbons;

import emord.javafx.ribbon.Ribbon;
import emord.javafx.ribbon.RibbonTab;

public abstract class RibbonTabController {
	
	protected RibbonTab tab;
	
	public abstract void addTab(Ribbon ribbon);
	
	public RibbonTab getTab() {
		return tab;
	}
}
