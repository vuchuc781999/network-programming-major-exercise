package com.chatapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Chat extends Thread {
    public static final int BUFFER_SIZE = 4096;
    public static ArrayList<Chat> onlineUsers = new ArrayList<>();
    private static int id = 1;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private int userId;
    private String username;

    public Chat(Socket socket) {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    synchronized private void workWithOnlineUsers(ActionType type, MessageHeader header, byte[] message) throws IOException {
        switch (type) {
            case ADD_USER:
                onlineUsers.add(this);
                userId = id;
                id++;
                dataOutputStream.write(ByteBuffer.allocate(4).putInt(userId).array(), 0, 4);
                System.out.println("User \"" + username + "\" has been added !!");
                break;
            case REMOVE_USER:
                onlineUsers.remove(this);
                System.out.println("User \"" + username + "\" has been removed !!");
                break;
            case GET_USERS:
                header.setSenderId(0);
                header.setReceiverId(userId);
                header.setContentLength(4);

                sendMessage(header, ByteBuffer.allocate(4).putInt(onlineUsers.size()).array());

                for (Chat user : onlineUsers) {
                    byte[] id = ByteBuffer.allocate(4).putInt(user.userId).array();
                    byte[] name = user.username.getBytes(StandardCharsets.UTF_8);

                    int length = Math.min(message.length - 4, id.length + name.length);
                    header.setContentLength(length);

                    for (int i = 0; i < length; i++) {
                        message[i] = i < id.length ? id[i] : name[i - id.length];
                    }
                    sendMessage(header, message);
                }
                break;
            case BROADCAST:
                for (Chat user: onlineUsers) {
                    user.sendMessage(header, message);
                }
                break;
        }
    }

    synchronized private void sendMessage(MessageHeader header, byte[] message) throws IOException {
        header.write(dataOutputStream);
        dataOutputStream.write(message, 0, Math.min(header.getContentLength(), BUFFER_SIZE));
    }

    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            MessageHeader header = new MessageHeader();

            while (true) {
                header.read(dataInputStream);

                if (header.getContentLength() <= 0) {
                    workWithOnlineUsers(ActionType.GET_USERS, header, buffer);
                    continue;
                }

                dataInputStream.read(buffer, 0, Math.min(header.getContentLength(), BUFFER_SIZE));

                if (username == null) {
                    username = new String(buffer, 0, Math.min(header.getContentLength(), BUFFER_SIZE), StandardCharsets.UTF_8);
                    workWithOnlineUsers(ActionType.ADD_USER, null, null);
                } else {
                    if (header.getReceiverId() <= 0) {
                        workWithOnlineUsers(ActionType.BROADCAST , header, buffer);
                    } else {
                        for (Chat user : onlineUsers) {
                            if (user.userId == header.getReceiverId()) {
                                user.sendMessage(header, buffer);
                                break;
                            }
                        }

                        sendMessage(header, buffer);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An user has just disconnected !!");
        } finally {
            try {
                workWithOnlineUsers(ActionType.REMOVE_USER, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
