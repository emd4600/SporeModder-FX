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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import sporemodder.MessageManager;
import sporemodder.MessageManager.MessageType;
import sporemodder.PathManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.GamePathConfiguration.GamePathType;

public class Project {
	
	public enum PackageSignature {
		NONE {
			@Override public String toString() {
				return "None";
			}
			@Override public String getFileName() {
				return null;
			}
			@Override public InputStream getInputStream() {
				return null;
			}
		},
		PATCH51 {
			@Override public String toString() {
				return "GA Patch 5.1";
			}
			@Override public String getFileName() {
				return "ExpansionPack1";
			}
			@Override public InputStream getInputStream() {
				return Project.class.getResourceAsStream("/sporemodder/resources/ExpansionPack1.prop");
			}
		},
		BOT_PARTS {
			@Override public String toString() {
				return "Bot Parts";
			}
			@Override public String getFileName() {
				return "BoosterPack2";
			}
			@Override public InputStream getInputStream() {
				return Project.class.getResourceAsStream("/sporemodder/resources/BoosterPack2.prop");
			}
		};
		
		public abstract String getFileName();
		public abstract InputStream getInputStream();
	}
	
	public static final String SETTINGS_FILE_NAME = "config.properties";
	private static final String PROPERTY_lastTimeUsed = "lastTimeUsed";
	private static final String PROPERTY_fixedTabPaths = "fixedTabPaths";
	private static final String PROPERTY_sources = "sources";
	private static final String PROPERTY_customPackPath = "packPath";  // for compatibility
	private static final String PROPERTY_packPathType = "packPathType";
	private static final String PROPERTY_packageName = "packageName";
	private static final String PROPERTY_packageSignature = "embeddedEditorPackages";  // for compatibility
	private static final String PROPERTY_isReadOnly = "isReadOnly";
	private static final String PROPERTY_showOnlyModded = "showOnlyModded";

	/** The name of the project, which is taken from the folder name. */
	private String name;
	
	/** The name of the DBPF file generated when packing. */
	private String packageName;

	/** Parent mod bundle that contains this project, if any */
	private ModBundle parentMod;
	
	/** The folder that contains the data of the project. */
	private File folder;
	/** External projects have a file in the Projects folder that links to the real path. */
	private File externalLink;
	
	private final List<Project> references = new ArrayList<>();
	
	/** The object that holds the path to the folder where the project DBPF is packed. */
	private final GamePathConfiguration packPath;
	
	/** A list of relative paths of all those files that are fixed tabs. */
	private final List<String> fixedTabPaths = new ArrayList<>();
	
	/** The embedded 'editorPackages~' file that represents the package signature. */
	private PackageSignature packageSignature = PackageSignature.NONE;
	
	/** Read only projects cannot be packed and its files cannot be directly edited. */
	private boolean isReadOnly;
	
	private final Properties settings = new Properties();
	
	/** Extra properties that can be used by plugins. */
	private Map<String, Object> extraProperties = new HashMap<>();
	
	
	public Project(String name) {
		this(name, new File(PathManager.get().getProjectsFolder(), name), null);
	}
	
	public Project(String name, File folder, File externalLink) {
		this.name = name;
		this.folder = folder;
		this.externalLink = externalLink;
		
		packPath = GamePathConfiguration.useGame();
		
		onNameChanged(null);
	}
	
	public List<Project> getReferences() {
		return references;
	}
	
	private String[] stringListSplit(String propertyName) {
		String property = settings.getProperty(propertyName);
		if (property != null && !property.isEmpty()) {
			String[] splits = property.split("\\|");
			String[] result = new String[splits.length];
			for (int i = 0; i < splits.length; ++i) {
				String str = splits[i];
				result[i] = str.substring(1, str.length()-1);
			}
			return result;
		}
		
		return new String[0];
	}
	
