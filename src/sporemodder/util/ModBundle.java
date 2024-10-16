package sporemodder.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sporemodder.PathManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModBundle {
    private static final Runtime.Version DEFAULT_INSTALLER_VERSION = Runtime.Version.parse("1.0.1.2");

    public enum FileTarget {
        /** Store files in DataEP1 (Galactic Adventures) */
        DATAEP1 {
            @Override public String toString() {
                return "DataEP1 (Galactic Adventures)";
            }
            @Override public String getXmlValue() {
                return "GalacticAdventures";
            }
        },
        /** Store files in Data (base Spore) */
        DATA {
            @Override public String toString() {
                return "Data (base Spore)";
            }
            @Override public String getXmlValue() {
                return "Spore";
            }
        },
        /** Store files in ModAPI mLibs folder */
        MODAPI {
            @Override public String toString() {
                return "ModAPI mLibs";
            }
            @Override public String getXmlValue() {
                return "";
            }
        };

        public static FileTarget fromGamePathType(GamePathConfiguration.GamePathType pathType) {
            return pathType == GamePathConfiguration.GamePathType.SPORE ? DATA : DATAEP1;
        }

        public abstract String getXmlValue();
    }

    public enum ExperimentalStatus {
        YES {
            @Override public String toString() {
                return "Yes";
            }
        },
        NO {
            @Override public String toString() {
                return "No";
            }
        },
        AUTO {
            @Override public String toString() {
                return "Auto";
            }
        }
    }

    private static final String MODINFO_EXPERIMENTAL_AUTO = "$SPOREMOD_EXPERIMENTAL";
    private static final String MODINFO_DLLS_BUILD = "$SPOREMOD_DLLS_BUILD";

    private static final String UNIQUETAG_REGEX = "a-zA-Z0-9_\\-";
    public static final String UNIQUETAG_ALLOWED_REGEX = "[" + UNIQUETAG_REGEX + "]";
    public static final String UNIQUETAG_FORBIDDEN_REGEX = "[^" + UNIQUETAG_REGEX + "]";

    public static final String NAME_ILLEGAL_CHARACTERS = "/\\[]{}";

    private final String name;
    private final List<Project> projects = new ArrayList<>();
    /** The base folder with all the data and source code of the mod. */
    private final File folder;
    private String displayName;
    private String uniqueTag;
    private String description = "";
    private String githubUrl;
    private String websiteUrl;
    private boolean hasCustomModInfo;
    private final Map<Project, FileTarget> packageFileTargets = new HashMap<>();
    /** List of any DLLs (or additional files) that get installed in the ModAPI mLibs folder */
    private final List<String> dllsToInstall = new ArrayList<>();
    private ExperimentalStatus experimentalStatus = ExperimentalStatus.AUTO;
    private boolean requiresGalaxyReset;
    private boolean causesSaveDataDependency;


    public ModBundle(String name) {
        this(name, new File(PathManager.get().getProjectsFolder(), name));
    }

    public ModBundle(String name, File folder) {
        this.name = name;
        this.displayName = name;
        this.folder = folder;
        this.uniqueTag = generateUniqueTagFromName(name);
    }

    public String getName() {
        return name;
    }

    public static String generateUniqueTagFromName(String name) {
        return name.replaceAll(UNIQUETAG_FORBIDDEN_REGEX, "");
    }

    public String getUniqueTag() {
        return uniqueTag;
    }

    public void setUniqueTag(String uniqueTag) {
        this.uniqueTag = uniqueTag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public List<String> getDllsToInstall() {
        return Collections.unmodifiableList(dllsToInstall);
    }

    public ExperimentalStatus getExperimentalStatus() {
        return experimentalStatus;
    }

    public void setExperimentalStatus(ExperimentalStatus experimentalStatus) {
        this.experimentalStatus = experimentalStatus;
    }

    public boolean isRequiresGalaxyReset() {
        return requiresGalaxyReset;
    }

    public void setRequiresGalaxyReset(boolean requiresGalaxyReset) {
        this.requiresGalaxyReset = requiresGalaxyReset;
    }

    public boolean isCausesSaveDataDependency() {
        return causesSaveDataDependency;
    }

    public void setCausesSaveDataDependency(boolean causesSaveDataDependency) {
        this.causesSaveDataDependency = causesSaveDataDependency;
    }

    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    public File getFolder() {
        return folder;
    }

    public Path getGitRepository() {
        return getFolder().toPath();
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
        packageFileTargets.put(project, FileTarget.fromGamePathType(project.getPackPath().getType()));
    }


    /**
     * Load all the projects contained inside this mod 'data' folder, does not add them to the ProjectManager
     * nor read their settings. It also fills the dllsToInstall list by auto-detecting Visual Studio
     * projects in the src/ folder
     */
    public void loadProjects() {
        assert projects.isEmpty();
        File dataFolder = getDataFolder();
        if (dataFolder.exists()) {
            for (File folder : Objects.requireNonNull(dataFolder.listFiles(File::isDirectory))) {
                Project project = new Project(folder.getName(), this);
                projects.add(project);
                packageFileTargets.put(project, FileTarget.fromGamePathType(project.getPackPath().getType()));
            }
        }

        List<String> dllFiles = detectDllFiles();
        if (dllFiles != null) {
            dllsToInstall.clear();
            dllsToInstall.addAll(dllFiles);
        }
    }

    /**
     * Returns true if the ModInfo.xml file contains more data than the SporeModder-FX GUI supports.
     * @return
     */
    public boolean hasCustomModInfo() {
        return hasCustomModInfo;
    }

    public FileTarget getPackageFileTarget(Project project) {
        return packageFileTargets.get(project);
    }

    public void setPackageFileTarget(Project project, FileTarget target) {
        packageFileTargets.put(project, target);
    }

    /**
     * Returns the ModInfo.xml file that contains the mod properties, and is stored in the mod's root folder.
     * @return
     */
    public File getModInfoFile() {
        return new File(getFolder(), "ModInfo.xml");
    }

    private List<String> detectDllFiles() {
        File srcFolder = getSrcFolder();
        if (!srcFolder.isDirectory()) {
            return null;
        }
        try (Stream<Path> stream = Files.walk(getSrcFolder().toPath())) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".vcxproj"))
                    .map(path -> path.getFileName().toString())
                    .map(fileName -> fileName.substring(0, fileName.length() - ".vcxproj".length()) + ".dll")
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Failed to detect Visual Studio Projects in mod: " + name);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Detects the Visual Studio projects in the src/ folder, and generates the .dll names from them.
     * It updates the dllsToInstall list and, if saveModInfo and the ModInfo.xml wasn't custom, it saves it with the new info.
     */
    public void detectAndUpdateDllFiles(boolean saveModInfo) {
        List<String> dllFiles = detectDllFiles();
        dllsToInstall.clear();
        if (dllFiles != null) {
            dllsToInstall.addAll(dllFiles);
        }
        if (!hasCustomModInfo && saveModInfo) {
            try {
                saveModInfo();
            } catch (ParserConfigurationException | TransformerException e) {
                System.err.println("Failed to save ModInfo in detectAndUpdateDllFiles() for mod: " + name);
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the ModInfo.xml file from the root folder, loading all its properties.
     */
    public void loadModInfo(boolean saveModInfoIfItNeedsUpdate) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        hasCustomModInfo = false;

        File xmlFile = getModInfoFile();
        if (!xmlFile.exists()) {
            return;
        }

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Element modElement = doc.getDocumentElement();
        if (!modElement.getTagName().equals("mod")) {
            throw new RuntimeException("Cannot parse ModInfo.xml: first element must be 'mod'");
        }

        displayName = modElement.getAttribute("displayName");
        // Use the folder name as default for display name
        if (displayName.isEmpty()) {
            displayName = name;
        }

        uniqueTag = modElement.getAttribute("unique");
        if (uniqueTag.isEmpty()) {
            uniqueTag = generateUniqueTagFromName(name);
        }

        description = modElement.getAttribute("description");
        githubUrl = modElement.getAttribute("githubUrl");
        websiteUrl = modElement.getAttribute("websiteUrl");

        String experimentalStr = modElement.getAttribute("isExperimental");
        if (experimentalStr.isBlank() || experimentalStr.equals(MODINFO_EXPERIMENTAL_AUTO)) {
            experimentalStatus = ExperimentalStatus.AUTO;
        } else if (experimentalStr.equalsIgnoreCase("true")) {
            experimentalStatus = ExperimentalStatus.YES;
        } else if (experimentalStr.equalsIgnoreCase("false")) {
            experimentalStatus = ExperimentalStatus.NO;
        } else {
            hasCustomModInfo = true;
        }

        String galaxyResetStr = modElement.getAttribute("requiresGalaxyReset");
        if (galaxyResetStr.isBlank() || experimentalStr.equalsIgnoreCase("false")) {
            requiresGalaxyReset = false;
        } else if (experimentalStr.equalsIgnoreCase("true")) {
            requiresGalaxyReset = true;
        } else {
            hasCustomModInfo = true;
        }

        String dataDependencyStr = modElement.getAttribute("causesSaveDataDependency");
        if (dataDependencyStr.isBlank() || dataDependencyStr.equalsIgnoreCase("false")) {
            causesSaveDataDependency = false;
        } else if (dataDependencyStr.equalsIgnoreCase("true")) {
            causesSaveDataDependency = true;
        } else {
            hasCustomModInfo = true;
        }

        // Validate other fields
        if (!modElement.getAttribute("hasCustomInstaller").equalsIgnoreCase("false")) {
            hasCustomModInfo = true;
        }
        String installerVersion = modElement.getAttribute("installerSystemVersion");
        if (!installerVersion.isEmpty()) {
            try {
                Runtime.Version.parse(installerVersion);
            } catch (Exception e) {
                hasCustomModInfo = true;
            }
        }

        // Removing old files is an advanced use case
        if (modElement.getElementsByTagName("remove").getLength() > 0) {
            hasCustomModInfo = true;
        }

        // Detect files to install
        // We have two special scenarios:
        // - ModInfo contains prerequisite files that are autodetected as mod projects or dlls.
        //   In this case, consider it as custom ModInfo.
        // - All prerequisite files are part of autodetected, but there are some autodetected projects/dlls
        //   that are not in ModInfo. In this case, don't consider it as custom, and just add them
        Map<String, Project> autoPackageNames = new HashMap<>();
        for (Project project : projects) {
            autoPackageNames.put(project.getPackageName(), project);
        }
        Set<String> unincludedPackages  = new HashSet<>(autoPackageNames.keySet());
        Set<String> unincludedDlls = new HashSet<>(dllsToInstall);

        NodeList prerequisiteFiles = modElement.getElementsByTagName("prerequisite");
        for (int i = 0; i < prerequisiteFiles.getLength(); i++) {
            Element element = (Element)prerequisiteFiles.item(i);

            String gameAttribute = element.getAttribute("game");
            FileTarget target = FileTarget.MODAPI;
            if (gameAttribute.equalsIgnoreCase("GalacticAdventures")) {
                target = FileTarget.DATAEP1;
            } else if (gameAttribute.equalsIgnoreCase("Spore")) {
                target = FileTarget.DATA;
            }

            String fileName = element.getTextContent();
            if (target == FileTarget.MODAPI) {
                unincludedDlls.remove(fileName);
            } else {
                if (!autoPackageNames.containsKey(fileName)) {
                    hasCustomModInfo = true;
                } else {
                    Project project = autoPackageNames.get(fileName);
                    packageFileTargets.put(project, target);
                    unincludedPackages.remove(fileName);
                }
            }
        }

        if (!hasCustomModInfo && saveModInfoIfItNeedsUpdate &&
                (!unincludedPackages.isEmpty() || !unincludedDlls.isEmpty())) {
            // Add any autodetected projects and dlls that were not in the ModInfo.xml
            saveModInfo();
        }
    }

    public void saveModInfo() throws ParserConfigurationException, TransformerException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();

        Element rootElement = document.createElement("mod");
        document.appendChild(rootElement);
        rootElement.setAttribute("displayName", displayName);
        rootElement.setAttribute("unique", uniqueTag);
        rootElement.setAttribute("description", description);
        if (githubUrl != null && !githubUrl.isBlank()) {
            rootElement.setAttribute("githubUrl", githubUrl);
        }
        if (websiteUrl != null && !websiteUrl.isBlank()) {
            rootElement.setAttribute("websiteUrl", websiteUrl);
        }
        rootElement.setAttribute("dllsBuild", MODINFO_DLLS_BUILD);
        rootElement.setAttribute("hasCustomInstaller", "false");
        rootElement.setAttribute("installerSystemVersion", DEFAULT_INSTALLER_VERSION.toString());

        String experimentalStr = MODINFO_EXPERIMENTAL_AUTO;
        if (experimentalStatus == ExperimentalStatus.YES) {
            experimentalStr = "true";
        } else if (experimentalStatus == ExperimentalStatus.NO) {
            experimentalStr = "false";
        }
        rootElement.setAttribute("isExperimental", experimentalStr);

        if (requiresGalaxyReset) {
            rootElement.setAttribute("requiresGalaxyReset", "true");
        }
        if (causesSaveDataDependency) {
            rootElement.setAttribute("causesSaveDataDependency", "true");
        }

        for (Project project : projects) {
            Element prerequisite = document.createElement("prerequisite");
            prerequisite.setAttribute("game", getPackageFileTarget(project).getXmlValue());
            prerequisite.appendChild(document.createTextNode(project.getPackageName()));
            rootElement.appendChild(prerequisite);
        }
        for (String dllFile : dllsToInstall) {
            Element prerequisite = document.createElement("prerequisite");
            prerequisite.appendChild(document.createTextNode(dllFile));
            rootElement.appendChild(prerequisite);
        }

        hasCustomModInfo = false;

        DOMSource dom = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult result = new StreamResult(getModInfoFile());
        transformer.transform(dom, result);
    }
}
