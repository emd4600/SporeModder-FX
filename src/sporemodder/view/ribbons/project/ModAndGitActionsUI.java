package sporemodder.view.ribbons.project;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import sporemodder.GitHubManager;
import sporemodder.util.GitCommands;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.dialogs.*;

import java.util.Optional;

public class ModAndGitActionsUI implements Controller, UIUpdateListener {
    @FXML
    private Node mainNode;

    @FXML
    private Button btnModProperties;
    @FXML
    private Button btnGitCommit;
    @FXML
    private Button btnGitSync;
    @FXML
    private Button btnGitPublish;
    @FXML
    private Button btnGitLogin;
    @FXML
    private Button btnCreateRepository;

    @Override
    public Node getMainNode() {
        return mainNode;
    }

    @FXML
    protected void initialize() {
        btnModProperties.setOnAction((event) -> {
            ModPropertiesUI.show(ProjectManager.get().getActiveModBundle(), true);
        });
        btnGitCommit.setOnAction((event) -> {
            if (!GitHubManager.get().requireGitInstalled()) {
                return;
            }
            GitCommitUI.show(ProjectManager.get().getActiveModBundle());
        });
        btnGitSync.setOnAction((event) -> {
            if (!GitHubManager.get().requireGitInstalled()) {
                return;
            }
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (checkAndShowUncommitedChangesDialog(modBundle,
                    "You have uncommitted changes. If you push without committed, these changes won't be uploaded, \n" +
                            "and pulling might cause merging trouble. Are you sure you want to continue?",
                    "Sync without committing")) {
                GitSyncUI.show(modBundle);
            }
        });
        btnGitPublish.setOnAction((event) -> {
            if (!GitHubManager.get().requireGitInstalled()) {
                return;
            }
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (checkAndShowUncommitedChangesDialog(modBundle,
                    "You have uncommitted changes. If you publish without committing, these changes won't be part \n" +
                            "of the published mod. Are you sure you want to continue?",
                    "Publish without committing")) {
                GitPublishModUI.show(modBundle);
            }
        });

        btnGitLogin.setOnAction(event -> {
            if (!GitHubManager.get().requireGitInstalled()) {
                return;
            }
            if (GitHubManager.get().requireUsernameAndEmail() && GitHubManager.get().requireUserAccessToken()) {
                UIManager.get().showDialog("You logged in successfully!");
                UIManager.get().setOverlay(false);
            }
        });

        btnCreateRepository.setOnAction(event -> {
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (!GitHubManager.get().warnIfRepositoryAlreadyHasRemote(modBundle.getGitRepository()) ||
                    !GitHubManager.get().requireUserAccessToken()) {
                return;
            }
            CreateRepositoryUI.show(modBundle);
        });

        UIManager.get().addListener(this);
    }

    private boolean checkAndShowUncommitedChangesDialog(ModBundle modBundle, String text, String continueText) {
        try {
            if (!GitCommands.gitGetUntrackedFiles(modBundle.getGitRepository()).isEmpty() ||
                    !GitCommands.gitGetModifiedFiles(modBundle.getGitRepository()).isEmpty()) {
                Label label = new Label();
                label.setWrapText(true);

                label.setText(text);

                Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("Commit changes");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.NO)).setText(continueText);
                alert.getDialogPane().setContent(label);

                Optional<ButtonType> result = UIManager.get().showDialog(alert);
                if (result.isPresent() && result.get() == ButtonType.YES) {
                    GitCommitUI.show(modBundle);
                    return false;
                } else if (result.isPresent() && result.get() == ButtonType.NO) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void onUIUpdate(boolean isFirstUpdate) {
        boolean hasNoActiveMod = ProjectManager.get().getActiveModBundle() == null;
        btnModProperties.setDisable(hasNoActiveMod);
        btnGitCommit.setDisable(hasNoActiveMod);
        btnCreateRepository.setDisable(hasNoActiveMod);
        // TODO for these, check that it has github url and user
        btnGitSync.setDisable(hasNoActiveMod);
        btnGitPublish.setDisable(hasNoActiveMod);
    }
}
