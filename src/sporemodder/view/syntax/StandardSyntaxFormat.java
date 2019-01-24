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
package sporemodder.view.syntax;

import java.util.Collection;

import org.fxmisc.richtext.model.StyleSpans;

public abstract class StandardSyntaxFormat implements SyntaxFormatFactory {
	
	private SyntaxHighlighter highlighter;

	public SyntaxHighlighter getHighlighter() {
		return highlighter;
	}

	public void setHighlighter(SyntaxHighlighter highlighter) {
		this.highlighter = highlighter;
	}

	@Override
	public StyleSpans<Collection<String>> generateStyle(String text) {
		if (highlighter != null) {
			highlighter.setText(text, null);
			return highlighter.generateStyleSpans();
		}
		else {
			return null;
		}
	}
}
