package sporemodder.extras.spuieditor;

import sporemodder.extras.spuieditor.components.SPUIComponent;

public interface ComponentContainer {
	
	public boolean isSPUIComponent();

	public void insertComponent(SPUIComponent component, int index);
	public void removeComponent(SPUIComponent component);
	
	public boolean nodeIsMovable();
	public boolean nodeAcceptsComponent(SPUIComponent other);
	public boolean nodeCanBeMovedAbove();
	public boolean nodeCanBeMovedBelow();

}
