package sporemodder.extras.spuieditor;

import sporemodder.extras.spuieditor.components.Image;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIDrawable;

public class RootComponentContainer implements ComponentContainer {
	private String name;
	private SPUIEditor editor;
	
	public RootComponentContainer(String name, SPUIEditor editor) {
		this.name = name;
		this.editor = editor;
	}

	@Override
	public void insertComponent(SPUIComponent component, int index) {
		if (component instanceof SPUIDrawable) {
			editor.addDrawableComponent((SPUIDrawable) component);
		}
		else if (component instanceof Image) {
			editor.addImageComponent((Image) component);
		}
	}

	@Override
	public void removeComponent(SPUIComponent component) {
		if (component instanceof SPUIDrawable) {
			editor.removeDrawableComponent((SPUIDrawable) component);
		}
		else if (component instanceof Image) {
			editor.removeImageComponent((Image) component);
		}
	}
	
	@Override
	public boolean isSPUIComponent() {
		return false;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean nodeIsMovable() {
		return false;
	}
	
	@Override
	public boolean nodeAcceptsComponent(SPUIComponent other) {
		return false;
	}
	
	@Override
	public boolean nodeCanBeMovedAbove() {
		return false;
	}
	
	@Override
	public boolean nodeCanBeMovedBelow() {
		return false;
	}
}
