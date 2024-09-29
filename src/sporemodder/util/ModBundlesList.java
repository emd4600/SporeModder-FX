package sporemodder.util;

import sporemodder.PathManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ModBundlesList {

    /** A map that assigns a mod bundle name (lowercase) to the mod bundle itself */
    private final Map<String, ModBundle> modBundles = new TreeMap<>();

    public boolean exists(String name) {
        return modBundles.containsKey(name.toLowerCase());
    }

    public ModBundle get(String name) {
        return modBundles.get(name.toLowerCase());
    }

    /**
     * Adds a mod bundle to the list, and saves the list.
     * @param modBundle
     * @throws IOException
     */
    public void add(ModBundle modBundle) throws IOException {
        modBundles.put(modBundle.getName().toLowerCase(), modBundle);
        saveList();
    }

    public Collection<ModBundle> getAll() {
        return modBundles.values();
    }

    public void loadList() throws IOException {
        Files.readAllLines(getFile().toPath()).forEach(line -> {
            ModBundle modBundle = null;
            if (line.contains("/") || line.contains("\\")) {
                // External mod folder
                File folder = new File(line);
                if (folder.exists() && folder.isDirectory()) {
                    modBundle = new ModBundle(folder.getName(), folder);
                } else {
                    System.err.println("Cannot load mod bundle '" + line + "', it is not a directory");
                }
            } else {
                // Normal mod inside SMFX Projects
                modBundle = new ModBundle(line);
            }
            if (modBundle != null) {
                modBundle.loadProjects();
                modBundles.put(modBundle.getName().toLowerCase(), modBundle);
            }
        });
    }

    private File getFile() {
        return PathManager.get().getProgramFile("ModBundles.txt");
    }

    private void saveList() throws IOException {
        Files.write(getFile().toPath(), modBundles.values().stream()
                .map(bundle -> bundle.isExternalFolder() ? bundle.getFolder().getAbsolutePath() : bundle.getName())
                .collect(Collectors.toList()));
    }
}
