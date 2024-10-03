package sporemodder.view.dialogs;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import sporemodder.GitManager;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GitSyncUI implements Controller {
    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;
    @FXML
    private Node mainNode;
    @FXML
    private TextArea consoleTextArea;
    @FXML
    private Button pushButton;
    @FXML
    private Button pullButton;

    private Runnable onConsoleDone = () -> {
        consoleTextArea.appendText("Done\n");
        pullButton.setDisable(false);
        pushButton.setDisable(false);
    };

    @FXML
    private void initialize() {
        pullButton.setOnAction(event -> {
            consoleTextArea.setText("Pulling...\n");
            pullButton.setDisable(true);
            pushButton.setDisable(true);
            try {
                GitManager.gitPull(new ConsoleOutputCommandTask(consoleTextArea, onConsoleDone), modBundle.getGitRepository());
            } catch (Exception e) {
                UIManager.get().showErrorDialog(e, "Failed to pull to git", false);
            }
        });
        pushButton.setOnAction(event -> {
            consoleTextArea.setText("Pushing...\n");
            pullButton.setDisable(true);
            pushButton.setDisable(true);
            try {
                GitManager.gitPush(new ConsoleOutputCommandTask(consoleTextArea, onConsoleDone), modBundle.getGitRepository());
            } catch (Exception e) {
                UIManager.get().showErrorDialog(e, "Failed to push to git", false);
            }
        });
    }

    private void showInternal() {
        dialog.setTitle("Synchronize (push/pull) with Git (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);

        UIManager.get().showDialog(dialog);
    }

    public static void show(ModBundle modBundle) {
        GitSyncUI node = UIManager.get().loadUI("dialogs/GitSyncUI");
        node.dialog = new Dialog<>();
        node.modBundle = modBundle;
        node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
