package sporemodder.extras.spuieditor.uidesigner;

import java.util.List;

import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;

public interface DesignerElement extends DesignerNode {
	
	public void fillPropertiesPanel(PropertiesPanel panel, DesignerClassDelegate delegate, SPUIEditor editor);
	
	public void getDesignerElements(List<DesignerElement> list);
	
	public String getName();
	
	public UIDesigner getDesigner();
	
}
