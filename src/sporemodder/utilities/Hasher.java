package sporemodder.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.util.NameRegistry;
import sporemodder.utilities.names.SimpleNameRegistry;

public class Hasher {
	
	public static SimpleNameRegistry UsedNames = null;
	
	public static NumberFormat getDecimalFormat(String pattern) {
		DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.getDefault());
		decimalSymbol.setDecimalSeparator('.');
		return new DecimalFormat(pattern, decimalSymbol);
	}
	
	/**
	 * Decodes the given String into the byte it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the byte to decode.
	 * @return The byte value of the String str.
	 */
	public static byte decodeByte(String str) {
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
	 * Decodes the given String into the byte it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the byte to decode.
	 * @return The byte value of the String str.
	 */
	public static short decodeUByte(String str) {
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
	 * Decodes the given String into the short it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the short to decode.
	 * @return The short value of the String str.
	 */
	public static short decodeShort(String str) {
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
	 * Decodes the given String into the short it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the short to decode.
	 * @return The short value of the String str.
	 */
	public static int decodeUShort(String str) {
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
			result = (short) (Integer.parseInt(str) & 0xFFFF);
		}
		
		return result;
	}
	
	/**
	 * Decodes the given String into the int it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the int to decode.
	 * @return The int value of the String str.
	 */
	public static int decodeInt(String str) {
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
			//result = Hasher.stringToFNVHash(str.substring(1));
			result = Hasher.getFileHash(str.substring(1));
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
	 * Decodes the given String into the int it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the int to decode.
	 * @return The int value of the String str.
	 */
	public static int decodeUInt(String str) {
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
			//result = Hasher.stringToFNVHash(str.substring(1));
			result = Hasher.getFileHash(str.substring(1));
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
	 * Decodes the given String into the long it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the long to decode.
	 * @return The long value of the String str.
	 */
	public static long decodeLong(String str) {
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
			//result = Hasher.stringToFNVHash(str.substring(1));
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
	 * Decodes the given String into the long it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the long to decode.
	 * @return The long value of the String str.
	 */
	public static long decodeULong(String str) {
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
			//result = Hasher.stringToFNVHash(str.substring(1));
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
	
	public static String validateIntString(String text, int offset, String str) {
		if (str.length() == 0) {
			return str;
		}
		
		StringBuffer sb = new StringBuffer(text);
		sb.insert(offset, str);
		
		String result = sb.toString();
		
		if (result.equals("0x") || result.equals("#")) {
			// we need to be able to write this
			return str;
		}
		
		try {
			decodeInt(sb.toString());
			return str;
		}
		catch (NumberFormatException e) {
			return "";
		}
	}
	
//	public static long decodeUInt(String str) {
//		long result = 0;
//		if (str.startsWith("0x")) {
//			result = Long.parseLong(str.substring(2), 16);
//		}
//		else if (str.startsWith("#")) {
//			result = Long.parseLong(str.substring(1), 16);
//		}
//		else if (str.startsWith("$")) {
//			//result = Hasher.stringToFNVHash(str.substring(1));
//			result = Hasher.getFileHash(str.substring(1)) & 0xFFFFFFFF;
//		}
//		else if (str.endsWith("b")) {
//			result = Long.parseLong(str.substring(0, str.length() - 1), 2);
//		}
//		else {
//			result = Integer.parseUnsignedInt(str) & 0xFFFFFFFFL;
//		}
//		
//		return result;
//	}
	
	public static String fillZeroInHexString(int num) {
		return String.format("%8s", Integer.toHexString(num)).replace(' ', '0');
	}
	
	public static int stringToFNVHash(String inputString) {
		return HashManager.get().fnvHash(inputString);
        /*char[] lower = inputString.toLowerCase().toCharArray();
        int rez = 0x811C9DC5;
        for (int i = 0; i < lower.length; i++) {
        	rez *= 0x1000193;
        	rez ^= lower[i];
        }
        return rez;*/
    }
	
	public static String hashToHex(int hash) {
		return HashManager.get().hexToString(hash);
		//return "#" + fillZeroInHexString(hash).toUpperCase();
	}
	
	public static String hashToHex(int hash, String prefix) {
		return prefix + fillZeroInHexString(hash).toUpperCase();
	}
	
	
	public static String getFileName(int hash) {
		return HashManager.get().getFileName(hash);
		//return getFileName(hash, "#");
	}
	
	public static String getTypeName(int hash) {
		return HashManager.get().getTypeName(hash);
		//return getTypeName(hash, "#");
	}
	
	public static String getPropName(int hash) {
		return HashManager.get().getPropName(hash);
		//return getPropName(hash, "#");
	}
	
	public static String getFileName(int hash, String prefix) {
		String str = HashManager.get().getFileName(hash);
		//String str = HashManager.get().getFileRegistry().getName(hash);
		if (str != null) {
			return str;
		} else {
			if (UsedNames != null) {
				str = UsedNames.getName(hash);
				if (str != null) {
					return str;
				}
			}
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	public static String getTypeName(int hash, String prefix) {
		String str = HashManager.get().getTypeName(hash);
		//String str = HashManager.get().getTypeRegistry().getName(hash);
		if (str != null) {
			return str;
		} else {
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	public static String getPropName(int hash, String prefix) {
		String str = HashManager.get().getPropName(hash);
		//String str = HashManager.get().getPropRegistry().getName(hash);
		if (str != null) {
			return str;
		} else {
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	
	public static int getFileHash(String name) {
		return HashManager.get().getFileHash(name);
		/*if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			if (name.length() == 1) {
				return -1;
			}
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			if (name.length() == 2) {
				return -1;
			}
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			if (!name.endsWith("~")) {
				int hash = stringToFNVHash(name);
				if (UsedNames != null) {
					UsedNames.addAlias(name, hash);
				}
				return hash;
			} else {
				int i = HashManager.get().getFileRegistry().getHash(name);
				if (i == -1) {
					throw new IllegalArgumentException("Unable to find " + name + " hash.  It doesn't exist or it's 0xFFFFFFFF");
				}
				if (UsedNames != null) {
					UsedNames.addAlias(name, i);
				}
				return i;
			}
		}*/
	}
	public static int getTypeHash(String name) {
		return HashManager.get().getTypeHash(name);
		/*if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = HashManager.get().getTypeRegistry().getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}*/
	}
	public static int getPropHash(String name) {
		return HashManager.get().getPropHash(name);
		/*if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = HashManager.get().getPropRegistry().getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}*/
	}
	
	public static String getSPUIName(int hash) {
		return HashManager.get().getSpuiName(hash);
		/*String str = HashManager.get().getSpuiRegistry().getName(hash);
		if (str != null) {
			return str;
		} else {
			return "#" + fillZeroInHexString(hash).toUpperCase();
		}*/
	}
	
	public static int getSPUIHash(String name) {
		return HashManager.get().getSpuiHash(name);
	}
	
	public static String getGlobalName(int hash) {
		String str = HashManager.get().getFileRegistry().getName(hash);
		if (str == null) {
			str = HashManager.get().getPropRegistry().getName(hash);
			if (str == null) {
				str = HashManager.get().getTypeRegistry().getName(hash);
				if (str == null) {
					return "#" + fillZeroInHexString(hash).toUpperCase();
				}
			}
		}
		return str;
	}
	
	public static int getGlobalHash(String name) {
		if (name.startsWith("#")) {
			return (int) Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return (int) Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = HashManager.get().getFileRegistry().getHash(name);
			if (i == -1) {
				i = HashManager.get().getPropRegistry().getHash(name);
				if (i == -1) {
					i = HashManager.get().getTypeRegistry().getHash(name);
					if (i == -1) {
						return stringToFNVHash(name);
					}
				}
			}
			return i;
		}
	}
	
	///////////////////////////
	///// CUSTOM REGISTRY /////
	///////////////////////////
	
	/**
	 * Returns the name equivalent of the given hash in the given registry, or an hexadecimal representation of the hash if no equivalent is found. 
	 * @param hash The hash to find.
	 * @param registry The registry where the hash will be searched.
	 * @return The string equivalent or an hexadecimal representation of the given hash in the given registry.
	 */
	public static String getName(int hash, NameRegistry registry) {
		String str = registry.getName(hash);
		if (str != null) {
			return str;
		} else {
			return "#" + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	
	public static int getHash(String name, NameRegistry registry) {
		if (name.startsWith("#")) {
			return (int) Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return (int) Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = registry.getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}
	}
	
}
