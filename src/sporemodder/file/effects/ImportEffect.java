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

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptWriter;

public class ImportEffect extends EffectComponent {
	
	public static final int MASK = 0x7F000000;
	public static final String KEYWORD = "import";

	public ImportEffect(EffectDirectory effectDirectory) {
		super(effectDirectory, 0);
	}

	@Override
	public EffectComponentFactory getFactory() {
		return null;
	}

	@Override
	public void read(StreamReader stream) throws IOException {
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("import").arguments(name);
	}

	@Override
	public void copy(EffectComponent component) {
	}
	
}
