package biz.nellemann.jsshd;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ResourceBundle;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Make all stages close and the app exit when the primary stage is closed
        Platform.setImplicitExit(true);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
        });

        // Set icon on the application bar
        var appIcon = new Image("/icon.png");
        primaryStage.getIcons().add(appIcon);

        // Set icon on the taskbar/dock
        if (Taskbar.isTaskbarSupported()) {
            var taskbar = Taskbar.getTaskbar();

            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                var dockIcon = defaultToolkit.getImage(getClass().getResource("/icon.png"));
                taskbar.setIconImage(dockIcon);
            }

        }

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("biz.nellemann.jsshd.application"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Minimal SSH File Server v" + fxmlLoader.getResources().getString("app.version"));
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}