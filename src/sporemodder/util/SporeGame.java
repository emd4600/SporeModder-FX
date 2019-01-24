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

import java.io.File;

/**
 * This class represents one of the installed Spore games (Spore, Galactic Adventures, C&C), keeping track
 * of the path where the game is installed, the Data folder path, and the executable path.
 */
public class SporeGame {

	private File installFolder;
	private File dataFolder;
	private File sporebinFolder;
	
	public File getInstallFolder() {
		return installFolder;
	}
	
	public File getDataFolder() {
		return dataFolder;
	}
	
	public File getSporebinFolder() {
		return sporebinFolder;
	}

	public void setInstallFolder(File installFolder) {
		this.installFolder = installFolder;
	}

	public void setDataFolder(File dataFolder) {
		this.dataFolder = dataFolder;
	}

	public void setSporebinFolder(File sporebinFolder) {
		this.sporebinFolder = sporebinFolder;
	}
	
	public File getExecutable() {
		if (sporebinFolder == null) return null;
		else return new File(sporebinFolder, "SporeApp.exe");
	}
	
}
