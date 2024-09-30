package sporemodder.view.ribbons.project;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.dialogs.ModPropertiesUI;
import sporemodder.view.dialogs.ProjectSettingsUI;

public class ModAndGitActionsUI implements Controller, UIUpdateListener {
    @FXML
    private Node mainNode;

    @FXML
    private Button btnModProperties;

    @Override
    public Node getMainNode() {
        return mainNode;
    }

    @FXML
    protected void initialize() {
        btnModProperties.setOnAction((event) -> {
            ModPropertiesUI.show(ProjectManager.get().getActiveModBundle(), true);
        });

        UIManager.get().addListener(this);
    }

    @Override
    public void onUIUpdate(boolean isFirstUpdate) {
        boolean hasNoActiveMod = ProjectManager.get().getActiveModBundle() == null;
        btnModProperties.setDisable(hasNoActiveMod);
    }
}
