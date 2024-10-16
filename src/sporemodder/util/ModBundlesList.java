package sporemodder.util;

import org.xml.sax.SAXException;
import sporemodder.PathManager;
import sporemodder.ProjectManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Keeps track of Mod Bundles by keeping a list of them. The list is saved
 * into a file every time a mod bundle is added.
 */
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

    public void loadList() {
        if (!Files.exists(getFile())) {
            return;
        }
        try {
            Files.readAllLines(getFile()).forEach(line -> {
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
        } catch (Exception e) {
            System.err.println("Failed to read mod bundles list: " + e.getMessage());
        }
    }

    /**
     * Reads the mod bundles file again, and removes from the list any mod bundles whose folder
     * no longer exists.
     */
    public void removeInexistantMods() {
        if (!Files.exists(getFile())) {
            return;
        }
        try {
            Files.readAllLines(getFile()).forEach(line -> {
                File modFolder;
                if (line.contains("/") || line.contains("\\")) {
                    // External mod folder
                    modFolder = new File(line);
                } else {
                    // Normal mod inside SMFX Projects
                    modFolder = new File(PathManager.get().getProjectsFolder(), line);
                }
                if (!modFolder.exists()) {
                    for (Project project : modBundles.get(modFolder.getName().toLowerCase()).getProjects()) {
                        try {
                            ProjectManager.get().deleteProject(project);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    modBundles.remove(modFolder.getName().toLowerCase());
                }
            });
            saveList();
        } catch (Exception e) {
            System.err.println("Failed to prune mod bundles list: " + e.getMessage());
        }
    }

    public void loadModInfos() {
        for (ModBundle modBundle : modBundles.values()) {
            try {
                modBundle.loadModInfo(true);
            } catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
                System.err.println("Failed to load ModInfo.xml for mod: " + modBundle.getName());
                e.printStackTrace();
            }
        }
    }

    private Path getFile() {
        return PathManager.get().getProgramFile("ModBundles.txt").toPath();
    }

    private void saveList() throws IOException {
        Files.write(getFile(), modBundles.values().stream()
                .map(bundle -> bundle.isExternalFolder() ? bundle.getFolder().getAbsolutePath() : bundle.getName())
                .collect(Collectors.toList()));
    }
}
