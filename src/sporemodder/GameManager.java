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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import sporemodder.util.GamePathConfiguration.GamePathType;
import sporemodder.view.dialogs.GamePathsUI;
import sporemodder.util.SporeGame;
import sporemodder.util.WinRegistry;

/**
 * This manager keeps track of the installed Spore games (Spore, Galactic Adventures, C&C).
 * The user can specifiy, through the program settings, a different path to the executable.
 */
public class GameManager extends AbstractManager {

	public static final String[] RegistryValues = { "InstallLoc", "Install Dir" };

	public static final String[] GARegistryKeys = { "SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE_EP1",
			"SOFTWARE\\Electronic Arts\\SPORE_EP1" };

	public static final String[] SporeRegistryKeys = { "SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE",
			"SOFTWARE\\Electronic Arts\\SPORE" };

	public static final String[] CCRegistryKeys = {
			"SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE(TM) Creepy & Cute Parts Pack",
			"SOFTWARE\\Electronic Arts\\SPORE(TM) Creepy & Cute Parts Pack" };
	
	public static final String SPORE_SPOREBIN = "Sporebin";
	public static final String GA_SPOREBIN = "SporebinEP1";
	
	private static final String PROPERTY_commandLineArgs = "gameCommandLine";  
	private static final String PROPERTY_gameToExecute = "gameToExecute";
	private static final String PROPERTY_pathSpore = "pathSpore";
	private static final String PROPERTY_pathGA = "pathGA";
	private static final String PROPERTY_pathCustom = "pathCustom";
	
	private static final String RegistryDataDir = "DataDir"; // Steam/GoG users don't have InstallLoc nor Install Dir
	
	public static enum GameType { SPORE, GA, CC };

	private boolean isSporeAuto = true;
	private boolean isGAAuto = true;
	private SporeGame spore;
	private SporeGame ga;
	private SporeGame cc;
	
	private File alternativeExecutable;
	
	private String commandLineArgs = "";
	private GamePathType gameToExecute = GamePathType.GALACTIC_ADVENTURES;

	/**
	 * Returns the current instance of the GameManager class.
	 */
	public static GameManager get() {
		return MainApp.get().getGameManager();
	}
	
	/**
	 * Returns the SPORE folder in %appdata%, if it exists, which contains the user-creation data, game saves and galaxy information.
	 * @return
	 */
	public File getAppDataFolder() {
		File appdata = new File(System.getenv("APPDATA"));
		if (appdata.exists()) {
			File result = new File(appdata, "SPORE");
			if (result.exists()) return result;
		}
		return null;
	}

	/**
	 * Returns the user-specified alternative executable. If this value is not null, this
	 * executable is preferred over the other game executables.
	 * @return
	 */
	public File getAlternativeExecutable() {
		return alternativeExecutable;
	}

	/**
	 * Sets the user-specified alternative executable. If this value is not null, this
	 * executable is preferred over the other game executables.
	 * @return
	 */
	public void setAlternativeExecutable(File alternativeExecutable) {
		this.alternativeExecutable = alternativeExecutable;
	}
	
	/**
	 * Returns the user-specified command line arguments that are passed when executing the game.
	 * @return
	 */
	public String getCommandLineArguments() {
		return commandLineArgs;
	}
	
	/**
	 * Sets the user-specified command line arguments that are passed when executing the game.
	 * @param commandLineArgs
	 */
	public void setCommandLineArguments(String commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}
	
	/**
	 * Tells which game should be executed: Spore, Galactic Adventures, or the custom path.
	 * @return GameType.SPORE, GameType.GALACTIC_ADVENTURES or GameType.CUSTOM
	 */
	public GamePathType getGameToExecute() {
		return gameToExecute;
	}
	
	/**
	 * Tells which game should be executed: Spore, Galactic Adventures, or the custom path.
	 * @param gameToExecute
	 */
	public void setGameToExecute(GamePathType gameToExecute) {
		this.gameToExecute = gameToExecute;
	}
	
	/**
	 * If Galactic Adventures is installed and detected, returns that.
	 * Otherwise, returns Spore if it is installed and detected, or null.
	 * @return
	 */
	public SporeGame getGame() {
		if (ga != null) return ga;
		else return spore;
	}

