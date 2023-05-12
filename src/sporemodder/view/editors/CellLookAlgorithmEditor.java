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

import sporemodder.file.cell.CellLookAlgorithmFile;
import sporemodder.view.UserInterface;

public class CellLookAlgorithmEditor extends ArgScriptEditor<CellLookAlgorithmFile> {
	
	public CellLookAlgorithmEditor() {
		super();
		
		CellLookAlgorithmFile unit = new CellLookAlgorithmFile();
		stream = unit.generateStream();
	}
	
	@Override protected void onStreamParse() {
		stream.getData().clear();
	}
	
	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Cell Stage Look Algorithm (LOOK_ALGORITHM)", "LOOK_ALGORITHM", null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
}

