package sporemodder.view.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import sporemodder.GitHubManager;
import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Timer;

public class SetGitUserUI implements Controller {
    private static final String EMAIL_CHARS_REGEX = "[a-zA-Z0-9._%+\\-@]+";
    private static final String WARNING_USERNAME_EMPTY = "Username cannot be empty";
    private static final String WARNING_EMAIL_EMPTY = "Email address cannot be empty";

    private Dialog<ButtonType> dialog;
    @FXML
    private Node mainNode;
    @FXML
    private Hyperlink gitDownloadLink;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private Label warningLabel;

    @FXML
    private void initialize() {
        // Add a warning icon to the warning label
        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        gitDownloadLink.setOnAction(event -> MainApp.get().getHostServices().showDocument(gitDownloadLink.getText()));

        emailTextField.setTextFormatter(new TextFormatter<>(c -> {
            if (c.getControlNewText().matches(EMAIL_CHARS_REGEX)) {
                return c;
            } else {
                return null;
            }
        }));

        usernameTextField.textProperty().addListener((e, oldValue, newValue) -> validateFields());
        emailTextField.textProperty().addListener((e, oldValue, newValue) -> validateFields());
    }

    private void validateFields() {
        boolean showWarning = true;

        if (usernameTextField.getText().isBlank()) {
            warningLabel.setText(WARNING_USERNAME_EMPTY);
        } else if (emailTextField.getText().isBlank()) {
            warningLabel.setText(WARNING_EMAIL_EMPTY);
        } else {
            showWarning = false;
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning);
    }

    private boolean showInternal() {
        dialog.setTitle("Connect with git/GitHub");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        String username = GitHubManager.get().getUsername();
        String email = GitHubManager.get().getEmailAddress();
        if (username == null) username = "";
        if (email == null) email = "";
        usernameTextField.setText(username);
        emailTextField.setText(email);

        Button okButton = ((Button)dialog.getDialogPane().lookupButton(ButtonType.OK));
        okButton.setText("Continue to log in");
        validateFields();

        Button cancelButton = ((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL));
        cancelButton.setText("Set up later");

        if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) == ButtonType.OK) {
            GitHubManager.get().setUsername(usernameTextField.getText());
            GitHubManager.get().setEmailAddress(emailTextField.getText());
            MainApp.get().saveSettings();

            ModBundle currentMod = ProjectManager.get().getActiveModBundle();
            if (currentMod != null) {
                try {
                    GitHubManager.get().configGitUsernameAndEmailForRepo(currentMod.getGitRepository());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean show() {
        SetGitUserUI node = UIManager.get().loadUI("dialogs/SetGitUserUI");
        node.dialog = new Dialog<>();
        return node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
