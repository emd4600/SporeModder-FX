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
package sporemodder.file.spui.uidesigner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sporemodder.HashManager;
import sporemodder.PathManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.StyleSheet;

public class SpuiDesigner {
	
	public static final String FOLDER_NAME = "UI Editor";
	
	//TODO improve this?
	private final List<String> interfaces = new ArrayList<>();

	/** A map of all enums declared in the designer, mapped to their name. */
	private final Map<String, DesignerEnum> enums = new HashMap<String, DesignerEnum>();
	
	/** A map of all classes/structs declared in the designer, mapped to their name. */
	// We keep insertion order as they are sort of grouped
	private final Map<String, DesignerClass> classes = new LinkedHashMap<String, DesignerClass>();
	/** A map of all classes declared in the designer, mapped to their proxy ID. */
	private final Map<Integer, DesignerClass> proxyMap = new HashMap<Integer, DesignerClass>();
	
	private boolean isLoaded;
	
	public Map<String, DesignerClass> getClasses() {
		return classes;
	}
	
	/**
	 * Returns the designer enumeration that has the given name, or null if it does not exist.
	 * This does not include enumerations declared inside classes/structs.
	 * @param name
	 * @return
	 */
	public DesignerEnum getEnum(String name) {
		return enums.get(name.toLowerCase());
	}
	
	/**
	 * Returns the designer class/struct that has the given name, or null if it does not exist.
	 * This does not include classes/structs declared inside other classes/structs.
	 * @param name
	 * @return
	 */
	public DesignerClass getClass(String name) {
		return classes.get(name.toLowerCase());
	}
	
	public DesignerClass getClass(int proxyID) {
		return proxyMap.get(proxyID);
	}
	
	public DesignerClass getClass(Class<? extends SpuiElement> javaClass) {
		for (DesignerClass clazz : classes.values()) {
			if (clazz.getJavaClass() == javaClass) return clazz;
		}
		return null;
	}

	public SpuiElement createElementWithDefaults(String name) {
		return getClass(name).createInstanceWithDefaults();
	}

	public <T extends SpuiElement> T createElementWithDefaults(Class<T> javaClass) {
		return (T)getClass(javaClass).createInstanceWithDefaults();
	}
	
	public List<DesignerClass> getImplementingClasses(String interfaceName) {
		List<DesignerClass> list = new ArrayList<>();
		
		for (DesignerClass clazz : classes.values()) {
			if (!clazz.isAbstract() && clazz.implementsInterfaceComplete(interfaceName)) {
				list.add(clazz);
			}
		}
		
		return list;
	}
	
	public boolean hasInterface(String name) {
		for (String str : interfaces) {
			if (str.equals(name)) return true;
		}
		return false;
	}
	
	public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		
		parser.parse(is, new DefaultHandler() {
			
			private DesignerClass currentClass;
			private DesignerEnum currentEnum;
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if (qName.equalsIgnoreCase("Interface")) {
					interfaces.add(attributes.getValue("name"));
				}
				else if (currentEnum != null) {
					currentEnum.startElement(uri, localName, qName, attributes);
				}
				else if (currentClass != null) {
					currentClass.startElement(uri, localName, qName, attributes);
				}
				else if (qName.equalsIgnoreCase(DesignerClass.KEYWORD) || qName.equalsIgnoreCase(DesignerClass.KEYWORD_STRUCT)) {
					
					currentClass = new DesignerClass(SpuiDesigner.this, qName.equalsIgnoreCase(DesignerClass.KEYWORD_STRUCT));
					currentClass.startElement(uri, localName, qName, attributes);
					//if (!classes.containsKey(currentClass.getName().toLowerCase()))
					classes.put(currentClass.getName().toLowerCase(), currentClass);
				}
				else if (qName.equalsIgnoreCase(DesignerEnum.KEYWORD)) {
					
					currentEnum = new DesignerEnum();
					currentEnum.startElement(uri, localName, qName, attributes);
					//if (!enums.containsKey(currentEnum.getName().toLowerCase()))
					enums.put(currentEnum.getName().toLowerCase(), currentEnum);
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (currentClass != null && (qName.equalsIgnoreCase(DesignerClass.KEYWORD) || qName.equalsIgnoreCase(DesignerClass.KEYWORD_STRUCT))) {
					if (!currentClass.isAbstract()) proxyMap.put(currentClass.getProxyID(), currentClass);
					currentClass = null;
				}
				else if (currentEnum != null && qName.equalsIgnoreCase(DesignerEnum.KEYWORD)) {
					currentEnum = null;
				}
				else if (currentClass != null) {
					currentClass.endElement(uri, localName, qName);
				}
				else if (currentEnum != null) {
					currentEnum.endElement(uri, localName, qName);
				}
			}
		});
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}
	
	public void load() {
		UIManager.get().tryAction(() -> {
			boolean hasProject = ProjectManager.get().getActive() != null;
			String projectUiDesignerFolder = HashManager.get().getFileName(0x0248E873);
			
			if (hasProject && false)
			{
				File projectDesignerFolder = ProjectManager.get().getFile(projectUiDesignerFolder);
				if (projectDesignerFolder != null) {
					File [] xmlFiles = projectDesignerFolder.listFiles(new FilenameFilter() {
					    @Override
					    public boolean accept(File dir, String name) {
					        return name.toLowerCase().endsWith(".xml");
					    }
					});
					
					for (File xml : xmlFiles) {
						System.out.println(xml.getAbsolutePath());
						try (InputStream is = new FileInputStream(xml)) {
							parse(is);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
			
			
			String fileName = File.separatorChar + "SporeUIDesignerProjectCommon.xml";
			//File projectFile = ProjectManager.get().getFile(projectUiDesignerFolder + fileName);
			
			File file = PathManager.get().getProgramFile(FOLDER_NAME + fileName);
				
			try (InputStream is = new FileInputStream(file)) {
				parse(is);
			}
			
			/*if (projectFile != null) {
				String data = new String(Files.readAllBytes(file.toPath())).replace("&#x10;", "&#10;");
				try (InputStream is = new ByteArrayInputStream(data.getBytes())) {
					parse(is);
				}
			}*/
			

			fileName = File.separatorChar + "SporeUIDesignerProjectCustom.xml";
			
			file = PathManager.get().getProgramFile(FOLDER_NAME + fileName);
			try (InputStream is = new FileInputStream(file)) {
				parse(is);
			}
			
			/*projectFile = ProjectManager.get().getFile(projectUiDesignerFolder + fileName);
			
			if (projectFile != null) {
				String data = new String(Files.readAllBytes(projectFile.toPath())).replace("&#x10;", "&#10;");
				try (InputStream is = new ByteArrayInputStream(data.getBytes())) {
					parse(is);
				}
			}*/
			
			fileName = File.separatorChar + "sporeuitextstyles.css";
			file = null;
			if (hasProject) file = ProjectManager.get().getFile(projectUiDesignerFolder + fileName);
			
			if (file == null)
				file = PathManager.get().getProgramFile(FOLDER_NAME + fileName);
			
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				StyleSheet.setActiveStyleSheet(StyleSheet.readStyleSheet(reader));
			}
			
			isLoaded = true;
		}, "Cannot load SPUI designer.");
	}
}
