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
package sporemodder.view.editors;

import java.util.Map;

import javafx.collections.MapChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptStream.HyperlinkData;
import sporemodder.file.effects.EffectComponent;
import sporemodder.file.effects.EffectDirectory;
import sporemodder.file.effects.EffectFileElement;
import sporemodder.file.effects.EffectUnit;
import sporemodder.file.effects.ImportEffect;
import sporemodder.file.locale.LocaleUnit;
import sporemodder.view.UserInterface;

public class PfxEditor extends ArgScriptEditor<EffectUnit> {
	
	private static class ExportPair {
		EffectComponent component;
		String exportName;
	}
	
	private static final double TAB_PANE_HEIGHT = 300;
	
	private final TabPane tabPane = new TabPane();
	private final ListView<EffectFileElement> componentsList = new ListView<>();
	private final ListView<ImportEffect> exportsList = new ListView<>();
	private final ListView<ExportPair> importsList = new ListView<>();
	
	private final Pane inspectorPane = new VBox(5);
	private final ScrollPane propertiesContainer = new ScrollPane();
	
	private final EffectUnit effectUnit = new EffectUnit(null);
	
	public PfxEditor() {
		super();
		
		stream = effectUnit.generateStream();
		
		Tab componentsTab = new Tab("Components", componentsList);
    	Tab exportsTab = new Tab("Exports", exportsList);
    	Tab importsTab = new Tab("Imports", importsList);
    	componentsTab.setClosable(false);
    	exportsTab.setClosable(false);
    	importsTab.setClosable(false);
    	
    	tabPane.getTabs().addAll(componentsTab, exportsTab, importsTab);
    	tabPane.getSelectionModel().select(0);
    	tabPane.setPrefHeight(TAB_PANE_HEIGHT);
    	
    	propertiesContainer.setFitToWidth(true);
    	
    	inspectorPane.getChildren().addAll(tabPane, propertiesContainer);
    	VBox.setVgrow(propertiesContainer, Priority.ALWAYS);
    	
    	effectUnit.getElements().addListener((MapChangeListener.Change<? extends EffectFileElement, ? extends Integer> c) -> {
    		fillComponentsList(c.getMap());
    	});
	}
	
	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Spore Effects (.pfx)", "pfx", inspectorPane);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
	
	@Override protected void onHyperlinkAction(HyperlinkData hyperlink) {
		super.onHyperlinkAction(hyperlink);
		
		String[] names;
		
		switch (hyperlink.type) {
		case EffectDirectory.HYPERLINK_FILE:
		case EffectDirectory.HYPERLINK_IMAGEMAP:  //TODO ?
			String[] strs = (String[]) hyperlink.object;
			if (strs.length == 2) {
				strs = new String[] {strs[0], strs[1], null};
			}
			hyperlinkOpenFile((String[]) hyperlink.object);
			break;
			
		case EffectDirectory.HYPERLINK_TEXTURE:
			names = (String[]) hyperlink.object;
			
			if (names[0] != null) {
				hyperlinkOpenFile(HashManager.get().getTypeName(0x02FABF01) + names[0] + '.' + HashManager.get().getTypeName(0x02FAC0B6));
			}
			break;
			
		case EffectDirectory.HYPERLINK_SPLITTER:
		case EffectDirectory.HYPERLINK_MATERIAL:
		case EffectDirectory.HYPERLINK_MAP:
			names = (String[]) hyperlink.object;
			String name = names[1];
			if (names[0] != null) {
				name = names[0] + '!' + name; 
			}
			moveTo(stream.getData().getResource(name));
			break;
			
		case ArgScriptStream.HYPERLINK_COLOR:
		case LocaleUnit.HYPERLINK_LOCALE:
			break;
			
		default:
			if (hyperlink.object instanceof EffectFileElement)
				moveTo((EffectFileElement) hyperlink.object);
		}
	}
	
	private void moveTo(EffectFileElement element) {
		if (element != null) {
			int position = stream.getData().getPosition(element);
			if (position != -1) {
				getCodeArea().moveTo(position);
				getCodeArea().requestFollowCaret();
			}
		}
	}
	
	private void fillComponentsList(Map<? extends EffectFileElement, ? extends Integer> map) {
		componentsList.getItems().setAll(map.keySet());
	}
	
	@Override protected void onStreamParse() {
		effectUnit.reset();
	}
}
