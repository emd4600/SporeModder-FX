package sporemodder;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import sporemodder.util.GamePathConfiguration;
import sporemodder.util.GitCommands;
import sporemodder.view.dialogs.GitAuthenticateUI;
import sporemodder.view.dialogs.GitPublishModUI;
import sporemodder.view.dialogs.SetGitUserUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;

public class GitHubManager extends AbstractManager {

    private static final String PROPERTY_gitUsername = "gitUsername";
    private static final String PROPERTY_gitEmail = "gitEmail";
    private static final String PROPERTY_hasShowGitDialog = "hasShowGitDialog";

    private String clientId;
    private String username;
    private String emailAddress;
    private String userAccessToken;
    private String lastDeviceLoginCode;
    private boolean hasShowGitDialog;

    /**
     * Returns the current instance of the GitHubManager class.
     */
    public static GitHubManager get() {
        return MainApp.get().getGitHubManager();
    }

    // If we reuse the same instance, it hangs on some requests...
    private HttpClient getHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.of(15, SECONDS)).build();
    }

    @Override
    public void initialize(Properties properties) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(GitHubManager.class.getResourceAsStream("/sporemodder/resources/githubApp.txt")))) {
            clientId = reader.readLine().trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        username = properties.getProperty(PROPERTY_gitUsername);
        emailAddress = properties.getProperty(PROPERTY_gitEmail);
        if (username != null) username = username.trim();
        if (emailAddress != null) emailAddress = emailAddress.trim();

        hasShowGitDialog = properties.getProperty(PROPERTY_hasShowGitDialog, "false").equals("true");
    }

    @Override public void saveSettings(Properties properties) {
        if (username != null && !username.isBlank()) {
            properties.put(PROPERTY_gitUsername, username);
        }
        if (emailAddress != null && !emailAddress.isBlank()) {
            properties.put(PROPERTY_gitEmail, emailAddress);
        }
        properties.put(PROPERTY_hasShowGitDialog, hasShowGitDialog ? "true" : "false");
    }

    public boolean hasUsernameAndEmail() {
        return username != null && !username.isBlank() && emailAddress != null && !emailAddress.isBlank();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Processes an HTTP response by checking the status code and if it's OK, splitting the body into key-value pairs
     * and returning them as a map. If the status code is not OK, throws an IOException with the body as the message.
     *
     * @param httpResponse the HTTP response to process
     * @return the map of key-value pairs from the response body
     * @throws IOException if the status code is not OK
     */
    private static Map<String, String> processHttpResponse(HttpResponse<String> httpResponse) throws IOException {
        if (httpResponse.statusCode() == HttpURLConnection.HTTP_OK) {
            String[] splits = httpResponse.body().split("&");
            Map<String, String> result = new HashMap<>();
            for (String split : splits) {
                String[] keyValue = split.split("=", 2);
                result.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
            }
            return result;
        } else {
            System.err.println("HTTP Error: " + httpResponse.statusCode());
            throw new IOException(httpResponse.body());
        }
    }

    private HttpRequest.Builder builderWithAuth(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + userAccessToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
    }

    public Map<String, String> requestDeviceLogin() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://github.com/login/device/code?client_id=" + clientId))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        Map<String, String> result = processHttpResponse(getHttpClient().send(request, HttpResponse.BodyHandlers.ofString()));
        lastDeviceLoginCode = result.get("device_code");
        return result;
    }

    public Map<String, String> checkDeviceLoginRequest() throws IOException, InterruptedException {
        assert lastDeviceLoginCode != null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://github.com/login/oauth/access_token" +
                        "?client_id=" + clientId +
                        "&device_code=" + lastDeviceLoginCode +
                        "&grant_type=urn:ietf:params:oauth:grant-type:device_code"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return processHttpResponse(getHttpClient().send(request, HttpResponse.BodyHandlers.ofString()));
    }

    private HttpResponse<String> getGitHubUserData() throws IOException, InterruptedException {
        HttpRequest request = builderWithAuth("https://api.github.com/user")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        return getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean validateUserAccessToken() throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = getGitHubUserData();
        // Our App does not have access for user data, but we don't care
        // We only want to know if we were authenticated
        return httpResponse.statusCode() != HttpURLConnection.HTTP_UNAUTHORIZED;
    }

    public JSONObject getGitHubUserDataJson() throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = getGitHubUserData();
        if (httpResponse.statusCode() == HttpURLConnection.HTTP_OK) {
            return new JSONObject(httpResponse.body());
        } else {
            return null;
        }
    }

    private boolean hasValidUserAccessToken() {
        if (userAccessToken == null) {
            return false;
        }
        try {
            return validateUserAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public boolean requireUserAccessToken() {
        if (hasValidUserAccessToken()) {
            return true;
        } else {
            return GitAuthenticateUI.show();
        }
    }

    public boolean requireUsernameAndEmail() {
        if (hasUsernameAndEmail()) {
            return true;
        } else {
            return SetGitUserUI.show();
        }
    }

    public JSONArray getGitHubSSHKeys() throws IOException, InterruptedException {
        HttpRequest request = builderWithAuth("https://api.github.com/user/keys")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() == HttpURLConnection.HTTP_OK) {
            return new JSONArray(httpResponse.body());
        } else {
            return null;
        }
    }

    public boolean addGitHubSSHKey(String sshKey) throws IOException, InterruptedException {
        String computerName = System.getenv("COMPUTERNAME");
        if (computerName == null) {
            computerName = System.getenv("HOSTNAME");
        }
        String title = emailAddress + (computerName == null ? "" : computerName);
        HttpRequest request = builderWithAuth("https://api.github.com/user/keys")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject()
                .put("title", title)
                .put("key", sshKey)
                .toString()))
                .build();
        HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return httpResponse.statusCode() == HttpURLConnection.HTTP_CREATED;
    }

    private String checkSSHKeyIfSameEmail(String sshKey) {
        String[] splits = sshKey.trim().split(" ");
        if (splits[splits.length - 1].equalsIgnoreCase(emailAddress)) {
            return sshKey.trim();
        }
        return null;
    }

    /**
     * Reads the contents of the file and, if the last segment (separated by whitespace) is the same as
     * the configured email address, it returns the contents.
     * @param path
     * @return
     * @throws IOException
     */
    private String readSSHKeyIfSameEmail(Path path) throws IOException {
        if (Files.exists(path)) {
            return checkSSHKeyIfSameEmail(Files.readString(path));
        }
        return null;
    }

    private Path findGitBashExecutable() {
        try {
            List<String> gitPaths = GitCommands.runCommandCaptureOutput(Paths.get(System.getProperty("user.home")), "where", "git.exe");
            if (!gitPaths.isEmpty()) {
                Path gitPath = Paths.get(gitPaths.get(0));
                Path gitBashPath = gitPath.resolve("../../bin/bash.exe");
                if (Files.exists(gitBashPath)) {
                    return gitBashPath.normalize();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static List<String> runGitBashCommand(Path gitBashPath, Path directory, String... args) throws IOException, InterruptedException {
        if (isWindowsOS()) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(' ');
            }
            String argString = sb.toString().trim();
            System.out.println(argString);
            return GitCommands.runCommandCaptureOutput(directory, gitBashPath.toAbsolutePath().toString(), "-c", argString);
        } else {
            return GitCommands.runCommandCaptureOutput(directory, args);
        }
    }

    /**
     * Returns the text of an existing ssh key in this computer associated with the current emailAddress.
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String findComputerSSHKeys() throws IOException, InterruptedException {
        // First try: find directly under User/.ssh/id_ed25519.pub or id_rsa.pub
        Path userDir = Paths.get(System.getProperty("user.home"));
        Path edFile = userDir.resolve(".ssh/id_ed25519_smfx.pub");
        String sshKey = readSSHKeyIfSameEmail(edFile);
        if (sshKey != null) {
            return sshKey;
        }

        edFile = userDir.resolve(".ssh/id_ed25519.pub");
        sshKey = readSSHKeyIfSameEmail(edFile);
        if (sshKey != null) {
            return sshKey;
        }

        Path rsaFile = userDir.resolve(".ssh/id_rsa.pub");
        sshKey = readSSHKeyIfSameEmail(rsaFile);
        if (sshKey != null) {
            return sshKey;
        }

        // Second try: using git bash
        boolean windowsOS = isWindowsOS();
        Path gitBashPath = windowsOS ? findGitBashExecutable() : null;
        if (!windowsOS || gitBashPath != null) {
            try {
                sshKey = checkSSHKeyIfSameEmail(runGitBashCommand(gitBashPath, userDir, "cat", "~/.ssh/id_ed25519.pub").get(0));
                if (sshKey != null) {
                    return sshKey;
                }
            } catch (Exception e) {
            }
            return checkSSHKeyIfSameEmail(runGitBashCommand(gitBashPath, userDir, "cat", "~/.ssh/id_rsa.pub").get(0));
        }

        return null;
    }

    // GitHub sshKey does not contain the email, which the computer one does at the end
    private String removeEmailFromSSHKey(String sshKey) {
        return sshKey.substring(0, sshKey.lastIndexOf(" "));
    }

    /**
     * Ensures that the GitHub account has an SSH key associated with the current computer.
     * First it finds the existing SSH keys in the computer, and tries connecting those to GitHub.
     * If no computer SSH key is found, it creates a new one.
     * Returns true if the SSH key existed or was created and added successfully, false otherwise.
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean ensureGitHubUserHasSSHKey() throws IOException, InterruptedException {
        String sshKey = null;
        try {
            sshKey = findComputerSSHKeys();
            if (sshKey != null) {
                System.out.println("[GitHubManager] Found existing SSH key in computer");
                sshKey = removeEmailFromSSHKey(sshKey);
                // Check if user already has this key in the GitHub account
                JSONArray keys = getGitHubSSHKeys();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); ++i) {
                        JSONObject item = keys.getJSONObject(i);
                        if (sshKey.equals(item.getString("key"))) {
                            System.out.println("[GitHubManager] SSH key already configured in GitHub account; nothing was done");
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        if (sshKey == null) {
            // Create new ssh key
            Path gitBashPath = null;
            if (isWindowsOS()) {
                gitBashPath = findGitBashExecutable();
                if (gitBashPath == null) {
                    throw new IOException("Failed to generate SSH keys, cannot find Git Bash");
                }
            }
            Path userHome = Paths.get(System.getProperty("user.home"));
            // This does not work on linux because it seems ProcessBuilder does not like empty parameters
            // In Windows it should work
            System.out.println("[GitHubManager] Creating a SSH key for GitHub account");
            runGitBashCommand(gitBashPath, userHome, "ssh-keygen", "-t", "ed25519",
                    "-C", emailAddress, "-f", "~/.ssh/id_ed25519_smfx", "-N", "''");
            sshKey = runGitBashCommand(gitBashPath, userHome, "cat", "~/.ssh/id_ed25519_smfx.pub").get(0);
        }

        // Add SSH key to GitHub
        return addGitHubSSHKey(sshKey);
    }

    /**
     * Sets the Git username and email for the given repository directory.
     * @param directory the directory of the repository
     * @throws IOException if there is an error running the command
     * @throws InterruptedException if the thread is interrupted
     */
    public void configGitUsernameAndEmailForRepo(Path directory) throws IOException, InterruptedException {
        GitCommands.gitConfig(directory, "user.name", username);
        GitCommands.gitConfig(directory, "user.email", emailAddress);
    }

    public boolean hasConfigGitUsernameAndEmailInRepo(Path directory) {
        try {
            // If it is not set the command returns 1, so it raises an exception
            GitCommands.gitConfigGet(directory, "user.name");
            GitCommands.gitConfigGet(directory, "user.email");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the user has Git installed.
     * @return true if the user has Git installed, false otherwise
     */
    public boolean hasGitInstalled() {
        try {
            // If it is not installed the command returns 1, so it raises an exception
            GitCommands.runCommand(Paths.get(System.getProperty("user.home")), "git", "--version");
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * If the user has Git installed, returns true. Otherwise, shows a dialog explaining that Git is not installed
     * and offers a link to download it. If the user closes the dialog and Git is still not installed, returns false.
     * @return true if the user has Git installed, false otherwise
     */
    public boolean requireGitInstalled() {
        if (hasGitInstalled()) {
            return true;
        } else {
            Label label = new Label("Git is not installed on your computer. You can download it here:");
            label.setWrapText(true);

            Hyperlink link = new Hyperlink("https://git-scm.com/downloads");
            link.setWrapText(true);
            link.setOnAction(event -> {
                MainApp.get().getHostServices().showDocument(link.getText());
            });

            VBox vbox = new VBox();
            vbox.setSpacing(5.0);
            vbox.getChildren().addAll(label, link);

            Alert alert = new Alert(Alert.AlertType.WARNING, null, ButtonType.OK);
            alert.setTitle("Git not installed");
            alert.getDialogPane().setContent(vbox);

            UIManager.get().showDialog(alert);

            return hasGitInstalled();
        }
    }

    public List<String> getUserRepositoryNames() {
        List<String> result = new ArrayList<>();
        try {
            HttpRequest request = builderWithAuth("https://api.github.com/user/repos" +
                    "?visibility=all" +
                    "&per_page=100" +
                    "&affiliation=owner")
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();
            HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() == HttpURLConnection.HTTP_OK) {
                JSONArray items = new JSONArray(httpResponse.body());
                for (int i = 0; i < items.length(); ++i) {
                    JSONObject item = items.getJSONObject(i);
                    result.add(item.getString("name"));
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject createGitHubRepository(String repositoryName, String description, boolean isPrivate) throws IOException, InterruptedException {
        HttpRequest request = builderWithAuth("https://api.github.com/user/repos")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject()
                        .put("name", repositoryName)
                        .put("description", description)
                        .put("homepage", "https://github.com/" + username + "/" + repositoryName)
                        .put("private", isPrivate)
                        .toString()))
                .build();
        HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return new JSONObject(httpResponse.body());
        } else {
            return null;
        }
    }

    /**
     * If the mod already has a remote repository configured, show a warning dialog
     * asking the user if they want to overwrite it. If the user chooses to overwrite,
     * this method returns true. If the user chooses to cancel, this method returns false.
     * If the mod does not have a remote repository configured, this method returns true.
     * @param directory The directory of the mod.
     * @return true if the user chooses to overwrite, false if the user chooses to cancel.
     */
    public boolean warnIfRepositoryAlreadyHasRemote(Path directory) {
        String remoteUrl = null;
        try {
            remoteUrl = GitCommands.gitGetOriginURL(directory);
        } catch (Exception e) {
        }

        if (remoteUrl != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    null,
                    ButtonType.YES, ButtonType.CANCEL);
            alert.setTitle("Mod already has repository");
            Label label = new Label("The mod already has a remote repository configured. Do you want to overwrite it?");
            label.setWrapText(true);
            alert.getDialogPane().setContent(label);

            Optional<ButtonType> result = UIManager.get().showDialog(alert);
            if (result.isPresent() && result.get() == ButtonType.YES) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void setRepositoryTopics(String repositoryName, List<String> topics) throws IOException, InterruptedException {
        HttpRequest request = builderWithAuth("https://api.github.com/repos/" + username + "/" + repositoryName + "/topics")
                .version(HttpClient.Version.HTTP_1_1)
                .PUT(HttpRequest.BodyPublishers.ofString(new JSONObject()
                        .put("names", new JSONArray().putAll(topics))
                        .toString()))
                .build();
        getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * If the user has never inputted their GitHub username, shows a dialog that asks for it.
     * After the dialog is shown, it will not be shown again.
     */
    public void showFirstTimeDialog() {
        if (!hasShowGitDialog) {
            SetGitUserUI.show();
            hasShowGitDialog = true;
            MainApp.get().saveSettings();
        }
    }
}
