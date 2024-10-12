package sporemodder.util;

import sporemodder.PathManager;
import sporemodder.ProjectManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Keeps track of available Projects. The list of projects is calculated
 * by finding all folders in Projects that are not mod bundles, and then
 * adding all the subprojects in mod bundles.
 * The class also manages the last time each project was active, and keeps
 * track of it in a file.
 */
public class ProjectsList {
    /** A map that assigns a project name (lowercase) to the project itself */
    private final Map<String, Project> projects = new TreeMap<>();

    public boolean exists(String name) {
        return projects.containsKey(name.toLowerCase());
    }

    public Project get(String name) {
        return projects.get(name.toLowerCase());
    }

    public void add(Project project) {
        projects.put(project.getName().toLowerCase(), project);
    }

    public void remove(Project project) {
        projects.remove(project.getName().toLowerCase());
    }

    /**
     * Returns an alphabetically ordered collection of all projects.
     * @return
     */
    public Collection<Project> getAll() {
        return projects.values();
    }

    /**
     * Load all projects from SMFX Projects folder that are not part of any mod bundle.
     */
    public void loadStandaloneProjects() {
        File projectsFolder = PathManager.get().getProjectsFolder();
        if (!projectsFolder.exists()) projectsFolder.mkdir();

        if (!projectsFolder.exists()) {
            System.err.println("Failed to load any projects, projects folder does not exist: " + projectsFolder.getAbsolutePath());
            return;
        }

        for (File folder : Objects.requireNonNull(projectsFolder.listFiles())) {
            if (ProjectManager.get().hasModBundle(folder.getName())) {
                continue;
            }
            Project project = folder.isDirectory()
                    ? new Project(folder.getName())
                    : parseExternalProjectLink(folder);
            if (project != null) {
                project.loadSettings();
                projects.put(project.getName().toLowerCase(), project);
            }
        }
    }

    /**
     * Returns a certain number of the most recent projects; that is, the projects that were last set as active.
     * @param count How many projects must be returned.
     * @return
     */
    public List<Project> getRecentProjects(int count) {
        List<Project> list = new ArrayList<>(projects.values());
        list.sort((arg0, arg1) -> -Long.compare(arg0.getLastTimeUsed(), arg1.getLastTimeUsed()));
        return list.subList(0, Math.min(count, list.size()));
    }

    private Project parseExternalProjectLink(File linkFile) {
        Project project = null;
        try {
            List<String> lines = Files.readAllLines(linkFile.toPath());

            if (lines.size() != 1) {
                System.err.println("File " + linkFile.getAbsolutePath() + " doesn't follow external project link format");
            }
            else {
                File externalFolder = new File(lines.get(0).trim());
                if (externalFolder.isDirectory()) {
                    project = new Project(linkFile.getName(), externalFolder, linkFile);
                }
                else {
                    System.err.println("Error reading external project link " + linkFile.getName() + ": folder " + externalFolder.getAbsolutePath() + " does not exist.");
                    return null;
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error reading external project link " + linkFile.getAbsolutePath());
        }
        return project;
    }

    private Path getTimesListFile() {
        return PathManager.get().getProgramFile("ProjectLastActiveTimes.txt").toPath();
    }

    /**
     * Saves the list of when each project was active for the last time.
     * @throws IOException
     */
    public void saveLastActiveTimes() throws IOException {
        Files.write(getTimesListFile(), projects.values().stream()
                .map(project -> project.getLastTimeUsed() + " " + project.getName())
                .collect(Collectors.toList()));
    }

    /**
     * Saves the list of when each project was active for the last time.
     * @throws IOException
     */
    public void saveLastActiveTimesNoException() {
        try {
            saveLastActiveTimes();
        } catch (IOException e) {
            System.err.println("Failed to save projects last active times: " + e.getMessage());
        }
    }

    public void loadLastActiveTimes() {
        if (!Files.exists(getTimesListFile())) {
            return;
        }
        try {
            Files.readAllLines(getTimesListFile()).forEach(line -> {
                String[] splits = line.split(" ", 2);
                assert splits.length == 2;
                long lastActiveTime = Long.parseLong(splits[0]);
                Project project = get(splits[1]);
                if (project != null) {
                    project.setLastTimeUsed(lastActiveTime);
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to read projects last active times list: " + e.getMessage());
        }
    }
}
