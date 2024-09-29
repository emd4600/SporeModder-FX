package sporemodder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class GitManager {

    public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
        if (directory == null || !Files.exists(directory)) {
            throw new RuntimeException("Can't run command in non-existing directory '" + directory + "'");
        }
        ProcessBuilder pb = new ProcessBuilder()
                .command(command)
                .directory(directory.toFile());
        Process p = pb.start();
//        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
//        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
//        outputGobbler.start();
//        errorGobbler.start();
        int exit = p.waitFor();
//        errorGobbler.join();
//        outputGobbler.join();
        if (exit != 0) {
            throw new AssertionError(String.format("runCommand returned %d", exit));
        }
    }

    public static void gitInit(Path directory) throws IOException, InterruptedException {
        runCommand(directory, "git", "init");
    }

    public static void gitAddAll(Path directory) throws IOException, InterruptedException {
        runCommand(directory, "git", "add", ".");
    }

    public static void gitCommit(Path directory, String message) throws IOException, InterruptedException {
        runCommand(directory, "git", "commit", "-m", message);
    }
}
