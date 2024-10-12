package sporemodder.view.dialogs;

import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import sporemodder.UIManager;
import sporemodder.util.GamePathConfiguration;
import sporemodder.util.ModBundle;
import sporemodder.util.Project;
import sporemodder.view.Controller;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModPropertiesUI implements Controller {

    private static final String WARNING_MOD_NAME_EMPTY = "Mod name cannot be empty";
    private static final String WARNING_UNIQUE_TAG_EMPTY = "Unique tag cannot be empty";
    private static final int MIN_UNIQUE_TAG_LENGTH = 6;
    private static final String WARNING_UNIQUE_TOO_SHORT = "Unique tag must have at least " + MIN_UNIQUE_TAG_LENGTH + " characters";


    private Dialog<ButtonType> dialog;
    private ModBundle modBundle;

    @FXML
    private Node mainNode;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField uniqueTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextField websiteTextField;
    @FXML
    private TextField githubTextField;
    @FXML
    private ChoiceBox<ModBundle.ExperimentalStatus> experimentalChoiceBox;
    @FXML
    private CheckBox galaxyResetCheckBox;
    @FXML
    private CheckBox dataDependencyCheckBox;

    @FXML
    private Accordion filesAccordion;
    @FXML
    private TitledPane packagesPane;
    @FXML
    private TitledPane dllsPane;
    @FXML
    private ListView<ProjectWithTarget> projectsListView;
    @FXML
    private ListView<String> dllsListView;

    @FXML
    private Label warningLabel;
    @FXML
    private Label customModInfoLabel;

    private static class ProjectWithTarget {
        private Project project;
        private ModBundle.FileTarget target;
    }

    @FXML
    private void initialize() {
        // Add a warning icon to the warning label
        warningLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));
        customModInfoLabel.setGraphic(UIManager.get().getAlertIcon(Alert.AlertType.WARNING, 16, 16));

        experimentalChoiceBox.getItems().addAll(
                ModBundle.ExperimentalStatus.AUTO,
                ModBundle.ExperimentalStatus.NO,
                ModBundle.ExperimentalStatus.YES);

        // Set cell factory for project packages
        projectsListView.setCellFactory((list) -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectWithTarget project, boolean empty) {
                super.updateItem(project, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label projectName = new Label(project.project.getName());
                    Label packageName = new Label(project.project.getPackageName());

                    ChoiceBox<ModBundle.FileTarget> targetChoiceBox = new ChoiceBox<>();
                    targetChoiceBox.getItems().addAll(ModBundle.FileTarget.DATAEP1, ModBundle.FileTarget.DATA);
                    targetChoiceBox.getSelectionModel().select(project.target);
                    targetChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                        project.target = newValue;
                    });
                    Label targetLabel = new Label("Install in:");

                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setSpacing(10.0);
                    hbox.getChildren().addAll(
                           projectName,
                           new Separator(Orientation.VERTICAL),
                           packageName,
                           new Separator(Orientation.VERTICAL),
                           targetLabel,
                           targetChoiceBox
                    );
                    setText(null);
                    setGraphic(hbox);
                }
            }
        });

