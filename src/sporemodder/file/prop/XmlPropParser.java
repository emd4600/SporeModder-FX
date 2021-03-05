/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.prop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;

public class XmlPropParser extends DefaultHandler {
	
	private static final int kPropertyInfoSize = 8;
	private static final int kArrayPropertyInfoSize = 16;
	
	private static final int kPropertyBufferSize = 512; 
	
	private static final Comparator<Integer> DESCENDING_COMPARATOR = new Comparator<Integer>() {
		@Override
        public int compare(Integer o1, Integer o2) {
			return Integer.compareUnsigned(o1, o2);
        }
    };
	
	private static final class PropertyData {
		byte[] data;
		long length;
		
		public PropertyData(MemoryStream stream) {
			data = stream.getRawData();
			length = stream.length();
		}
	}
	
	private MemoryStream stream;  // temporary stream
	
	private final Map<Integer, PropertyData> propertiesData;
	
	private final StringBuilder content = new StringBuilder();

	private int nTotalSize = 4;
	
	private boolean bInsideProperties = false;
	
	private int propertyID = 0;
	private int nCurrentType = -1;
	private int nFlags = 0;
	
	private boolean bIsArray = false;
	private boolean bIsInArrayData = false;  // are we inside the data of an array?
	private int nArrayItemCount = 0;
	private int nArrayItemSize = 0;
	
	// complex (vector/color) properties
	private boolean bIsComplexProperty = false;
	private boolean bIsInComplexComponent = false;
	private float[] complexPropertyData;
	
	
	private Attributes attributes;
	
	// for error diagnosing
	private String lastQName;
	private String lastPropertyName;
	
	
	private List<String> autoLocaleStrings;
	private String autoLocaleName;
	
	private XmlPropParser(boolean keepOrder) {
		if (keepOrder) {
			propertiesData = new LinkedHashMap<Integer, PropertyData>();
		}
		else {
			propertiesData = new TreeMap<Integer, PropertyData>(DESCENDING_COMPARATOR);
		}
	}
	
	
	@Override
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		
		this.attributes = attributes;
		this.lastQName = qName;
		
		content.setLength(0);
		
