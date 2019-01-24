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

package sporemodder.view;

import javafx.scene.Node;

/**
 * An interface used by all classes that are the controllers of a FXML layout.
 *
 */
public interface Controller {
	/**
	 * Returns the main node of the layout, which can be used to insert it to other layout elements.  
	 * @return The main node of the layout.
	 */
	public Node getMainNode();
	
	/**
	 * Used for those FXML files that do not have a controller. It's needed in order to get the main node of the layout.
	 */
	public class PlaceholderController implements Controller {
		
		private Node mainNode;
		
		public PlaceholderController(Node mainNode) {
			this.mainNode = mainNode;
		}

		@Override
		public Node getMainNode() {
			return mainNode;
		}
		
	}
}
