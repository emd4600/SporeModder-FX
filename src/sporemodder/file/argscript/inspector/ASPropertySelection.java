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
package sporemodder.file.argscript.inspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;
import sporemodder.file.DocumentStructure.DocumentFragment;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.util.inspector.InspectorRadioButton;
import sporemodder.util.inspector.InspectorUnit;
import sporemodder.util.inspector.InspectorValue;

public class ASPropertySelection<T> {
	
	private String title;
	private String descriptionCode;
	
	private InspectorUnit<ArgScriptStream<T>> inspector;
	private DocumentFragment parentFragment;
	
	/** The fragment that contains the selected property, or null if no property is selected. */
	private DocumentFragment fragment;
	
	private InspectorRadioButton selectedButton;

	private VBox internalPane;
	
	private final ToggleGroup toggleGroup = new ToggleGroup();
	
	private final List<String[]> keywords = new ArrayList<String[]>();
	private final List<InspectorRadioButton> buttons = new ArrayList<InspectorRadioButton>();
	
	/** Assigns to every button the property group it belongs to. */
	private final HashMap<InspectorRadioButton, ASPropertyGroup> buttonToGroup = new HashMap<InspectorRadioButton, ASPropertyGroup>();
	
	public ASPropertySelection(InspectorUnit<ArgScriptStream<T>> inspector, DocumentFragment parentFragment, String title, String descriptionCode) {
		this.inspector = inspector;
		this.parentFragment = parentFragment;
		this.title = title;
		this.descriptionCode = descriptionCode;
		
		initializeUI();
	}
	
	private void initializeUI() {
		internalPane = new VBox();
		
		TitledPane titledPane = new TitledPane();
		titledPane.setContent(internalPane);
		if (title != null) {
			titledPane.setText(title);
		}
		
		if (descriptionCode != null) {
			new InspectorValue(null, DocumentationManager.get().getDocumentation(descriptionCode)).generateUI(internalPane);
		}
		
		inspector.add(titledPane);
	}
	
	public void add(ASPropertyGroup group, String title, String text, String ... keywords) {
		
		InspectorRadioButton rb = new InspectorRadioButton(title, false, toggleGroup,  null,
		(event) -> {
			if (fragment != null) inspector.setSelectedFragment(fragment);
		});
		
		rb.setSelectionListener((obs, oldValue, newValue) -> {
			if (newValue.booleanValue()) {
				
				if (fragment == null) {
					fragment = ArgScriptInspector.insertLine(inspector, parentFragment, text);
				}
				else {
					ASLineInspector<T> lineInspector = new ASLineInspector<T>(inspector, fragment, text);
					
					// 1. Get the previous group
					ASPropertyGroup previousGroup = buttonToGroup.get(selectedButton);
					
					// 2. Only keep arguments/options if the groups are equal and not null
					if (group != null && previousGroup == group) {
						
						ArgScriptLine currentLine = ArgScriptInspector.toLine(inspector, fragment);
						
						int first = group.getFirstCommonArgument();
						int common = group.getNumberOfCommonArguments();
						
						ArgScriptArguments args = new ArgScriptArguments();
						if (currentLine.getArguments(args, first + common, Integer.MAX_VALUE)) {
							
							List<String> splits = lineInspector.getSplits();
							
							for (int i = 0; i < common; i++) {
								splits.set(1 + first + i, args.get(first + i));
							}
							
							for (String commonOption : group.getOptions()) {
								if (currentLine.getOptionArguments(args, commonOption, 0, Integer.MAX_VALUE)) {
									
									splits.add("-" + commonOption);
									splits.addAll(args.get());
								}
							}
						}
					}
					
					lineInspector.submitText();
				}
				
				selectedButton = rb;
				
				inspector.getTextEditor().applySyntaxHighlighting();
			}
		});
		
		this.keywords.add(keywords);
		buttonToGroup.put(rb, group);
		buttons.add(rb);
	}
	
	private void findFragment() {
		fragment = null;
		
		for (DocumentFragment f : parentFragment.children) {

			String fragmentKeyword = ArgScriptInspector.getKeyword(f); 
			for (int i = 0; i < keywords.size(); i++) {
				for (String keyword : keywords.get(i)) {
					if (keyword.equalsIgnoreCase(fragmentKeyword)) {
						
						fragment = f;
						selectedButton = buttons.get(i);
						break;
					}
				}
			}
		}
	}

	public void generateUI() {
		findFragment();
		
		if (fragment != null) {
			selectedButton.setSelected(true);
		}
		
		for (InspectorRadioButton rb : buttons) {
			rb.generateUI(internalPane);
		}
		
		RadioButton rb = new RadioButton("None");
		rb.setToggleGroup(toggleGroup);
		rb.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue.booleanValue() && fragment != null) {
				
				ArgScriptInspector.removeLine(inspector, fragment);
				
				inspector.getTextEditor().applySyntaxHighlighting();
				
				fragment = null;
				selectedButton = null;
			}
		});
		
		internalPane.getChildren().add(rb);
	}
}
