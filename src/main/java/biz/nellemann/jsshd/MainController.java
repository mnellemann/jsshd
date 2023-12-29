package biz.nellemann.jsshd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainController {

    private final static Logger log = LoggerFactory.getLogger(MainController.class);

    private SshServer sshd;
    private String systemTempDirectory;


    @FXML
    private BorderPane borderPane;

    @FXML
    private Spinner<Integer> intPort;

    @FXML
    private Label labelHomeDirectory;

    @FXML
    private TextField usernameString;

    @FXML
    private TextField passwordString;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnStop;

    @FXML
    public TextArea textAreaLog;


    @FXML
    public void initialize() throws Exception {
        PrintStream printStream = new PrintStream(new MyCustomAppender(textAreaLog));
        System.setOut(printStream);
        System.setErr(printStream);

        systemTempDirectory = System.getProperty("java.io.tmpdir");
        labelHomeDirectory.setText(systemTempDirectory);
    }


    @FXML
    protected void onSelectButtonClick() {

        Stage stage = (Stage) borderPane.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if(selectedDirectory != null){
            systemTempDirectory = selectedDirectory.getAbsolutePath();
            labelHomeDirectory.setText(systemTempDirectory);
        }
    }


    @FXML
    protected void onStartButtonClick() {
        if(systemTempDirectory == null || systemTempDirectory.isEmpty()) {
            log.warn("No local folder selected.");
            return;
        }

        textAreaLog.setText("");
        Platform.runLater(() -> {
            try {
                startServer();
                btnStart.disableProperty().setValue(true);
                btnStop.disableProperty().setValue(false);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });
    }


    @FXML
    protected void onStopButtonClick() {
        Platform.runLater(() -> {
            btnStart.disableProperty().setValue(false);
            btnStop.disableProperty().setValue(true);
            stopServer();
        });
    }


    private void startServer() throws Exception {

        try {
            sshd = SshServer.setUpDefaultServer();
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

            SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder().build();
            factory.addSftpEventListener(new MySftpEventListener());
            sshd.setSubsystemFactories(Collections.singletonList(factory));

            sshd.setPasswordAuthenticator(new MyPasswordAuthenticator(usernameString.getText(), passwordString.getText()));
            sshd.setPort(intPort.getValue());
            sshd.start();
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }


    private void stopServer() {
        try {
            sshd.stop();
            sshd = null;
        } catch (Exception e) {
            log.warn("stopServer() - {}", e.getMessage());
        }
    }


}
