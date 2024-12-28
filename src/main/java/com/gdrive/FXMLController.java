package com.gdrive;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLController implements javafx.fxml.Initializable {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ToggleButton modeSwitch;
    @FXML
    private TextField timeInvField;
    @FXML
    private Button browseFilesButton;
    @FXML
    private Button browsePathButton;
    @FXML
    private ListView<String> fileListView;
    @FXML
    private TextField downloadPathField;
    @FXML
    private Button startButton;

    private ObservableList<String> fileNames;

    // Create a custom OutputStream that writes to the TextArea
    private class TextAreaOutputStream extends OutputStream {
        private final StringBuilder builder = new StringBuilder();

        @Override
        public void write(int b) {
            builder.append((char) b);
            if ((char) b == '\n') {
                // Update the TextArea when a new line is written to the OutputStream
                terminalOutputArea.appendText(builder.toString());
                builder.setLength(0); // Clear the StringBuilder
                // Auto-scroll the TextArea to the bottom
                terminalOutputArea.setScaleZ(Double.MAX_VALUE);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initializing FXMLController");
        // Redirect System.out to the TextAreaOutputStream
//        System.setOut(new PrintStream(new TextAreaOutputStream()));
//        System.setErr(new PrintStream(new TextAreaOutputStream()));  // Redirect errors as well

        // Example: Output something to System.out immediately when the application starts
        System.out.println("Application has started.");
       // runTerminalCommand("ls"); // Run a terminal command (e.g., 'ls') automatically
        loadFormDataFromJSON();
        setupTimeInvFieldFormatter();
    }

    private void setupTimeInvFieldFormatter() {
        System.out.println("Setting up time interval field formatter");
        TextFormatter<String> numberFormatter = createNumberFormatter();
        timeInvField.setTextFormatter(numberFormatter);
        setupTimeInvFieldListener();
    }

    private TextFormatter<String> createNumberFormatter() {
        System.out.println("Creating number formatter");
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() < change.getControlText().length()) {
                return change;
            }
            return change.getText().matches("[0-9.]") ? change : null;
        });
    }

    private void setupTimeInvFieldListener() {
        System.out.println("Setting up time interval field listener");
        timeInvField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                validateTimeInput(oldValue, newValue);
            }
        });
    }

    private void validateTimeInput(String oldValue, String newValue) {
        System.out.println("Validating time input: " + newValue);
        try {
            Float value = Float.valueOf(newValue);
            if (value < 0 || value > 23) {
                timeInvField.setText(oldValue);
            }
        } catch (NumberFormatException e) {
            timeInvField.setText(oldValue);
        }
    }

    @FXML
    private void toggleModeAction(ActionEvent event) {
        System.out.println("Toggling mode " + formData.isDownloadMode());
        if (modeSwitch.isSelected()) {
            formData.setDownloadMode(true);
            modeSwitch.setText("Download");
        } else {
            formData.setDownloadMode(false);
            modeSwitch.setText("Upload");
        }
    }

    @FXML
    public void browseMultipleFiles() {
        System.out.println("Browsing multiple files");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog((Stage) browseFilesButton.getScene().getWindow());

        handleSelectedFiles(selectedFiles);
    }

    private void handleSelectedFiles(List<File> selectedFiles) {
        System.out.println("Handling selected files");
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            fileNames = FXCollections.observableArrayList();
            selectedFiles.forEach(file -> fileNames.add(file.getAbsolutePath()));
            fileListView.setItems(fileNames);
        } else {
            showAlert("No Files Selected", "No files were selected.", AlertType.WARNING);
        }
    }

    @FXML
    public void handleFileDoubleClick(MouseEvent event) {
        System.out.println("Handling file double click");
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            System.out.println("You double-clicked on: " + selectedFile);
        }
    }

    @FXML
    public void browseSaveFolder() {
        System.out.println("Browsing save folder");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Download Save Folder");

        File selectedFolder = directoryChooser.showDialog((Stage) browsePathButton.getScene().getWindow());

        if (selectedFolder != null) {
            downloadPathField.setText(selectedFolder.getAbsolutePath());
        } else {
            showAlert("No Folder Selected", "No folder was selected.", AlertType.WARNING);
        }
    }

    private void showAlert(String title, String content, AlertType alertType) {
        System.out.println("Showing alert: " + title);
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSaveButtonAction() {
        System.out.println("Handling save button action");
        String email = emailField.getText() != null ? emailField.getText() : "";
        boolean isDownloadMode = formData.isDownloadMode();
        double timeInterval = Double.parseDouble(timeInvField.getText() != null ? timeInvField.getText() : "0");
        List<String> selectedFiles = fileListView.getItems() != null ? fileListView.getItems() : Collections.emptyList();
        String downloadPath = downloadPathField.getText() != null ? downloadPathField.getText() : "";

        formData = new FXMLData(email, isDownloadMode, timeInterval, downloadPath, selectedFiles);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("formData.json"), formData);
            showAlert("Success", "Form data saved successfully", AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save form data \n" + e , AlertType.ERROR);
        }
    }

    FXMLData formData = new FXMLData();

    public void loadFormDataFromJSON() {
        System.out.println("Loading form data from JSON");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            formData = objectMapper.readValue(new File("formData.json"), FXMLData.class);

            emailField.setText(formData.getEmail());
            modeSwitch.setSelected(formData.isDownloadMode());
            timeInvField.setText(String.valueOf(formData.getTimeInterval()));
            downloadPathField.setText(formData.getDownloadPath());
            fileListView.setItems(FXCollections.observableArrayList(formData.getSelectedFiles()));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load form data \n " + e, AlertType.ERROR);
        }
    }

    GoogleDriveUploader uploader = new GoogleDriveUploader();
    Task uploadTask;

    @FXML
    private void handleStartButtonAction() {
        System.out.println("Handling start button action");

        // Disable the button to prevent multiple triggers
        startButton.setDisable(true);

        // Create a Task to run the uploader in the background
        uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    uploader.startMethod(formData.getEmail(), formData.isDownloadMode(),

                            formData.getTimeInterval(), formData.getDownloadPath(), formData.getSelectedFiles());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e; // Propagate the exception to the task's failure state
                }
                return null;
            }
        };

        // Handle success
        uploadTask.setOnSucceeded(event -> {
            System.out.println("Operation completed successfully.");
            startButton.setDisable(false); // Re-enable the button
        });

        // Handle failure
        uploadTask.setOnFailed(event -> {
            System.out.println("Operation failed.");
            startButton.setDisable(false); // Re-enable the button
            Throwable error = uploadTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        // Run the task in a background thread
        Thread backgroundThread = new Thread(uploadTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    @FXML
    private void handleStopButtonAction() {
        if (uploadTask != null && uploadTask.isRunning()) {
            uploadTask.cancel(); // Request cancellation
            startButton.setDisable(false);
            System.out.println("Stop button clicked. Task cancellation requested.");
        }
    }

    @FXML
    private TextArea terminalOutputArea;

    @FXML
    private Button runCommandButton;

    // Method to handle the button click event
    @FXML
    private void runCommand() {
        runTerminalCommand("ls"); // Example: 'ls' command for Unix-based systems
    }

    // Method to run the terminal command and capture its output
    private void runTerminalCommand(String command) {
        try {
            // Create ProcessBuilder for terminal command
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").startsWith("Windows")) {
                processBuilder.command("cmd.exe", "/c", command); // Windows
            } else {
                processBuilder.command("bash", "-c", command); // Unix-based (Linux/macOS)
            }

            // Start the process and get the output
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Capture the output of the command and print to System.out (which is redirected to TextArea)
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage()); // Print errors to System.err (redirected to TextArea)
        }
    }

}

class FXMLData {
    @JsonProperty("email")
    private String email;
    @JsonProperty("isDownloadMode")
    private boolean isDownloadMode;
    @JsonProperty("timeInterval")
    private double timeInterval;
    @JsonProperty("downloadPath")
    private String downloadPath;
    @JsonProperty("selectedFiles")
    private List<String> selectedFiles;

    public FXMLData() {
    }

    public FXMLData(String email, boolean isDownloadMode, double timeInterval, String downloadPath, List<String> selectedFiles) {
        this.email = email;
        this.isDownloadMode = isDownloadMode;
        this.timeInterval = timeInterval;
        this.downloadPath = downloadPath;
        this.selectedFiles = selectedFiles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isDownloadMode() {
        return isDownloadMode;
    }

    public void setDownloadMode(boolean downloadMode) {
        this.isDownloadMode = downloadMode;
    }

    public double getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(double timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    @Override
    public String toString() {
        return "FXMLData [email=" + email + ", password=" + ", isDownloadMode=" + isDownloadMode + ", timeInterval=" + timeInterval + ", downloadPath=" + downloadPath + ", selectedFiles=" + selectedFiles + "]";
    }
}