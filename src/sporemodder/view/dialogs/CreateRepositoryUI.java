package sporemodder.view.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import sporemodder.GitHubManager;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.util.GitCommands;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateRepositoryUI implements Controller {
    private static final String VALID_CHARACTERS = "\\w\\d_\\-.";

    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;
    @FXML
    private Node mainNode;
    @FXML
    private TextField repoNameTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextField urlTextField;
    @FXML
    private Label warningLabel;
    @FXML
    private CheckBox isPrivateCheckBox;

    private final List<String> existingRepositories = new ArrayList<>();

    @FXML
    private void initialize() {
        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        repoNameTextField.setTextFormatter(new TextFormatter<>(c -> {
            if (c.getControlNewText().matches("[" + VALID_CHARACTERS +"]*")) {
                return c;
            } else {
                return null;
            }
        }));

        repoNameTextField.textProperty().addListener((e, oldValue, newValue) -> {
            urlTextField.setText("https://github.com/" + GitHubManager.get().getUsername() + "/" + newValue);
            validateFields();
        });
    }

    private void validateFields() {
        boolean showWarning = true;
        if (repoNameTextField.getText().isBlank()) {
            warningLabel.setText("Repository name cannot be empty");
        } else if (existingRepositories.contains(repoNameTextField.getText().toLowerCase())) {
            warningLabel.setText("Repository already exists");
        } else {
            showWarning = false;
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning);
    }

    private void showInternal() {
        existingRepositories.clear();
        existingRepositories.addAll(GitHubManager.get().getUserRepositoryNames().stream().map(String::toLowerCase).collect(Collectors.toList()));

        dialog.setTitle("Create new GitHub Repository (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        String repoName = modBundle.getName().replace(' ', '-');
        repoName = repoName.replaceAll("[^" + VALID_CHARACTERS + "]", "");

        repoNameTextField.setText(repoName);
        descriptionTextField.setText(modBundle.getDescription());

        validateFields();

        if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) == ButtonType.OK) {
            UIManager.get().tryAction(() -> {
                JSONObject repository = GitHubManager.get().createGitHubRepository(
                        repoNameTextField.getText(),
                        descriptionTextField.getText(),
                        isPrivateCheckBox.isSelected()
                );
                if (repository != null) {
                    String newUrl = repository.getString("html_url");
                    modBundle.setDescription(descriptionTextField.getText());
                    modBundle.setGithubUrl(newUrl);
                    if (modBundle.getWebsiteUrl() == null || modBundle.getWebsiteUrl().isBlank()) {
                        modBundle.setWebsiteUrl(newUrl);
                    }
                    modBundle.saveModInfo();

                    try {
                        // Try changing existing one, otherwise add a new one
                        GitCommands.gitSetOriginURL(modBundle.getGitRepository(), repository.getString("ssh_url"));
                    } catch (Exception e) {
                        GitCommands.gitAddOriginURL(modBundle.getGitRepository(), repository.getString("ssh_url"));
                    }
                    GitCommands.runCommand(modBundle.getGitRepository(), "git", "push", "--set-upstream", "origin", "main");

                    GitHubManager.get().setRepositoryTopics(repoNameTextField.getText(), List.of("spore-mod", "spore"));

                    Label label = new Label("Your repository was successfully created at:");
                    Hyperlink link = new Hyperlink(newUrl);
                    link.setOnAction(event -> {
                        MainApp.get().getHostServices().showDocument(newUrl);
                    });

                    VBox vbox = new VBox();
                    vbox.setSpacing(5.0);
                    vbox.getChildren().addAll(label, link);

                    Alert alert = new Alert(Alert.AlertType.NONE, null, ButtonType.OK);
                    alert.setTitle("Repository created");
                    alert.getDialogPane().setContent(vbox);
                    UIManager.get().showDialog(alert);
                } else {
                    Label label = new Label("Could not create the repository. You need to \"install\" " +
                            "SporeModder FX to your GitHub account, to allow it to create repositories. You " +
                            "can do it at this link:");
                    label.setWrapText(true);
                    Hyperlink link = new Hyperlink("https://github.com/apps/sporemodder-fx");
                    link.setOnAction(event -> {
                        MainApp.get().getHostServices().showDocument(link.getText());
                    });

                    VBox vbox = new VBox();
                    vbox.setSpacing(5.0);
                    vbox.getChildren().addAll(label, link);

                    Alert alert = new Alert(Alert.AlertType.ERROR, null, ButtonType.OK);
                    alert.setTitle("Failed to create repository");
                    alert.getDialogPane().setContent(vbox);
                    UIManager.get().showDialog(alert);
                }
            }, "Failed to create repository");
        }
    }

    public static void show(ModBundle modBundle) {
        CreateRepositoryUI node = UIManager.get().loadUI("dialogs/CreateRepositoryUI");
        node.modBundle = modBundle;
        node.dialog = new Dialog<>();
        node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
