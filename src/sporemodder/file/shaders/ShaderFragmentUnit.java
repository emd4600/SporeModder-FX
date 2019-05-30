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
package sporemodder.file.shaders;

import sporemodder.file.argscript.ArgScriptStream;

/**
 * Convenience class used for parsing a single shader fragment file.
 */
public class ShaderFragmentUnit {
	
	private ShaderFragment fragment;
	private String requiredType;
	private boolean isAlreadyParsed;
	
	public ArgScriptStream<ShaderFragmentUnit> generateStream() {
		
		ArgScriptStream<ShaderFragmentUnit> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser(VertexShaderFragment.KEYWORD, new VertexShaderFragment.Parser());
		stream.addParser(PixelShaderFragment.KEYWORD, new PixelShaderFragment.Parser());
		
		return stream;
	}

	public ShaderFragment getFragment() {
		return fragment;
	}

	public void setFragment(ShaderFragment fragment) {
		this.fragment = fragment;
	}
	
	public String getRequiredType() {
		return requiredType;
	}

	public void setRequiredType(String requiredType) {
		this.requiredType = requiredType;
	}
	
	public boolean isAlreadyParsed() {
		return isAlreadyParsed;
	}
	
	public void setAlreadyParsed(boolean value) {
		this.isAlreadyParsed = value;
	}
}
