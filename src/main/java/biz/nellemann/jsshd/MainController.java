package biz.nellemann.jsshd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final String OPENSSH_PRIVATE_KEY_CONTENT = """
 -----BEGIN OPENSSH PRIVATE KEY-----
 b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAABsgAAAAdzc2gtZH
 NzAAAAgQDTQYaDg3ATS4iWWiqy3B3vLGN/p3jhnfbffKhxprFvhJAvbJRBngr8xfe572dR
 vu01mFLzc7SYMqSsKofxA7sQWEHOkuB+bajWh9RXxF7eQtMbCyea42+pSGENWAH1lGQatS
 fXIUVuTnUmHl/uzM5hV6hikOLJnyBQwst5huWIhQAAABUAwKjM4z/fTNQg8Sh7L2MWRqvH
 /fcAAACAWUJiaac2X8yzut7i5LW1YKS7Mfs53ma6syB6b4s82yWriPTJVAOOHzL1c72gpX
 00iCnCUL9t3h/u8hovqL8BKoIV7NvbBYOn33Cer+Dkm434lCD0nW2rdqBi5UXny1S310p4
 RL/WmtFpOKOvMKJIsgFqpK2UcRv5C6bm3eA9MnsAAACBAJUMETfLmWTrSlIBenJ6Xs8iU7
 nlbBaSyF/B3loR89zrpLEHxl3lwByXcNqt2+/5BMe4sbOJTqL899JWcfCd1mDV/89eGY3t
 JEfy6K2dKNFK3hvhxfSDa5fUNKPR484hbbT2KHzeW5+V5gd6LYk/2QG4AoX8t6YDaZGAc5
 p4IGmXAAAB+K0FcICtBXCAAAAAB3NzaC1kc3MAAACBANNBhoODcBNLiJZaKrLcHe8sY3+n
 eOGd9t98qHGmsW+EkC9slEGeCvzF97nvZ1G+7TWYUvNztJgypKwqh/EDuxBYQc6S4H5tqN
 aH1FfEXt5C0xsLJ5rjb6lIYQ1YAfWUZBq1J9chRW5OdSYeX+7MzmFXqGKQ4smfIFDCy3mG
 5YiFAAAAFQDAqMzjP99M1CDxKHsvYxZGq8f99wAAAIBZQmJppzZfzLO63uLktbVgpLsx+z
 neZrqzIHpvizzbJauI9MlUA44fMvVzvaClfTSIKcJQv23eH+7yGi+ovwEqghXs29sFg6ff
 cJ6v4OSbjfiUIPSdbat2oGLlRefLVLfXSnhEv9aa0Wk4o68wokiyAWqkrZRxG/kLpubd4D
 0yewAAAIEAlQwRN8uZZOtKUgF6cnpezyJTueVsFpLIX8HeWhHz3OuksQfGXeXAHJdw2q3b
 7/kEx7ixs4lOovz30lZx8J3WYNX/z14Zje0kR/LorZ0o0UreG+HF9INrl9Q0o9HjziFttP
 YofN5bn5XmB3otiT/ZAbgChfy3pgNpkYBzmnggaZcAAAAUOyNEy6IloTZIpSAaoDa8YTtV
 JMAAAAAcbWFya0BtYXJrYm9vay5wcnYuZGsuaWJtLmNvbQECAwQFBg==
 -----END OPENSSH PRIVATE KEY-----""";

    private SshServer sshd;
    private String systemTempDirectory;
    private Path keyFile;


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

        System.err.println("System TMP: " + systemTempDirectory);
        keyFile = Paths.get(systemTempDirectory, "jsshd-server.key");

        try {
            Files.writeString(keyFile, OPENSSH_PRIVATE_KEY_CONTENT, StandardCharsets.UTF_8);
            log.info("Writing SSH Host Key: " + keyFile.toString());
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }

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
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyFile));

            SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder()
                    .build();
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