	@Override
	public void initialize(Properties properties) {
		
		commandLineArgs = properties.getProperty(PROPERTY_commandLineArgs, "");
		String gameStr = properties.getProperty(PROPERTY_gameToExecute);
		if ("CUSTOM".equals(gameStr)) {
			gameToExecute = GamePathType.CUSTOM;
		} else if ("SPORE".equals(gameStr)) {
			gameToExecute = GamePathType.SPORE;
		} else if ("GALACTIC_ADVENTURES".equals(gameStr)) {
			gameToExecute = GamePathType.GALACTIC_ADVENTURES;
		}
		
		Map<GameType, SporeGame> map = autoDetectPaths();
		
		ga = map.get(GameType.GA);
		spore = map.get(GameType.SPORE);
		cc = map.get(GameType.CC);
		
		// Read paths from settings
		String path = properties.getProperty(PROPERTY_pathSpore, "AUTO");
		if (!path.equals("AUTO") && !path.isEmpty()) {
			spore = createSpore(path);
			isSporeAuto = spore != null;
		}
		
		path = properties.getProperty(PROPERTY_pathGA, "AUTO");
		if (!path.equals("AUTO") && !path.isEmpty()) {
			ga = createGA(path);
			isGAAuto = ga != null;
		}
		
		path = properties.getProperty(PROPERTY_pathCustom);
		if (path != null && !path.isEmpty()) {
			alternativeExecutable = new File(path);
		}
	}
	
	public SporeGame createSpore(String path) {
		path = moveToSporebin(SPORE_SPOREBIN, path, true);
		if (path == null) return null;
		
		SporeGame spore = new SporeGame();
		
		spore.setSporebinFolder(new File(path));
		spore.setInstallFolder(spore.getSporebinFolder().getParentFile());
		spore.setDataFolder(moveToData(GameType.SPORE, spore.getInstallFolder()));
		
		return spore;
	}
	
	public SporeGame createGA(String path) {
		path = moveToSporebin(GA_SPOREBIN, path, true);
		if (path == null) return null;
		
		SporeGame ga = new SporeGame();
		
		ga.setSporebinFolder(new File(path));
		ga.setInstallFolder(ga.getSporebinFolder().getParentFile());
		ga.setDataFolder(moveToData(GameType.GA, ga.getInstallFolder()));
		
		return ga;
	}
	
