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
package sporemodder.view;

/**
 * A functional interface that is called when some important action is executed, allowing to update UI elements accordingly. Important actions
 * that trigger this are:
 * <li>The user interface is loaded.
 * <li>Changing the current project.
 * <li>Creating/deleting a new project.
 * <li>Saving the program settings.
 * <li>Saving the project settings.
 * <li>Selecting a project item.
 * <li>Saving the active file.
 * <li>Packing a project.
 */
@FunctionalInterface
public interface UIUpdateListener {
	public void onUIUpdate(boolean isFirstUpdate);
}
