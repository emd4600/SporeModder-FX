package sporemodder.view.dialogs;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import sporemodder.util.GitCommands;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleOutputCommandTask extends GitCommands.CommandTask {

    private Runnable onDone;
    private TextArea consoleTextArea;

    public ConsoleOutputCommandTask(TextArea consoleTextArea, Runnable onDone) {
        this.consoleTextArea = consoleTextArea;
        this.onDone = onDone;
    }

    @Override
    public Void call() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String text = line;
                Platform.runLater(() -> consoleTextArea.appendText(text + "\n"));
            }
        }
        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException(String.format("runCommand returned %d", exit));
        }
        Platform.runLater(onDone);
        return null;
    }
}