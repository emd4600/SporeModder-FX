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
package sporemodder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sporemodder.UIManager.UILoadMessage;
import sporemodder.util.MessageListener;

public class MessageManager extends AbstractManager {

	public enum MessageType
	{
		/**Called when a user interface is loaded. 
		 * <p>
		 * The <code>args</code> of this message is a {@link UILoadMessage} object that contains the name of the UI loaded,
		 * and the loaded control (cast it to the appropriate type in the <code>sporemodder.view</code> package to use it).
		 */
		OnUILoad,
		
		/** Called when the program settings are being saved. When this message is sent, 
		 * all settings from other managers in the program have already been saved. 
		 * <p>
		 * The <code>args</code> of this message is the {@link Properties} object where settings are saved.
		 */
		OnSettingsSave,
		
		/** Called when the program settings are being loaded. When this message is sent, 
		 * all settings from other managers in the program have already been loaded. 
		 * <p>
		 * The <code>args</code> of this message is the {@link Properties} object that contains the settings.
		 */
		OnSettingsLoad,
		
		/** Called when the settings of a project are being saved. When this message is sent,
		 * all settings from the project have already been saved. 
		 * <p>
		 * The <code>args</code> of this message is
		 * the {@link Project} object that is being saved (you can use {@link Project.getSettings()})
		 */
		OnProjectSettingsSave,
		
		/** Called when the settings of a project are being loaded. When this message is sent,
		 * all settings from the project have already been loaded. 
		 * <p>
		 * The <code>args</code> of this message is
		 * the {@link Project} object that is being loaded (you can use {@link Project.getSettings()})
		 */
		OnProjectSettingsLoad,
		
		/** Called when a file is loaded into an editor. When this message is sent, the editor has already been selected
		 * (use an [{@link EditorFactory} if you want to provide your custom editors). 
		 * <p>
		 * The <code>args</code> of this message is the {@link ProjectItem} that was just loaded. The corresponding editor
		 * can be accessed with {@link EditorManager.getActiveEditor()}
		 */
		OnFileLoad,
		
		/** Called when the active editor changes. 
		 * <p>
		 * The <code>args</code> of this message is the {@link ItemEditor} that is now active.
		 */
		OnEditorSetAsActive,
		
		/** Called when the active editor changes, only if there was an active editor previously.
		 * <p>
		 * The <code>args</code> of this message is the {@link ItemEditor} that is was active before.
		 */
		OnEditorUnsetAsActive,
		
		/** Called when the user attempts to run the game (or when a developer does so programmatically via the 
		 * {@link GameManager}). This message is generated before the game is run.
		 * <p>
		 * The <code>args</code> of this message is the path that will be executed. If the listener returns false,
		 * the operation will be aborted and the game won't be run. 
		 */
		BeforeGameRun,
		
		/** Called when the game is run by using the {@link GameManager}.
		 * <p>
		 * The <code>args</code> of this message is the {@link Process} generated. 
		 */
		OnGameRun,
		
		/** Called just before a DBPF (.package) unpacking operation begins.
		 * <p>
		 * The <code>args</code> of this message is the {@link DBPFUnpackingTask} that will be used to unpack.
		 */
		BeforeDbpfUnpack,
		
		/** Called just after a DBPF (.package) has finished unpacking.
		 * <p>
		 * The <code>args</code> of this message is the {@link DBPFUnpackingTask} that has been used to unpack.
		 */
		OnDbpfUnpack,
		
		/** Called just before a DBPF (.package) packing operation begins.
		 * <p>
		 * The <code>args</code> of this message is the {@link DBPFPackingTask} that will be used to pack.
		 */
		BeforeDbpfPack,
		
		/** Called just after a DBPF (.package) has finished packing. When this message is sent the packer still hasn't closed,
		 * 
		 * <p>
		 * The <code>args</code> of this message is the {@link DBPFPackingTask} that has been used to pack.
		 */
		OnDbpfPack
	}
	
	/**
	 * Returns the current instance of the MessageManager class.
	 */
	public static MessageManager get() {
		return MainApp.get().getMessageManager();
	}
	
	private final Map<MessageType, Set<MessageListener>> listeners = new HashMap<>();
	
	/**
	 * Registers the given listener to receive all messages of the specified type.
	 * A single listener can be registered to more than one message type.
	 * 
	 * @param type The type of message the listener reacts to.
	 * @param listener
	 */
	public void addListener(MessageType type, MessageListener listener) {
		Set<MessageListener> set = listeners.get(type);
		if (set == null) {
			set = new HashSet<MessageListener>();
			listeners.put(type, set);
		}
		set.add(listener);
	}
	
	/**
	 * Makes the given listener to stop receiving messages of the specified type. The listener will still
	 * receive all other messages it is registered to.
	 * 
	 * @param type The type of message the listener won't react to anymore.
	 * @param listener
	 */
	public void removeListener(MessageType type, MessageListener listener) {
		Set<MessageListener> set = listeners.get(type);
		if (set != null) {
			set.remove(listener);
		}
	}
	
	/**
	 * Sends a message, notifying all listeners that are registered to that message type.
	 * The method will return the output of the last listener it executed.
	 * @param type The type of message sent.
	 * @param args An optional arguments object, depends on the type of message.
	 */
	public Object postMessage(MessageType type, Object args) {
		Set<MessageListener> set = listeners.get(type);
		Object result = null;
		if (set != null) for (MessageListener l : set) result = l.handleMessage(type, args);
		return result;
	}
	
	/**
	 * Tells whether there are any message listeners registered to the specified type. 
	 * @param type
	 * @return
	 */
	public boolean hasListeners(MessageType type) {
		Set<MessageListener> set = listeners.get(type);
		return set != null && !set.isEmpty();
	}
}
