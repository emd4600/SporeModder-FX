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

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptStream.HyperlinkData;
import sporemodder.file.effects.EffectComponent;
import sporemodder.file.effects.EffectFileElement;
import sporemodder.file.effects.EffectUnit;
import sporemodder.file.effects.ImportEffect;

public class PfxEditor extends ArgScriptEditor<EffectUnit> {
	
	public static final String HYPERLINK_FILE = "file";
	public static final String HYPERLINK_TEXTURE = "file-texture";
	public static final String HYPERLINK_IMAGEMAP = "file-imagemap";
	public static final String HYPERLINK_LOCALE = "file-locale";
	public static final String HYPERLINK_MATERIAL = "material";
	public static final String HYPERLINK_MAP = "map";
	public static final String HYPERLINK_SPLITTER = "splitter";
	
	public PfxEditor() {
		super();
		
		EffectUnit unit = new EffectUnit(null);
		stream = unit.generateStream();
	}
	
	public static String getHyperlinkType(EffectComponent element) {
		if (element.getFactory() == null) return ImportEffect.KEYWORD;
		else return element.getFactory().getKeyword();
	}
	
	@Override protected void onHyperlinkAction(HyperlinkData hyperlink) {
		String[] names;
		
		switch (hyperlink.type) {
		case HYPERLINK_FILE:
		case HYPERLINK_IMAGEMAP:  //TODO ?
			String[] strs = (String[]) hyperlink.object;
			if (strs.length == 2) {
				strs = new String[] {strs[0], strs[1], null};
			}
			hyperlinkOpenFile((String[]) hyperlink.object);
			break;
			
		case HYPERLINK_TEXTURE:
			names = (String[]) hyperlink.object;
			
			if (names[0] != null) {
				hyperlinkOpenFile(HashManager.get().getTypeName(0x02FABF01) + names[0] + '.' + HashManager.get().getTypeName(0x02FAC0B6));
			}
			break;
			
		case HYPERLINK_SPLITTER:
		case HYPERLINK_MATERIAL:
		case HYPERLINK_MAP:
			names = (String[]) hyperlink.object;
			String name = names[1];
			if (names[0] != null) {
				name = names[0] + '!' + name; 
			}
			moveTo(stream.getData().getResource(name));
			break;
			
		default:
			moveTo((EffectFileElement) hyperlink.object);
		}
	}
	
	private void moveTo(EffectFileElement element) {
		if (element != null) {
			int position = stream.getData().getPosition(element);
			if (position != -1) {
				getCodeArea().moveTo(position);
				getCodeArea().requestFollowCaret();
			}
		}
	}
}
