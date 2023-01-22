package sporemodder.extras.spuieditor.uidesigner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DesignerEnum implements DesignerNode {
	
	private String name;
	private final HashMap<Integer, String> intValues = new HashMap<Integer, String>();
	private final HashMap<String, Integer> stringValues = new HashMap<String, Integer>();
	private final List<String> values = new ArrayList<String>();	

	@Override
	public void parseElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("Enum")) {
			name = attributes.getValue("name");
		}
		else if (qName.equalsIgnoreCase("EnumVal")) {
			String name = attributes.getValue("name");
			int index = Integer.decode(attributes.getValue("value"));
			
			intValues.put(index, name);
			stringValues.put(name, index);
			values.add(name);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue(int index) {
		return intValues.get(index);
	}
	
	public int getValue(String value) {
		return stringValues.get(value);
	}
	
	public List<String> getStringValues() {
		return values;
	}
}
