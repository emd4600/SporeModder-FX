package sporemodder.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.pctp.TxtToPctp;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.spui.TxtToSpui;
import sporemodder.files.formats.tlsa.TxtToTlsa;
import sporemodder.userinterface.dialogs.UIChooseProjectPath;
import sporemodder.utilities.names.NameRegistry;
import sporemodder.utilities.names.SimpleNameRegistry;

public class Project {
	public enum GamePathType {
		SPORE, 
		GALACTIC_ADVENTURES, 
		CUSTOM
		};
		
	public enum EditorsPackages {
		NONE {
			@Override
			public String toString() {
				return "None";
			}
		},
		PATCH51 {
			@Override
			public String toString() {
				return "GA Patch 5.1";
			}
		},
		BOT_PARTS {
			@Override
			public String toString() {
				return "Bot Parts";
			}
		}
	}
	
	private static final String SETTINGS_FILE_NAME = "config.properties";
	private static final String PROPERTY_LAST_TIME_USED = "lastTimeUsed";
	private static final String PROPERTY_SOURCES = "sources";
	private static final String PROPERTY_PACK_PATH_TYPE = "packPathType";
	private static final String PROPERTY_PACK_PATH = "packPath";
	private static final String PROPERTY_PACKAGE_NAME = "packageName";
	private static final String PROPERTY_GAME_PATH_TYPE = "gamePathType";
	private static final String PROPERTY_GAME_PATH = "gamePath";
	private static final String PROPERTY_GAME_COMMANDS = "gameCommands";
	private static final String PROPERTY_GAME_DEFAULT = "gameDefault";
	private static final String PROPERTY_EMBEDDED_PACKAGES = "embeddedEditorPackages";
	
	private final List<Project> sources = new ArrayList<Project>();
	private long lastTimeUsed;
	private String name;
	// Used when reading sources, since not all projects are ready yet we'll update the sources when we're done
	private List<String> sourceNames; 
	private Properties properties;
	
	private String packageName;
	private boolean isDefaultPackageName;
	private GamePathType packPathType = GamePathType.CUSTOM;
	private String customPackPath = "";
	
	private boolean defaultGamePath = true;
	private String gamePath = "";
	private GamePathType gamePathType = GamePathType.CUSTOM;
	private String gameCommandLine = "";
	
	private boolean convertPROP = true;
	private boolean convertRW4 = true;
	private boolean convertTLSA = true;
	private boolean convertPCTP = true;
	private boolean convertSPUI = true;
	private boolean convertRAST;
	private boolean convertGAIT = true;
	private boolean convertEffects = true;
	private int compressingLimit = -1;
	
	private EditorsPackages embeddedEditorPackages = EditorsPackages.NONE;
	
	public Project(String name) {
		this.name = name;
		
		packageName = getDefaultPackageName(name);
		isDefaultPackageName = true;
		
		if (SporeGame.hasGalacticAdventures()) {
			packPathType = GamePathType.GALACTIC_ADVENTURES;
			gamePathType = GamePathType.GALACTIC_ADVENTURES;
		} 
		else if (SporeGame.hasSpore()) {
			packPathType = GamePathType.SPORE;
			gamePathType = GamePathType.SPORE;
		} 
		else {
			packPathType = GamePathType.CUSTOM;
			gamePathType = GamePathType.CUSTOM;
		}
	}
	
	public Project() {
		isDefaultPackageName = true;
		
		if (SporeGame.hasGalacticAdventures()) {
			packPathType = GamePathType.GALACTIC_ADVENTURES;
			gamePathType = GamePathType.GALACTIC_ADVENTURES;
		} 
		else if (SporeGame.hasSpore()) {
			packPathType = GamePathType.SPORE;
			gamePathType = GamePathType.SPORE;
		} 
		else {
			packPathType = GamePathType.CUSTOM;
			gamePathType = GamePathType.CUSTOM;
		}
	}
	
	public String getProjectName() {
		return name;
	}
	
	// this only changes the name internally. Use rename to change the folder name too
	public void setProjectName(String name) {
		this.name = name;
		
		if (isDefaultPackageName) {
			packageName = getDefaultPackageName(name);
		}
	}
	
	public void rename(String name) {
		File folder = getProjectPath();
		this.name = name;
		if (folder.exists()) folder.renameTo(getProjectPath());
		
		if (isDefaultPackageName) {
			packageName = getDefaultPackageName(name);
		}
		MainApp.getUserInterface().updateProjects();
		MainApp.getUserInterface().updateProjectName();
	}
	
