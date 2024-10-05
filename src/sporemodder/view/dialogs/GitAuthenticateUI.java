package sporemodder.view.dialogs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.json.JSONObject;
import sporemodder.GitHubManager;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.view.Controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GitAuthenticateUI implements Controller {

    private class AuthenticateCheckTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                Map<String, String> result = GitHubManager.get().checkDeviceLoginRequest();
                if (result.containsKey("access_token")) {
                    GitHubManager.get().setUserAccessToken(result.get("access_token"));
                    currentTimer.cancel();

                    Platform.runLater(() -> {
                        dialog.setResult(ButtonType.OK);
                        dialog.close();
                    });

                } else if (result.containsKey("interval")) {
                    currentTimer.cancel();
                    long newIntervalMSs = (Integer.max(Integer.parseInt(result.get("interval")), 5) + 1) * 1000L;
                    currentTimer = new Timer();
                    currentTimer.scheduleAtFixedRate(new AuthenticateCheckTimerTask(), newIntervalMSs, newIntervalMSs);
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> UIManager.get().showErrorDialog(e, "Failed to wait for device login", true));
            }
        }
    }
    private Timer currentTimer;

    private Dialog<ButtonType> dialog;
    @FXML
    private Node mainNode;
    @FXML
    private Hyperlink githubLoginLink;
    @FXML
    private TextField loginCodeTextField;

    @FXML
    private void initialize() {
        githubLoginLink.setOnAction(event -> MainApp.get().getHostServices().showDocument(githubLoginLink.getText()));
    }

    private boolean showInternal() {
        dialog.setTitle("Authenticate to GitHub");

        dialog.getDialogPane().setContent(mainNode);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = ((Button)dialog.getDialogPane().lookupButton(ButtonType.OK));
        okButton.setText("Waiting...");
        okButton.setDisable(true);

        Map<String, String> result;
        try {
            result = GitHubManager.get().requestDeviceLogin();
        } catch (IOException | InterruptedException e) {
            UIManager.get().showErrorDialog(e, "Failed to communicate with GitHub", true);
            return false;
        }

        loginCodeTextField.setText(result.get("user_code"));
        githubLoginLink.setText(URLDecoder.decode(result.get("verification_uri"), StandardCharsets.UTF_8));

        int intervalSeconds = 5;
        if (result.containsKey("interval")) {
            intervalSeconds = Integer.max(Integer.parseInt(result.get("interval")), intervalSeconds);
        }
        currentTimer = new Timer();
        currentTimer.scheduleAtFixedRate(new AuthenticateCheckTimerTask(), 0, (intervalSeconds + 1) * 1000L);

        dialog.setOnShown(event -> {
            loginCodeTextField.requestFocus();
            loginCodeTextField.selectAll();
            loginCodeTextField.requestFocus();
        });

        boolean dialogResult = UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) == ButtonType.OK;
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer.purge();
        }
        if (dialogResult) {
            // Check if authenticated user is the same as the SMFX config one
            try {
                JSONObject userData = GitHubManager.get().getGitHubUserDataJson();
                if (userData != null && userData.has("login")) {
                    String userDataLogin = userData.getString("login");
                    if (!userDataLogin.equals(GitHubManager.get().getUsername())) {
                        UIManager.get().showDialog(Alert.AlertType.WARNING,
                                "You authenticated as '" + userDataLogin + "', but the username saved in SporeModder FX" +
                                        " is '" + GitHubManager.get().getUsername() + "'. This might cause problems with git.\n" +
                                        "If you want to change it, restart SporeModder FX, or change the username in 'config.properties'.");

                        GitHubManager.get().setUsername(userDataLogin);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Try to generate SSH keys
            UIManager.get().tryAction(() -> {
                if (!GitHubManager.get().ensureGitHubUserHasSSHKey()) {
                    throw new RuntimeException("Failed to generate SSH keys. Some git actions might not work.");
                }
            }, "Failed to generate SSH keys. Some git actions might not work.", false);
        }
        return dialogResult;
    }

    /**
     * Shows the authentication dialog. Once the user is authenticated, it will set the user access token
     * in the GitHubManager, and return true. It will also try to generate the SSH keys for the user,
     * if needed. If the authentication fails, returns false.
     *
     * @return {@code true} if the user authenticated successfully, {@code false} otherwise.
     */
    public static boolean show() {
        GitAuthenticateUI node = UIManager.get().loadUI("dialogs/GitAuthenticateUI");
        node.dialog = new Dialog<>();
        return node.showInternal();
    }

    @Override
    public Node getMainNode() {
        return mainNode;
    }
}
