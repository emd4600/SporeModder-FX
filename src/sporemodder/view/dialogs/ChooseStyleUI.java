package sporemodder.view.dialogs;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sporemodder.UIManager;

public class ChooseStyleUI {
	
	@FXML private RadioButton lightTheme;
	@FXML private RadioButton darkTheme;

	@FXML
	private void initialize() {
		Image light = new Image(ChooseStyleUI.class.getResourceAsStream("/sporemodder/resources/LightTheme.png"));
		Image dark = new Image(ChooseStyleUI.class.getResourceAsStream("/sporemodder/resources/DarkTheme.png"));
		
		ImageView lightView = new ImageView();
		lightView.setImage(light);
		
		ImageView darkView = new ImageView();
		darkView.setImage(dark);
		
		lightTheme.setGraphic(lightView);
		darkTheme.setGraphic(darkView);
		
		lightTheme.getStyleClass().remove("radio-button");
		lightTheme.getStyleClass().add("toggle-button");
		
		darkTheme.getStyleClass().remove("radio-button");
		darkTheme.getStyleClass().add("toggle-button");
		
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(lightTheme, darkTheme);
		
		lightTheme.setSelected(true);
		lightTheme.requestFocus();
	}
	
	public static String show(Image icon) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(UIManager.class.getResource("/sporemodder/view/dialogs/ChooseStyleUI.fxml"));
		Node main = loader.load();
		ChooseStyleUI controller = (ChooseStyleUI) loader.getController();
		if (controller == null) return null;
		
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Choose SporeModder FX Style");
		dialog.getDialogPane().setContent(main);
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
		dialog.initModality(Modality.APPLICATION_MODAL);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(icon);
		
		if (dialog.showAndWait().orElse(ButtonType.CLOSE) != ButtonType.OK) return null;
		else {
			return controller.lightTheme.isSelected() ? "Default" : "Dark";
		}
	}
}