	public File getProjectPath() {
		return new File(MainApp.getProjectsPath(), name);
	}
	
//	public List<DefaultMutableTreeNode> getTreeNodes() {
//		return treeNodes;
//	}
	
	public long getLastTimeUsed() {
		return lastTimeUsed;
	}
	
	public void setLastTimeUsed() {
		lastTimeUsed = System.currentTimeMillis();
	}
	
	public void updateSources() {
		if (sourceNames != null) {
			sources.clear();
			MainApp.getProjectsByNames(sourceNames, sources);
		}
	}
	
	public void loadSourceProjects() throws IOException {
		if (sourceNames != null) {
			sources.clear();
			
			for (String str : sourceNames) {
				Project p = MainApp.getProjectByName(str);
				if (p == null) {
					p = Project.loadProject(new File(MainApp.getProjectsPath(), str));
				}

				if (p != null) {
					sources.add(p);
					MainApp.addProject(p);
				}
			}
		}
	}
	
	public List<Project> getSources() {
		return sources;
	}
	
	public void setSources(List<String> names) {
		sourceNames = names;
		updateSources();
	}
	
	public File getPackageFile() {
		String path = null;
		SporeGame game = null;
		if (packPathType == GamePathType.GALACTIC_ADVENTURES) {
			game = SporeGame.getGalacticAdventures();
		}
		else if (packPathType == GamePathType.SPORE) {
			game = SporeGame.getSpore();
		}
		
		if (game == null) {
			path = customPackPath;
		}
		else {
			path = game.getDataDir();
		}
		
		File file = null;
		if (path == null || path.length() == 0) {
			file = MainApp.getProjectsPath();
		}
		else {
			file = new File(path);
		}
		
		return new File(file, packageName);
	}
	
	public GamePathType getPackPathType() {
		return packPathType;
	}
	public String getCustomPackPath() {
		return customPackPath;
	}
	
	
	public String getCustomGamePath() {
		return gamePath;
	}

	public GamePathType getGamePathType() {
		return gamePathType;
	}

	public String getGameCommandLine() {
		return gameCommandLine;
	}

	public void setGamePath(String gamePath) {
		this.gamePath = gamePath;
	}

	public void setGamePathType(GamePathType gamePathType) {
		this.gamePathType = gamePathType;
	}

	public void setGameCommandLine(String gameCommandLine) {
		this.gameCommandLine = gameCommandLine;
	}

	public boolean isDefaultGamePath() {
		return defaultGamePath;
	}

	public void setDefaultGamePath(boolean defaultGamePath) {
		this.defaultGamePath = defaultGamePath;
	}
	
	public EditorsPackages getEmbeddedEditorPackages() {
		return embeddedEditorPackages;
	}

	public void setEmbeddedEditorPackages(EditorsPackages embeddedEditorPackages) {
		this.embeddedEditorPackages = embeddedEditorPackages;
	}

	
	public static Project loadProject(File file) throws IOException {
		Project project = new Project(file.getName());
		
		File propertiesFile = new File(file, SETTINGS_FILE_NAME);
		if (propertiesFile.exists()) {
			project.readProperties(propertiesFile);
		}
		
		return project;
	}

	public static void loadProjects(List<Project> projects, File folder) throws IOException {
		if (folder == null || !folder.isDirectory()) {
			// throw new IOException("Given projects path is not a folder: " + path);
			new UIChooseProjectPath("Choose Projects Folder", "The existing Projects folder cannot be found. Please choose a valid Projects folder.");
			folder = MainApp.getProjectsPath();
		}
		if (folder == null || !folder.isDirectory()) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "The program can't be started without a valid Projects path.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		File[] files = folder.listFiles();
		
		for (File f : files) {
			if (f.isDirectory())
			{
				projects.add(loadProject(f));
			}
		}
	}
	
	/**
	 * Deletes a dir recursively deleting anything inside it.
	 * @param dir The dir to delete
	 * @return true if the dir was successfully deleted
	 */
	private static boolean deleteDirectory(File dir) {
	    if(! dir.exists() || !dir.isDirectory())    {
	        return false;
	    }

	    String[] files = dir.list();
	    for(int i = 0, len = files.length; i < len; i++)    {
	        File f = new File(dir, files[i]);
	        if(f.isDirectory()) {
	            deleteDirectory(f);
	        }else   {
	            f.delete();
	        }
	    }
	    return dir.delete();
	}
	
