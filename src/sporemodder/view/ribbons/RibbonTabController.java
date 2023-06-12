package sporemodder.view.ribbons;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonTab;

public abstract class RibbonTabController {
	
	protected RibbonTab tab;
	
	public abstract void addTab(Ribbon ribbon);
	
	public RibbonTab getTab() {
		return tab;
	}
}
