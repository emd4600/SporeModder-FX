package sporemodder.view.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.view.Controller;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class CreateProjectSimpleUI implements Controller {

    private Dialog<ButtonType> dialog;
    @FXML
    private Node mainNode;
    @FXML
    private TextField modNameTextField;
    @FXML
    private Label warningLabel;

    @FXML
    private void initialize() {
        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        // Set a project text
        modNameTextField.setText("Project " + (ProjectManager.get().getProjects().size() + 1));

        modNameTextField.requestFocus();
        modNameTextField.selectAll();
        modNameTextField.requestFocus();

        // Ban illegal characters
        modNameTextField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (ModBundle.NAME_ILLEGAL_CHARACTERS.chars().anyMatch(c -> text.indexOf(c) != -1)) {
                return null;
            } else {
                return change;
            }
        }));
    }

    private void validateModFields() {
        boolean showWarning = true;
        // Show warning if name collides
        String modName = modNameTextField.getText();
        if (ProjectManager.get().hasModBundle(modName) ||
                ProjectManager.get().hasProject(modName)) {
            warningLabel.setText("A project with this name already exists");
        } else if (modName.isBlank()) {
            warningLabel.setText("Mod name cannot be blank");
        } else {
            showWarning = false;
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning);
    }

    public void createMod() throws IOException, InterruptedException, ParserConfigurationException, TransformerException {
        ProjectManager.get().createNewMod(
                modNameTextField.getText(),
                ModBundle.generateUniqueTagFromName(modNameTextField.getText()),
                "",
                modNameTextField.getText(),
                null
        );
    }

    public static void show() {
        ProjectManager.get().removeInexistantMods();

        CreateProjectSimpleUI node = UIManager.get().loadUI("dialogs/CreateProjectSimpleUI");
        node.dialog = new Dialog<>();
        node.dialog.getDialogPane().setContent(node.getMainNode());

        node.dialog.setTitle("Create new mod project");
        node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK, ButtonType.NEXT);

        ((Button)node.dialog.getDialogPane().lookupButton(ButtonType.NEXT)).setText("Advanced Options");

        node.validateModFields();

        UIManager.get().showDialog(node.dialog).ifPresent(result -> {
            if (result == ButtonType.NEXT) {
                CreateProjectUI.show();
            } else if (result == ButtonType.OK && UIManager.get().tryAction(node::createMod,
                    "Cannot initialize project. Try manually deleting the project folder in SporeModder FX\\Projects\\"))
            {
                ProjectManager.get().setActive(ProjectManager.get().getProject(node.modNameTextField.getText()));
            }
        });
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
