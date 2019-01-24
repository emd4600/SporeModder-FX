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
package sporemodder.file.effects;

import java.util.HashMap;
import java.util.Map;

import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;

public class EffectUnit {

	/** A map that assigns a name for each effect component in this PFX unit. All components are stored here
	 * regardless of its type, since there cannot be two effect components with the same name. */
	private final Map<String, EffectComponent> components = new HashMap<String, EffectComponent>();
	
	/** A map that assigns a name for each effect resource in this PFX unit. All components are stored here
	 * regardless of its type, since there cannot be two effect resources with the same name. */
	private final Map<String, EffectResource> resources = new HashMap<String, EffectResource>();
	
	/** To avoid throwing exceptions, we instead store the error here and let the user retrieve it if necessary. */
	private String lastError;
	
	/** A map that assigns a visual effect to the name with which the effect is exported. */
	private final Map<String, VisualEffect> exports = new HashMap<String, VisualEffect>();
	
	/** A map that assigns an imported effect to the name with which the effect is exported. */
	private final Map<String, ImportEffect> exportedImports = new HashMap<String, ImportEffect>();
	
	/** Contains the start positions of each one of the components/resources. */
	private final Map<EffectFileElement, Integer> elementPositions = new HashMap<EffectFileElement, Integer>();
	
	/** The current effect that is being parsed (we can only parse one at a time). */
	private VisualEffect currentEffect;
	
	private ArgScriptStream<EffectUnit> stream;
	
	private final EffectDirectory effectDirectory; 
	
	private boolean isParsingComponent;
	
	public EffectUnit(EffectDirectory effectDirectory) {
		this.effectDirectory = effectDirectory;
	}
	
	public EffectDirectory getEffectDirectory() {
		return effectDirectory;
	}
	
	public boolean isParsingComponent() {
		return isParsingComponent;
	}

	public void setParsingComponent(boolean isParsingComponent) {
		this.isParsingComponent = isParsingComponent;
	}

	public Map<String, EffectComponent> getComponents() {
		return components;
	}
	
	public Map<String, EffectResource> getResources() {
		return resources;
	}
	
	public Map<String, VisualEffect> getExports() {
		return exports;
	}
	
	public Map<String, ImportEffect> getExportedImports() {
		return exportedImports;
	}
	
	public int getPosition(EffectFileElement element) {
		Integer pos = elementPositions.get(element);
		return pos == null ? -1 : pos;
	}
	
	public void setPosition(EffectFileElement element, int pos) {
		elementPositions.put(element, pos);
	}
	
	/** 
	 * Returns the last document error that has happened during an operation in this EffectUnit object, if any. 
	 * The document error acts as a replacement for exceptions.
	 * @return
	 */
	public String getLastError() {
		return lastError;
	}
	
	/**
	 * Returns the current VisualEffect that is being processed. You can use this to safely add components
	 * to that effect. Returns null if no effect is currently being parsed.
	 * @return
	 */
	public VisualEffect getCurrentEffect() {
		return currentEffect;
	}
	
	public void setCurrentEffect(VisualEffect effect) {
		this.currentEffect = effect;
	}
	
	/**
	 * Tells whether a component with this name already exists in this PFX unit.
	 * @param name
	 * @return
	 */
	public boolean hasComponent(String name) {
		return components.containsKey(name);
	}
	
	/**
	 * Tells whether a resource with this name already exists in this PFX unit.
	 * @param name
	 * @return
	 */
	public boolean hasResource(String name) {
		return resources.containsKey(name);
	}
	
	/**
	 * Returns the effect component present in this unit that uses this name, ensuring it is of the specified type.
	 * If no component with this name exists, or it does exist but it does not have the correct type, the method returns null;
	 * the error can be retrieved using the {@link #getLastError()} method.
	 * @param name The name of the effect component.
	 * @param type The class of the required effect type, such as <code>SoundEffect.class</code>
	 * @param typeName For error displaying, the name of the component type, such as <i>sound</i>
	 * @return The effect component, or null if there was an error.
	 */
	public EffectComponent getComponent(String name, Class<?> type, String typeName) {
		EffectComponent component = components.getOrDefault(name, null);
		
		if (component == null) {
			lastError = String.format("There is no effect component called '%s' in this PFX unit.", name);
			return null;
		}
		
		if (component.getClass() != type) {
			
			// Special case: import effects are considered visual effects as well
			if (!(type == VisualEffect.class && component instanceof ImportEffect)) {
				lastError = String.format("Effect component '%s' is not a %s component.", name, typeName);
				return null;
			}
		}
		
		return component;
	}
	