	public Map<GameType, SporeGame> autoDetectPaths()  {
		Map<GameType, SporeGame> map = new HashMap<GameType, SporeGame>();
		
		try {
			
			String path = getFromRegistry(GameType.GA);
			if (path != null) {
				map.put(GameType.GA, createGA(path));
			}
			
			path = getFromRegistry(GameType.SPORE);
			if (path != null) {
				map.put(GameType.SPORE, createSpore(path));
			}
			
			path = getFromRegistry(GameType.CC);
			if (path != null) {
				SporeGame cc = new SporeGame();
				map.put(GameType.CC, cc);
				
				cc.setInstallFolder(new File(path));
			}
		} 
		// Just catch all exceptions: executing in Linux throws MethodNotFoundException
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	@Override public void saveSettings(Properties properties) {
		properties.put(PROPERTY_commandLineArgs, commandLineArgs);
		
		String gameStr = "CUSTOM";
		if (gameToExecute == GamePathType.SPORE) gameStr = "SPORE";
		else if (gameToExecute == GamePathType.GALACTIC_ADVENTURES) gameStr = "GALACTIC_ADVENTURES";
		
		properties.put(PROPERTY_gameToExecute, gameStr);
		
		
		properties.put(PROPERTY_pathSpore, (isSporeAuto || spore == null) ? "AUTO" : spore.getInstallFolder().getAbsolutePath());
		properties.put(PROPERTY_pathGA, (isGAAuto || ga == null) ? "AUTO" : ga.getInstallFolder().getAbsolutePath());
		properties.put(PROPERTY_pathCustom, alternativeExecutable == null ? "" : alternativeExecutable.getAbsolutePath());
	}
	
	
	public Process execute(String path, String ... args) throws IOException, URISyntaxException {
		if (path == null) return null;
		
		String[] newArgs = new String[args.length + 1];
		System.arraycopy(args, 0, newArgs, 1, args.length);

		if (new File(path).exists()) {
			newArgs[0] = path;
			return new ProcessBuilder(newArgs).start();
		}
		else {
			// For Steam URIs
			Desktop.getDesktop().browse(new URI(path));
			return null;
		}
	}
	
	public Process runGame() throws IOException, URISyntaxException {
		String path = null;
		
		if (gameToExecute == null || gameToExecute == GamePathType.CUSTOM) {
			path = alternativeExecutable.getAbsolutePath();
		} 
		else if (gameToExecute == GamePathType.GALACTIC_ADVENTURES) {
			if (ga == null) {
				throw new IOException("Cannot execute Galactic Adventures, no path has been specified.");
			}
			path = ga.getExecutable().getAbsolutePath();
		} 
		else if (gameToExecute == GamePathType.SPORE) {
			if (spore == null) {
				throw new IOException("Cannot execute Spore, no path has been specified.");
			}
			path = spore.getExecutable().getAbsolutePath();
		}
		
		if (path == null) return null;
		else {
			if (commandLineArgs == null) return execute(path);
			else return execute(path, commandLineArgs);
		}
	}
	
	public boolean hasSpore() {
		return spore != null;
	}
	
	public boolean hasGalacticAdventures() {
		return ga != null;
	}
	
	public boolean hasCreepyAndCute() {
		return cc != null;
	}
	
	/**
	 * Returns whether there is any game detected or a user-specified executable.
	 * If the value returned is true, it means that at least Spore, Galactic Adventures or the user executable
	 * can be run.
	 * @return
	 */
	public boolean canRunGame() {
		return spore != null || ga != null || alternativeExecutable != null;
	}
	
	public SporeGame getSpore() {
		return spore;
	}
	
	public SporeGame getGalacticAdventures() {
		return ga;
	}
	
	public SporeGame getCreepyAndCute() {
		return cc;
	}
	
	public void setSpore(SporeGame game) {
		spore = game;
		isSporeAuto = false;
	}
	
	public void setGalacticAdventures(SporeGame game) {
		ga = game;
		isGAAuto = false;
	}
	
	public void setCreepyAndCute(SporeGame game) {
		cc = game;
	}

 	private static String getFromRegistry(String[] keys)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {

		String result = null;

		for (String key : keys) {
			for (String value : RegistryValues) {
				result = getRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, key, value);
				if (result != null) {

					return fixPath(result);
				}
			}
		}

		// not found? try with DataDir; some users only have that one
		for (String key : GARegistryKeys) {
			result = getRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, key, RegistryDataDir);
			if (result != null) {

				return fixPath(result);
			}
		}

		return null;
	}

	private static String getRegistryValue(int hkey, String key, String value) {
		try {
			return WinRegistry.valueForKey(hkey, key, value);
		} catch (Exception e) {
			return null;
		}
	}

	private static String getFromRegistry(GameType game)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		if (game == GameType.GA) {
			return getFromRegistry(GARegistryKeys);
		} else if (game == GameType.SPORE) {
			return getFromRegistry(SporeRegistryKeys);
		} else {
			return getFromRegistry(CCRegistryKeys);
		}
	}

	// remove "" if necessary
	private static String fixPath(String path) {
		if (path.startsWith("\"")) {
			return path.substring(1, path.length() - 1);
		} else {
			return path;
		}
	}
	
	public File findInstallationFolder(String sporebinName, File folder) {
		String path = moveToSporebin(sporebinName, folder.getAbsolutePath(), true);
		if (path == null) return null;
		else return new File(path).getParentFile();
	}

	// This method returns the path to the folder that contains the executable
	private static String moveToSporebin(String folderName, String path, boolean bRecursive) {
		if (!path.endsWith("\\")) {
			path += "\\";
		}

		if (new File(path, "SporeApp.exe").exists()) {
			return path;
		}

		if (new File(path, folderName).exists()) {
			return path + folderName + "\\";
		}

		if (bRecursive) {
			return moveToSporebin(folderName, new File(path).getParent(), false);
		}

		return null;
	}

	private static File moveToData(GameType game, File installLoc) {
		if (game == GameType.SPORE) {
			return new File(installLoc, "Data");
		} else if (game == GameType.GA) {
			// Steam and GoG uses DataEP1
			File outputPath = new File(installLoc, "DataEP1");
			if (outputPath.exists()) {
				return outputPath;
			} else {
				return new File(installLoc, "Data");
			}
		} else {
			// Creepy and Cute uses the installation path itself
			return installLoc;
		}
	}
	
	public void showFirstTimeDialog() {
		if (!hasSpore() || !hasGalacticAdventures()) {
			GamePathsUI.show(false);
		}
	}
}
