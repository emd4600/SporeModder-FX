/****************************************************************************
* Copyright (C) 2018 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/

package sporemodder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.imageio.ImageIO;

import javafx.animation.FadeTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sporemodder.MessageManager.MessageType;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.UserInterface;
import sporemodder.view.dialogs.ChooseStyleUI;

/**
 * This class manages everything related with user interface in SporeModder. It contains some utility methods to load images and user interfaces.
 * Most important methods are:
 * <li>{@link #loadUI(String)}: Loads an FXML user interface controller.</li>
 * <li>{@link #loadImage(String)} and {@link #loadIcon(String)}: Loads an image, depends on the current style.</li>
 * <li>{@link #getUserInterface()}: Returns the main user interface object.</li>
 * <li>{@link #addStylesheets(List)}: Loads .css stylesheet files to be used when styling the UI, syntax highlighting, etc </li>
 * <li>{@link #tryAction(SimpleAction, String)}: Executes an action an shows an error dialog if something fails.</li>
 */
public class UIManager extends AbstractManager {
	
	/**
	 * Returns the current instance of the UIManager class.
	 */
	public static UIManager get() {
		return MainApp.get().getUIManager();
	}
	
	/**
	 * Used by the {@link MessageType.OnUILoad} message. Contains two things:
	 * <li><b>name</b>: The name of the FXML layout loaded, relative to the <code>sporemodder.view</code> package. For example <i>EditorPaneUI</i>, <i>dialogs/CreateProjectUI</i>,...
	 * <li><b>controller</b>: The loaded UI class. Cast it to the appropriate type in the <code>sporemodder.view</code> package to use it
	 */
	public static class UILoadMessage {
		public String name;
		public Controller controller;
	}
	
	private static final String PROPERTY_hasSelectedStyle = "hasSelectedStyle";
	private static final String PROPERTY_selectedStyle = "selectedStyle";
	private static final String PROPERTY_isFirstTime = "isFirstTime";
	
	/** The current style used in the program. */
	private String currentStyle = "Default";
	/** The style that will be saved in the settings. This avoids weird changes while the program is open, if the user changes the style. */
	private String selectedStyle = currentStyle;
	private boolean hasSelectedStyle = false;
	
	private boolean isShowingOverlay;
	
	private Stage primaryStage;
	private Scene mainScene;
	private final List<UIUpdateListener> updateListeners = new ArrayList<UIUpdateListener>();
	
	/** The class that controls the structure of the user interface. */
	private UserInterface userInterface;
	
	/** The .css files that must be loaded after the JavaFX scene is shown. */
	private final List<String> stylesheetsToLoad = new ArrayList<String>();
	
	private boolean showingIntroUI = true;
	
	private boolean isFirstTime = true;
	
	
	private ProgressBar taskbarProgress;
	// We need it to take the snapshot
	private boolean isShowingTaskbarProgress;
	private double programProgress;
	
	private Image programIcon;
	
	private double dpiScaling;

	private String initializationError;
	

	/**
	 * Returns the JavaFX main Scene.
	 */
	public Scene getScene() {
		return mainScene;
	}
	
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setInitializationError(String initializationError) {
		this.initializationError = initializationError;
	}

	public void showInitializationError() {
		if (initializationError != null) {
			UIManager.get().showDialog(Alert.AlertType.ERROR, initializationError);
		}
	}

	@Override
	public void initialize(Properties properties) {
		
		currentStyle = selectedStyle = properties.getProperty(PROPERTY_selectedStyle, "Default");
		isFirstTime = Boolean.parseBoolean(properties.getProperty(PROPERTY_isFirstTime, "true"));
		hasSelectedStyle = Boolean.parseBoolean(properties.getProperty(PROPERTY_hasSelectedStyle, "false"));
		
		taskbarProgress = new ProgressBar();
		
		new Scene(taskbarProgress);
		
		taskbarProgress.setPrefWidth(32);
		taskbarProgress.setPrefHeight(32);
		
		programIcon = loadImage("program-icon.png");
	}
	
	@Override public void saveSettings(Properties properties) {
		properties.put(PROPERTY_selectedStyle, selectedStyle);
		properties.put(PROPERTY_isFirstTime, Boolean.toString(isFirstTime));
		properties.put(PROPERTY_hasSelectedStyle, Boolean.toString(hasSelectedStyle));
	}
	
	
	/**
	 * Loads all the user interface and prepares it to be shown. This does not set the user interface visible.
	 * @param primaryStage The JavaFX Stage object.
	 */
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		dpiScaling = Font.getDefault().getSize() / 12.0;
		
