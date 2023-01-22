package sporemodder.utilities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

public class SporeGame {
	
	public static final String[] RegistryValues = { "InstallLoc", "Install Dir" };

    public static final String[] RegistryKeys = { 
                                         "SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE_EP1",
                                         "SOFTWARE\\Electronic Arts\\SPORE_EP1"
                                     };

    public static final String[] SporeRegistryKeys = { 
                                         "SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE",
                                         "SOFTWARE\\Electronic Arts\\SPORE"
                                                };

    public static final String[] CCRegistryKeys = { 
                                         "SOFTWARE\\Wow6432Node\\Electronic Arts\\SPORE(TM) Creepy & Cute Parts Pack",
                                         "SOFTWARE\\Electronic Arts\\SPORE(TM) Creepy & Cute Parts Pack"
                                     };


    public static final String RegistryDataDir = "DataDir";  // Steam/GoG users don't have InstallLoc nor Install Dir
    
    private static enum GameType { SPORE, GA, CC };
    
	private static SporeGame SPORE = null;
	private static SporeGame GALACTIC_ADVENTURES = null;
	
	private File installLoc;
	private File dataLoc;
	private File sporebinLoc;
	
	private SporeGame(GameType type) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		String path = getFromRegistry(type);
		if (path != null) {
			sporebinLoc = new File(moveToSporebin(type == GameType.SPORE ? "Sporebin" : "SporebinEP1", path, true));
			installLoc = sporebinLoc.getParentFile();
			dataLoc = moveToData(type, installLoc);
		}
	}
	
	private static String getFromRegistry(String[] keys) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException
    {

        String result = null;

        for (String key : keys)
        {
            for (String value : RegistryValues)
            {
            	result = getRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, key, value);
                if (result != null)
                {

                    return fixPath(result);
                }
            }
        }

        // not found? try with DataDir; some users only have that one
        for (String key : RegistryKeys)
        {
            result = getRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, key, RegistryDataDir);
            if (result != null)
            {

                return fixPath(result);
            }
        }

        return null;
    }
	
	private static String getRegistryValue(int hkey, String key, String value) {
		try {
			return WinRegistry.valueForKey(hkey, key, value);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private static String getFromRegistry(GameType game) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException
    {
        if (game == GameType.GA)
        {
            return getFromRegistry(RegistryKeys);
        }
        else if (game == GameType.SPORE)
        {
            return getFromRegistry(SporeRegistryKeys);
        }
        else
        {
            return getFromRegistry(CCRegistryKeys);
        }
    }
	
	// remove "" if necessary
    private static String fixPath(String path)
    {
        if (path.startsWith("\""))
        {
            return path.substring(1, path.length() - 1);
        }
        else
        {
            return path;
        }
    }
    
    // This method returns the path to the folder that contains the executable
    public static String moveToSporebin(String folderName, String path, boolean bRecursive)
    {
        if (!path.endsWith("\\"))
        {
            path += "\\";
        }

        if (new File(path, "SporeApp.exe").exists())
        {
            return path;
        }

        if (new File(path, folderName).exists())
        {
            return path + folderName + "\\";
        }

        if (bRecursive)
        {
            return moveToSporebin(folderName, new File(path).getParent(), false);
        }

        return null;
    }
    
    public static File moveToData(GameType game, File installLoc)
    {
        if (game == GameType.SPORE)
        {
            return new File(installLoc, "Data");
        }
        else if (game == GameType.GA)
        {
            // Steam and GoG uses DataEP1
        	File outputPath = new File(installLoc, "DataEP1");
        	if (outputPath.exists()) {
        		return outputPath;
        	}
            else
            {
                return new File(installLoc, "Data" );
            }
        }
        else
        {
            // Creepy and Cute uses the installation path itself
            return installLoc;
        }
    }
	
	public String getInstallLoc() {
		return installLoc.getAbsolutePath();
	}
	
	public String getDataDir() {
		return dataLoc.getAbsolutePath();
	}
	
	public String getGamePath() {
		return new File(sporebinLoc, "SporeApp.exe").getAbsolutePath();
	}
	
	public static Process execute(String path, String ... args) throws IOException, URISyntaxException {
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
	
	public static boolean hasSpore() {
		return SPORE != null;
	}
	
	public static boolean hasGalacticAdventures() {
		return GALACTIC_ADVENTURES != null;
	}
	
	public static SporeGame getSpore() {
		return SPORE;
	}
	
	public static SporeGame getGalacticAdventures() {
		return GALACTIC_ADVENTURES;
	}
	
	public static void init() {
		try {
			SPORE = new SporeGame(GameType.SPORE);
			
		} catch (Exception e) {
			System.err.println("Couldn't find Spore!");
			e.printStackTrace();
			SPORE = null;
		}
		
		try {
			GALACTIC_ADVENTURES = new SporeGame(GameType.GA);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldn't find Galactic Adventures!");
			e.printStackTrace();
			GALACTIC_ADVENTURES = null;
		}
		
	}
}