		if (qName.equalsIgnoreCase("properties")) {
			bInsideProperties = true;
		}
		else {
			if (!bInsideProperties) {
				throw new SAXException("PROP File: Properties must be inside <properties> tag.");
			}
			
			// we are not parsing an array property
			if (nCurrentType == -1) {
				
				String name = qName.toLowerCase();
				
				nCurrentType = getType(name);
				bIsArray = name.endsWith("s");
				nFlags = 0;
				
				
				if (nCurrentType == -1) {
					throw new SAXException("PROP File: Unrecognised property type '" + qName + "'.");
				}
				
				if (requiresArray(nCurrentType) && !bIsArray) {
					throw new SAXException("PROP File: Properties of type '" + qName + "' must be an array.");
				}
				
				lastPropertyName = attributes.getValue("name");
				if (lastPropertyName == null) {
					throw new SAXException("PROP File: Property must have a 'name' attribute.");
				}
				
				propertyID = HashManager.get().getPropHash(lastPropertyName);
				
				// create a new byte array to store the data
				stream.reset(kPropertyBufferSize);
				
				stream.setLength(bIsArray ? kArrayPropertyInfoSize : kPropertyInfoSize);
				stream.seek(bIsArray ? kArrayPropertyInfoSize : kPropertyInfoSize);
				
				
				// we only do this for single properties; arrays do it every time 
				if (!bIsArray) {
					if (isComplexType(nCurrentType)) {
						complexPropertyData = createComplexArray(nCurrentType);
						bIsComplexProperty = true;
					}
					else {
						bIsComplexProperty = false;
					}
					
					// parse types that don't require content (like key)
					if (!requiresContent(nCurrentType) && !bIsComplexProperty) {
						convert(stream, attributes, null);
					}
				}
				else {
					nFlags |= 0x30;
					
					nArrayItemSize = getItemSize(nCurrentType);
					
					if (isComplexType(nCurrentType)) {
						bIsComplexProperty = true;
					}
					else {
						bIsComplexProperty = false;
					}
				}
				
				return;
			}
			else if (bIsArray) {
				// we have a nCurrentType and bIsArray, therefore we are parsing an array property
				
				// are we already parsing a value?
				if (!bIsInArrayData) {
					bIsInArrayData = true;
					
					// reset the vector/color data
					if (bIsComplexProperty) {
						complexPropertyData = createComplexArray(nCurrentType);
					}
					
					nArrayItemCount++;
					
					// parse types that don't require content (like key)
					if (!requiresContent(nCurrentType) && !bIsComplexProperty) {
						convert(stream, attributes, null);
					}
					
					return;
				}
				else {
					// we are parsing a value, so this must be a component.
					if (bIsComplexProperty && !bIsInComplexComponent) {
						bIsInComplexComponent = true;
						return;
					}
					// don't return so we throw an error
				}
				
			}
			// If we are parsing a complex property, then this is a component
			else if (bIsComplexProperty) {
				bIsInComplexComponent = true;
				return;
			}
			
			throw new SAXException("PROP File: Property '" + lastPropertyName + "' has not been closed correctly. "
					+ "Are you missing the '</" + lastQName + "'> tag?");
		}
	}
	
	@Override
	public void endElement(String uri, String localName,String qName) throws SAXException {
		if (!bInsideProperties) {
			throw new SAXException("PROP File: Properties must be inside <properties> tag.");
		}
		
		if (qName.equalsIgnoreCase("properties")) {
			if (bIsInArrayData || bIsArray) {
				throw new SAXException("PROP File: Property '" + lastPropertyName + "' has not been closed correctly. "
						+ "Are you missing the '</" + lastQName + ">' tag?");
			}
			bInsideProperties = false;
			return;
		}
		
		// Apply characters now
		if (nCurrentType != -1 && (requiresContent(nCurrentType) || (bIsComplexProperty && bIsInComplexComponent))) {
			if (bIsComplexProperty) {
				if (bIsInComplexComponent) {
					parseComplexComponent(content.toString());
				}
				else if (content.length() != 0) {
					throw new SAXException("PROP File: Data in the " + lastQName + " property '" + lastPropertyName + "' must be contained inside component tags (<x></x> etc).");
				}
			}
			else {
				if ((bIsArray && !lastQName.endsWith("s") && bIsInArrayData) || !bIsArray) {
					convert(stream, attributes, content.toString());
				}
			}
		}
		
		// We finish the property ONLY if the closed tag was actually a property, and not a component like 'x'
		if (!bIsInComplexComponent) {
			
			if (bIsInArrayData) {
				bIsInArrayData = false;
				
				// vector/color properties still need to be converted at this point
				if (bIsComplexProperty) {
					convert(stream, null, null);
				}
			}
			else {
				// arrays do this on every value, not when the array itself is closed 
				if (!bIsArray) {
					// vector/color properties still need to be converted at this point
					if (bIsComplexProperty) {
						convert(stream, null, null);
					}
				}
				
				// write property info
				writePropertyInfo();
				
				// store the data
				PropertyData data = new PropertyData(stream);
				propertiesData.put(propertyID, data);
				
				nTotalSize += data.length;
				
				// reset information
				nCurrentType = -1;
				
				bIsArray = false;
				bIsInArrayData = false;
				nArrayItemCount = 0;
				nArrayItemSize = 0;
				
				// complex (vector/color) properties
				bIsComplexProperty = false;
				bIsInComplexComponent = false;
				complexPropertyData = null;
			}
		}
		else {
			// we have finished a component tag, so we are no longer inside a component
			bIsInComplexComponent = false;
		}
	}
	
	private void writePropertyInfo() throws SAXException {
		try {
			stream.seek(0);
			stream.writeInt(propertyID);
			stream.writeShort(nCurrentType);
			stream.writeShort(nFlags);
			if (bIsArray) {
				stream.writeInt(nArrayItemCount);
				stream.writeInt(nArrayItemSize);
			}
		}
		catch (Exception e) {
			throw new SAXException(e);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String text = new String(ch, start, length);
		text = text.trim();
		if (text.length() == 0) return;
		
		if (nCurrentType == -1) {
			// a comment
			if (text.startsWith("#")) return;
			else if (!text.isEmpty()) throw new SAXException("PROP File: Data must be inside a property.");
		}
		else {
			if (bIsArray) {
				if (!bIsInArrayData && !bIsInComplexComponent && !text.isEmpty()) {
					if (requiresContent(nCurrentType)) {
						throw new SAXException("PROP File: Data in the array property '" + lastPropertyName + "' must be contained inside properties."
								+ "Are you missing the <" + lastQName + "></" + lastQName + "> tags?");
					}
					else {
						throw new SAXException("PROP File: Incorrect usage of property type '" + lastQName + "' in property '" + lastPropertyName + "'.");
					}
				}
			}
			
			content.append(ch, start, length);
		}
	}
	
	
	private static float[] createComplexArray(int type) {
		if (type == PropertyVector2.TYPE_CODE) return new float[2];
		else if (type == PropertyVector3.TYPE_CODE) return new float[3];
		else if (type == PropertyVector4.TYPE_CODE) return new float[4];
		else if (type == PropertyColorRGB.TYPE_CODE) return new float[3];
		else if (type == PropertyColorRGBA.TYPE_CODE) return new float[] {0, 0, 0, 1f};
		else if (type == PropertyBBox.TYPE_CODE) return new float[6];
		else return null;
	}
	
	private void parseComplexComponent(String text) throws SAXException {
		// BBox uses min/max, the others use xyzw/rgba
		if (nCurrentType == PropertyBBox.TYPE_CODE) {
			
			if (lastQName.equalsIgnoreCase("min"))
			{
				String[] splits = text.split(", ");
				complexPropertyData[0] = Float.parseFloat(splits[0]);
				complexPropertyData[1] = Float.parseFloat(splits[1]);
				complexPropertyData[2] = Float.parseFloat(splits[2]);
				
				return;
			}
			else if (lastQName.equalsIgnoreCase("max"))
			{
				String[] splits = text.split(", ");
				complexPropertyData[3] = Float.parseFloat(splits[0]);
				complexPropertyData[4] = Float.parseFloat(splits[1]);
				complexPropertyData[5] = Float.parseFloat(splits[2]);
				
				return;
			}
		}
		else {
			if (lastQName.equalsIgnoreCase("x") || lastQName.equalsIgnoreCase("r")) 
			{
				complexPropertyData[0] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("y") || lastQName.equalsIgnoreCase("g")) 
			{
				complexPropertyData[1] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("z") || lastQName.equalsIgnoreCase("b")) 
			{
				
				if (nCurrentType != PropertyVector3.TYPE_CODE && nCurrentType != PropertyVector4.TYPE_CODE
						&& nCurrentType != PropertyColorRGB.TYPE_CODE && nCurrentType != PropertyColorRGBA.TYPE_CODE) {
					
					throw new SAXException("PROP File: Property '" + lastPropertyName + "': z/b component is only accepted in vector3/vector4/colorRGB/colorRGBA properties.");
				}
					
				complexPropertyData[2] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("w") || lastQName.equalsIgnoreCase("a")) 
			{
				
				if (nCurrentType != PropertyVector4.TYPE_CODE && nCurrentType != PropertyColorRGBA.TYPE_CODE) {
					
					throw new SAXException("PROP File: Property '" + lastPropertyName + "': w/a component is only accepted in vector4/colorRGBA properties.");
				}
					
				complexPropertyData[3] = Float.parseFloat(text);
				
				return;
			}
			
		}
		
		throw new SAXException("PROP File: Property '" + lastPropertyName + "': Unknown component '" + lastQName + "'.");
	}
	
	private void convert(MemoryStream stream, Attributes attributes, String text) throws SAXException {
		try {
			switch (nCurrentType) {
			case PropertyBool.TYPE_CODE: PropertyBool.fastConvertXML(stream, attributes, text); break;
			case PropertyChar.TYPE_CODE: PropertyChar.fastConvertXML(stream, attributes, text); break;
			case PropertyDouble.TYPE_CODE: PropertyDouble.fastConvertXML(stream, attributes, text); break;
			case PropertyFloat.TYPE_CODE: PropertyFloat.fastConvertXML(stream, attributes, text); break;
			case PropertyInt16.TYPE_CODE: PropertyInt16.fastConvertXML(stream, attributes, text); break;
			case PropertyInt32.TYPE_CODE: PropertyInt32.fastConvertXML(stream, attributes, text); break;
			case PropertyInt64.TYPE_CODE: PropertyInt64.fastConvertXML(stream, attributes, text); break;
			case PropertyInt8.TYPE_CODE: PropertyInt8.fastConvertXML(stream, attributes, text); break;
			case PropertyKey.TYPE_CODE: PropertyKey.fastConvertXML(stream, attributes, text, bIsArray); break;
			case PropertyString8.TYPE_CODE: PropertyString8.fastConvertXML(stream, attributes, text); break;
			case PropertyString16.TYPE_CODE: PropertyString16.fastConvertXML(stream, attributes, text); break;
			case PropertyUInt16.TYPE_CODE: PropertyUInt16.fastConvertXML(stream, attributes, text); break;
			case PropertyUInt32.TYPE_CODE: PropertyUInt32.fastConvertXML(stream, attributes, text); break;
			case PropertyUInt64.TYPE_CODE: PropertyUInt64.fastConvertXML(stream, attributes, text); break;
			case PropertyUInt8.TYPE_CODE: PropertyUInt8.fastConvertXML(stream, attributes, text); break;
			case PropertyWChar.TYPE_CODE: PropertyWChar.fastConvertXML(stream, attributes, text); break;
			case PropertyTransform.TYPE_CODE: PropertyTransform.fastConvertXML(stream, attributes, text); break;
			case PropertyText.TYPE_CODE:
				PropertyText.fastConvertXML(stream, attributes, text, autoLocaleStrings, autoLocaleName);
				
				break;
			
			case PropertyVector2.TYPE_CODE:
			case PropertyVector3.TYPE_CODE:
			case PropertyVector4.TYPE_CODE:
			case PropertyColorRGB.TYPE_CODE:
			case PropertyColorRGBA.TYPE_CODE:
				stream.writeLEFloats(complexPropertyData);
				
				if (!bIsArray) {
					for (int i = complexPropertyData.length; i < 4; i++) {
						stream.writePadding(4);
					}
				}
				break;
				
			case PropertyBBox.TYPE_CODE:
				stream.writeLEFloats(complexPropertyData);
				break;
				
			}
			
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}
	
	private static int getItemSize(int type) throws SAXException {
		switch (type) {
		case PropertyBool.TYPE_CODE: return PropertyBool.ARRAY_SIZE;
		case PropertyChar.TYPE_CODE: return PropertyChar.ARRAY_SIZE;
		case PropertyDouble.TYPE_CODE: return PropertyDouble.ARRAY_SIZE;
		case PropertyFloat.TYPE_CODE: return PropertyFloat.ARRAY_SIZE;
		case PropertyInt16.TYPE_CODE: return PropertyInt16.ARRAY_SIZE;
		case PropertyInt32.TYPE_CODE: return PropertyInt32.ARRAY_SIZE;
		case PropertyInt64.TYPE_CODE: return PropertyInt64.ARRAY_SIZE;
		case PropertyInt8.TYPE_CODE: return PropertyInt8.ARRAY_SIZE;
		case PropertyKey.TYPE_CODE: return PropertyKey.ARRAY_SIZE;
		case PropertyString8.TYPE_CODE: return PropertyString8.ARRAY_SIZE;
		case PropertyString16.TYPE_CODE: return PropertyString16.ARRAY_SIZE;
		case PropertyUInt16.TYPE_CODE: return PropertyUInt16.ARRAY_SIZE;
		case PropertyUInt32.TYPE_CODE: return PropertyUInt32.ARRAY_SIZE;
		case PropertyUInt64.TYPE_CODE: return PropertyUInt64.ARRAY_SIZE;
		case PropertyUInt8.TYPE_CODE: return PropertyUInt8.ARRAY_SIZE;
		case PropertyWChar.TYPE_CODE: return PropertyWChar.ARRAY_SIZE;
		case PropertyVector2.TYPE_CODE: return PropertyVector2.ARRAY_SIZE;
		case PropertyVector3.TYPE_CODE: return PropertyVector3.ARRAY_SIZE;
		case PropertyVector4.TYPE_CODE: return PropertyVector4.ARRAY_SIZE;
		case PropertyColorRGB.TYPE_CODE: return PropertyColorRGB.ARRAY_SIZE;
		case PropertyColorRGBA.TYPE_CODE: return PropertyColorRGBA.ARRAY_SIZE;
		case PropertyTransform.TYPE_CODE: return PropertyTransform.ARRAY_SIZE;
		case PropertyText.TYPE_CODE: return PropertyText.ARRAY_SIZE;
		case PropertyBBox.TYPE_CODE: return PropertyBBox.ARRAY_SIZE;
		default: return 0;
		}
	}

	private static int getType(String keyword) {
		if (keyword.startsWith("bbox")) 			return PropertyBBox.TYPE_CODE;
		else if (keyword.startsWith("bool")) 		return PropertyBool.TYPE_CODE;
		else if (keyword.startsWith("char"))		return PropertyChar.TYPE_CODE;
		else if (keyword.startsWith("colorrgba"))	return PropertyColorRGBA.TYPE_CODE;
		else if (keyword.startsWith("colorrgb"))	return PropertyColorRGB.TYPE_CODE;
		else if (keyword.startsWith("double"))		return PropertyDouble.TYPE_CODE;
		else if (keyword.startsWith("float")) 		return PropertyFloat.TYPE_CODE;
		else if (keyword.startsWith("int16")) 		return PropertyInt16.TYPE_CODE;
		else if (keyword.startsWith("int32")) 		return PropertyInt32.TYPE_CODE;
		else if (keyword.startsWith("int64")) 		return PropertyInt64.TYPE_CODE;
		else if (keyword.startsWith("int8")) 		return PropertyInt8.TYPE_CODE;
		else if (keyword.startsWith("key")) 		return PropertyKey.TYPE_CODE;
		else if (keyword.startsWith("string16")) 	return PropertyString16.TYPE_CODE;
		else if (keyword.startsWith("string8")) 	return PropertyString8.TYPE_CODE;
		else if (keyword.startsWith("text")) 		return PropertyText.TYPE_CODE;
		else if (keyword.startsWith("transform")) 	return PropertyTransform.TYPE_CODE;
		else if (keyword.startsWith("uint16")) 		return PropertyUInt16.TYPE_CODE;
		else if (keyword.startsWith("uint32")) 		return PropertyUInt32.TYPE_CODE;
		else if (keyword.startsWith("uint64")) 		return PropertyUInt64.TYPE_CODE;
		else if (keyword.startsWith("uint8")) 		return PropertyUInt8.TYPE_CODE;
		else if (keyword.startsWith("vector2"))		return PropertyVector2.TYPE_CODE;
		else if (keyword.startsWith("vector3")) 	return PropertyVector3.TYPE_CODE;
		else if (keyword.startsWith("vector4")) 	return PropertyVector4.TYPE_CODE;
		else if (keyword.startsWith("wchar")) 		return PropertyWChar.TYPE_CODE;
		else return -1;
	}
	
	
	/** Returns false if the type is a single-line property, true if it is not. */
	private static boolean isComplexType(int type) {
		return (type == PropertyBBox.TYPE_CODE || type == PropertyColorRGBA.TYPE_CODE || type == PropertyColorRGB.TYPE_CODE
				|| type == PropertyVector2.TYPE_CODE || type == PropertyVector3.TYPE_CODE || type == PropertyVector4.TYPE_CODE);
	}
	
	private static boolean requiresArray(int type) {
		return type == PropertyText.TYPE_CODE || type == PropertyTransform.TYPE_CODE || type == PropertyBBox.TYPE_CODE;
	}
	
	private static boolean requiresContent(int type) {
		return type != PropertyKey.TYPE_CODE && !isComplexType(type) /* && !requiresArray(type) */;
	}
	
	public static MemoryStream xmlToProp(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		return (MemoryStream) xmlToProp(in, null, null, null);
	}
	
	public static StreamWriter xmlToProp(InputStream in, StreamWriter out, List<String> autoLocaleStrings, String autoLocaleName) throws ParserConfigurationException, SAXException, IOException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		
		XmlPropParser converter = new XmlPropParser(false);
		
		try (MemoryStream stream = new MemoryStream()) { 

			converter.stream = stream;
			converter.autoLocaleStrings = autoLocaleStrings;
			converter.autoLocaleName = autoLocaleName;
		
			parser.parse(in, converter);
		}
		
		if (out == null) {
			out = new MemoryStream(converter.nTotalSize);
		}
		
		out.writeInt(converter.propertiesData.size());
		
		for (PropertyData propertyData : converter.propertiesData.values()) {
			out.write(propertyData.data, 0, (int) propertyData.length);
		}
		
		return out;
	}
	
	public static Map<Integer, PropertyData> xmlToRawData(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		
		XmlPropParser converter = new XmlPropParser(false);
		
		try (MemoryStream stream = new MemoryStream()) { 

			converter.stream = stream;
		
			parser.parse(in, converter);
			
			return converter.propertiesData;
		}
	}
}
