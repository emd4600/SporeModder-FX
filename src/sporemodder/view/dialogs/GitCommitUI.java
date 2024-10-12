package sporemodder.view.dialogs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Callback;
import sporemodder.GitHubManager;
import sporemodder.util.GitCommands;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitCommitUI implements Controller {
    private static class CommitFile {
        public final String name;
        public final BooleanProperty selectedProperty;

        public CommitFile(String name) {
            this.name = name;
            selectedProperty = new SimpleBooleanProperty(true);
        }
    }

    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;
    @FXML
    private Node mainNode;

    @FXML
    private Accordion filesAccordion;
    @FXML
    private ListView<CommitFile> modifiedFilesListView;
    @FXML
    private ListView<CommitFile> newFilesListView;
    @FXML
    private TitledPane modifiedFilesPane;
    @FXML
    private TitledPane newFilesPane;
    @FXML
    private TextArea commitMessageTextArea;
    @FXML
    private Label warningLabel;

    @FXML
    private void initialize() {
        Callback<ListView<CommitFile>, ListCell<CommitFile>> cellFactory = (list -> new ListCell<>() {
            @Override
            protected void updateItem(CommitFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setMnemonicParsing(false);
                    checkBox.selectedProperty().bindBidirectional(item.selectedProperty);
                    checkBox.setText(item.name);
                    setGraphic(checkBox);
                }
            }
        });

        modifiedFilesListView.setCellFactory(cellFactory);
        newFilesListView.setCellFactory(cellFactory);

        modifiedFilesPane.setExpanded(true);
        newFilesPane.setExpanded(false);

        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        commitMessageTextArea.textProperty().addListener((obs, oldValue, newValue) -> {
            checkCommitText(newValue);
        });
    }

    private void doGitCommit() throws IOException, InterruptedException {
        List<String> files = Stream.concat(
                modifiedFilesListView.getItems().stream(),
                newFilesListView.getItems().stream()
        ).filter(file -> file.selectedProperty.get()).map(file -> file.name).collect(Collectors.toList());

        GitCommands.gitAddAll(modBundle.getGitRepository(), files);
        GitCommands.gitCommit(modBundle.getGitRepository(), commitMessageTextArea.getText());
    }

    private void checkCommitText(String newValue) {
        boolean notValid = newValue == null || newValue.isBlank();
        warningLabel.setVisible(notValid);
        dialog.getDialogPane().lookupButton(ButtonType.APPLY).setDisable(notValid);
    }

    private void showInternal(List<String> modifiedFiles, List<String> newFiles) {
        dialog.setTitle("Commit changes to Git (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);

        modifiedFiles.stream().map(CommitFile::new).forEach(modifiedFilesListView.getItems()::add);
        newFiles.stream().map(CommitFile::new).forEach(newFilesListView.getItems()::add);

        filesAccordion.setExpandedPane(modifiedFilesListView.getItems().isEmpty() ? newFilesPane : modifiedFilesPane);

        ((Button)dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setText("Commit");
        checkCommitText(null);

        Optional<ButtonType> result = UIManager.get().showDialog(dialog);
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            boolean doCommit = true;
            if (!GitHubManager.get().hasConfigGitUsernameAndEmailInRepo(modBundle.getGitRepository())) {
                doCommit = UIManager.get().tryAction(
                        () -> GitHubManager.get().configGitUsernameAndEmailForRepo(modBundle.getGitRepository()),
                        "Failed to configure git username and email. Commit will not be performed.", false);
            }
            if (doCommit) {
                UIManager.get().tryAction(this::doGitCommit, "Failed to commit changes into git");
            }
        }
    }

    public static void show(ModBundle modBundle) {
        List<String> modifiedFiles;
        List<String> newFiles;
        try {
            modifiedFiles = GitCommands.gitGetModifiedFiles(modBundle.getGitRepository());
        } catch (Exception e) {
            UIManager.get().showErrorDialog(e, "Error getting unstaged files, the contents of the lists may not be accurate", false);
            e.printStackTrace();
            modifiedFiles = Collections.emptyList();
        }
        try {
            newFiles = GitCommands.gitGetUntrackedFiles(modBundle.getGitRepository());
        } catch (Exception e) {
            UIManager.get().showErrorDialog(e, "Error getting unstaged files, the contents of the lists may not be accurate", false);
            e.printStackTrace();
            newFiles = Collections.emptyList();
        }

        if (newFiles.isEmpty() && modifiedFiles.isEmpty()) {
            UIManager.get().showDialog(Alert.AlertType.INFORMATION, "There are no modified or new files to commit");
        } else {
            GitCommitUI node = UIManager.get().loadUI("dialogs/GitCommitUI");
            node.dialog = new Dialog<>();
            node.modBundle = modBundle;
            node.showInternal(modifiedFiles, newFiles);
        }
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
