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

import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.effects.EffectFileElement.ElementFactory;

public interface EffectComponentFactory extends ElementFactory {

	public int getMinVersion();
	public int getMaxVersion();
	public String getKeyword();
	
	public void addEffectParser(ArgScriptStream<EffectUnit> stream);
	public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock);
	
	public boolean onlySupportsInline();
	
	public EffectComponent create(EffectDirectory effectDirectory, int version);
	
	public Class<? extends EffectComponent> getComponentClass();
	
//	//TODO
//	default boolean buildInspector(InspectorUnit<ArgScriptStream<EffectUnit>> inspector, DocumentFragment fragment) {
//		return false;
//	}
}
