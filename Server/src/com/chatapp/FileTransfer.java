package com.chatapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransfer extends Thread {
    public static final int PORT = 9001;
    private static FileTransfer fileTransfer = null;

    public static FileTransfer getInstance() {
        if (fileTransfer == null) {
            fileTransfer = new FileTransfer();
        }

        return fileTransfer;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            FileThread fileThread = null;

            while (true) {
                Socket socket = serverSocket.accept();

                fileThread = new FileThread(socket);
                fileThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
