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
package sporemodder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DocumentationManager extends AbstractManager {
	
	public static class DocumentationLink {
		public String title;
		public String url;
	}
	
	public static class DocumentationLinkCategory {
		public String name;
		public final List<DocumentationLink> links = new ArrayList<>();
	}
	
//	public static class DocumentationLinkPage {
//		public String name;
//		public final List<DocumentationLinkCategory> categories = new ArrayList<>();
//	}
	
	private final Map<String, Properties> loadedFiles = new HashMap<>();
	private final Map<String, List<DocumentationLinkCategory>> docLinks = new HashMap<>();

	/**
	 * Returns the class that controls the documentation of the program.
	 * @return The documentation manager
	 */
	public static DocumentationManager get() {
		return MainApp.get().getDocumentationManager();
	}
	
	@Override public void initialize(Properties settings) {
		try {
			loadDocLinks(NetworkUtils.getJSON(NetworkUtils.getUrl("https://raw.githubusercontent.com/emd4600/SporeModder-FX/master/smfx_docs.json")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the documentation string associated with the given code. If the code is, for example, "particles.alpha.vary", that same code will be searched
	 * in the "particles" file. 
	 * If nothing was assigned to the given documentation code, or the text assigned was blank, the documentation code itself is returned.
	 * @param fileName
	 * @param code
	 * @return
	 */
	public String getDocumentation(String code) {
		return getDocumentation(code.split("\\.")[0], code);
	}
	
	/**
	 * Returns the documentation string found in the given file, which is given without extension (for example, just "properties"). 
	 * If nothing was assigned to the given documentation code, or the text assigned was blank, the documentation code itself is returned.
	 * @param fileName
	 * @param code
	 * @return
	 */
	public String getDocumentation(String fileName, String code) {
		Properties properties = loadFile(fileName);
		
		if (properties == null) {
			return code;
		} else {
			
			String result = properties.getProperty(code);
			
			if (result == null && code.endsWith(".")) {
				// Special case: particles.rate and particles.rate. might be the same
				result = properties.getProperty(code.substring(0, code.length() - 1));
			}
			
			if (result == null && !code.endsWith(".")) {
				// Special case: particles.rate and particles.rate. might be the same
				result = properties.getProperty(code + ".");
			}
			
			// Return the result or the code
			if (result == null || result.trim().isEmpty()) {
				return code;
			} else {
				return result;
			}
		}
	}
	
	public List<DocumentationLinkCategory> getDocumentationLinks(String docsEntry) {
		return docLinks.get(docsEntry);
	}
	
	public Pane createDocumentationPane(String entry) {
		List<DocumentationLinkCategory> categories = getDocumentationLinks(entry);
		if (categories == null) return null;
		
		Pane pane = new VBox();
		pane.setPadding(new Insets(5, 0, 0, 0));
		
		for (DocumentationLinkCategory category : categories) {
			if (!category.name.isEmpty()) {
				Label label = new Label(category.name);
				label.getStyleClass().add("inspector-docs-title");
				pane.getChildren().add(label);
			}
			for (DocumentationLink link : category.links) {
				Hyperlink hl = new Hyperlink(link.title);
				hl.setWrapText(true);
				hl.getStyleClass().add("inspector-docs-link");
				if (link.url.isEmpty()) {
					hl.setDisable(true);
				} 
				else {
					hl.setTooltip(new Tooltip(link.url));
					hl.setOnAction(event -> {
						MainApp.get().getHostServices().showDocument(link.url);
					});
				}
				pane.getChildren().add(hl);
			}
		}
		
		return pane;
	}
	
	private Properties loadFile(String fileName) {
		Properties result = loadedFiles.get(fileName);
		
		if (result == null) {
			File file = new File(PathManager.get().getProgramFile("Documentation"), fileName + ".txt");
			
			result = new Properties();
			
			try (FileReader reader = new FileReader(file)) {
				result.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				
				return null;
			}
			
			loadedFiles.put(fileName, result);
		}
		
		return result;
	}
	
	private void loadDocLinks(JSONObject json) {
		JSONObject obj = json.getJSONObject("entries");
		Set<String> keys = obj.keySet();
		
		for (String key : keys) {
			List<DocumentationLinkCategory> categories = new ArrayList<>();
			
			JSONArray jsonCategories = obj.getJSONArray(key);
			for (int i = 0; i < jsonCategories.length(); ++i) {
				JSONObject jsonCat = jsonCategories.getJSONObject(i);
				DocumentationLinkCategory cat = new DocumentationLinkCategory();
				cat.name = jsonCat.getString("name");
				
				JSONArray jsonItems = jsonCat.getJSONArray("items");
				for (int j = 0; j < jsonItems.length(); ++j) {
					JSONObject jsonItem = jsonItems.getJSONObject(j);
					DocumentationLink link = new DocumentationLink();
					link.title = jsonItem.getString("title");
					link.url = jsonItem.getString("url");
					cat.links.add(link);
				}
				
				categories.add(cat);
			}

			docLinks.put(key, categories);
		}
		
		obj = json.getJSONObject("redirections");
		keys = obj.keySet();
		for (String key : keys) {
			docLinks.put(key, docLinks.get(obj.getString(key)));
		}
	}
}
