package chat;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.ArrayList;

public class ChatController {
    @FXML
    private ComboBox<Integer> usersComboBox;
    @FXML
    private ListView<String> messagesListView;
    @FXML
    private TextField inputTextField;
    @FXML
    private CheckBox sendFileCheckBox;

    private MessageSender sender;
    private MessageReceiver receiver;

    @FXML
    public void initialize() throws IOException {
        sender = MessageSender.getInstance();
        receiver = MessageReceiver.getInstance();
        messagesListView.itemsProperty().bind(receiver.getRenderedMessagesProperty());
        usersComboBox.itemsProperty().bind(receiver.getRenderedFriendsProperty());
    }

    public void onRefresh(ActionEvent event) throws IOException {
        sender.getOnlineUsers();
        usersComboBox.getSelectionModel().selectFirst();
    }

    public void onSend(ActionEvent event) throws IOException {
        if (usersComboBox.getValue() != null && inputTextField.getText() != null) {
            if (!inputTextField.getText().trim().isEmpty()) {
                if (sendFileCheckBox.isSelected()) {
                    FileSender fileSender = new FileSender(inputTextField.getText().trim());
                    fileSender.start();
                }

                sender.sendMessage(usersComboBox.getValue(), inputTextField.getText().trim());
                inputTextField.clear();
                inputTextField.requestFocus();
            }
        }
    }

    public void onChoose(ActionEvent event) throws IOException {
        if (usersComboBox.getValue() != null) {
            receiver.setCurrentFriendId(usersComboBox.getValue());
        }
    }

    public void onClicked(MouseEvent event) {
        if (messagesListView.getSelectionModel().getSelectedItem() != null) {
            String message = messagesListView.getSelectionModel().getSelectedItem();
            FileReceiver fileReceiver = new FileReceiver(message.substring(message.indexOf(32)).trim());
            fileReceiver.start();
        }
    }
}
