package sporemodder.extras.spuieditor.uidesigner;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.utilities.Hasher;

public class DesignerClass implements DesignerElement {
	
	private String name;
	private int proxyID = -1;
	
	private UIDesigner parentDesigner;
	private DesignerClass baseClass;
	
	private final List<String> implementedInterfaces = new ArrayList<String>();
	private String className;
	
	private final List<DesignerElement> elements = new ArrayList<DesignerElement>();
	
	public DesignerClass(UIDesigner parentDesigner) {
		this.parentDesigner = parentDesigner;
	}

	@Override
	public String toString() {
		return "DesignerClass [name=" + name + ", proxyID=" + Hasher.hashToHex(proxyID, "0x") + "]";
	}

	@Override
	public void parseElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("Class") || qName.equalsIgnoreCase("Struct")) {
			name = attributes.getValue("name");
			
			String proxyStr = attributes.getValue("proxy");
			
			if (proxyStr != null) {
				proxyID = Hasher.getFileHash(proxyStr);
				
//				if (proxyStr.startsWith("utfwin:")) {
//					proxyStr = proxyStr.substring("utfwin:".length());
//				}
//				
//				proxyID = Hasher.decodeInt(proxyStr);
			}
			
			String baseStr = attributes.getValue("base");
			if (baseStr != null) {
				baseClass = parentDesigner.getClass(baseStr);
			}
			
			className = attributes.getValue("classname");
		}
		else if (qName.equalsIgnoreCase("Implements")) {
			implementedInterfaces.add(attributes.getValue("name"));
		}
	}

	public String getName() {
		return name;
	}

	public int getProxyID() {
		return proxyID;
	}

	public List<DesignerElement> getElements() {
		return elements;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProxyID(int proxyID) {
		this.proxyID = proxyID;
	}

	@Override
	public void getDesignerElements(List<DesignerElement> list) {
		if (baseClass != null) {
			baseClass.getDesignerElements(list);
		}
		
		for (DesignerElement element : elements) {
			element.getDesignerElements(list);
		}
	}
	
	public DesignerProperty getProperty(int proxyID) {
		for (DesignerElement element : elements) {
			if (element instanceof DesignerProperty) {
				if (((DesignerProperty) element).getProxyID() == proxyID) {
					return (DesignerProperty) element;
				}
			}
		}
		return null;
	}
	
	public void fillPropertiesPanel(PropertiesPanel panel, DesignerClassDelegate delegate, SPUIEditor editor) {
		
		List<DesignerElement> elements = new ArrayList<DesignerElement>();
		getDesignerElements(elements);
		
		for (DesignerElement element : elements) {
			if (delegate.isValid(element)) {
				element.fillPropertiesPanel(panel, delegate, editor);
			}
		}
	}
	
	public static interface DesignerClassDelegate {
		 public boolean isValid(DesignerElement element);
		 
		 public void setValue(DesignerProperty property, Object value, int index);
		 public Object getValue(DesignerProperty property);
		 public void propertyComponentAdded(DesignerProperty property, Object component);
	}
	
	@Override
	public UIDesigner getDesigner() {
		return parentDesigner;
	}
	
	public String getClassName() {
		return className;
	}
	
	public boolean IsImplementingInterface(String interfaceName) {
		if (baseClass != null && baseClass.IsImplementingInterface(interfaceName)) {
			return true;
		}
		return implementedInterfaces.contains(interfaceName);
	}
}