	/**
	 * Loads the project settings from the configuration file inside the project folder.
	 */
	public void loadSettings() {
		File file = new File(folder, SETTINGS_FILE_NAME);
		if (file.exists()) {
			try (InputStream stream = new FileInputStream(file)) {
				settings.load(stream);
				
				ProjectManager projectManager = ProjectManager.get();
				
				String[] sourceNames = stringListSplit(PROPERTY_sources);
				for (String str : sourceNames) {
					Project p = projectManager.getProject(str);
					if (p != null) references.add(p);
				}
				
				String[] tabPaths = stringListSplit(PROPERTY_fixedTabPaths);
				for (String str : tabPaths) {
					fixedTabPaths.add(str);
				}
				
				packageName = settings.getProperty(PROPERTY_packageName);
				if (packageName == null) packageName = getDefaultPackageName(name);
				
				String packPathStr = settings.getProperty(PROPERTY_packPathType);
				if (packPathStr != null) {
					packPath.setCustomPath(settings.getProperty(PROPERTY_customPackPath, ""));
					packPath.setType(GamePathType.valueOf(packPathStr));
				}
				
				String packageSignatureStr = settings.getProperty(PROPERTY_packageSignature, "None");
				for (PackageSignature enumEntry : PackageSignature.values()) {
					if (packageSignatureStr.equals(enumEntry.toString())) {
						packageSignature = enumEntry;
					}
				}
				
				isReadOnly = Boolean.parseBoolean(settings.getProperty(PROPERTY_isReadOnly, "false"));
				
				MessageManager.get().postMessage(MessageType.OnProjectSettingsLoad, this);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getDefaultPackageName(String name) {
		return name.replaceAll("\\s", "_") + ".package";
	}
	
	/**
	 * Saves the project settings into the configuration file inside the project folder.
	 */
	public void saveSettings() {
		try (OutputStream stream = new FileOutputStream(new File(folder, SETTINGS_FILE_NAME))) {
			
			//if (!sources.isEmpty()) {
			// Do this even if it's empty, as we need to update it if sources were removed
			{
				StringBuilder sb = new StringBuilder();
				
				for (int i = 0; i < references.size(); i++) {
					sb.append("\"" + references.get(i).name + "\"");
					if (i != references.size()-1) sb.append("|");
				}
				
				settings.setProperty(PROPERTY_sources, sb.toString());
			}
			if (!fixedTabPaths.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				
				for (int i = 0; i < fixedTabPaths.size(); i++) {
					sb.append("\"" + fixedTabPaths.get(i) + "\"");
					if (i != fixedTabPaths.size()-1) sb.append("|");
				}
				
				settings.setProperty(PROPERTY_fixedTabPaths, sb.toString());
			}
			
			settings.put(PROPERTY_packageName, packageName);
			
			if (packPath.getCustomPath() != null) {
				settings.put(PROPERTY_customPackPath, packPath.getCustomPath());
			}
			settings.put(PROPERTY_packPathType, packPath.getType().toString());
			
			settings.put(PROPERTY_packageSignature, packageSignature.toString());
			
			settings.put(PROPERTY_isReadOnly, Boolean.toString(isReadOnly));
			
			MessageManager.get().postMessage(MessageType.OnProjectSettingsSave, this);
			
			settings.store(stream, null);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
	}
	
	/**
	 * Returns the object that is used to load/store the project settings.
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}
	
	/**
	 * Tells if the project is read-only. Read-only projects cannot be packed and its files cannot be directly edited.
	 * @return
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * Sets whether the project should be read-only. Read-only projects cannot be packed and its files cannot be directly edited.
	 * @param isReadOnly
	 */
	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
	
	/** 
	 * Returns a list of relative paths of all those files that are fixed tabs. 
	 * @return
	 */
	public List<String> getFixedTabPaths() {
		return fixedTabPaths;
	}
	
	public boolean isShowOnlyModded() {
		return Boolean.parseBoolean(settings.getProperty(PROPERTY_showOnlyModded, "False"));
	}
	
	public void setShowOnlyModded(boolean value) {
		settings.setProperty(PROPERTY_showOnlyModded, Boolean.toString(value));
	}

	/**
	 * Returns the last time this project was active in the program, in milliseconds.
	 * @return
	 */
	public long getLastTimeUsed() {
		return Long.parseLong(settings.getProperty(PROPERTY_lastTimeUsed, "-1"));
	}
	
	/**
	 * Updates the last time this project was active in the program, setting it to the current time.
	 */
	public void updateLastTimeUsed() {
		settings.setProperty(PROPERTY_lastTimeUsed, Long.toString(System.currentTimeMillis()));
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	// This method assumes that the name is valid and there aren't any other projects with the name
	private void onNameChanged(String oldName) {
		if (oldName != null && !oldName.equalsIgnoreCase(name)) {
			if (externalLink != null) {
				File newFile = new File(externalLink.getParentFile(), name);
				if (newFile.exists()) {
					throw new IllegalArgumentException("File " + newFile.getAbsolutePath() + " already exists");
				}
				if (!externalLink.renameTo(newFile)) {
					throw new IllegalArgumentException("Could not rename project external link file");
				}
				externalLink = newFile;
			}
			else {
				File newFolder = new File(PathManager.get().getProjectsFolder(), name);
				if (newFolder.exists()) {
					throw new IllegalArgumentException("Folder " + newFolder.getAbsolutePath() + " already exists");
				}
				if (!folder.renameTo(newFolder)) {
					throw new IllegalArgumentException("Could not rename project folder");
				}
				folder.renameTo(newFolder);
				folder = newFolder;
			}
		}
		
		// Keep the package name if it was not generated automatically
		if (oldName == null || packageName == getDefaultPackageName(oldName)) {
			packageName = getDefaultPackageName(name);
		}
	}
	
	/** Gets the name of the project, which is taken from the folder name. */
	public String getName() {
		return name;
	}
	
	/** Changes the name of this project. This also renames the project folder or external link file. */
	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		onNameChanged(oldName);
	}

	public File getFolder() {
		return folder;
	}
	
	/**
	 * Returns the output file (which might not exist) where the project will be packed as a DBPF file.
	 * @return
	 */
	public File getOutputPackage() {
		File folder = packPath.getDataFolder();
		
		// The user did not specify a custom path or it does not exist.
		if (folder == null || !folder.isDirectory()) {
			return null;
		}
		
		return new File(folder, packageName);
	} 
	
	public GamePathConfiguration getPackPath() {
		return packPath;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName == null ? getDefaultPackageName(name) : packageName;
	}

	public PackageSignature getPackageSignature() {
		return packageSignature;
	}

	public void setPackageSignature(PackageSignature packageSignature) {
		this.packageSignature = packageSignature;
	}
	
	/**
	 * Returns a map that can be used by plugins to store extra properties in this Project.
	 * These properties won't be saved/loaded by default; if you want to save/load them, use the {@link MessageManager}
	 * @return
	 */
	public Map<String, Object> getExtraProperties() {
		return extraProperties;
	}

	/** For projects that are not stored in the standard Projects folder, this sets the file that links to the real folder. */
	public void setExternalLinkFile(File linkFile) {
		this.externalLink = linkFile;
	}

	public ModBundle getParentMod() {
		return parentMod;
	}

	public void setParentMod(ModBundle parentMod) {
		this.parentMod = parentMod;
	}
}
