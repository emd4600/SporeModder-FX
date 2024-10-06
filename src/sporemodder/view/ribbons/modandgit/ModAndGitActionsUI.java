package sporemodder.view.ribbons.modandgit;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    @FXML
    private Button btnConnectExistingRepository;

    @Override
    public Node getMainNode() {
        return mainNode;
    }

    private Node createSizedGraphic(ImageView imageView, int width) {
        HBox pane = new HBox(imageView);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(imageView.getFitHeight());
        pane.setPrefWidth(width);
//        imageView.setTranslateX((width - imageView.getFitWidth()) / 2);
        return pane;
    }

    @FXML
    protected void initialize() {

        UIManager ui = UIManager.get();

        btnModProperties.setGraphic(ui.loadIcon("config.png", 0, 38, true));
        btnGitSync.setGraphic(createSizedGraphic(ui.loadIcon("git-sync.png", 0, 38, true), 55));
        btnCreateRepository.setGraphic(ui.loadIcon("git-new-repo.png", 0, 38, true));
        btnGitCommit.setGraphic(ui.loadIcon("git-commit.png", 0, 38, true));
        btnGitLogin.setGraphic(ui.loadIcon("git-login.png", 0, 38, true));
        btnGitPublish.setGraphic(ui.loadIcon("git-publish.png", 0, 38, true));
        btnConnectExistingRepository.setGraphic(ui.loadIcon("git-connect-existing-repo.png", 0, 38, true));

        btnModProperties.setTooltip(new Tooltip("Configure the properties of the mod, such as name and description"));
        btnGitSync.setTooltip(new Tooltip("Upload or download changes from the git repository"));
        btnCreateRepository.setTooltip(new Tooltip("Create a new GitHub repository for this mod"));
        btnGitCommit.setTooltip(new Tooltip("Commit (i.e. save) changes to the git repository"));
        btnGitLogin.setTooltip(new Tooltip("Log in to your GitHub account"));
        btnGitPublish.setTooltip(new Tooltip("Package and publish the mod into the GitHub repository"));
        btnConnectExistingRepository.setTooltip(new Tooltip("Connect the mod with an existing GitHub repository"));

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
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (!GitHubManager.get().requireGitInstalled() ||
                    !requireGitRemote(modBundle, "Pull & Push")) {
                return;
            }
            if (checkAndShowUncommitedChangesDialog(modBundle,
                    "You have uncommitted changes. If you push without committed, these changes won't be uploaded, \n" +
                            "and pulling might cause merging trouble. Are you sure you want to continue?",
                    "Sync without committing")) {
                GitSyncUI.show(modBundle);
            }
        });
        btnGitPublish.setOnAction((event) -> {
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (!GitHubManager.get().requireGitInstalled() ||
                    !requireGitRemote(modBundle, "Mod Publish")) {
                return;
            }
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
            if (!GitHubManager.get().requireGitInstalled() ||
                    !GitHubManager.get().warnIfRepositoryAlreadyHasRemote(modBundle.getGitRepository()) ||
                    !GitHubManager.get().requireUserAccessToken()) {
                return;
            }
            CreateRepositoryUI.show(modBundle);
        });

        btnConnectExistingRepository.setOnAction(event -> {
            ModBundle modBundle = ProjectManager.get().getActiveModBundle();
            if (!GitHubManager.get().requireGitInstalled() ||
                    !GitHubManager.get().warnIfRepositoryAlreadyHasRemote(modBundle.getGitRepository())) {
                return;
            }
            ConnectToRepositoryUI.show(modBundle);
        });

        UIManager.get().addListener(this);
    }

    private boolean requireGitRemote(ModBundle modBundle, String actionName) {
        if (GitCommands.gitGetOriginURL(modBundle.getGitRepository()) != null) {
            return true;
        } else {
            UIManager.get().showDialog(Alert.AlertType.INFORMATION,
                    "This repository doesn't have a remote set up. " + actionName + " is not possible.\n" +
                            "Use 'Create GitHub Repository' or 'Connect to Existing Repository' before.");

            return false;
        }
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
        btnConnectExistingRepository.setDisable(hasNoActiveMod);
        btnGitSync.setDisable(hasNoActiveMod);
        btnGitPublish.setDisable(hasNoActiveMod);
    }
}
