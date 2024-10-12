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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import javafx.scene.control.Alert;
import sporemodder.file.cnv.CnvUnit;
import sporemodder.util.NameRegistry;

/**
 * A class used to control hashes (hexadecimal 32-bit integers used as IDs) and everything related to them,
 * including name registries, name parsing, etc
 */
public class HashManager extends AbstractManager {
	
	/**
	 * Returns the current instance of the HashManager class.
	 */
	public static HashManager get() {
		return MainApp.get().getHashManager();
	}

	/** The symbols used to print floating point values. This decides the decimal separator: we must always use '.' to avoid language problems. */
	private DecimalFormatSymbols decimalSymbols;
	/** The format that decides the number of decimals used to print floating point values. */
	private String decimalFormat;
	/** The default decimal format object used to print float values. */
	private DecimalFormat defaultDecimalFormat;
	
	/** The original registry used to look for instance and group IDs; it is read from reg_file.txt */
	private final NameRegistry originalFileRegistry = new NameRegistry(this, "File Names", "reg_file.txt");
	/** The original registry used to look for type IDs; it is read from reg_type.txt */
	private final NameRegistry originalTypeRegistry = new NameRegistry(this, "Types", "reg_type.txt");
	/** The original registry used to look for property IDs; it is read from reg_property.txt */
	private final NameRegistry originalPropRegistry = new NameRegistry(this, "Properties", "reg_property.txt");
	
	/** The registry used to look for simulator attribute IDs; it is read from reg_simulator.txt */
	private final NameRegistry simulatorRegistry = new NameRegistry(this, "Simulator Attributes", "reg_simulator.txt");
	
	/** The registry used to look for instance and group IDs; it is read from reg_file.txt */
	private NameRegistry fileRegistry = originalFileRegistry;
	/** The registry used to look for type IDs; it is read from reg_type.txt */
	private NameRegistry typeRegistry = originalTypeRegistry;
	/** The registry used to look for property IDs; it is read from reg_property.txt */
	private NameRegistry propRegistry = originalPropRegistry;
	
	/** A temporary registry that keeps names used by a certain Project. This is only updated when doing certain actions, like packing the mod. */
	private final NameRegistry projectRegistry = new NameRegistry(this, "Names used by the project", "names.txt");
	private boolean updateProjectRegistry;
	
	/** A temporary registry that keeps track of all types/properties names used; can be used to import old projects without losing information. */
	private NameRegistry extraRegistry;
	
	private final HashMap<String, NameRegistry> registries = new HashMap<String, NameRegistry>();
	
	@Override
	public void initialize(Properties properties) {
		decimalSymbols = new DecimalFormatSymbols(Locale.getDefault());
		decimalSymbols.setDecimalSeparator('.');
		decimalFormat = "#.#######";
		defaultDecimalFormat = new DecimalFormat(decimalFormat, decimalSymbols);
		defaultDecimalFormat.setNegativePrefix("-");

		try {
			fileRegistry.read(PathManager.get().getProgramFile(fileRegistry.getFileName()));
		} catch (Exception e) {
			UIManager.get().setInitializationError("The file name registry (reg_file.txt) is corrupt or missing.");
		}
		try {
			typeRegistry.read(PathManager.get().getProgramFile(typeRegistry.getFileName()));
		} catch (Exception e) {
			UIManager.get().setInitializationError("The types registry (reg_type.txt) is corrupt or missing.");
		}
		try {
			propRegistry.read(PathManager.get().getProgramFile(propRegistry.getFileName()));
		} catch (Exception e) {
			UIManager.get().setInitializationError("The property registry (reg_property.txt) is corrupt or missing.");
		}
		try {
			simulatorRegistry.read(PathManager.get().getProgramFile(simulatorRegistry.getFileName()));
			simulatorRegistry.read(PathManager.get().getProgramFile("reg_simulator_stub.txt"));
		} catch (Exception e) {
			UIManager.get().setInitializationError("The simulator attributes registry (reg_simulator.txt or reg_simulator_stub.txt) is corrupt or missing.");
		}
		
		CnvUnit.loadNameRegistry();
		
		registries.put(fileRegistry.getFileName(), fileRegistry);
		registries.put(typeRegistry.getFileName(), typeRegistry);
		registries.put(propRegistry.getFileName(), propRegistry);
		registries.put(simulatorRegistry.getFileName(), simulatorRegistry);
		registries.put(projectRegistry.getFileName(), projectRegistry);
	}
	