	private static void deleteDirectory2(File dir) {
		try {
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>(){
			    @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
			      throws IOException {
			      Files.delete(file);
			      return FileVisitResult.CONTINUE;
			    }

			    @Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
			      return handleException(e);
			    }

			    private FileVisitResult handleException(final IOException e) {
			      e.printStackTrace(); // replace with more robust error handling
			      return FileVisitResult.TERMINATE;
			    }

			    @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
			      throws IOException {
			      if(e!=null)return handleException(e);
			      Files.delete(dir);
			      return FileVisitResult.CONTINUE;
			    }
			  });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createNewProject() {
		File folder = getProjectPath();
		if (folder.exists()) {
			if (folder.isFile()) folder.delete();
			else deleteDirectory2(folder);
		}
		folder.mkdir();
		
		if (MainApp.checkProjectName(name)) {
			MainApp.setProject(this, name);
		}
		else {
			MainApp.addProject(this);
		}
	}
	
	public void setPackingConverters(boolean convertPROP, boolean convertRW4, boolean convertTLSA, boolean convertPCTP, boolean convertSPUI, 
			boolean convertRAST, boolean convertEffects) {
		this.convertPROP = convertPROP;
		this.convertRW4 = convertRW4;
		this.convertTLSA = convertTLSA;
		this.convertPCTP = convertPCTP;
		this.convertSPUI = convertSPUI;
		this.convertRAST = convertRAST;
		//TODO GAIT
		this.convertEffects = convertEffects;
	}
	
	public List<ConvertAction> getPackingConverters() {
		List<ConvertAction> converters = new ArrayList<ConvertAction>();
		
		if (convertPROP) converters.add(new XmlToProp());
		if (convertRW4) converters.add(new DDSToRw4());
		if (convertTLSA) converters.add(new TxtToTlsa());
		if (convertPCTP) converters.add(new TxtToPctp());
		if (convertSPUI) converters.add(new TxtToSpui());
		if (convertRAST) converters.add(new DDStoRast());
		//TODO GAIT
		if (convertEffects) converters.add(new EffectPacker());
		
		return converters;
	}
	
	public boolean isConvertPROP() {
		return convertPROP;
	}

	public boolean isConvertRW4() {
		return convertRW4;
	}

	public boolean isConvertTLSA() {
		return convertTLSA;
	}

	public boolean isConvertPCTP() {
		return convertPCTP;
	}

	public boolean isConvertSPUI() {
		return convertSPUI;
	}

	public boolean isConvertRAST() {
		return convertRAST;
	}

	public boolean isConvertGAIT() {
		return convertGAIT;
	}

	public boolean isConvertEffects() {
		return convertEffects;
	}

	public void setConvertPROP(boolean convertPROP) {
		this.convertPROP = convertPROP;
	}

	public void setConvertRW4(boolean convertRW4) {
		this.convertRW4 = convertRW4;
	}

	public void setConvertTLSA(boolean convertTLSA) {
		this.convertTLSA = convertTLSA;
	}

	public void setConvertPCTP(boolean convertPCTP) {
		this.convertPCTP = convertPCTP;
	}

	public void setConvertSPUI(boolean convertSPUI) {
		this.convertSPUI = convertSPUI;
	}

	public void setConvertRAST(boolean convertRAST) {
		this.convertRAST = convertRAST;
	}

	public void setConvertGAIT(boolean convertGAIT) {
		this.convertGAIT = convertGAIT;
	}

	public void setConvertEffects(boolean convertEffects) {
		this.convertEffects = convertEffects;
	}
	

	public int getCompressingLimit() {
		return compressingLimit;
	}

	public void setCompressingLimit(int compressingLimit) {
		this.compressingLimit = compressingLimit;
	}
	