		restoreTitle();
		primaryStage.getIcons().setAll(programIcon);
		
		boolean mustSaveSettings = false;
		if (!hasSelectedStyle) {
			try {
				String style = ChooseStyleUI.show(programIcon);
				if (style != null) {
					currentStyle = style;
					selectedStyle = style;
					hasSelectedStyle = true;
					mustSaveSettings = true;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		userInterface = new UserInterface();
		userInterface.initialize();
		
		if (mainScene == null) {
			mainScene = new Scene(userInterface, 1400, 700);
			primaryStage.setScene(mainScene);
			
			// Apply styling
			mainScene.getStylesheets().add(PathManager.get().getStyleFile("basic.css").toURI().toString());
			mainScene.getStylesheets().add(PathManager.get().getStyleFile("syntax.css").toURI().toString());
			mainScene.getStylesheets().add(PathManager.get().getStyleFile("color-swatch.css").toURI().toString());
			
			mainScene.getStylesheets().addAll(stylesheetsToLoad);
		}
		
		if (mustSaveSettings) {
			MainApp.get().saveSettings();
		}
	}
	
	public double scaleByDpi(double inValue) {
		return inValue * dpiScaling;
	}
	
	/**
	 * Shows the main user interface. This should only be used by MainApp.
	 */
	public void show() {
		
		restoreTitle();
		
		primaryStage.setMaximized(true);
	
		primaryStage.show();
	}
	
	/**
	 * Removes the introduction UI and shows the main user interface, with the editor, inspector and project tree.
	 */
	public void showMainUI() {
		if (showingIntroUI) {
			userInterface.showMainUI();
			showingIntroUI = true;
		}
	}
	
	/**
	 * Resets the title of the main stage to the original, which is "SporeModder FX [Version]"
	 */
	public void restoreTitle() {
		primaryStage.setTitle("SporeModder FX " + UpdateManager.get().getVersionInfo());
	}
	
	
	/**
	 * Sets the additional info to the program title; this is usually the project name. The resulting title will be "SporeModder FX [Version] - [Info]".
	 * If you want to remove the additional info text, use {@link #restoreTitle()}
	 * @param info
	 */
	public void setTitleInfo(String info) {
		primaryStage.setTitle("SporeModder FX " + UpdateManager.get().getVersionInfo() + " - " + info);
	}
	
	/**
	 * Loads the user interface with the given name and returns its controller. If a layout does not have a controller, a basic instance of
	 * the Controller class is returned so that it's possible to get the main layout node.
	 * Example names: "ProjectTreeUI", "dialogs/ConvertSPUIDialogUI". 
	 * If there is an error while loading the user interface, it returns null.
	 * <p>
	 * This method generates a {@link MessageType.OnUILoad} message with parameters of type {@link UILoadMessage}.
	 * @param name The name of the user interface to load, with no extension; it allows subfolders. For example: "ProjectTreeUI", "dialogs/ConvertSPUIDialogUI".
	 * @return The controller of the user interface, or null if there's an error..
	 */
	@SuppressWarnings("unchecked")
	public <T extends Controller> T loadUI(String name) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(UIManager.class.getResource("/sporemodder/view/" + name + ".fxml"));
			Node main = loader.load();
			Controller controller = loader.getController();
			
			if (controller == null) {
				controller = new Controller.PlaceholderController(main);
			}
			UILoadMessage message = new UILoadMessage();
			message.name = name;
			message.controller = controller;
			MessageManager.get().postMessage(MessageType.OnUILoad, message);
			return (T) controller;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * Same as {@link #loadUI(String)}, but here the URL itself must be specified.
	 * Returns the loaded class that extends {@link Controller}.
	 * If the file cannot be loaded, returns null.
	 * @param url
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Controller> T loadUI(URL url) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(url);
			Node main = loader.load();
			Controller controller = loader.getController();
			
			return controller != null ? (T) controller : (T) new Controller.PlaceholderController(main);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * Loads the image with the given name and returns an ImageView capable of displaying it, using the specified fit width/height.
	 * Setting a value of 0 for those will use the image's width/height.
	 * @param name
	 * @return
	 */
	public ImageView loadIcon(String name, double fitWidth, double fitHeight, boolean preserveRatio) {
		try {
		Image image = loadImage(name);
		
		ImageView imageView = new ImageView();
		imageView.setImage(image);
//		imageView.setFitWidth(fitWidth);
//		imageView.setFitHeight(fitHeight);
		imageView.setFitWidth(scaleByDpi(fitWidth));
		imageView.setFitHeight(scaleByDpi(fitHeight));
		imageView.setPreserveRatio(preserveRatio);
		
		return imageView;
		}
		catch (Exception ex) {
			return new ImageView();
		}
	}
	
//	public ImageView loadIconDPI(String name, double fitWidth, double fitHeight, boolean preserveRatio) {
//		Image image = loadImage(name);
//		
//		ImageView imageView = new ImageView();
//		imageView.setImage(image);
//		imageView.setFitWidth(scaleByDpi(fitWidth));
//		imageView.setFitHeight(scaleByDpi(fitHeight));
//		imageView.setPreserveRatio(preserveRatio);
//		
//		return imageView;
//	}
	
	/**
	 * Loads the image with the given name and returns an ImageView capable of displaying it. 
	 * @param name
	 * @return
	 */
	public ImageView loadIcon(String name) {
		return loadIcon(name, 0, 0, true);
	}
	
	/**
	 * Loads the image with the given name and returns an Image object that contains it.
	 * @param name
	 * @return
	 */
	public Image loadImage(String name) {
		return new Image(PathManager.get().getStyleFile(name).toURI().toString());
	}
	
	/**
	 * Returns the object that contains all the structure of the user interface.
	 */
	public UserInterface getUserInterface() {
		return userInterface;
	}
	
	/**
	 * Adds .css stylesheets that will be loaded once the UI is shown. If the UI is already being shown,
	 * the .css files will be loaded inmediately. 
	 * @param paths A list with the paths to the .css files.
	 */
	public void addStylesheets(List<String> paths) {
		
		stylesheetsToLoad.addAll(paths);
		
		if (mainScene != null) {
			mainScene.getStylesheets().addAll(stylesheetsToLoad);
		}
	}
	
	/**
	 * Adds an object that will be notified whenever a change in the user interface happens.
	 * @param listener
	 */
	public void addListener(UIUpdateListener listener) {
		updateListeners.add(listener);
	}
	
	/**
	 * Notifies a change in the user interface: this will call all {@link UIUpdateListener} instances
	 * that have been added to the manager.
	 * @param isFirstUpdate Whether this is the first UI update that ever happens in the program.
	 */
	public void notifyUIUpdate(boolean isFirstUpdate) {
		for (UIUpdateListener listener : updateListeners) {
			listener.onUIUpdate(isFirstUpdate);
		}
	}
	
	/**
	 * Returns the style that is currently being displayed.
	 * @return
	 */
	public String getCurrentStyle() {
		return currentStyle;
	}
	
	/**
	 * Returns the currently selected style. This is no necessarily the stye that is on display; this is the one that will be saved
	 * into the program configuration, and therefore will be displayed the enxt time the program is started.
	 * @return
	 */
	public String getSelectedStyle() {
		return selectedStyle;
	}
	
	/**
	 * Sets the currently selected style. This is not necessarily the style that is on display; this is the one that will be saved
	 * into the program configuration, and therefore will be displayed the next time the program is started.
	 * @return
	 */
	public void setSelectedStyle(String style) {
		selectedStyle = style;
	}
	
	/**
	 * Returns a list of all the available style names. Those are taken from the "Styles" folder in the program.
	 * @return
	 */
	public List<String> getAvailableStyles() {
		List<String> list = new ArrayList<>();
		for (File f : PathManager.get().getProgramFile("Styles").listFiles()) {
			if (f.isDirectory()) list.add(f.getName()); 
		}
		return list;
	}
	
	
	/**
	 * Enables or disables the gray overlay that is shown above the user interface, but below alerts. 
	 * Generally you do not need to set this manually, but it's recommended when you use non-SporeModder dialogs (such as FileChooser)
	 * for consistency reasons.
	 * @param isEnabled
	 */
	public void setOverlay(boolean isEnabled) {
		Pane overlay = userInterface.getDialogOverlay();
		
		if (isEnabled && isShowingOverlay) return;
		if (!isEnabled && !isShowingOverlay) return;
		
		overlay.setMouseTransparent(!isEnabled);
		
		FadeTransition fade = new FadeTransition(Duration.millis(200), overlay);
		fade.setAutoReverse(false);
		fade.setCycleCount(1);
		
		if (isEnabled) {
			fade.setFromValue(0);
			fade.setToValue(1);
		} else {
			fade.setFromValue(1);
			fade.setToValue(0);
		}
		
		fade.play();
		
		isShowingOverlay = isEnabled;
	}
	
	/**
	 * Returns whether the gray overlay (blocks the user interface but not dialogs) is being shown.
	 * @return
	 */
	public boolean isShowingOverlay() {
		return isShowingOverlay;
	}
	
	/**
	 * Shows a dialog, blocking user input from the main stage. While the dialog is shown, a dark overlay is shown over the main stage
	 * to drag the user attention into the dialog. 
	 * <p>
	 * The parameter <code>disableOverlayOnClose</code> tells whether the overlay should be disabled when the dialog closes.
	 * Usually this is the wanted behavior, but when another dialog will be shown after this one it's recommended to set this
	 * parameter to true, as this will avoid the overlay blinking in and out.
	 * @param dialog
	 * @param disableOverlayOnClose Whether the overlay should be removed when the dialog is closed.
	 * @return
	 */
	public <R> Optional<R> showDialog(Dialog<R> dialog, boolean disableOverlayOnClose) {
		
		boolean wasShowingOverlay = isShowingOverlay;
		
		setOverlay(true);
		
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(primaryStage);
		Optional<R> result = dialog.showAndWait();
		
		// Don't disable the overlay if there was another dialog showing
		if (disableOverlayOnClose && !wasShowingOverlay) {
			setOverlay(false);
		}
		
		return result;
	}
	
	/**
	 * Shows a dialog using {@link #showDialog(Dialog, boolean)}, always disabling the overlay after the dialog is closed.
	 * @param dialog
	 * @return
	 */
	public <R> Optional<R> showDialog(Dialog<R> dialog) {
		return showDialog(dialog, true);
	}
	
	public void showDialog(AlertType type, String text) {
		Label label = new Label();
		label.setWrapText(true);
		
		label.setText(text);
		
		Alert alert = new Alert(type, null, ButtonType.OK);
		alert.getDialogPane().setContent(label);
		showDialog(alert);
	}
	
	public void showDialog(String text) {
		showDialog(AlertType.NONE, text);
	}
	
	
	public boolean isShowingTaskbarProgress() {
		return isShowingTaskbarProgress;
	}
	
	private void setProgramProgress(double progress) {
		
		programProgress = progress;
		
		if (progress == 1.0) {
			// Stop showing when the progress is completed
			isShowingTaskbarProgress = false;
			restoreTaskbarProgress();
		}
		else {
			isShowingTaskbarProgress = true;
			// Produces an annoying blinking in the program icon and slows down the process a lot
//			updateTaskbarProgress();
		}
	}
	
	private double getTaskbarProgress() {
		return taskbarProgress.getProgress();
	}
	
	private void updateTaskbarProgress() {
//		WritableImage image = new WritableImage((int) taskbarProgress.getPrefWidth(), (int) taskbarProgress.getPrefHeight());
//		
//		taskbarProgress.snapshot(new SnapshotParameters(), image);
		
		WritableImage image = new WritableImage((int) programIcon.getWidth(), (int) programIcon.getHeight());
		
//		// #497743
//		double r = 0x49 / 255.0;
//		double g = 0x77 / 255.0;
//		double b = 0x43 / 255.0;
		// #5CAD51
		double r = 0x5C / 255.0;
		double g = 0xAD / 255.0;
		double b = 0x51 / 255.0;
		double opacity = 0.9;
		
		double xLimit = programIcon.getWidth() * programProgress;
		
		PixelReader iconReader = programIcon.getPixelReader();
		PixelWriter writer = image.getPixelWriter();
		
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color iconColor = iconReader.getColor(x, y);
				
				if (x <= xLimit) {
					double a = iconColor.getOpacity();
					double ia = 1.0 - a;
					
					if (a == 0) {
						writer.setColor(x, y, new Color(r, g, b, opacity));
					} else {
						writer.setColor(x, y, new Color(iconColor.getRed()*a + r*ia, iconColor.getGreen()*a + g*ia, iconColor.getBlue()*a + b*ia, 1.0));
					}
				}
				else {
					writer.setColor(x, y, iconColor);
				}
			}
		}
		
		try {
			File tempFile = File.createTempFile("temp-sporemodder-icon", ".png");
			
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", tempFile);
			
			primaryStage.getIcons().setAll(new Image(tempFile.toURI().toString()));
			
			tempFile.delete();
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void restoreTaskbarProgress() {
		primaryStage.getIcons().setAll(programIcon);
	}
	
	/**
	 * Returns a node that represents one of the following alert icons:
	 * <li>CONFIRMATION</li>
	 * <li>ERROR</li>
	 * <li>INFORMATION</li>
	 * <li>WARNING</li>
	 * @param type
	 * @param width
	 * @param height
	 * @return
	 */
	public Node getAlertIcon(AlertType type, int width, int height) {
		String typeImage = null;
		if (type == AlertType.CONFIRMATION) {
			typeImage = "dialog-confirmation";
		}
		else if (type == AlertType.ERROR) {
			typeImage = "dialog-error";
		}
		else if (type == AlertType.INFORMATION) {
			typeImage = "dialog-information";
		}
		else if (type == AlertType.WARNING) {
			typeImage = "dialog-warning";
		}
		
		return loadIcon(typeImage + ".png", width, height, true);
	}
	
	@FunctionalInterface
	public interface SimpleAction {
		public void doAction() throws Exception;
	}
	
	/**
	 * Executes an action that might cause an exception: if it does throw an exception, an error
	 * dialog will be shown to the user. This will be done with the {@link #showErrorDialog(Throwable, String, boolean)}
	 * method, which shows a dialog with a simple error message and information about the exception.
	 * <p>
	 * This method can take a lambda, for example:
	 * {@code tryAction(() -> ProjectManager.get().removeItem(item), "The item cannot be removed")}
	 * @param action
	 * @param errorText
	 * @return
	 */
	public boolean tryAction(SimpleAction action, String errorText) {
		try {
			action.doAction();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(e, errorText, true);
			return false;
		}
	}

	public boolean tryAction(SimpleAction action, String errorText, boolean disableOverlay) {
		try {
			action.doAction();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(e, errorText, disableOverlay);
			return false;
		}
	}
	
	/**
	 * Shows an error dialog that displays information about an exception. This method blocks execution until the user closes the dialog.
	 * @param e The exception, part of its stack trace will be shown.
	 * @param errorText The basic error text used to inform the user. 
	 * @param disableOverlay Whether the dialog overlay must be disabled once the dialog is closed 
	 * (use <code>false</code> if you will show more dialogs after this).
	 */
	public void showErrorDialog(Throwable e, String errorText, boolean disableOverlay) {
		Alert alert = new Alert(AlertType.ERROR, (errorText == null ? "" : (errorText + " ")), ButtonType.OK);
		if (e.getLocalizedMessage() != null) {
			alert.getDialogPane().setContentText(alert.getDialogPane().getContentText() + e.getLocalizedMessage());
		}
		alert.setTitle("Error");
		alert.getDialogPane().setExpandableContent(createExceptionArea(e));
		
		showDialog(alert, disableOverlay);
	}
	
	/**
	 * Creates a text area node that shows the stack trace of a throwable exception.
	 * @param e
	 * @return
	 */
	public Node createExceptionArea(Throwable e) {
		TextArea infoText = new TextArea(getStackTraceString(e, ""));
		infoText.setEditable(false);
		infoText.setWrapText(true);
		infoText.getStyleClass().clear();
		infoText.getStyleClass().add("label");
		infoText.getStyleClass().add("dialog-exception-details");
		return infoText;
	}
	
	private static final int MAX_STACKTRACE = 6;
	private static String getStackTraceString(Throwable e, String indent) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(e.toString());
	    sb.append("\n");

	    StackTraceElement[] stack = e.getStackTrace();
	    if (stack != null) {
	        for (int i = 0; i < MAX_STACKTRACE && i < stack.length; i++) {
	        	StackTraceElement stackTraceElement = stack[i];
	            sb.append(indent);
	            sb.append("\tat ");
	            sb.append(stackTraceElement.toString());
	            sb.append("\n");
	        }
	    }

	    Throwable[] suppressedExceptions = e.getSuppressed();
	    // Print suppressed exceptions indented one level deeper.
	    if (suppressedExceptions != null) {
	        for (Throwable throwable : suppressedExceptions) {
	            sb.append(indent);
	            sb.append("\tSuppressed: ");
	            sb.append(getStackTraceString(throwable, indent + "\t"));
	        }
	    }

	    Throwable cause = e.getCause();
	    if (cause != null) {
	        sb.append(indent);
	        sb.append("Caused by: ");
	        sb.append(getStackTraceString(cause, indent));
	    }

	    return sb.toString();
	}
	
	/**
	 * Returns true if it's the first time the program is opened.
	 * @return
	 */
	public boolean isFirstTime() {
		return isFirstTime;
	}

	/**
	 * This setting tells whether it's the first time the program is opened. On the first time, some special dialogs
	 * might be shown to the user.
	 * @param isFirstTime
	 */
	public void setFirstTime(boolean isFirstTime) {
		this.isFirstTime = isFirstTime;
	}
}
