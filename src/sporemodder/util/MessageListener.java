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

import sporemodder.MessageManager.MessageType;

/**
 * An utility class used to react to certain events in the program. This functional interface consists of a
 * single method that is called every time a certain event (message) happens in the program. Message listeners
 * must use the {@link MessageManager} class to control which types of messages they can react to; a single
 * listener instance can react to more than one type of message.
 * 
 * The available message types are defined in the {@link MessageManager.MessageType} enumeration. Each message
 * might come with certain parameters that provide information about the event: each message type might use its own parameters.
 * Also some message types will require the listener to return a value.
 */
@FunctionalInterface
public interface MessageListener {

	/**
	 * Called when a message happens. Will only be called if this listener was registered to receive this
	 * type of messages. The <code>args</code> parameter contains information about the message, its
	 * type depends on the type of message.
	 * @param type The type of message received.
	 * @param args The arguments of that message, depends on the type.
	 * @returns The return value depends on the type of message, for most messages it will be ignored so it can be null.
	 */
	public Object handleMessage(MessageType type, Object args);
}
