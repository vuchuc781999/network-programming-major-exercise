package chat;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

public class MessageStorage {
    private ArrayList<Pair<Friend, String>> messages;

    public MessageStorage() {
        messages = new ArrayList<>();
    }

    public void addMessage(Friend friend, String message) {
        messages.add(new Pair<>(friend, message));
    }

    public ArrayList<Pair<Friend, String>> getMessages() {
        return messages;
    }

    public String getMessageAt(int i) throws IOException {
        Pair<Friend, String> message = messages.get(i);
        if (message.getKey().getId() == MessageSender.getInstance().getYou().getId()) {
            return "You: " + message.getValue();
        }
        return message.getKey().getName() + ": " + message.getValue();
    }
}
