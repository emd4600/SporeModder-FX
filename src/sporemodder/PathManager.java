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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * A small class used to control the path where the program is, and merge relative paths accordingly.
 */
public class PathManager extends AbstractManager {

	/** The folder where the program files are. */
	private File programFolder;
	/** The folder where SporeModder projects are. */
	private File projectsFolder;
	
	
	/**
	 * Returns the current instance of the PathManager class.
	 */
	public static PathManager get() {
		return MainApp.get().getPathManager();
	}
	
	
	@Override
	public void initialize(Properties properties) {
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			// First we try to get the program folder from two different sources
			programFolder = new File(System.getProperty("user.dir"));
			if (programFolder == null || !programFolder.exists())
			{
				try {
					programFolder = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath());
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				programFolder = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath());
				System.out.println("programFolder path 1: " + programFolder.getPath());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				
				try {
					String newPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
					System.out.println("newPath: " + (newPath != null) + ", " + newPath);
					programFolder = new File(newPath).getParentFile();
					System.out.println("programFolder path 2: " + programFolder.getPath());
				
				} catch (Exception ex) {
					ex.printStackTrace();
					
					programFolder = new File(System.getProperty("user.dir"));
					System.out.println("programFolder path 3: " + programFolder.getPath());
				}
			}
			
			if (!new File(programFolder, "SporeModderFX.jar").exists()) {
				programFolder = programFolder.getParentFile();
				System.out.println("programFolder path 4: " + programFolder.getPath());
			}
		}
		
		projectsFolder = new File(programFolder, "Projects");
	}
	
	/** Returns the folder where the program files are. */
	public File getProgramFolder() {
		return programFolder;
	}
	
	/** Returns the folder where SporeModder projects are. */
	public File getProjectsFolder() {
		return projectsFolder;
	}
	
	/**
	 * Returns the file that corresponds to the given relative path, in the program folder. For example, if the program is at
	 * "C:\SporeModder" and you provide the relative path "WinMerge\WinMerge.exe", the file "C:\SporeModder\WinMerge\WinMerge.exe"
	 * will be returned.
	 * @param relativePath The path of the file, relative to the program folder.
	 */
	public File getProgramFile(String relativePath) {
		return new File(programFolder, relativePath);
	}
	
	/**
	 * Returns the styling file that needs to be used for the current UI theme.
	 * @param name
	 * @return
	 */
	public File getStyleFile(String name) {
		return getProgramFile("Styles" + File.separatorChar + UIManager.get().getCurrentStyle() + File.separatorChar + name);
	}
}
