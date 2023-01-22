package sporemodder.extras.spuieditor.uidesigner;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;

public class DesignerCategory implements DesignerElement {

	private String name;
	private final List<DesignerElement> elements = new ArrayList<DesignerElement>();
	
	private UIDesigner designer;
	
	public DesignerCategory(UIDesigner designer) {
		this.designer = designer;
	}
	
	@Override
	public void parseElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("Category")) {
			name = attributes.getValue("name");
		}
	}

	@Override
	public void fillPropertiesPanel(PropertiesPanel panel, DesignerClassDelegate delegate, SPUIEditor editor) {
		
		PropertiesPanel categoryPanel = new PropertiesPanel(name);
		
		for (DesignerElement element : elements) {
			if (delegate.isValid(element)) {
				element.fillPropertiesPanel(categoryPanel, delegate, editor);
			}
		}
		
		if (categoryPanel.getComponents().length > 0) {
			panel.addPanel(categoryPanel);
		}
	}
	
	@Override
	public void getDesignerElements(List<DesignerElement> list) {
		DesignerCategory category = null;
		
		for (int i = 0; i < list.size(); i++) {
			DesignerElement elem = list.get(i);
			if (elem.getName().equals(name)) {
				category = new DesignerCategory(designer);
				category.name = name;
				category.elements.addAll(((DesignerCategory) elem).elements);
				
				list.set(i, category);
				
				break;
			}
		}
		
		if (category == null) {
			category = this;
			list.add(category);	
		}
		
		for (DesignerElement p : elements) {
			p.getDesignerElements(category.elements);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public List<DesignerElement> getElements() {
		return elements;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public UIDesigner getDesigner() {
		return designer;
	}
}
