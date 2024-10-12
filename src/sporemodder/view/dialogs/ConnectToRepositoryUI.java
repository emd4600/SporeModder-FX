package sporemodder.view.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import sporemodder.UIManager;
import sporemodder.util.GitCommands;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

public class ConnectToRepositoryUI implements Controller {
    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;
    @FXML
    private Node mainNode;
    @FXML
    private TextField urlTextField;
    @FXML
    private Label warningLabel;

    @FXML
    private void initialize() {
        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        urlTextField.textProperty().addListener((e, oldValue, newValue) -> validateFields());
    }

    private void validateFields() {
        boolean showWarning = true;
        if (urlTextField.getText().isBlank()) {
            warningLabel.setText("Repository URL cannot be empty");
        } else if (!urlTextField.getText().startsWith("https://github.com/") && !urlTextField.getText().startsWith("https://www.github.com/")) {
            warningLabel.setText("Repository URL must begin with 'https://github.com/'");
        } else {
            showWarning = false;
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning);
    }

    private void showInternal() {
        dialog.setTitle("Connect Mod to existing GitHub Repository (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        validateFields();

        urlTextField.requestFocus();

        if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) == ButtonType.OK) {
            UIManager.get().tryAction(() -> {
                String newUrl = urlTextField.getText();
                modBundle.setGithubUrl(newUrl);
                if (modBundle.getWebsiteUrl() == null || modBundle.getWebsiteUrl().isBlank()) {
                    modBundle.setWebsiteUrl(newUrl);
                }
                modBundle.saveModInfo();

                // +1 to remove trailing /
                String urlEnd = newUrl.substring(newUrl.indexOf("github.com") + "github.com".length() + 1);
                String sshUrl = "git@github.com:" + urlEnd + ".git";
                try {
                    // Try changing existing one, otherwise add a new one
                    GitCommands.gitSetOriginURL(modBundle.getGitRepository(), sshUrl);
                } catch (Exception e) {
                    GitCommands.gitAddOriginURL(modBundle.getGitRepository(), sshUrl);
                }

                // Ensure upstream is set for current branch
                String branchName = "main";
                try {
                    branchName = GitCommands.gitGetCurrentBranch(modBundle.getGitRepository());
                } catch (Exception ignored) {
                }
                GitCommands.runCommand(modBundle.getGitRepository(), "git", "branch", "--set-upstream-to=origin/" + branchName);
            }, "Failed to connect to existing repository");
        }
    }

    public static void show(ModBundle modBundle) {
        ConnectToRepositoryUI node = UIManager.get().loadUI("dialogs/ConnectToRepositoryUI");
        node.modBundle = modBundle;
        node.dialog = new Dialog<>();
        node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
