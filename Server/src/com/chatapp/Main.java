package com.chatapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PORT = 9000;

    private ServerSocket serverSocket;

    public Main() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            Chat chat = null;

            System.out.println("Wait for a client ...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("A client connected !!!");

                chat = new Chat(socket);
                chat.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main server = new Main();
        FileTransfer fileTransferServer = FileTransfer.getInstance();
        fileTransferServer.start();

        server.listen();
    }
}
