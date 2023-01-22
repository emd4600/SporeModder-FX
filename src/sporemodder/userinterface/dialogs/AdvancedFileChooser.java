package sporemodder.userinterface.dialogs;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

import net.tomahawk.ExtensionsFilter;
import net.tomahawk.XFileDialog;

import sporemodder.MainApp;
import sporemodder.userinterface.ErrorManager;

public class AdvancedFileChooser implements ActionListener {
	public static enum ChooserType {OPEN, SAVE};
	
	private JTextField target;
	private FileNameExtensionFilter[] filters;
	private Component parent;
	private int selectionMode;
	private boolean multiSelectionEnabled;
	private String defaultFile = null;
	private ChooserType type = ChooserType.OPEN;
	
	public AdvancedFileChooser(JTextField target, Component parent, int selectionMode, boolean multiSelectionEnabled, ChooserType type, FileNameExtensionFilter ... filters) {
		this.target = target;
		this.filters = filters;
		this.parent = parent;
		this.selectionMode = selectionMode;
		this.multiSelectionEnabled = multiSelectionEnabled;
		this.type = type;
	}
	
	public AdvancedFileChooser(JTextField target, Component parent, int selectionMode, boolean multiSelectionEnabled, String defaultFile, ChooserType type, FileNameExtensionFilter ... filters) {
		this.target = target;
		this.filters = filters;
		this.parent = parent;
		this.selectionMode = selectionMode;
		this.multiSelectionEnabled = multiSelectionEnabled;
		this.defaultFile = defaultFile;
		this.type = type;
	}
	
	public AdvancedFileChooser(JTextField target, Component parent, ChooserType type, FileNameExtensionFilter ... filters) {
		this(target, parent, JFileChooser.FILES_AND_DIRECTORIES, true, type, filters);
	}
	
	public AdvancedFileChooser(JTextField target, Component parent, String defaultFile, ChooserType type, FileNameExtensionFilter ... filters) {
		this(target, parent, JFileChooser.FILES_AND_DIRECTORIES, true, defaultFile, type, filters);
	}
	
	public void setSelectionMode(int selectionMode, boolean multiSelectionEnabled) {
		this.selectionMode = selectionMode;
		this.multiSelectionEnabled = multiSelectionEnabled;
	}
	
	public int getSelectionMode() {
		return selectionMode;
	}
	
