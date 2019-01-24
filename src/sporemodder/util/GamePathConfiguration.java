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
package sporemodder.util;

import java.io.File;

import sporemodder.GameManager;

/**
 * A class that stores common settings relating game paths:
 * <li>Whether to use the Spore, Galactic Adventures or a custom path.
 * <li>The custom path (if any).
 *
 */
public class GamePathConfiguration {

	public enum GamePathType {
		SPORE, 
		GALACTIC_ADVENTURES, 
		CUSTOM
	};
	
	private GamePathType type;
	private String customPath;
	
	public GamePathConfiguration(String customPath) {
		this.type = GamePathType.CUSTOM;
		this.customPath = customPath;
	}
	
	public GamePathConfiguration(GamePathType type) {
		if (type == GamePathType.CUSTOM) {
			throw new IllegalArgumentException("Use the other constructor for custom paths.");
		}
		this.type = type;
	}
	
	public String getCustomPath() {
		return customPath;
	}
	
	public GamePathType getType() {
		return type;
	}
	
	public void setCustomPath(String customPath) {
		this.customPath = customPath;
		this.type = GamePathType.CUSTOM;
	}
	
	public void setType(GamePathType type) {
		this.type = type;
	}
	
	public File getDataFolder() {
		if (type == GamePathType.GALACTIC_ADVENTURES) {
			return GameManager.get().getGalacticAdventures().getDataFolder();
		}
		else if (type == GamePathType.SPORE) {
			return GameManager.get().getSpore().getDataFolder();
		}
		else {
			return customPath == null ? null : new File(customPath);
		}
	}
	
	public File getSporebinFolder() {
		if (type == GamePathType.GALACTIC_ADVENTURES) {
			return GameManager.get().getGalacticAdventures().getSporebinFolder();
		}
		else if (type == GamePathType.SPORE) {
			return GameManager.get().getSpore().getSporebinFolder();
		}
		else {
			return customPath == null ? null : new File(customPath);
		}
	}
	
	public File getInstallFolder() {
		if (type == GamePathType.GALACTIC_ADVENTURES) {
			return GameManager.get().getGalacticAdventures().getInstallFolder();
		}
		else if (type == GamePathType.SPORE) {
			return GameManager.get().getSpore().getInstallFolder();
		}
		else {
			return customPath == null ? null : new File(customPath);
		}
	}
	
	public File getExecutable() {
		if (type == GamePathType.GALACTIC_ADVENTURES) {
			return GameManager.get().getGalacticAdventures().getExecutable();
		}
		else if (type == GamePathType.SPORE) {
			return GameManager.get().getSpore().getExecutable();
		}
		else {
			return customPath == null ? null : new File(customPath);
		}
	}
	
	/**
	 * Creates a new GamePathSettings object that tries to prioritize Galactic Adventures, Spore and finally
	 * custom paths if none of those games are detected.
	 * 
	 * This requires the GameManager, so it must be used after program initialization.
	 */
	public static GamePathConfiguration useGame() {
		GameManager games = GameManager.get();
		
		if (games.hasGalacticAdventures()) {
			return new GamePathConfiguration(GamePathType.GALACTIC_ADVENTURES);
		}
		else if (games.hasSpore()) {
			return new GamePathConfiguration(GamePathType.SPORE);
		}
		else {
			return new GamePathConfiguration((String) null);
		}
	}
}
