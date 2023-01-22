package sporemodder.extras.spuieditor.uidesigner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sporemodder.MainApp;
import sporemodder.UIManager;
//import sporemodder.userinterface.ErrorManager;

public class UIDesigner {
	
	public static final UIDesigner Designer = new UIDesigner();
	static {
		UIManager.get().tryAction(() -> {
		//try {
			Designer.parse(UIDesigner.class.getResourceAsStream("/sporemodder/extras/spuieditor/resources/SporeUIDesignerProjectCommon.xml"));
			Designer.parse(UIDesigner.class.getResourceAsStream("/sporemodder/extras/spuieditor/resources/SporeUIDesignerProjectCustom.xml"));
			
		/*} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Unable to start UI designer. Please report this error:\n" + ErrorManager.getStackTraceString(e), 
					"Fatal Error", JOptionPane.ERROR_MESSAGE);
		}*/
		}, "Unable to start UI designer. Please report this error.");
	}
	
	private final List<DesignerClass> classes = new ArrayList<DesignerClass>();
	private final List<DesignerEnum> enums = new ArrayList<DesignerEnum>();

	public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		
		parser.parse(is, new DefaultHandler() {
			
			private DesignerClass currentClass;
			private DesignerCategory currentCategory;
			private DesignerEnum currentEnum;
			private DesignerProperty currentProperty;
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				
				if (qName.equalsIgnoreCase("Class") || qName.equalsIgnoreCase("Struct")) {
					currentClass = new DesignerClass(UIDesigner.this);
					currentClass.parseElement(uri, localName, qName, attributes);
					classes.add(currentClass);
				}
				else if (currentClass != null && qName.equalsIgnoreCase("Category")) {
					currentCategory = new DesignerCategory(UIDesigner.this);
					currentCategory.parseElement(uri, localName, qName, attributes);
					currentClass.getElements().add(currentCategory);
				}
				else if (qName.equalsIgnoreCase("Enum")) {
					currentEnum = new DesignerEnum();
					currentEnum.parseElement(uri, localName, qName, attributes);
					enums.add(currentEnum);
				}
				else if (currentEnum != null && qName.equalsIgnoreCase("EnumVal")) {
					currentEnum.parseElement(uri, localName, qName, attributes);
				}
				else if (currentClass != null && qName.equalsIgnoreCase("Property")) {
					currentProperty = new DesignerProperty(UIDesigner.this);
					currentProperty.parseElement(uri, localName, qName, attributes);
					
					if (currentCategory != null) {
						currentCategory.getElements().add(currentProperty);
					}
					else {
						currentClass.getElements().add(currentProperty);
					}
				}
				else if (currentProperty != null && qName.equalsIgnoreCase("Index")) {
					currentProperty.parseElement(uri, localName, qName, attributes);
				}
				else if (currentClass != null && qName.equalsIgnoreCase("Implements")) {
					currentClass.parseElement(uri, localName, qName, attributes);
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.equalsIgnoreCase("Class") || qName.equalsIgnoreCase("Struct")) {
					currentClass = null;
				}
				else if (qName.equalsIgnoreCase("Category")) {
					currentCategory = null;
				}
				else if (qName.equalsIgnoreCase("Enum")) {
					currentEnum = null;
				}
				else if (qName.equalsIgnoreCase("Property")) {
					currentProperty = null;
				}
			}
		});
		
	}
	
	public List<DesignerClass> getClasses() {
		return classes;
	}

	public DesignerClass getClass(String name) {
		for (DesignerClass clazz : classes) {
			if (clazz.getName().equals(name)) {
				return clazz;
			}
		}
		return null;
	}
	
	public DesignerClass getClass(int proxyID) {
		for (DesignerClass clazz : classes) {
			if (clazz.getProxyID() == proxyID) {
				return clazz;
			}
		}
		return null;
	}
	
	public DesignerEnum getEnum(String name) {
		for (DesignerEnum e : enums) {
			if (e.getName().equals(name)) {
				return e;
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		UIDesigner designer = new UIDesigner();
		try (InputStream is = UIDesigner.class.getResourceAsStream("/sporemodder/extras/spuieditor/resources/sporeuidesignerprojectcommon.xml")) {
			designer.parse(is);
		}
		
		for (DesignerClass clazz : designer.classes) {
			System.out.println(clazz.toString());
		}
	}
}
