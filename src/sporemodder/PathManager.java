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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

/**
 * A small class used to control the path where the program is, and merge relative paths accordingly.
 */
public class PathManager extends AbstractManager {

	/** The folder where the program files are. */
	private File programFolder;
	/** The folder where SporeModder projects are. */
	private File projectsFolder;
	private boolean isDefaultProjectsFolder;
	/** Path that will get saved as the Projects folder */
	public String nextProjectsFolder;

	private static final String PROPERTY_projectsPath = "projectsFolderPath";
	
	
	/**
	 * Returns the current instance of the PathManager class.
	 */
	public static PathManager get() {
		return MainApp.get().getPathManager();
	}
	
	
	@Override
	public void initialize(Properties properties) {
		String protocol = PathManager.class.getResource("PathManager.class").getProtocol();
		if (Objects.equals(protocol, "jar")) {
		    // run in jar
			// First we try to get the program folder from two different sources
			//programFolder = new File(System.getProperty("user.dir"));
			try {
				programFolder = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (programFolder == null || !programFolder.exists())
			{
				try {
					programFolder = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath());
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (programFolder == null || !programFolder.exists()) {
				programFolder = new File(System.getProperty("user.dir"));
			}
		} else if (Objects.equals(protocol, "file")) {
		    // run in ide
			programFolder = new File(System.getProperty("user.dir"));
		}
	}

	public void loadSettings(Properties properties) {
		String path = properties.getProperty(PROPERTY_projectsPath);
		if (path != null) {
			projectsFolder = new File(path);
			if (!Files.isDirectory(projectsFolder.toPath())) {
				UIManager.get().setInitializationError("The path to the projects folder does not exist: " + path);
				projectsFolder = null;
			}
		}
		if (projectsFolder == null) {
			projectsFolder = getDefaultProjectsFolder();
		}
		// Check if the custom path points to the same folder
		try {
			isDefaultProjectsFolder = Files.isSameFile(projectsFolder.toPath(), getDefaultProjectsFolder().toPath());
		} catch (IOException e) {
			e.printStackTrace();
			isDefaultProjectsFolder = path == null || path.isBlank();
		}
	}

	@Override public void saveSettings(Properties properties) {
		if (nextProjectsFolder != null) {
			// non null but blank means reset to default
			if (nextProjectsFolder.isBlank()) {
				properties.remove(PROPERTY_projectsPath);
			} else {
				properties.put(PROPERTY_projectsPath, nextProjectsFolder);
			}
		} else if (!isDefaultProjectsFolder) {
			properties.put(PROPERTY_projectsPath, projectsFolder.getAbsolutePath());
		}
	}

	public File getDefaultProjectsFolder() {
		return new File(programFolder, "Projects");
	}

	public String getProjectsFolderStringForSettings() {
		if (nextProjectsFolder != null) {
			return nextProjectsFolder;
		} else if (isDefaultProjectsFolder) {
			return "";
		} else {
			return projectsFolder.getAbsolutePath();
		}
	}

	public boolean isDefaultProjectsFolder() {
		return isDefaultProjectsFolder;
	}

	public void setNextProjectsFolder(String path) {
		nextProjectsFolder = path;
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