	public boolean isMultipleSelectionEnabled() {
		return multiSelectionEnabled;
	}
	
	
	// launches the correct chooser depending on the system
	public String launch() {
		boolean isWindows = System.getProperty("os.name").contains("Windows");
		
		if (isWindows) {
			// use xfiledialog
			try {
				return launchWindows();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(parent, "Error with file dialog:\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		else {
			return launchDefault();
		}
		
	}
	
	private String launchWindows() throws IOException {
		XFileDialog.setTraceLevel(0);
		XFileDialog dialog = new XFileDialog(parent);
		dialog.setTitle("Open Files");
		
//		File lastFolder = MainApp.getLastFileChooserPath();
//		String lastPath = null;
//		
//		if (lastFolder != null) {
//			lastPath = lastFolder.getAbsolutePath();
//			dialog.setDirectory(lastPath);
//		}
//		else {
//			lastPath = dialog.getDirectory();
//		}
		
//		if (!lastPath.endsWith("\\")) lastPath += "\\";
		
		if (filters != null) {
			for (int i = 0; i < filters.length; i++) {
				if (filters[i] != null) {
					ExtensionsFilter extFilter = new ExtensionsFilter(filters[i].getDescription(), Arrays.asList(filters[i].getExtensions()));
					
					dialog.addFilters(extFilter);
				}
			}
		}
		
//		if (defaultFile != null) {
//			dialog.setDirectory(lastPath + defaultFile);
//		}
		
		File file = null;
		String path = null;
		File selectedFile = null;
		
		while (file == null) {
			if (multiSelectionEnabled && type != ChooserType.SAVE) {
				String[] files = null;
				if (selectionMode == JFileChooser.DIRECTORIES_ONLY) {
					files = dialog.getFolders();
				} else {
					files = dialog.getFiles();
				}
				
				if (files == null || files.length == 0) {
					return null;
				}
				
				StringBuilder sb = new StringBuilder();
				
				for (int i = 0; i < files.length; i++) {
					// weird way to check if it's a relative path
//					if (!files[i].contains(":\\")) {
//						sb.append(lastPath);
//					}
					if (!files[i].contains(":\\")) {
						sb.append(new File(dialog.getDirectory(), files[i]).getCanonicalPath());
					}
					else {
						sb.append(files[i]);
					}
					if (i != files.length-1) {
						sb.append("|");
					}
				}
				
				path = sb.toString();
				
				if (!files[0].contains(":\\")) {
					selectedFile = new File(dialog.getDirectory(), files[0]).getCanonicalFile();
				}
				else {
					selectedFile = new File(files[0]);
				}
			}
			else {
				if (selectionMode == JFileChooser.DIRECTORIES_ONLY) {
					path = dialog.getFolder();
				} else {
					path = dialog.getSaveFile();
					//path = type == ChooserType.SAVE ? dialog.getSaveFile() : dialog.getFile();
				}
				if (path == null) {
					return null;
				}
				if (!path.contains(":\\")) {
					path = new File(dialog.getDirectory(), path).getCanonicalPath();
				}
				
				selectedFile = new File(path);
			}
			
			// msut only check on files
			if (defaultFile != null && selectionMode == JFileChooser.FILES_ONLY) {
				if (!showExtensionError(selectedFile.getName(), getExtension(defaultFile))) {
					continue;
				}
			}
			file = selectedFile.getParentFile();
		}
		
		System.out.println(path);
		
		MainApp.setLastFileChooserPath(file);
		return path;
	}
	
	private String launchDefault() {
		JFileChooser chooser = new JFileChooser();
		
		File lastFolder = MainApp.getLastFileChooserPath();
		String lastPath = null;
		
		if (lastFolder != null) {
			chooser.setCurrentDirectory(lastFolder);
			lastPath = lastFolder.getAbsolutePath();
		}
		else {
			lastPath = chooser.getCurrentDirectory().getAbsolutePath();
		}
		
		
		chooser.setFileSelectionMode(selectionMode);
		chooser.setMultiSelectionEnabled(multiSelectionEnabled);
		if (filters != null && filters.length > 0) {
			chooser.addChoosableFileFilter(filters[0]);
			chooser.setFileFilter(filters[0]);
		}
		if (defaultFile != null) {
			try {
		        FileChooserUI fcUi = chooser.getUI();
		        chooser.setSelectedFile(new File(lastPath + "\\" + defaultFile));
		        Class<? extends FileChooserUI> fcClass = fcUi.getClass();
		        Method setFileName = fcClass.getMethod("setFileName", String.class);
		        setFileName.invoke(fcUi, defaultFile);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		int result = type == ChooserType.SAVE ? chooser.showSaveDialog(parent) : chooser.showOpenDialog(parent);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = null;
			String path = null;
			
			if (multiSelectionEnabled) {
				File[] files = chooser.getSelectedFiles();
				StringBuilder sb = new StringBuilder();
				
				for (int i = 0; i < files.length; i++) {
					sb.append(files[i].getAbsolutePath());
					if (i != files.length-1) {
						sb.append("|");
					}
				}
				
				path = sb.toString();
				file = files[0].getParentFile();
			}
			else {
				file = chooser.getSelectedFile().getParentFile();
				path = chooser.getSelectedFile().getAbsolutePath();
			}
			
			
			showExtensionError(file.getName(), getExtension(defaultFile));
			
			System.out.println(path);
			
			MainApp.setLastFileChooserPath(file);
			return path;
		}
		
		return null;
	}
	
	private static String getExtension(String name) {
		int indexOf = name.indexOf(".");
		if (indexOf == -1) {
			return "";
		} else {
			return name.substring(indexOf + 1);
		}
	} 
	
	// returns a boolean if the user didn't accept it
	private boolean showExtensionError(String name, String extension) {
		if (extension != null && !name.endsWith("." + extension)) {
			return JOptionPane.showConfirmDialog(parent, "Incorrect file type, ." + extension + " expected. Are you sure you want to select this file?", 
					"Incorrect file type", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
		}
		return true;
	}
	
	private static String getExtensionsString(String[] ext) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ext.length; i++) {
			sb.append("*.");
			sb.append(ext[i]);
			if (i + 1 < ext.length) {
				sb.append(";");
			}
		}
		return sb.toString();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String result = launch();
//		String result = launchAdvanced();
		if (target != null && result != null) {
			target.setText(result);
		}
	}
	
	public static class FieldFileDrop extends DropTarget {
		/**
		 * 
		 */
		private static final long serialVersionUID = 23453739596477103L;
		
		private JTextField target;
		//TODO this is for the output file; is it necessary?
//		private boolean singleFile;
		
		public FieldFileDrop(JTextField target) {
			this.target = target;
//			this.singleFile = singleFile;
		}
		
		@Override
		public synchronized void drop(DropTargetDropEvent event) {
			event.acceptDrop(DnDConstants.ACTION_COPY);
			try {
				@SuppressWarnings("unchecked")
				List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				
				int count = droppedFiles.size();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < droppedFiles.size(); i++) {
					sb.append(droppedFiles.get(i));
					
					if (i != count-1) {
						sb.append("|");
					}
				}
				
				target.setText(sb.toString());
				
			} catch (UnsupportedFlavorException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
