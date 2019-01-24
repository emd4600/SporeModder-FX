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
import java.util.List;

import javafx.scene.layout.VBox;
import sporemodder.file.argscript.ArgScriptArguments;

public class ASOptionInspector extends ASAbstractOptionInspector {

	/** The text used in the option tag. */
	private String name;
	private final List<ASValueInspector> values = new ArrayList<ASValueInspector>();
	private int optionalIndex = -1;
	
	public ASOptionInspector(String name, ASValueInspector ... values) {
		this.name = name;
		
		for (ASValueInspector value : values) {
			add(value);
		}
	}
	
	public ASOptionInspector setOptionalIndex(int optionalIndex) {
		this.optionalIndex = optionalIndex;
		for (int i = optionalIndex; i < values.size(); i++) {
			values.get(i).setOptional(true);
		}
		return this;
	}
	
	@Override
	public void setLineInspector(ASLineInspector<?> lineInspector, boolean updateArguments) {
		super.setLineInspector(lineInspector, updateArguments);
		
		for (ASValueInspector value : values) {
			value.setLineInspector(lineInspector, this);
		}
		
		if (updateArguments) basicArgumentUpdate();
	}
	
	private void basicArgumentUpdate() {
		args = new ArgScriptArguments();
		if (!lineInspector.getLine().getOptionArguments(args, name, 0, Integer.MAX_VALUE)) {
			// Just a way to know that the option did not exist
			args = null;
		}
	}
	
	@Override
	public void updateArguments() {
		//TODO properly calculate minimum and maximum arguments?
		basicArgumentUpdate();
		
		if (args != null) {
			// Now the arguments were changed, maybe we should remove the option
			boolean isDefault = true;
			for (ASValueInspector value : values) {
				if (!value.isDefault()) {
					isDefault = false;
					break;
				}
			}
			if (isDefault) {
				// Start with 1, the option keyword
				int removeCount = 1;
				for (ASValueInspector value : values) { 
					removeCount += value.getRemoveableCount();
				}
				
				int removeIndex = args.getSplitIndex() - 1;
				for (int i = 0; i < removeCount; i++) {
					lineInspector.getSplits().remove(removeIndex);
				}
				
				// Update the text
				lineInspector.submitText();
				// We have removed the option, so no args
				args = null;
			}
			else {
				// Maybe we do not need to remove the option, but we can remove optional arguments
				if (optionalIndex != -1) {
					isDefault = true;
					int removeCount = 0;
					for (int i = optionalIndex; i < values.size(); i++) {
						removeCount += values.get(i).getRemoveableCount();
						if (!values.get(i).isDefault()) {
							isDefault = false;
							break;
						}
					}
					
					if (isDefault) {
						int removeIndex = 0;
						// Add the count of the non-optional values
						for (int i = 0; i < optionalIndex; i++) {
							removeIndex += values.get(i).getArgumentCount();
						}
						
						// Some values might have returned default, but just because they don't exist
						removeCount = Math.min(removeIndex + removeCount, args.get().size()) - removeIndex;
						
						removeIndex += args.getSplitIndex();
						
						if (removeCount > 0) {
							for (int i = 0; i < removeCount; i++) {
								lineInspector.getSplits().remove(removeIndex);
							}
							
							lineInspector.submitText();
						}
					}
				}
			}
		}
	}
	
	public void add(ASValueInspector valueInspector) {
		// Calculate the argument index
		int argIndex = 0;
		for (ASValueInspector value : values) {
			int argCount = value.getArgumentCount();
			if (argCount == -1) {
				throw new UnsupportedOperationException("Cannot add a value inspector after an indefinite nubmer of arguments.");
			}
			argIndex += argCount;
		}
		
		valueInspector.setLineInspector(lineInspector, this);
		valueInspector.setArgIndex(argIndex);
		values.add(valueInspector);
	}

	@Override
	public void generateUI(VBox pane) {
		for (ASValueInspector valueInspector : values) {
			valueInspector.generateUI(pane);
		}
	}
	
	/**
	 * Writes this option if it doesn't exist yet into the splits, without updating the text editor.
	 * @return The index of the option.
	 */
	@Override
	public int createIfNecessary(ASValueInspector caller) {
		if (args == null) {
			// Arguments weren't found, so create the option. The split index is the current size.
			List<String> splits = lineInspector.getSplits();
			splits.add("-" + name);
			
			int splitIndex = splits.size();
			
			for (ASValueInspector valueInspector : values) {
				valueInspector.addDefaultValue(splits);
			}
			
			return splitIndex;
		}
		else {
			if (caller.isOptional) {
				createOptionalIfNecessary(caller.argIndex);
			}
			
			return args.getSplitIndex();
		}
		
	}
	
	private void createOptionalIfNecessary(int argIndex) {
		if (args.getSize() <= argIndex) {
			for (int i = args.getSize(); i <= argIndex; i++) {
				List<String> newSplits = new ArrayList<String>();
				values.get(i).addDefaultValue(newSplits);
				
				for (int j = 0; j < newSplits.size(); j++) {
					lineInspector.getSplits().add(args.getSplitIndex() + i + j, newSplits.get(j));
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public List<ASValueInspector> getValues() {
		return values;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (ASValueInspector value : values) {
			value.setEnabled(isEnabled);
		}
	}
	
	
}
