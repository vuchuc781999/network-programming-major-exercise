package chat;

public class Friend {
    private final int id;
    private final String name;
    private final MessageStorage messageStorage;

    public Friend(int id, String name) {
        this.id = id;
        this.name = name;
        messageStorage = new MessageStorage();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

    public void addMessage(String message) {
        messageStorage.addMessage(this, message);
    }

    public void addYourMessage(Friend you, String message) {
        messageStorage.addMessage(you, message);
    }
}
