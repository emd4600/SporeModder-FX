package sporemodder.view.ribbons;

import emd4600.javafx.ribbon.Ribbon;
import emd4600.javafx.ribbon.RibbonTab;

public abstract class RibbonTabController {
	
	protected RibbonTab tab;
	
	public abstract void addTab(Ribbon ribbon);
	
	public RibbonTab getTab() {
		return tab;
	}
}
