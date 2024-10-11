package sporemodder.util;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitCommands {

    public static String getPublishedUrl(String githubUrl) {
        if (!githubUrl.endsWith("/")) {
            githubUrl += "/";
        }
        return githubUrl + "releases/latest";
    }

    public static abstract class CommandTask extends Task<Void> {
        public Process process;
    }

    public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
        runCommandCaptureOutput(directory, command);
    }

    /**
     * Runs a command and returns a list of all the lines from the process standard output.
     * @param directory
     * @param command
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> runCommandCaptureOutput(Path directory, String... command) throws IOException, InterruptedException {
        if (directory == null || !Files.exists(directory)) {
            throw new IOException("Can't run command in non-existing directory '" + directory + "'");
        }
        ProcessBuilder pb = new ProcessBuilder()
                .redirectErrorStream(true)
                .command(command)
                .directory(directory.toFile());
        Process p = pb.start();
        List<String> lines = new ArrayList<>();
        Thread outputReader = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outputReader.start();
        int exit = p.waitFor();
        if (exit != 0) {
            outputReader.join();
            StringBuilder sb = new StringBuilder();
            sb.append("runCommand returned ");
            sb.append(exit);
            sb.append("\n");
            System.err.println("[ERROR] runCommand returned " + exit);
            for (String line : lines) {
                System.err.println(line);
                sb.append(line).append('\n');
            }
            throw new IOException(sb.toString());
        }
        // Wait for the thread to finish
        outputReader.join();
        return lines;
    }

    /**
     * Runs a command asynchronously in a separate thread, and calls methods on the provided task depending on the status of the process.
     * The task's {@link #process} field is set to the process.
     * @param task
     * @param directory
     * @param command
     * @return the thread that was started
     * @throws IOException
     * @throws InterruptedException
     */
    public static Thread runCommandAsync(CommandTask task, Path directory, String... command) throws IOException, InterruptedException {
        if (directory == null || !Files.exists(directory)) {
            throw new IOException("Can't run command in non-existing directory '" + directory + "'");
        }
        ProcessBuilder pb = new ProcessBuilder()
                .redirectErrorStream(true)
                .command(command)
                .directory(directory.toFile());
        task.process = pb.start();
        Thread thread = new Thread(task);
        thread.start();
        return thread;
    }

    /**
     * Returns a list of the relative paths to all untracked (but not ignored) git files in the given directory.
     * @param directory
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> gitGetUntrackedFiles(Path directory) throws IOException, InterruptedException {
        return runCommandCaptureOutput(directory, "git", "ls-files", "--exclude-standard", "--others");
    }


    /**
     * Returns a list of the relative paths to all modified files (both staged and unstaged) in the given directory.
     * @param directory
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> gitGetModifiedFiles(Path directory) throws IOException, InterruptedException {
        List<String> files = new ArrayList<>();
        files.addAll(runCommandCaptureOutput(directory, "git", "diff", "--name-only", "--cached"));  // staged
        files.addAll(runCommandCaptureOutput(directory, "git", "diff", "--name-only"));  // unstaged
        return files;
    }

    /**
     * Asynchronously runs the equivalent of <code>git push</code> in the given directory and returns the thread
     * that runs the given task.
     * @param task the task to run
     * @param directory the directory to run the command in
     * @return the thread that was started
     * @throws IOException if there is an error running the command
     * @throws InterruptedException if the thread is interrupted
     */
    public static Thread gitPush(CommandTask task, Path directory) throws IOException, InterruptedException {
        return runCommandAsync(task, directory, "git", "push");
    }

    /**
     * Asynchronously runs the equivalent of <code>git pull</code> in the given directory and returns the thread
     * that runs the given task.
     * @param task the task to run
     * @param directory the directory to run the command in
     * @return the thread that was started
     * @throws IOException if there is an error running the command
     * @throws InterruptedException if the thread is interrupted
     */
    public static Thread gitPull(CommandTask task, Path directory) throws IOException, InterruptedException {
        return runCommandAsync(task, directory, "git", "pull");
    }

    public static Thread gitCreateTag(CommandTask task, Path directory, String tag) throws IOException, InterruptedException {
        return runCommandAsync(task, directory, "git", "tag", tag);
    }

    public static Thread gitPushTag(CommandTask task, Path directory, String tag) throws IOException, InterruptedException {
        return runCommandAsync(task, directory, "git", "push", "origin", "tag", tag);
    }

    /**
     * Adds the given list of files to the Git index in the given directory.
     * @param directory
     * @param files
     * @throws IOException
     * @throws InterruptedException
     */
    public static void gitAddAll(Path directory, List<String> files) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(files.size() + 2);
        command.add("git");
        command.add("add");
        command.addAll(files);
        runCommand(directory, command.toArray(new String[0]));
    }

    /**
     * Initializes a new Git repository in the given directory.
     * @param directory
     * @throws IOException
     * @throws InterruptedException
     */
    public static void gitInit(Path directory) throws IOException, InterruptedException {
        runCommand(directory, "git", "init");
    }

    /**
     * Adds all files in the given directory to the Git index.
     * @param directory
     * @throws IOException
     * @throws InterruptedException
     */
    public static void gitAddAll(Path directory) throws IOException, InterruptedException {
        runCommand(directory, "git", "add", ".");
    }

    public static void gitCommit(Path directory, String message) throws IOException, InterruptedException {
        runCommand(directory, "git", "commit", "-m", message);
    }

    public static void gitSetMainBranch(Path directory, String branch) throws IOException, InterruptedException {
        runCommand(directory, "git", "branch", "-m", branch);
    }

    public static void gitConfig(Path directory, String key, String value) throws IOException, InterruptedException {
        runCommand(directory, "git", "config", key, value);
    }

    public static void gitConfigGet(Path directory, String key) throws IOException, InterruptedException {
        runCommand(directory, "git", "config", "--get", key);
    }

    /**
     * Returns the URL of the remote named "origin" for the given Git repository directory,
     * or null if there is no such remote or if the command fails.
     * @param directory the directory of the Git repository
     * @return the URL of the remote named "origin", or null
     */
    public static String gitGetOriginURL(Path directory) {
        try {
            return runCommandCaptureOutput(directory, "git", "remote", "get-url", "origin").get(0);
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public static void gitSetOriginURL(Path directory, String url) throws IOException, InterruptedException {
        runCommand(directory, "git", "remote", "set-url", "origin", url);
    }

    public static void gitAddOriginURL(Path directory, String url) throws IOException, InterruptedException {
        runCommand(directory, "git", "remote", "add", "origin", url);
    }

    public static String gitGetCurrentBranch(Path directory) throws IOException, InterruptedException {
        return runCommandCaptureOutput(directory, "git", "rev-parse", "--abbrev-ref", "HEAD").get(0);
    }
}
