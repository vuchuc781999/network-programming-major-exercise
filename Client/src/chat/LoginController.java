package chat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameTextField;

    public void onStart(ActionEvent event) throws IOException {
        if (usernameTextField.getText() != null) {
            String username = usernameTextField.getText().trim();
            if (!(username.isEmpty())) {
                MessageSender sender = MessageSender.getInstance();
                sender.login(username);

                Stage stage =(Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("chat.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                stage.setTitle("User: " + username);
                stage.setScene(scene);

                MessageReceiver receiver = MessageReceiver.getInstance();
                receiver.start();
            }
        }
    }
}
