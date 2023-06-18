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

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;

public abstract class EffectBlockParser<T extends EffectComponent> extends ArgScriptBlock<EffectUnit> {

	protected String name;
	protected T effect;
	
	protected abstract T createEffect(EffectDirectory effectDirectory);
	
	@Override
	public void parse(ArgScriptLine line) {
		if (data.isParsingComponent()) {
			stream.addError(line.createError("You cannot create components inside other components."));
		} else {
			effect = createEffect(stream.getData().getEffectDirectory());
			
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1, 3)) {
				name = args.get(0);
				effect.name = name;
				
				if (stream.getData().hasComponent(name, effect.getFactory().getTypeCode())) {
					stream.addError(line.createErrorForArgument("A '" + effect.getFactory().getKeyword() + "' component with this name already exists in this file.", 0));
				}
				
				if (args.size() > 1) {
					if (args.size() != 3 || !args.get(1).equals(":")) {
						stream.addError(line.createErrorForArgument("Wrong format for effect inheritance. Correct format is `name : parentName`", 2));
					} else {
						String parentName = args.get(2);
						EffectComponent parent = stream.getData().getComponent(parentName, effect.getFactory().getTypeCode());
						if (parent != null) effect.copy(parent);
					}
				}
			}
			
			data.setPosition(effect, stream.getLinePositions().get(stream.getCurrentLine()));
			
			additionalLineParsing(line);
			
			data.setParsingComponent(true);
			stream.startBlock(this);
		}
	}
	
	@Override
	public void onBlockEnd() {
		if (effect != null) stream.getData().addComponent(name, effect);
		data.setParsingComponent(false);
	}
	
	@Override
	public final void setData(ArgScriptStream<EffectUnit> stream, EffectUnit data) {
		super.setData(stream, data);
		
		addParsers();
	}
	
	public abstract void addParsers();
	
	protected void additionalLineParsing(ArgScriptLine line) {
		
	}
}
