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
import java.util.Map;

import javafx.scene.control.Tooltip;
import sporemodder.file.dbpf.DBPFUnpackingTask.DBPFItemFilter;

public abstract class ProjectPreset {

	private String name;
	private String description;
	private final String[] projectNames;
	private boolean isRecommendable;
	private DBPFItemFilter itemFilter;
	
	public ProjectPreset(String name, String description, boolean isRecommendable, DBPFItemFilter itemFilter, String ... projectNames) {
		super();
		this.name = name;
		this.description = description;
		this.projectNames = projectNames;
		this.isRecommendable = isRecommendable;
		this.itemFilter = itemFilter;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public DBPFItemFilter getItemFilter() {
		return itemFilter;
	}
	
	public Tooltip buildTooltip() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(description);
		sb.append('\n');
		sb.append("The following packages are included:\n");
		for (int i = 0; i < projectNames.length; i++) {
			if (i != 0) sb.append('\n');
			
			sb.append(" - ");
			sb.append(projectNames[i]);
		}
		
		return new Tooltip(sb.toString());
	}
	
	public String[] getProjectNames() {
		return projectNames;
	}
	
	public abstract void getFiles(Map<String, File> files);
	
	public boolean isRecommendable() {
		return isRecommendable;
	}
}
