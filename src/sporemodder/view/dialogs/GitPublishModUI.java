package sporemodder.view.dialogs;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import sporemodder.util.GitCommands;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import java.io.IOException;
import java.util.function.BiConsumer;

public class GitPublishModUI implements Controller {
    private static final String WARNING_EMPTY = "Version tag cannot be empty";
    private static final String ERROR_MOD_PUBLISH = "Failed to publish mod";

    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;
    @FXML
    private Node mainNode;
    @FXML
    private TextArea consoleTextArea;
    @FXML
    private TextField versionTextField;
    @FXML
    private Label warningLabel;

    @FXML
    private void initialize() {
        versionTextField.setTextFormatter(new TextFormatter<>(c -> {
            String text = c.getText();
            if (text.matches("[a-zA-Z0-9\\.-]*")) {
                return c;
            } else {
                return null;
            }
        }));
        versionTextField.textProperty().addListener((e, oldValue, newValue) -> validateVersionTag());

        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));
        warningLabel.setText(WARNING_EMPTY);
    }

    private void validateVersionTag() {
        String text = versionTextField.getText();
        boolean showWarning = true;
        if (text.isBlank()) {
            warningLabel.setText(WARNING_EMPTY);
        } else {
            try {
                String warningText = validateVersionTag(text);
                if (warningText != null) {
                    warningLabel.setText(warningText);
                } else {
                    showWarning = false;
                }
            } catch (Exception e) {
                warningLabel.setText(e.getMessage());
                e.printStackTrace();
            }
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning);
    }

    private void showInternal() {
        dialog.setTitle("Synchronize (push/pull) with Git (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Button button = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        button.setText("Publish");

        // Avoid the Publish button closing by default
        button.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            button.setDisable(true);
            consoleTextArea.clear();
            UIManager.get().tryAction(this::doPublishMod, ERROR_MOD_PUBLISH, false);
        });

        validateVersionTag();

        UIManager.get().showDialog(dialog);
    }

    private void doPublishMod() throws IOException, InterruptedException {
        consoleTextArea.appendText("Creating tag...\n");

        ConsoleOutputCommandTask pushTagTask = new ConsoleOutputCommandTask(consoleTextArea, () -> {
            String publishedUrl = GitCommands.getPublishedUrl(modBundle.getGithubUrl());

            Hyperlink hyperlink = new Hyperlink(publishedUrl);
            hyperlink.setOnAction(event -> {
                MainApp.get().getHostServices().showDocument(publishedUrl);
            });

            Label label1 = new Label("Congratulations! Your mod '" + modBundle.getName() + "', version " +
                    versionTextField.getText() + " has been uploaded, and will appear in a few minutes as a draft:");
            label1.setWrapText(true);

            Label label2 = new Label("The mod is not public yet. When you are sure you want to release it, head over to the link and make it public.");
            label2.setWrapText(true);

            VBox vbox = new VBox();
            vbox.setPrefWidth(400.0);
            vbox.getChildren().addAll(label1, hyperlink, label2);

            Alert alert = new Alert(Alert.AlertType.NONE, null, ButtonType.OK);
            alert.setTitle("Mod published");
            alert.getDialogPane().setContent(vbox);
            UIManager.get().showDialog(alert);

            dialog.setResult(ButtonType.OK);
            dialog.close();
        });
        ConsoleOutputCommandTask pushTask = new ConsoleOutputCommandTask(consoleTextArea, () -> {
            consoleTextArea.appendText("Pushing tag...\n");
            UIManager.get().tryAction(() ->  GitCommands.gitPushTag(pushTagTask, modBundle.getGitRepository(), versionTextField.getText()),
                    ERROR_MOD_PUBLISH, false);
        });
        ConsoleOutputCommandTask createTagTask = new ConsoleOutputCommandTask(consoleTextArea, () -> {
            consoleTextArea.appendText("Pushing changes...\n");
            UIManager.get().tryAction(() -> GitCommands.gitPush(pushTask, modBundle.getGitRepository()),
                    ERROR_MOD_PUBLISH, false);
        });

        GitCommands.gitCreateTag(createTagTask, modBundle.getGitRepository(), versionTextField.getText());

        BiConsumer<ConsoleOutputCommandTask, WorkerStateEvent> failAction =
                (task, event) -> UIManager.get().showErrorDialog(task.getException(), ERROR_MOD_PUBLISH, false);

        pushTask.setOnFailed(event -> failAction.accept(pushTask, event));
        pushTagTask.setOnFailed(event -> failAction.accept(pushTagTask, event));
        createTagTask.setOnFailed(event -> failAction.accept(createTagTask, event));
    }

    public static void show(ModBundle modBundle) {
        GitPublishModUI node = UIManager.get().loadUI("dialogs/GitPublishModUI");
        node.dialog = new Dialog<>();
        node.modBundle = modBundle;
        node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }

    private static boolean isPositiveInteger(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Validates a version tag.
     * <p>
     * A version tag must have the following format:
     * <ul>
     * <li>Must begin with 'v' or a number</li>
     * <li>Must have at most 4 parts, separated by dots '.'</li>
     * <li>Parts must be positive integers, except for the last part which can have a letter or a hyphen and some word</li>
     * <li>Parts cannot be empty</li>
     * </ul>
     * <p>
     * If the version tag is invalid, returns an error message. Otherwise, returns null.
     * @param versionTag the version tag to validate
     * @return an error message if the version tag is invalid, null otherwise
     */
    private String validateVersionTag(String versionTag) {
        if (versionTag.isBlank()) {
            return "Version tag cannot be empty";
        } else if (!(versionTag.charAt(0) == 'v' || Character.isDigit(versionTag.charAt(0)))) {
            return "Version tag must begin with 'v' or a number";
        } else {
            if (versionTag.charAt(0) == 'v') {
                versionTag = versionTag.substring(1);
            }
            if (versionTag.isBlank()) {
                return "Version tag cannot be empty";
            }
            if (versionTag.endsWith(".")) {
                return "Version tag cannot end with a dot";
            }
            String[] splits = versionTag.split("\\.");
            if (splits.length > 3) {
                return "Version tag cannot have more than 3 parts";
            }
            for (int i = 0; i < splits.length - 1; i++) {
                if (splits[i].isEmpty()) {
                    return "Version tag cannot have empty parts";
                } else if (!isPositiveInteger(splits[i])) {
                    return "Version tag parts must be positive integers";
                }
            }
            // Last part is special, can have a letter or a hyphen and some word
            String[] lastPartSplits = splits[splits.length - 1].split("-", 2);
            if (lastPartSplits[0].isEmpty()) {
                return "Version tag cannot have empty parts";
            } else {
                String testStr = lastPartSplits[0];
                if (testStr.length() > 1) {
                    testStr = testStr.substring(0, testStr.length() - 1);
                }
                if (!isPositiveInteger(testStr)) {
                    return "Version tag parts must be positive integers";
                }
            }

            return null;
        }
    }
}
