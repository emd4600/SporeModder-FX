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

package sporemodder.util;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

/**
 * The interface that all plugins must implement. It's the base class that defines the plugin functionality.
 */
public interface SporeModderPlugin extends Plugin {
	
	/**
	 * Returns a list with the path to all .css stylesheet files provided in this plugin, or null if there are none.
	 */
	public List<String> getStylesheets();
	
	/**
	 * Called after all the program managers have been initialized, but just before the program UI is shown.
	 */
	public void initialize();
	
	/**
	 * Called when the program is closed, all program managers are still available at this time.
	 */
	public void dispose();
}
