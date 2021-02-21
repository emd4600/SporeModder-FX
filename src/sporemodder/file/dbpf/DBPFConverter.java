package sporemodder.file.dbpf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.FileStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import sporemodder.FileManager;
import sporemodder.FormatManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.util.ProjectItem;
import sporemodder.view.dialogs.PackProgressUI;
import sporemodder.view.dialogs.ProgressDialogUI;
import sporemodder.view.dialogs.UnpackPackageUI;

public class DBPFConverter implements Converter {
	
	private static String extension = null;
	public static final int TYPE_ID = 0x06EFC6AA;
	
	private DBPFUnpackingTask createUnpackTask(StreamReader stream, File outputFile) throws Exception {
		if (outputFile.exists() && outputFile.isDirectory()) {
			for (File file : outputFile.listFiles()) {
				file.delete();
			}
		}
		outputFile.mkdir();
		
		List<Converter> converters = new ArrayList<>();
		for (Converter c : FormatManager.get().getConverters()) {
			if (c.isEnabledByDefault()) converters.add(c);
		}
		
		stream.setBaseOffset(stream.getFilePointer());
		DBPFUnpackingTask task = new DBPFUnpackingTask(stream, outputFile, null, converters);

		return task;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		long oldBase = stream.getBaseOffset();
		
		DBPFUnpackingTask task = createUnpackTask(stream, Converter.getOutputFile(key, outputFolder, "unpacked"));
		task.call();
		
		stream.setBaseOffset(oldBase);
		
		// Always return true, even if there were errors?
		return true;
		
	}
	
	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		DBPFPackingTask task = new DBPFPackingTask(input, output);
		task.call();
		
		return true;
	}

	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			String[] splits = input.getName().split("\\.", 2);
			
			ResourceKey name = packer.getTemporaryName();
			name.setInstanceID(HashManager.get().getFileHash(splits[0]));
			name.setGroupID(groupID);
			name.setTypeID(TYPE_ID);  // soundProp or prop
			
			packer.writeFile(name, stream -> {
				DBPFPackingTask task = new DBPFPackingTask(input, stream);
				task.call();
				
				// The task will have disabled this, enable it again
				HashManager.get().setUpdateProjectRegistry(true);
			});
			
			return true;
		}
		return false;
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == TYPE_ID;
	}

	private void checkExtensions() {
		if (extension == null) {
			extension = HashManager.get().getTypeName(TYPE_ID);
		}
	}
	
	@Override
	public boolean isEncoder(File file) {
		checkExtensions();
		return file.isDirectory() && file.getName().endsWith("." + extension + ".unpacked");
	}

	@Override
	public String getName() {
		return "Localization Package (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return false;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		return TYPE_ID;
	}
	
	private void unpackDialog(File inputFile, File outputFile) throws Exception {
		try (StreamReader stream = new FileStream(inputFile, "r")) {
			DBPFUnpackingTask task = createUnpackTask(stream, outputFile);
			
			ProgressDialogUI progressUI = UIManager.get().loadUI("dialogs/ProgressDialogUI");
			Dialog<ButtonType> progressDialog = progressUI.createDialog(task);
			progressDialog.setTitle("Unpacking " + inputFile.getName());
			
			progressUI.setOnSucceeded(() -> {
				if (task.getValue() != null) {
					UIManager.get().showErrorDialog(task.getValue(), "Could not unpack file.", false);
				}
				else if (task.getExceptions().isEmpty()) {
					Alert alert = new Alert(AlertType.INFORMATION, "Unpack finished", ButtonType.OK);
					alert.setContentText("Successfully unpacked in " + (task.getEllapsedTime() / 1000.0f) + " seconds.");
					UIManager.get().showDialog(alert);
				}
				else {
					UnpackPackageUI.showErrorDialog(task);
				}
			});
			
			progressUI.setOnFailed(() -> {
				UIManager.get().showErrorDialog(task.getException(), "Fatal error, file could not be unpacked", true);
			});
			
			
			// Show progress
			progressUI.getProgressBar().progressProperty().bind(task.progressProperty());
			progressUI.getLabel().textProperty().bind(task.messageProperty());
			
			UIManager.get().showDialog(progressDialog);
			
			// Ensure the overlay is not showing
			UIManager.get().setOverlay(false);
		}
	}
	
	private void packDialog(File inputFolder, File outputFile) throws Exception {
		String projectName = FileManager.removeExtension(outputFile.getName());
		
		PackProgressUI.show(inputFolder, outputFile, projectName, false);
		
		// Ensure the overlay is not showing
		UIManager.get().setOverlay(false);
	}

	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot()) {
			
			if (item.isMod() && isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Pack into ." + HashManager.get().getTypeName(TYPE_ID));
				menuItem.setMnemonicParsing(false);
				menuItem.setOnAction(event -> {
					// This is after isEncoder(), so we can assume it has extension
					final String name = item.getName().substring(0, item.getName().lastIndexOf("."));
					final File outputFile = new File(item.getFile().getParentFile(), name);
					
					boolean result = UIManager.get().tryAction(() -> {
						packDialog(item.getFile(), outputFile);
						ProjectManager.get().selectItem(ProjectManager.get().getSiblingItem(item, name));
					}, "Cannot encode file.");
					if (!result) {
						// Delete the file, as it hasn't been written properly
						outputFile.delete();
					}
				});
				contextMenu.getItems().add(menuItem);
			}
			else {
				ResourceKey key = ProjectManager.get().getResourceKey(item);
				
				if (isDecoder(key)) {
					MenuItem menuItem = new MenuItem("Unpack ." + HashManager.get().getTypeName(TYPE_ID));
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "unpacked");
						boolean result = UIManager.get().tryAction(() -> {
							
							unpackDialog(item.getFile(), outputFile);
							ProjectManager.get().selectItem(ProjectManager.get().getSiblingItem(item, outputFile.getName()));
								
						}, "Cannot decode file.");
						if (!result) {
							// Delete the file, as it hasn't been written properly
							outputFile.delete();
						}
					});
					contextMenu.getItems().add(menuItem);
				}
			}
		}
	}

}
