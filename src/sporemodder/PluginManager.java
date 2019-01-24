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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import sporemodder.util.SporeModderPlugin;

public class PluginManager extends AbstractManager {

	private final List<SporeModderPlugin> plugins = new ArrayList<SporeModderPlugin>();
	
	/**
	 * Returns the current instance of the PluginManager class.
	 */
	public static PluginManager get() {
		return MainApp.get().getPluginManager();
	}
	
	public List<SporeModderPlugin> getPlugins() {
		return plugins;
	}
	
	@Override
	public void initialize(Properties properties) {
		net.xeoh.plugins.base.PluginManager pm = PluginManagerFactory.createPluginManager();
		
		pm.addPluginsFrom(PathManager.get().getProgramFile("Plugins").toURI());
		
		PluginManagerUtil util = new PluginManagerUtil(pm);
		plugins.clear();
		plugins.addAll(util.getPlugins(SporeModderPlugin.class));
		
		for (SporeModderPlugin plugin : plugins) {
			plugin.initialize();
		}
	}
	
	@Override 
	public void dispose() {
		for (SporeModderPlugin plugin : plugins) {
			plugin.dispose();
		}
	}
}