	public void readProperties(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			properties = new Properties();
			properties.load(in);
			
			lastTimeUsed = Long.parseLong(properties.getProperty(PROPERTY_LAST_TIME_USED, "-1"));
			String sourcesValue = properties.getProperty(PROPERTY_SOURCES, "");
			if (sourcesValue.length() > 0) {
				String[] names = sourcesValue.split("\\|");
				sourceNames = new ArrayList<String>(names.length);
				for (String name : names) {
					sourceNames.add(name.substring(1, name.length()-1));
				}
			}
			
			packPathType = GamePathType.valueOf(properties.getProperty(PROPERTY_PACK_PATH_TYPE, GamePathType.CUSTOM.toString()));
			customPackPath = properties.getProperty(PROPERTY_PACK_PATH, "");
			String defaultPackageName = getDefaultPackageName(name);
			packageName = properties.getProperty(PROPERTY_PACKAGE_NAME, defaultPackageName);
			isDefaultPackageName = defaultPackageName.equals(packageName);
			
			gamePath = properties.getProperty(PROPERTY_GAME_PATH, "");
			String gamePathTypeStr = properties.getProperty(PROPERTY_GAME_PATH_TYPE);
			if (gamePathTypeStr == null) {
				if (SporeGame.hasGalacticAdventures()) {
					gamePathType = GamePathType.GALACTIC_ADVENTURES;
				} else if (SporeGame.hasSpore()) {
					gamePathType = GamePathType.SPORE;
				} else {
					gamePathType = GamePathType.CUSTOM;
				}
			}
			else {
				gamePathType = GamePathType.valueOf(gamePathTypeStr);
			}
			gameCommandLine = properties.getProperty(PROPERTY_GAME_COMMANDS, "");
			defaultGamePath = Boolean.parseBoolean(properties.getProperty(PROPERTY_GAME_DEFAULT, "true"));
			
			String embeddedEditorPackagesStr = properties.getProperty(PROPERTY_EMBEDDED_PACKAGES);
			if (embeddedEditorPackagesStr != null) {
				for (EditorsPackages enumEntry : EditorsPackages.values()) {
					if (embeddedEditorPackagesStr.equals(enumEntry.toString())) {
						embeddedEditorPackages = enumEntry;
					}
				}
			}
		}
	}
	
	private void writeProperties(File file) throws IOException {
		if (properties == null) {
			properties = new Properties();
		}
		try (FileOutputStream out = new FileOutputStream(file)) {
			
			properties.setProperty(PROPERTY_LAST_TIME_USED, Long.toString(lastTimeUsed));
			
			if (sources != null) {
				int size = sources.size();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < size; i++) 
				{
					sb.append("\"" + sources.get(i).getProjectName() + "\"");
					if (i != size-1) {
						sb.append("|");
					}
				}
				
				properties.setProperty(PROPERTY_SOURCES, sb.toString());
			}
			
			if (packPathType != null) {
				properties.setProperty(PROPERTY_PACK_PATH_TYPE, packPathType.toString());
			}
			if (customPackPath != null) properties.setProperty(PROPERTY_PACK_PATH, customPackPath);
			
			if (!isDefaultPackageName) {
				properties.setProperty(PROPERTY_PACKAGE_NAME, packageName);
			}
			
			properties.setProperty(PROPERTY_GAME_DEFAULT, Boolean.toString(defaultGamePath));
			properties.setProperty(PROPERTY_GAME_PATH, gamePath);
			properties.setProperty(PROPERTY_GAME_PATH_TYPE, gamePathType.toString());
			properties.setProperty(PROPERTY_GAME_COMMANDS, gameCommandLine);
			
			if (embeddedEditorPackages != null) {
				properties.setProperty(PROPERTY_EMBEDDED_PACKAGES, embeddedEditorPackages.toString());
			}
			
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean writeProperties() {
		File file = new File(getProjectPath(), SETTINGS_FILE_NAME);
		try {
			writeProperties(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void loadNames() {
		File file = getFile("sporemaster/names.txt");
		if (file != null) {
			try {
				Hasher.UsedNames = new SimpleNameRegistry(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Hasher.UsedNames = null;
			}
		} else {
			// disable the other project's names
			Hasher.UsedNames = null;
		}
	}
	
	public void saveNames() {
		if (Hasher.UsedNames != null) {
			File file = new File(getProjectPath(), "sporemaster/names.txt");
			file.getParentFile().mkdir();
			
			try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
				Hasher.UsedNames.write(out);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasSource(String filePath) {
		if (sources == null) {
			return false;
		}
		File projectsPath = MainApp.getProjectsPath();

		for (Project source : sources) {
			File file = new File(projectsPath, source.name + "\\" + filePath);
			if (file.exists()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the first source project that contains the given file, or null if there are no sources/the file wasn't found. 
	 * <code>filePath</code> is expected to be the path to the file relative to the current project; that is, with no projects
	 * path nor project name. For example, if you have the file <italics>C:\SporeModder\Projects\My Project\folder\file.txt</italics>,
	 * you should pass <italics>folder\file.txt</italics> to this method.<p>
	 * The file will be searched for each source like <code>MainApp.getProjectsPath() + source.getProjectName() + "\\" + filePath</code>.
	 * This file can then be accessed with <code>MainApp.getProjectsPath() + getSourceByFile(filePath).getProjectName() + "\\" + 
	 * filePath</code>.
	 * 
	 * @param filePath The path to the file relative to the current project.
	 * @return The first source project that contains the given file or null if there are no sources/the file wasn't found.
	 */
	public Project getSourceByFile(String filePath) {
		if (sources == null) {
			return null;
		}
		File projectsPath = MainApp.getProjectsPath();

		for (Project source : sources) {
			File file = new File(projectsPath, source.name + "\\" + filePath);
			if (file.exists()) {
				return source;
			}
		}
		
		return null;
	}
	
	public Project getProjectByFile(String filePath) {
		if (getModFile(filePath) != null) {
			return this;
		}
		else {
			return getSourceByFile(filePath);
		}
	}
	
	public File getSourceFile(String filePath) {
		if (sources == null) {
			return null;
		}
		
		Project source = getSourceByFile(filePath);
		if (source != null) {
			return new File(source.getProjectPath(), filePath);
		} else {
			return null;
		}
	}
	
	public File getModFile(String filePath) {
		File file = new File(getProjectPath(), filePath);
		if (file.exists()) {
			return file;
		}
		return null;
	}
	
	public File getFile(String filePath) {
		File file = getModFile(filePath);
		if (file == null) {
			return getSourceFile(filePath);
		}
		else {
			return file;
		}
	}
	
	public void setPackPath(GamePathType type, String path) {
		packPathType = type;
		customPackPath = path;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String name) {
		packageName = name;
		isDefaultPackageName = false;
	}
	
	public boolean useDefaultPackageName() {
		return isDefaultPackageName;
	}
	
	public static String getCompletePath(TreePath path) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(MainApp.getProjectsPath().getAbsolutePath());
		
		Object[] strs = path.getPath();
		for (Object s : strs) {
			filePath.append("\\");
			if (s instanceof ProjectTreeNode) {
				filePath.append(((ProjectTreeNode)s).name);
			} else {
				filePath.append(s);
			}
		}
		
		return filePath.toString();
	}
	
	public static String getRelativePath(Object[] path) {
		StringBuilder filePath = new StringBuilder();
		
//		Object[] strs = path.getPath();
		for (int i = 1; i < path.length; i++) {
			if (i != 1) filePath.append("\\");
			
			Object obj = path[i];
			if (obj instanceof ProjectTreeNode) {
				filePath.append(((ProjectTreeNode)obj).name);
			} 
			else {
				filePath.append(obj);
			}
		}
		
		return filePath.toString();
	}
	
	public static String getRelativePath(TreePath path) {
		return getRelativePath(path.getPath());
	}
	
	public static String getDefaultPackageName(String name) {
		return name.replaceAll("\\s", "_") + ".package";
	}
	
	private void loadNodes(ProjectTreeModel treeModel, File rootFolder, DefaultMutableTreeNode rootNode, boolean isMod, int level) {
		File[] folders = rootFolder.listFiles();
		
		for (File folder : folders) 
		{
			long nanoTime1 = System.nanoTime();
			//if (folder.isFile()) continue;
			
			// Try to find this folder in nodes:
			boolean found = false;
			for (int i = 0; i < rootNode.getChildCount(); i++) {
//				DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(i);
				
				ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(i);
				// we've found the file
				if (node.name.equals(folder.getName())) 
				{
					node.isMod = isMod;
					// No need to modify this; we'll have to add its subfolders, though
					if (folder.isDirectory()) {
						loadNodes(treeModel, folder, node, isMod, level + 1);
					}
					
					//TODO Load subfolders
					found = true;
					break;
				}
			}
			
			// this folder hasn't been loaded yet, we'll load it now
			if (!found) {
				if (folder.getName().equals(SETTINGS_FILE_NAME) && level == 0 /*folder.getParentFile().getName().equals(name)*/) {
					continue;
				}
				
				ProjectTreeNode newNodeObject = new ProjectTreeNode();
				// When we add the mod files the sources are already set, so if mod files haven't been found it means they don't have source
				newNodeObject.isMod = isMod;
				if (!isMod) {
					newNodeObject.isSource = true;
				}
				newNodeObject.name = folder.getName();
//				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNodeObject);
				
				if (folder.isDirectory()) {
					loadNodes(treeModel, folder, newNodeObject, isMod, level + 1);
				}
				
				if (treeModel != null) {
					// We want to order the nodes alphabetically
					// so first we'll find the closer node
//					treeModel.insertNodeInto(newNodeObject, rootNode, rootNode.getChildCount());
					
//					int ind = 0;
//					@SuppressWarnings("rawtypes")
//					Enumeration e = rootNode.children();
//					while (e.hasMoreElements()) {
//						ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
////						System.out.println(newNodeObject.name + "\t" + node.name + "\t" + newNodeObject.name.compareTo(node.name) + "\t" + ind);
//						// without ignore case it doesn't work for some reason
//						if (newNodeObject.name.compareToIgnoreCase(node.name) < 0) {
//							break;
//						}
//						ind++;
//					}
//					
//					treeModel.insertNodeInto(newNodeObject, rootNode, ind);
					
					treeModel.insertNode(newNodeObject, rootNode);
				} else {
					rootNode.add(newNodeObject);
				}
				//TODO Load subfolders
			}
			nanoTime1 = System.nanoTime() - nanoTime1;
//			System.out.println(folder.getName() + "\t" + nanoTime1);
//			System.out.println(String.format("%1$10d", nanoTime1));
		}
	}
	
//	private void loadNodesFast(DefaultTreeModel treeModel, File rootFolder, DefaultMutableTreeNode rootNode, boolean isMod, int level) {
//		File[] folders = rootFolder.listFiles();
//		
//		for (File folder : folders) 
//		{
//			long nanoTime1 = System.nanoTime();
//			//if (folder.isFile()) continue;
//			
//			// Try to find this folder in nodes:
//			boolean found = false;
//			for (int i = 0; i < rootNode.getChildCount(); i++) {
////				DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(i);
//				
//				ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(i);
//				// we've found the file
//				if (node.name.equals(folder.getName())) 
//				{
//					node.isMod = isMod;
//					// No need to modify this; we'll have to add its subfolders, though
//					if (folder.isDirectory()) {
//						loadNodes(treeModel, folder, node, isMod, level + 1);
//					}
//					
//					//TODO Load subfolders
//					found = true;
//					break;
//				}
//			}
//			
//			// this folder hasn't been loaded yet, we'll load it now
//			if (!found) {
//				if (folder.getName().equals(SETTINGS_FILE_NAME) && level == 0 /*folder.getParentFile().getName().equals(name)*/) {
//					continue;
//				}
//				
//				ProjectTreeNode newNodeObject = new ProjectTreeNode();
//				// When we add the mod files the sources are already set, so if mod files haven't been found it means they don't have source
//				newNodeObject.isMod = isMod;
//				if (!isMod) {
//					newNodeObject.isSource = true;
//				}
//				newNodeObject.name = folder.getName();
////				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNodeObject);
//				
//				if (folder.isDirectory()) {
//					loadNodes(treeModel, folder, newNodeObject, isMod, level + 1);
//				}
//				
//				if (treeModel != null) {
//					// We want to order the nodes alphabetically
//					// so first we'll find the closer node
////					treeModel.insertNodeInto(newNodeObject, rootNode, rootNode.getChildCount());
//					
//					int ind = 0;
//					@SuppressWarnings("rawtypes")
//					Enumeration e = rootNode.children();
//					while (e.hasMoreElements()) {
//						ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
////						System.out.println(newNodeObject.name + "\t" + node.name + "\t" + newNodeObject.name.compareTo(node.name) + "\t" + ind);
//						// without ignore case it doesn't work for some reason
//						if (newNodeObject.name.compareToIgnoreCase(node.name) < 0) {
//							break;
//						}
//						ind++;
//					}
//					
//					treeModel.insertNodeInto(newNodeObject, rootNode, ind);
//				} else {
//					rootNode.add(newNodeObject);
//				}
//				//TODO Load subfolders
//			}
//			nanoTime1 = System.nanoTime() - nanoTime1;
////			System.out.println(folder.getName() + "\t" + nanoTime1);
//			System.out.println(String.format("%1$10d", nanoTime1));
//		}
//	}
	
	private void loadNodes(ProjectTreeModel treeModel, File rootFolder, DefaultMutableTreeNode rootNode, int level) {
		loadNodes(treeModel, rootFolder, rootNode, false, level);
//		loadNodesFast(treeModel, rootFolder, rootNode, false, level);
	}
	
	public boolean loadNodesEx(ProjectTreeModel treeModel, DefaultMutableTreeNode rootNode) {
		long time1 = System.currentTimeMillis();
		//List<DefaultMutableTreeNode> result = new ArrayList<DefaultMutableTreeNode>(treeNodes);
		if (sources != null) {
			ListIterator<Project> iterable = sources.listIterator(sources.size());
			
			// We'll load the last important sources first 
			while (iterable.hasPrevious())
			{
				Project source = iterable.previous();
				
				File root = source.getProjectPath();
				
				loadNodes(treeModel, root, rootNode, 0);
			}
		}
		
		File rootFolder = getProjectPath();
		loadNodes(treeModel, rootFolder, rootNode, true, 0);
		
		time1 = System.currentTimeMillis() - time1;
		System.out.println("loadNodesEx: " + time1);
		return true;
	}
	
	
	
//	private class LoadedNode {
//		private SortedMap<String, LoadedNode> childs = new SortedMap<String, LoadedNode>();
//	}
	public boolean loadNodesFastEx(ProjectTreeModel treeModel, ProjectTreeNode rootNode) {
		long time1 = System.currentTimeMillis();
		//List<DefaultMutableTreeNode> result = new ArrayList<DefaultMutableTreeNode>(treeNodes);
		if (sources != null) {
			ListIterator<Project> iterable = sources.listIterator(sources.size());
			
			// We'll load the least important sources first 
			while (iterable.hasPrevious())
			{
				Project source = iterable.previous();
				
				File root = source.getProjectPath();
				
				loadNodesFast(treeModel, root, rootNode, false, 0, true);
			}
		}
		
		File rootFolder = getProjectPath();
		loadNodesFast(treeModel, rootFolder, rootNode, true, 0, true);
		
		time1 = System.currentTimeMillis() - time1;
		System.out.println("loadNodesFastEx: " + time1);
		
//		JOptionPane.showMessageDialog(MainApp.getUserInterface(), Double.toString(time1 / 1000.0) + " ms to load project.");
		
		return true;
	}
	
	public static void loadNodesFast(ProjectTreeModel treeModel, File rootFolder, ProjectTreeNode rootNode, boolean isMod, int level, boolean parentExists) {
		File[] folders = rootFolder.listFiles();
		
		for (File folder : folders) 
		{
			boolean isDirectory = folder.isDirectory();
			
			if (level == 0 && !isDirectory) {
				continue;
			}
			
			// Try to find this folder in nodes:
			String name = folder.getName();
			
			int childIndex = -1;
			// no need to search it if we know it won't be there
			if (parentExists) {
				childIndex = rootNode.getChildIndex(name);
			}
			
			if (childIndex != -1) {
				ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(childIndex);  // no filters
				node.isMod = isMod;
				// No need to modify this; we'll have to add its subfolders, though
				if (isDirectory) {
					loadNodesFast(treeModel, folder, node, isMod, level + 1, true);
				}
			}
			else {
				// this folder hasn't been loaded yet, we'll load it now
				
				ProjectTreeNode newNodeObject = new ProjectTreeNode(name, false);
				// When we add the mod files the sources are already set, so if mod files haven't been found it means they don't have source
				newNodeObject.isMod = isMod;
				if (!isMod) {
					newNodeObject.isSource = true;
				}
				
				if (isDirectory) {
					// the node didn't exist, so their children won't exist neither
					loadNodesFast(treeModel, folder, newNodeObject, isMod, level + 1, false);
				}
				
				treeModel.insertNode(newNodeObject, rootNode);
			}
			
		}
	}
	
	public static void main(String[] args) {
		System.out.println("f".compareTo("a"));
		System.out.println("f".compareTo("s"));
		System.out.println("f".compareTo("S"));
		System.out.println("folder inside folder".compareTo("SRNS-scn_effects_televisionPlanet02.prop.xml"));
	}
}
