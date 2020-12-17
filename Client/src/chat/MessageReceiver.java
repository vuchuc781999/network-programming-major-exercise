package chat;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageReceiver extends Thread {
    private static MessageReceiver instance = null;

    private final MessageSender sender;
    private final ArrayList<Friend> onlineFriends;
    private final MessageStorage publicGroup;
    private final MessageHeader header;
    private final byte[] buffer;

    private int currentFriendId;
    private final ArrayList<String> renderedMessages;
    private final ListProperty<String> renderedMessagesProperty;
    private final ArrayList<Integer> renderedFriends;
    private final ListProperty<Integer> renderedFriendsProperty;

    private int onlineFriendsQuantity;

    private MessageReceiver() throws IOException {
        sender = MessageSender.getInstance();
        onlineFriends = new ArrayList<>();
        publicGroup = new MessageStorage();
        header = new MessageHeader();
        buffer = new byte[MessageSender.BUFFER_SIZE];

        currentFriendId = 0;
        renderedMessages = new ArrayList<>();
        renderedMessagesProperty = new SimpleListProperty<>();
        renderedFriends = new ArrayList<>();
        renderedFriendsProperty = new SimpleListProperty<>();
        renderedFriends.add(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                renderedFriendsProperty.set(FXCollections.observableArrayList(renderedFriends));
            }
        });

        onlineFriendsQuantity = 0;
    }

    public static MessageReceiver getInstance() throws IOException {
        if (instance == null) {
            instance = new MessageReceiver();
        }

        return instance;
    }

    public ListProperty<String> getRenderedMessagesProperty() {
        return renderedMessagesProperty;
    }

    public ListProperty<Integer> getRenderedFriendsProperty() {
        return renderedFriendsProperty;
    }

    public void setCurrentFriendId(int currentFriendId) throws IOException {
        if(this.currentFriendId != currentFriendId) {
            this.currentFriendId = currentFriendId;

            rerenderMessages();
        }
    }

    synchronized public void rerenderMessages() throws IOException {
        renderedMessages.clear();

        if (currentFriendId <= 0) {
            for (int i = 0; i < publicGroup.getMessages().size(); i++) {
                renderedMessages.add(publicGroup.getMessageAt(i));
            }
        } else {
            for (Friend friend: onlineFriends) {
                if (friend.getId() == currentFriendId) {
                    for (int i = 0; i < friend.getMessageStorage().getMessages().size(); i++) {
                        renderedMessages.add(friend.getMessageStorage().getMessageAt(i));
                    }
                    break;
                }
            }
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                renderedMessagesProperty.set(FXCollections.observableArrayList(renderedMessages));
            }
        });
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (sender.getYou() == null) {
                    sender.getDataInputStream().read(buffer, 0, 4);
                    sender.setYou(ByteBuffer.wrap(buffer, 0, 4).getInt());
                } else {
                    header.read(sender.getDataInputStream());

                    if (header.getSenderId() == 0) {
                        if (onlineFriendsQuantity <= 0) {
                            sender.getDataInputStream().read(buffer, 0, 4);
                            onlineFriendsQuantity = ByteBuffer.wrap(buffer, 0, 4).getInt();
                        } else {
                            sender.getDataInputStream().read(buffer, 0, 4);
                            int tempId = ByteBuffer.wrap(buffer, 0, 4).getInt();
                            sender.getDataInputStream().read(buffer, 0, header.getContentLength() - 4);
                            String tempName = new String(buffer, 0, header.getContentLength() - 4, StandardCharsets.UTF_8);

                            if (tempId != sender.getYou().getId()) {
                                int i = 0;
                                for (; i < onlineFriends.size(); i++) {
                                    if (onlineFriends.get(i).getId() == tempId) {
                                        break;
                                    }
                                }
                                if (i == onlineFriends.size()) {
                                    onlineFriends.add(new Friend(tempId, tempName));
                                }
                            }

                            onlineFriendsQuantity--;
                            if (onlineFriendsQuantity <= 0) {
                                renderedFriends.clear();
                                renderedFriends.add(0);
                                for (Friend friend : onlineFriends) {
                                    renderedFriends.add(friend.getId());
                                }

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        renderedFriendsProperty.set(FXCollections.observableArrayList(renderedFriends));
                                    }
                                });
                            }
                        }
                    } else {
                        sender.getDataInputStream().read(buffer, 0, header.getContentLength());
                        String message = new String(buffer, 0, header.getContentLength(), StandardCharsets.UTF_8);

                        if (header.getSenderId() == sender.getYou().getId()) {
                            if (header.getReceiverId() <= 0) {
                                publicGroup.addMessage(sender.getYou(), message);
                            } else {
                                for (Friend friend : onlineFriends) {
                                    if (friend.getId() == header.getReceiverId()) {
                                        friend.getMessageStorage().addMessage(sender.getYou(), message);
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (header.getReceiverId() <= 0) {
                                for (Friend friend : onlineFriends) {
                                    if (friend.getId() == header.getSenderId()) {
                                        publicGroup.addMessage(friend, message);
                                        break;
                                    }
                                }
                            } else {
                                for (Friend friend : onlineFriends) {
                                    if (friend.getId() == header.getSenderId()) {
                                        friend.getMessageStorage().addMessage(friend, message);
                                        break;
                                    }
                                }
                            }
                        }

                        if (header.getReceiverId() == currentFriendId || header.getSenderId() == currentFriendId) {
                            rerenderMessages();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Exit !!!");
        } finally {
            try {
                sender.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
