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

import emord.filestructures.StructureIgnore;

public abstract class EffectComponent implements EffectFileElement {
	
	@StructureIgnore protected String name;
	@StructureIgnore protected int version;
	@StructureIgnore protected EffectDirectory effectDirectory;
	
	public EffectComponent(EffectDirectory effectDirectory, int version) {
		this.effectDirectory = effectDirectory;
		this.version = version;
	}
	
	@Override public String toString() {
		String name = getName();
		if (name == null) name = super.toString();
		return getFactory().getKeyword() + ' '  + name;
	}
	
	@Override public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public abstract void copy(EffectComponent component);

	public abstract EffectComponentFactory getFactory();
	
	@Override public boolean isEffectComponent() {
		return true;
	}
}
