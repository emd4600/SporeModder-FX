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
package sporemodder.file.spui.components;

import java.util.List;

import javafx.event.Event;
import javafx.scene.paint.Color;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.file.spui.StyleSheetInstance;

public interface IWindow {
	
	public static final int STATE_FLAG_CLICKED = 2;
	public static final int STATE_FLAG_SELECTED = 4;  // this is for buttonFlags, but we can use it here as well
	public static final int STATE_FLAG_HOVER = 8;
	
	public static final int FLAG_VISIBLE = 0x00000001;
	public static final int FLAG_ENABLED = 0x00000002;
	public static final int FLAG_IGNORE_MOUSE = 0x00000010;
	public static final int FLAG_CLICK_TO_FRONT = 0x00000004;
	public static final int FLAG_ALWAYS_IN_FRONT = 0x00000040;
	public static final int FLAG_CLIP = 0x00000400;
	public static final int FLAG_SHOW_TEXT = 8;
	
	public boolean handleEvent(SpuiViewer viewer, Event event);
	
	public int getFlags();
	public void setFlag(int flag, boolean value);

	public SPUIRectangle getArea();
	public SPUIRectangle getRealArea();
	
	public LocalizedText getCaption();
	public void setCaption(LocalizedText text);
	
	public Color getFillColor();
	public void setFillColor(Color color);
	
	public IDrawable getFillDrawable();
	public void setFillDrawable(IDrawable drawable);
	
	public Color getShadeColor();
	public void setShadeColor(Color color);
	
	public StyleSheetInstance getTextFont();
	public void setTextFont(StyleSheetInstance textFont);
	
	public List<IWinProc> getWinProcs();
	public List<IWindow> getChildren();
	
	public IWindow getParent();

	public int getState();
	public void setState(int state);
}