	public EffectComponent getComponent(ArgScriptArguments args, int index, Class<?> type, String typeName) {
		EffectComponent component = getComponent(args.get(index), type, typeName);
		if (component == null) {
			args.getStream().addError(new DocumentError(lastError, args.getRealPosition(args.getPosition(index)), args.getRealPosition(args.getEndPosition(index))));
		}
		return component;
	}
	
	/**
	 * Returns the effect resource present in this unit that uses this name, ensuring it is of the specified type.
	 * If no resource with this name exists, or it does exist but it does not have the correct type, the method returns null;
	 * the error can be retrieved using the {@link #getLastError()} method.
	 * @param name The name of the effect resource.
	 * @param type The class of the required resource type, such as <code>MaterialResource.class</code>
	 * @param typeName For error displaying, the name of the resource type, such as <i>material</i>
	 * @return The effect component, or null if there was an error.
	 */
	public EffectResource getResource(String name, Class<?> type, String typeName) {
		EffectResource component = resources.getOrDefault(name, null);
		
		if (component == null) {
			lastError = String.format("There is no effect resource called '%s' in this PFX unit.", name);
			return null;
		}
		
		if (component.getClass() != type) {
			lastError = String.format("Effect resource '%s' is not a %s resource.", name, typeName);
			return null;
		}
		
		return component;
	}
	
	public EffectResource getResource(String name) {
		return resources.get(name);
	}
	
	public EffectResource getResource(ArgScriptArguments args, int index, Class<?> type, String typeName) {
		EffectResource resource = getResource(args.get(index), type, typeName);
		if (resource == null) {
			args.getStream().addError(new DocumentError(lastError, args.getPosition(index), args.getEndPosition(index)));
		}
		return resource;
	}
	
	/**
	 * Adds a new component with the given name.
	 * @param name
	 * @param component
	 */
	public void addComponent(String name, EffectComponent component) {
		components.put(name, component);
	}
	
	/**
	 * Adds a new resource with the given name.
	 * @param name
	 * @param resource
	 */
	public void addResource(String name, EffectResource resource) {
		resources.put(name, resource);
	}
	
	public void addExport(String name, EffectComponent component) {
		if (component.getFactory() == null) {
			exportedImports.put(name, (ImportEffect)component);
		} else {
			exports.put(name, (VisualEffect)component);
		}
	}
	
	/**
	 * Generates the ArgScript stream used to parse PFX units.
	 * @return
	 */
	public ArgScriptStream<EffectUnit> generateStream() {

		stream = new ArgScriptStream<EffectUnit>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		for (int i = 0; i < VisualEffectBlock.APP_FLAGS.size(); ++i) {
			stream.setVariable(VisualEffectBlock.APP_FLAGS.get(i), Integer.toString(i));
		}
		
		stream.setOnStartAction((stream, data) -> {
			data.components.clear();
			data.currentEffect = null;
			data.exports.clear();
			data.lastError = null;
			data.resources.clear();
		});
		
		for (EffectComponentFactory factory : EffectDirectory.getFactories()) {
			if (factory != null) {
				factory.addEffectParser(stream);
			}
		}
		for (EffectResourceFactory factory : EffectDirectory.getResourceFactories()) {
			if (factory != null) {
				factory.addParser(stream);
			}
		}
		
		stream.addParser(ImportEffect.KEYWORD, ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				ImportEffect effect = new ImportEffect(stream.getData().getEffectDirectory());
				effect.setName(args.get(0));
				
				parser.getData().setPosition(effect, stream.getLinePositions().get(stream.getCurrentLine()));
				parser.getData().addComponent(effect.getName(), effect);
			}
		}));
		
		stream.addParser("export", ArgScriptParser.create((parser, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1, 2)) {
				String name = args.get(0);
				String exportName = args.size() == 1 ? name : args.get(1);
				
				EffectComponent component = components.getOrDefault(name, null);
				
				if (component == null) {
					lastError = String.format("There is no effect called '%s' in this PFX unit.", name);
					parser.getStream().addError(line.createErrorForArgument(lastError, 0));
					return;
				}
				
				// Can only export imports and visual effects
				if (component.getFactory() != null && !(component instanceof VisualEffect)) {
					lastError = "Only effects can be exported. Components cannot be exported.";
					parser.getStream().addError(line.createErrorForArgument(lastError, 0));
					return;
				}
				
				parser.getData().addExport(exportName, component);
			}
		}));

		return stream;
	}
}
