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

import java.io.IOException;

import sporemodder.file.shaders.PixelShaderFragment;
import sporemodder.file.shaders.ShaderFragmentUnit;
import sporemodder.file.shaders.VertexShaderFragment;
import sporemodder.util.ProjectItem;

public class ShaderFragmentEditor extends ArgScriptEditor<ShaderFragmentUnit> {
	
	public ShaderFragmentEditor() {
		super();
		
		ShaderFragmentUnit unit = new ShaderFragmentUnit();
		stream = unit.generateStream();
	}
	
	@Override public void loadFile(ProjectItem item) throws IOException {
		if (item != null) {
			if (item.getName().endsWith(".vertex_fragment")) {
				stream.getData().setRequiredType(VertexShaderFragment.KEYWORD);
			} else {
				stream.getData().setRequiredType(PixelShaderFragment.KEYWORD);
			}
		}
		
		super.loadFile(item);
	}
	
	@Override protected void onStreamParse() {
		stream.getData().setFragment(null);
		stream.getData().setAlreadyParsed(false);
	}
}
