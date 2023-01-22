package sporemodder.extras.spuieditor.uidesigner;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface DesignerNode {

	public void parseElement(String uri, String localName,String qName, Attributes attributes) throws SAXException;
	
}
