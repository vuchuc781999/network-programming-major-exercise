package com.chatapp;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileThread extends Thread{
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private byte[] buffer;

    public FileThread(Socket socket) {
        this.socket = socket;
    }

    private void upload() throws IOException {
        dataInputStream.read(buffer, 0, 4);
        int filenameLength = ByteBuffer.wrap(buffer, 0, 4).getInt();
        dataInputStream.read(buffer, 0, Math.min(filenameLength, Chat.BUFFER_SIZE));
        String filename = new String(buffer, 0, Math.min(filenameLength, Chat.BUFFER_SIZE), StandardCharsets.UTF_8);
        dataInputStream.read(buffer, 0, 8);
        long filesize = ByteBuffer.wrap(buffer, 0, 8).getLong();

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(filename)));

        while (filesize >= Chat.BUFFER_SIZE) {
            dataInputStream.read(buffer, 0, Chat.BUFFER_SIZE);
            bufferedOutputStream.write(buffer, 0, Chat.BUFFER_SIZE);
            filesize -= Chat.BUFFER_SIZE;
        }

        if (filesize > 0) {
            dataInputStream.read(buffer, 0, (int) filesize);
            bufferedOutputStream.write(buffer, 0, (int) filesize);
            System.out.println("ok ok");
        }
        bufferedOutputStream.close();
    }

    private void download() throws IOException {
        dataInputStream.read(buffer, 0, 4);
        int filenameLength = ByteBuffer.wrap(buffer, 0, 4).getInt();
        dataInputStream.read(buffer, 0, Math.min(filenameLength, Chat.BUFFER_SIZE));
        String filename = new String(buffer, 0, Math.min(filenameLength, Chat.BUFFER_SIZE), StandardCharsets.UTF_8);

        File file = new File(filename);
        if (file.exists()) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            long filesize = file.length();

            dataOutputStream.write(ByteBuffer.allocate(8).putLong(filesize).array(), 0, 8);

            while (filesize >= Chat.BUFFER_SIZE) {
                bufferedInputStream.read(buffer, 0, Chat.BUFFER_SIZE);
                dataOutputStream.write(buffer, 0, Chat.BUFFER_SIZE);
                filesize -= Chat.BUFFER_SIZE;
            }
            if (filesize > 0) {
                bufferedInputStream.read(buffer, 0, (int) filesize);
                dataOutputStream.write(buffer, 0, (int) filesize);
            }

            bufferedInputStream.close();
        } else {
            dataOutputStream.write(ByteBuffer.allocate(8).putLong(0).array(), 0, 8);
        }
    }

    public void run() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            buffer = new byte[Chat.BUFFER_SIZE];

            while (true) {
                dataInputStream.read(buffer, 0, 1);

                if (buffer[0] == -1) {
                    upload();
                }

                if (buffer[0] == 1) {
                    download();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
