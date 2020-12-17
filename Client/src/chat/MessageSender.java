package chat;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageSender {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9000;
    public static final int BUFFER_SIZE = 4096;
    private static MessageSender instance = null;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private final MessageHeader header;
    private Friend you;
    private String username;

    private MessageSender() throws IOException {
        header = new MessageHeader();
        you = null;
        connect();
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public Friend getYou() {
        return you;
    }

    public void setYou(int id) {
        you = new Friend(id, username);
    }

    private void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void disconnect() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    public static MessageSender getInstance() throws IOException {
        if (instance == null) {
            instance = new MessageSender();
        }

        return instance;
    }

    public void login(String username) throws IOException {
        header.setSenderId(0);
        header.setReceiverId(0);

        byte[] buffer = username.getBytes(StandardCharsets.UTF_8);

        header.setContentLength(buffer.length);
        header.write(dataOutputStream);
        dataOutputStream.write(buffer, 0, buffer.length);

        this.username = username;
    }

    public void getOnlineUsers() throws IOException {
        header.setSenderId(you.getId());
        header.setReceiverId(0);
        header.setContentLength(0);

        header.write(dataOutputStream);
    }

    public void sendMessage(int friendId, String message) throws IOException {
        header.setSenderId(you.getId());
        header.setReceiverId(friendId);

        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

        header.setContentLength(buffer.length);
        header.write(dataOutputStream);
        dataOutputStream.write(buffer, 0, buffer.length);
    }
}