//        // Set C++ DLLs editable items
//        dllsListView.setCellFactory(TextFieldListCell.forListView());
//        // Add a listener to add a new item at the end if the last item is being edited
//        dllsListView.setOnEditCommit(event -> {
//            int index = event.getIndex();
//            String newValue = event.getNewValue();
//
//            // Update the edited item
//            dllsListView.getItems().set(index, newValue);
//
//            // If the last item is edited, add a new blank item at the end
//            if (index == dllsListView.getItems().size() - 1 && !newValue.isEmpty()) {
//                dllsListView.getItems().add(DLLS_LIST_NEW_ITEM_TEXT);
//            }
//        });

        // Ban certain characters from unique tag
        uniqueTextField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches(ModBundle.UNIQUETAG_ALLOWED_REGEX + "+")) {
                return change;
            } else {
                return null;  // reject the change
            }
        }));

        // Update warnings
        // If uniqueTag is following the display name, then update automatically
        nameTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (Objects.equals(uniqueTextField.getText(), ModBundle.generateUniqueTagFromName(oldValue))) {
                uniqueTextField.setText(ModBundle.generateUniqueTagFromName(newValue));
            }
            updateWarnings();
        });
        uniqueTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            updateWarnings();
        });
    }

    private boolean showInternal(boolean saveSettingsOnExit) {
        dialog.setTitle("Mod Properties (" + modBundle.getName() + ")");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);

        nameTextField.setText(modBundle.getDisplayName());
        uniqueTextField.setText(modBundle.getUniqueTag());
        descriptionTextField.setText(modBundle.getDescription());
        websiteTextField.setText(modBundle.getWebsiteUrl());
        githubTextField.setText(modBundle.getGithubUrl());

        experimentalChoiceBox.getSelectionModel().select(modBundle.getExperimentalStatus());
        dataDependencyCheckBox.setSelected(modBundle.isCausesSaveDataDependency());
        galaxyResetCheckBox.setSelected(modBundle.isRequiresGalaxyReset());

        projectsListView.getItems().setAll(modBundle.getProjects().stream().map(project -> {
            ProjectWithTarget projectWithTarget = new ProjectWithTarget();
            projectWithTarget.project = project;
            projectWithTarget.target = modBundle.getPackageFileTarget(project);
            return projectWithTarget;
        }).collect(Collectors.toList()));

        filesAccordion.setExpandedPane(packagesPane);

        customModInfoLabel.setVisible(modBundle.hasCustomModInfo());

        modBundle.detectAndUpdateDllFiles(false);
        dllsListView.getItems().setAll(modBundle.getDllsToInstall());
//        dllsListView.getItems().add(DLLS_LIST_NEW_ITEM_TEXT);

        Optional<ButtonType> result = UIManager.get().showDialog(dialog);
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            UIManager.get().tryAction(this::saveModInfo, "Couldn't save mod properties into ModInfo.xml");
            return true;
        } else {
            return false;
        }
    }

    private void saveModInfo() throws ParserConfigurationException, TransformerException {
        modBundle.setDisplayName(nameTextField.getText());
        modBundle.setDescription(descriptionTextField.getText());
        modBundle.setUniqueTag(uniqueTextField.getText());
//        modBundle.setGithubUrl(githubTextField.getText());
        modBundle.setWebsiteUrl(websiteTextField.getText());
        modBundle.setCausesSaveDataDependency(dataDependencyCheckBox.isSelected());
        modBundle.setRequiresGalaxyReset(galaxyResetCheckBox.isSelected());
        modBundle.setExperimentalStatus(experimentalChoiceBox.getValue());

        // We don't support editing the DLLs to install, always default to autodetected
        modBundle.detectAndUpdateDllFiles(false);
//        modBundle.getDllsToInstall().clear();
//        // Discard the last item, which is a placeholder "Double click to add new file"
//        modBundle.getDllsToInstall().addAll(dllsListView.getItems().subList(0, dllsListView.getItems().size() - 1));

        projectsListView.getItems().forEach(projectWithTarget -> modBundle.setPackageFileTarget(projectWithTarget.project, projectWithTarget.target));

        modBundle.saveModInfo();
    }

    private void updateWarnings() {
        boolean showWarning = true;
        if (nameTextField.getText().isBlank()) {
            warningLabel.setText(WARNING_MOD_NAME_EMPTY);
        } else if (uniqueTextField.getText().isBlank()) {
            warningLabel.setText(WARNING_UNIQUE_TAG_EMPTY);
        } else if (uniqueTextField.getText().trim().length() < MIN_UNIQUE_TAG_LENGTH) {
            warningLabel.setText(WARNING_UNIQUE_TOO_SHORT);
        } else {
            showWarning = false;
        }

        warningLabel.setVisible(showWarning);
        dialog.getDialogPane().lookupButton(ButtonType.APPLY).setDisable(showWarning);
    }

    public static boolean show(ModBundle modBundle, boolean saveSettingsOnExit) {
        ModPropertiesUI node = UIManager.get().loadUI("dialogs/ModPropertiesUI");
        node.dialog = new Dialog<>();
        node.modBundle = modBundle;
        return node.showInternal(saveSettingsOnExit);
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