	public void replaceRegistries(NameRegistry file, NameRegistry prop, NameRegistry type) {
		fileRegistry = file == null ? originalFileRegistry : file;
		propRegistry = prop == null ? originalPropRegistry : prop;
		typeRegistry = type == null ? originalTypeRegistry : type;
	}
	
	public NameRegistry getRegistry(String fileName) {
		return registries.get(fileName);
	}
	
	public NameRegistry getRegistryByDescription(String description) {
		for (NameRegistry reg : registries.values()) {
			if (reg.getDescription().equals(description)) {
				return reg;
			}
		}
		return null;
	}
	
	public NameRegistry getFileRegistry() {
		return fileRegistry;
	}

	public NameRegistry getTypeRegistry() {
		return typeRegistry;
	}

	public NameRegistry getPropRegistry() {
		return propRegistry;
	}

	public NameRegistry getSimulatorRegistry() {
		return simulatorRegistry;
	}

	public NameRegistry getProjectRegistry() {
		return projectRegistry;
	}

	public boolean mustUpdateProjectRegistry() {
		return updateProjectRegistry;
	}
	
	public void setUpdateProjectRegistry(boolean value) {
		this.updateProjectRegistry = value;
	}

	public NameRegistry getExtraRegistry() {
		return extraRegistry;
	}

	public void setExtraRegistry(NameRegistry extraRegistry) {
		this.extraRegistry = extraRegistry;
	}

	/**
	 * Returns the string that represents this float, using the default pattern: 7 decimals of precision.
	 * @param value The value that will be turned into a string.
	 */
	public String floatToString(float value) {
		if (Float.isNaN(value)) return "NaN";
		return defaultDecimalFormat.format(value);
	}
	
	/**
	 * Returns the string that represents this float, using the format pattern given.
	 * @param value The value that will be turned into a string.
	 * @param format The pattern that will be parsed by DecimalFormat.
	 */
	public String floatToString(float value, String format) {
		return new DecimalFormat(format, decimalSymbols).format(value);
	}
	
	/**
	 * Returns the string that represents this float, using the default pattern: 7 decimals of precision.
	 * @param value The value that will be turned into a string.
	 */
	public String doubleToString(double value) {
		return defaultDecimalFormat.format(value);
	}
	
	/**
	 * Returns the string that represents this float, using the format pattern given.
	 * @param value The value that will be turned into a string.
	 * @param format The pattern that will be parsed by DecimalFormat.
	 */
	public String doubleToString(double value, String format) {
		return new DecimalFormat(format, decimalSymbols).format(value);
	}
	
	/**
	 * Calculates the 32-bit FNV hash used by Spore for the given string.
	 * It is case-insensitive: the string is converted to lower-case before calculating the hash.
	 * @param string The string whose hash will be calculated.
	 * @return The equivalent hash.
	 */
	public int fnvHash(String string) {
		char[] lower = string.toLowerCase().toCharArray();
        int rez = 0x811C9DC5;
        for (int i = 0; i < lower.length; i++) {
        	rez *= 0x1000193;
        	rez ^= lower[i];
        }
        return rez;
	}
	
	/**
	 * Returns a string formatted like <code>0xXXXXXXXX</code>, replacing the X with the hexadecimal
	 * representation of the given integer. For example, the number 7234234 would return <code>0x006e62ba</code>.
	 * @param num The integer that will be converted into an hexadecimal string.
	 * @return
	 */
	public String hexToString(int num) {
		return "0x" + String.format("%8s", Integer.toHexString(num)).replace(' ', '0');
	}
	
	/**
	 * Same as {@link #hexToString(int)}, but this gives the hexadecimal number in uppercase letters.
	 * @param num The integer that will be converted into an hexadecimal string.
	 * @return
	 */
	public String hexToStringUC(int num) {
		return "0x" + String.format("%8s", Integer.toHexString(num).toUpperCase()).replace(' ', '0');
	}
	
