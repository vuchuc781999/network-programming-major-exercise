package chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Chat app");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    MessageSender sender = MessageSender.getInstance();
                    sender.disconnect();

                    Platform.exit();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
