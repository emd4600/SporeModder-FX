/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

import java.io.File;

/**
 * This interface represents the syntax highlighting for a certain file format. It has a method that must generate the 
 * style spans (that is, what css styles must be applied to certain parts of text) so that the RichText CodeArea can use it.
 */
public interface SyntaxFormatFactory extends SyntaxFormat {
	/**
	 * Tells whether this syntax highlighting must be used in the given file.
	 * @param file
	 * @return Whether the file can be viewed with this syntax highlighting.
	 */
	public boolean isSupportedFile(File file);
}
