package chat;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileReceiver extends Thread {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9001;

    private String filename;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private byte[] buffer;

    public FileReceiver(String filename) {
        this.filename = filename;
        buffer = new byte[MessageSender.BUFFER_SIZE];
    }

    public void run() {
        try {
            Socket socket = new Socket(HOST, PORT);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            buffer[0] = 1;
            dataOutputStream.write(buffer, 0, 1);
            byte[] filenameBuffer = filename.getBytes(StandardCharsets.UTF_8);
            dataOutputStream.write(ByteBuffer.allocate(4).putInt(filenameBuffer.length).array(), 0, 4);
            dataOutputStream.write(filenameBuffer, 0, filenameBuffer.length);

            dataInputStream.read(buffer, 0, 8);
            long filesize = ByteBuffer.wrap(buffer, 0, 8).getLong();
            if (filesize > 0) {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(filename)));

                while (filesize >= MessageSender.BUFFER_SIZE) {
                    dataInputStream.read(buffer, 0, MessageSender.BUFFER_SIZE);
                    bufferedOutputStream.write(buffer, 0, MessageSender.BUFFER_SIZE);
                    filesize -= MessageSender.BUFFER_SIZE;
                }

                if (filesize > 0) {
                    dataInputStream.read(buffer, 0, (int) filesize);
                    bufferedOutputStream.write(buffer, 0, (int) filesize);
                }

                bufferedOutputStream.close();
            }

            dataOutputStream.close();
            dataInputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
