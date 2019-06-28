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

package sporemodder;

import java.util.Properties;

/**
 * The parent of all manager classes. A manager is a class that is only created once on startup, and that controls specific
 * features of the program. Managers are stored in the MainApp class.
 */
public abstract class AbstractManager {
	
	/**
	 * Called when the program is started. The settings should be read here.
	 * This is called automatically by the main program, developers should never call this.
	 */
	public void initialize(Properties properties) {
		
	}

	/**
	 * Called when the program is closed.
	 * This is called automatically by the main program, developers should never call this.
	 */
	public void dispose() {
		
	}
	
	/**
	 * Called when the program settings are being saved. This must not write any file, just add any properties that have to be saved.
	 * @param properties
	 */
	public void saveSettings(Properties properties) {
		
	}
}
