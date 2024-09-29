package sporemodder.util;

import sporemodder.PathManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModBundle {
    private final String name;
    private final List<Project> projects = new ArrayList<>();
    /** The base folder with all the data and source code of the mod. */
    private final File folder;

    public ModBundle(String name) {
        this(name, new File(PathManager.get().getProjectsFolder(), name));
    }

    public ModBundle(String name, File folder) {
        this.name = name;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public File getFolder() {
        return folder;
    }

    /**
     * Returns the 'data' folder that contains this mod's SMFX package projects.
     * @return
     */
    public File getDataFolder() {
        return new File(folder, "data");
    }

    /**
     * Returns the 'src' folder that contains C++ source code projects.
     * @return
     */
    public File getSrcFolder() {
        return new File(folder, "src");
    }

    /**
     * Returns true if this mod is stored in an external folder, outside the SMFX Projects folder.
     * @return
     */
    public boolean isExternalFolder() {
        File projectsFolder = PathManager.get().getProjectsFolder();
        try {
            return !Files.isSameFile(getFolder().getParentFile().toPath(), projectsFolder.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void addProject(Project project) {
        projects.add(project);
    }


    /**
     * Load all the projects contained inside this mod 'data' folder, does not add them to the ProjectManager
     * nor read their settings.
     */
    public void loadProjects() {
        assert projects.isEmpty();
        File dataFolder = getDataFolder();
        if (dataFolder.exists()) {
            for (File folder : Objects.requireNonNull(dataFolder.listFiles(File::isDirectory))) {
                Project project = new Project(folder.getName(), folder, null);
                projects.add(project);
            }
        }
    }
}