	/**
	 * Returns a string formatted like <code>0xXXXXXXXXXXXXXXXX</code>, replacing the X with the hexadecimal
	 * representation of the given 64-bit integer. For example, the number 7234234 would return <code>0x00000000006e62ba</code>.
	 * @param num The long that will be converted into an hexadecimal string.
	 * @return
	 */
	public String hexToString(long num) {
		return "0x" + String.format("%16s", Long.toHexString(num)).replace(' ', '0');
	}
	
	/**
	 * Same as {@link #hexToString(int)}, but this gives the hexadecimal number in uppercase letters.
	 * @param num The integer that will be converted into an hexadecimal string.
	 * @return
	 */
	public String hexToStringUC(long num) {
		return "0x" + String.format("%16s", Long.toHexString(num).toUpperCase()).replace(' ', '0');
	}
	
	/**
	 * Returns the equivalent 8-bit signed integer parsed from the given string. The following formats are allowed:
	 *  <li><code>53</code>: It is parsed as a decimal number, so 53 is returned.
	 *  <li><code>0xba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so -70 is returned.
	 *  <li><code>#ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so -70 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 * @param str The string to decode into a number.
	 * @return The equivalent 8-bit signed integer (<code>int8</code>).
	 */
	public byte int8(String str) {
		byte result = 0;
		
		if (str.startsWith("0x")) {
			result = (byte) (Short.parseShort(str.substring(2), 16) & 0xFF);
		}
		else if (str.startsWith("#")) {
			result = (byte) (Short.parseShort(str.substring(1), 16) & 0xFF);
		}
		else if (str.endsWith("b")) {
			result = (byte) (Short.parseShort(str.substring(0, str.length() - 1), 2) & 0xFF);
		}
		else {
			result = Byte.parseByte(str);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 8-bit unsigned integer parsed from the given string. The following formats are allowed:
	 *  <li><code>53</code>: It is parsed as a decimal number, so 53 is returned.
	 *  <li><code>0xba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 186 is returned.
	 *  <li><code>#ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 186 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 * @param str The string to decode into a number.
	 * @return The equivalent 8-bit unsigned integer (<code>uint8</code>).
	 */
	public short uint8(String str) {
		short result = 0;
		
		if (str.startsWith("0x")) {
			result = (short) (Short.parseShort(str.substring(2), 16) & 0xFF);
		}
		else if (str.startsWith("#")) {
			result = (short) (Short.parseShort(str.substring(1), 16) & 0xFF);
		}
		else if (str.endsWith("b")) {
			result = (short) (Short.parseShort(str.substring(0, str.length() - 1), 2) & 0xFF);
		}
		else {
			result = (short) (Short.parseShort(str) & 0xFF);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 16-bit signed integer parsed from the given string. The following formats are allowed:
	 *  <li><code>5340</code>: It is parsed as a decimal number, so 5340 is returned.
	 *  <li><code>0x00ba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 186 is returned.
	 *  <li><code>#ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 186 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 * @param str The string to decode into a number.
	 * @return The equivalent 16-bit signed integer (<code>int16</code>).
	 */
	public short int16(String str) {
		short result = 0;
		
		if (str.startsWith("0x")) {
			result = (short) (Integer.parseInt(str.substring(2), 16) & 0xFFFF);
		}
		else if (str.startsWith("#")) {
			result = (short) (Integer.parseInt(str.substring(1), 16) & 0xFFFF);
		}
		else if (str.endsWith("b")) {
			result = (short) (Integer.parseInt(str.substring(0, str.length() - 1), 2) & 0xFFFF);
		}
		else {
			result = Short.parseShort(str);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 16-bit unsigned integer parsed from the given string. The following formats are allowed:
	 *  <li><code>-8900</code>: It is parsed as a decimal number, so -8900 is returned.
	 *  <li><code>0xba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 186 is returned.
	 *  <li><code>#ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 186 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 * @param str The string to decode into a number.
	 * @return The equivalent 16-bit unsigned integer (<code>uint16</code>).
	 */
	public int uint16(String str) {
		int result = 0;
		
		if (str.startsWith("0x")) {
			result = Integer.parseInt(str.substring(2), 16) & 0xFFFF;
		}
		else if (str.startsWith("#")) {
			result = Integer.parseInt(str.substring(1), 16) & 0xFFFF;
		}
		else if (str.endsWith("b")) {
			result = Integer.parseInt(str.substring(0, str.length() - 1), 2) & 0xFFFF;
		}
		else {
			result = Integer.parseInt(str) & 0xFFFF;
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 32-bit signed integer parsed from the given string. The following formats are allowed:
	 *  <li><code>5309</code>: It is parsed as a decimal number, so 5309 is returned.
	 *  <li><code>0x6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 7234234 is returned.
	 *  <li><code>#6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 7234234 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 *  <li><code>$Creature</code>: The hash of '<i>Creature</i>' is returned, using the {@link #getFileHash(String)} method.
	 * @param str The string to decode into a number.
	 * @return The equivalent 32-bit signed integer (<code>int32</code>).
	 */
	public int int32(String str) {
		int result = 0;
		
		if (str == null || str.length() == 0) {
			return 0;
		}
		
		if (str.startsWith("0x")) {
			result = Integer.parseUnsignedInt(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Integer.parseUnsignedInt(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			result = getFileHash(str.substring(1));
		}
		else if (str.endsWith("b")) {
			result = Integer.parseUnsignedInt(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Integer.parseInt(str);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 32-bit unsigned integer parsed from the given string. The following formats are allowed:
	 *  <li><code>5309</code>: It is parsed as a decimal number, so 5309 is returned.
	 *  <li><code>0x6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 7234234 is returned.
	 *  <li><code>#6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 7234234 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 *  <li><code>$Creature</code>: The hash of '<i>Creature</i>' is returned, using the {@link #getFileHash(String)} method.
	 * @param str The string to decode into a number.
	 * @return The equivalent 32-bit unsigned integer (<code>uint32</code>).
	 */
	public int uint32(String str) {
		int result = 0;
		
		if (str == null || str.length() == 0) {
			return 0;
		}
		
		if (str.startsWith("0x")) {
			result = Integer.parseUnsignedInt(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Integer.parseUnsignedInt(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			result = getFileHash(str.substring(1));
		}
		else if (str.endsWith("b")) {
			result = Integer.parseUnsignedInt(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Integer.parseUnsignedInt(str);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 64-bit signed integer parsed from the given string. The following formats are allowed:
	 *  <li><code>5309</code>: It is parsed as a decimal number, so 5309 is returned.
	 *  <li><code>0x6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 7234234 is returned.
	 *  <li><code>#6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 7234234 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 *  <li><code>$Creature</code>: The hash of '<i>Creature</i>' is returned, using the {@link #getFileHash(String)} method.
	 * @param str The string to decode into a number.
	 * @return The equivalent 64-bit signed integer (<code>int64</code>).
	 */
	public long int64(String str) {
		long result = 0;
		
		if (str == null || str.length() == 0) {
			return 0;
		}
		
		if (str.startsWith("0x")) {
			result = Long.parseUnsignedLong(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Long.parseUnsignedLong(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			result = Long.parseUnsignedLong(str.substring(1));
		}
		else if (str.endsWith("b")) {
			result = Long.parseUnsignedLong(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Long.parseLong(str);
		}
		
		return result;
	}
	
	/**
	 * Returns the equivalent 64-bit unsigned integer parsed from the given string. Given that Java's native types only has support for
	 * signed longs, the result will end up being a signed long with the same bytes as the real, unsigned value.The following formats are allowed:
	 *  <li><code>5309</code>: It is parsed as a decimal number, so 5309 is returned.
	 *  <li><code>0x6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>0x</i>, so 7234234 is returned.
	 *  <li><code>#6e62ba</code>: It is parsed as a hexadecimal number ignoring the <i>#</i>, so 7234234 is returned.
	 *  <li><code>b10011</code>: It is parsed as a binary number ignoring the <i>b</i>, so 19 is returned.
	 *  <li><code>$Creature</code>: The hash of '<i>Creature</i>' is returned, using the {@link #getFileHash(String)} method.
	 * @param str The string to decode into a number.
	 * @return The equivalent 64-bit unsigned integer (<code>uint64</code>).
	 */
	public long uint64(String str) {
		long result = 0;
		
		if (str == null || str.length() == 0) {
			return 0;
		}
		
		if (str.startsWith("0x")) {
			result = Long.parseUnsignedLong(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Long.parseUnsignedLong(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			result = Long.parseUnsignedLong(str.substring(1));
		}
		else if (str.endsWith("b")) {
			result = Long.parseUnsignedLong(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Long.parseUnsignedLong(str);
		}
		
		return result;
	}
	
	/**
	 * Calculates what's the appropriate representation for the given int32 value: a plain decimal, an hexadecimal
	 * number, or a name (in case the value is actually a hash). If the last situation happens, the name will be put
	 * inside <code>hash(NAME)</code> as if it was an ArgScript function.
	 * <p>
	 * Internally, this method checks whether the number is within certain bounds considered acceptable for a 
	 * decimal number: [-10000000, 10000000]. In case this fails, the method will check if there is any name available for that
	 * value, and will return it or a hexadecimal representation otherwise.
	 * @param value
	 * @return
	 */
	public String formatInt32(int value) {
		if (value <= -10000000 || value >= 10000000) {
			// Is it a name?
			String str = getFileNameOptional(value);
			
			if (str == null) {
				return hexToString(value);
			}
			else {
				return "hash(" + str + ")";
			}
		}
		else {
			return Integer.toString(value);
		}
	}
	
	/**
	 * Calculates what's the appropriate representation for the given int64 value: a plain decimal or an hexadecimal
	 * number.
	 * <p>
	 * Internally, this method checks whether the number is within certain bounds considered acceptable for a 
	 * decimal number: [-10000000, 10000000]. In case this fails, the method will return it or a hexadecimal representation otherwise.
	 * @param value
	 * @return
	 */
	public String formatInt64(long value) {
		if (value <= -10000000 || value >= 10000000) {
			return hexToString(value);
		}
		else {
			return Long.toString(value);
		}
	}
	
	/**
	 * Same as {@link #formatInt32(int)}, but this method assumes the value is always a number, therefore not checking any name registry.
	 * @param value
	 * @return
	 */
	public String formatNumberInt32(int value) {
		if (value <= -10000000 || value >= 10000000) {
			return hexToString(value);
		}
		else {
			return Integer.toString(value);
		}
	}
	
	/**
	 * Calculates what's the appropriate representation for the given int64 value: a plain decimal, an hexadecimal
	 * number, or a name (in case the value is actually a hash). If the last situation happens, the name will be put
	 * inside <code>hash(NAME)</code> as if it was an ArgScript function.
	 * <p>
	 * Internally, this method checks whether the number is within certain bounds considered acceptable for a 
	 * decimal number: [0, 10000000]. In case this fails, the method will check if there is any name available for that
	 * value, and will return it or a hexadecimal representation otherwise.
	 * @param value
	 * @return
	 */
	public String formatUInt32(long value) {
		if (value >= 10000000) {
			// Is it a name?
			int intValue = (int) (value & 0xFFFFFFFF);
			String str = getFileNameOptional(intValue);
			
			if (str == null) {
				return hexToString(intValue);
			}
			else {
				return "hash(" + str + ")";
			}
		}
		else {
			return Long.toString(value);
		}
	}
	
	
	/**
	 * Returns the string that represents the given hash, taken from the simulator attribute IDs registry (reg_simulator.txt).
	 * @param hash The 32-bit integer hash.
	 */
	public String getSimulatorName(int hash) {
		String str = simulatorRegistry.getName(hash);
		if (str != null) {
			return str;
		} else {
			return hexToStringUC(hash);
		}
	}
	
	/**
	 * Returns the 32-bit integer that represents the hash of the given name, taken from the simulator attribute IDs registry (reg_simulator.txt).
	 * Unlike other registies, the name is expected to be on the registry; if it is not, instead of returning its hash, it will return -1.
	 * <li>If the string begins with <code>0x</code> or <code>#</code>, it will be interpreted as a 8-digit or less hexadecimal number.
	 * <li>If the input string is null or the name is not found in the registry, this method returns -1.
	 */
	public int getSimulatorHash(String name) {
		if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		}
		else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} 
		else {
			Integer i = simulatorRegistry.getHash(name);
			return i == null ? -1 : i;
		}
	}
	
	/**
	 * Returns the string that represents the given hash, taken from the instance and group IDs registry (reg_file.txt).
	 * File hashes are different to the rest because they have support for <i>aliases</i>: Only names that end with the symbol <i>~</i> can be 
	 * assigned to any hash; the rest of the names are always assigned to their equivalent FNV hash.
	 * <li>If the name is not found in the registry, the hexadecimal representation of the hash will be returned, such as <code>0x006E62BA</code>.
	 */
	public String getFileName(int hash) {
		String str = getFileNameOptional(hash);
		if (str != null) {
			return str;
		} else {
			return hexToStringUC(hash);
		}
	}
	
	// Returns null if no name is found
	private String getFileNameOptional(int hash) {
		String str = fileRegistry.getName(hash);
		if (str != null) {
			return str;
		} else {
			return projectRegistry.getName(hash);
		}
	}
	
	/**
	 * Returns the string that represents the given hash, taken from the type IDs registry (reg_type.txt).
	 * <li>If the name is not found in the registry, the hexadecimal representation of the hash will be returned, such as <code>0x006E62BA</code>.
	 */
	public String getTypeName(int hash) {
		String str = typeRegistry.getName(hash);
		if (str == null && extraRegistry != null) {
			str = extraRegistry.getName(hash);
		}
		if (str != null) {
			return str;
		} else {
			return hexToStringUC(hash);
		}
	}
	
	/**
	 * Returns the string that represents the given hash, taken from the property IDs registry (reg_prop.txt).
	 * <li>If the name is not found in the registry, the hexadecimal representation of the hash will be returned, such as <code>0x006E62BA</code>.
	 */
	public String getPropName(int hash) {
		String str = propRegistry.getName(hash);
		if (str == null && extraRegistry != null) {
			str = extraRegistry.getName(hash);
		}
		if (str != null) {
			return str;
		} else {
			return hexToStringUC(hash);
		}
	}
	
	/**
	 * Returns the integer that represents the hash of the given name, taken from the group and instance IDs registry (reg_file.txt).
	 * File hashes are different to the rest because they have support for <i>aliases</i>: Only names that end with the symbol <i>~</i> can be 
	 * assigned to any hash; the rest of the names are always assigned to their equivalent FNV hash.
	 * <li>If the name is not found in the registry, its FNV hash is returned.
	 * <li>If the string begins with <code>0x</code> or <code>#</code>, it will be interpreted as a 8-digit or less hexadecimal number.
	 * <li>If the input string is null, this method returns -1.
	 */
	public int getFileHash(String name) {
		if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} 
		else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} 
		else {
			if (!name.endsWith("~")) {
				int hash = fnvHash(name);
				if (updateProjectRegistry) {
					projectRegistry.add(name, hash);
				}
				return hash;
			} 
			else {
				String lc = name.toLowerCase();
				Integer i = fileRegistry.getHash(lc);
				if (i == null) {
					i = projectRegistry.getHash(lc);
				}
				if (i == null) {
					throw new IllegalArgumentException("Unable to find " + name + " hash.  It does not exist in the reg_file registry.");
				}
				if (updateProjectRegistry) {
					projectRegistry.add(name, i);
				}
				return i;
			}
		}
	}
	
	/**
	 * Returns the integer that represents the hash of the given name, taken from the type IDs registry (reg_type.txt).
	 * <li>If the name is not found in the registry, its FNV hash is returned.
	 * <li>If the string begins with <code>0x</code> or <code>#</code>, it will be interpreted as a 8-digit or less hexadecimal number.
	 * <li>If the input string is null, this method returns -1.
	 */
	public int getTypeHash(String name) {
		if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		}
		else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} 
		else {
			Integer i = typeRegistry.getHash(name);
			if (i == null && extraRegistry != null) {
				extraRegistry.add(name, fnvHash(name));
			}
			return i == null ? fnvHash(name) : i;
		}
	}
	
	/**
	 * Returns the integer that represents the hash of the given name, taken from the property IDs registry (reg_prop.txt).
	 *  <li>If the name is not found in the registry, its FNV hash is returned.
	 *  <li>If the string begins with <code>0x</code> or <code>#</code>, it will be interpreted as a 8-digit or less hexadecimal number.
	 *  <li>If the input string is null, this method returns -1.
	 */
	public int getPropHash(String name) {
		if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} 
		else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} 
		else {
			Integer i = propRegistry.getHash(name);
			if (i == null && extraRegistry != null) {
				extraRegistry.add(name, fnvHash(name));
			}
			return i == null ? fnvHash(name) : i;
		}
	}
}
