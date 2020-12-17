package chat;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileSender extends Thread{
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9001;

    private String filename;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private byte[] buffer;

    public FileSender(String filename) {
        this.filename = filename;
        buffer = new byte[MessageSender.BUFFER_SIZE];
    }

    public void run() {
        try {
            File file = new File(filename);
            if (file.exists()) {
                Socket socket = new Socket(HOST, PORT);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

                buffer[0] = -1;
                dataOutputStream.write(buffer, 0, 1);
                byte[] filenameBuffer = filename.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.write(ByteBuffer.allocate(4).putInt(filenameBuffer.length).array(), 0, 4);
                dataOutputStream.write(filenameBuffer, 0, filenameBuffer.length);
                long filesize = file.length();
                dataOutputStream.write(ByteBuffer.allocate(8).putLong(filesize).array());

                while (filesize >= MessageSender.BUFFER_SIZE) {
                    bufferedInputStream.read(buffer, 0, MessageSender.BUFFER_SIZE);
                    dataOutputStream.write(buffer, 0, MessageSender.BUFFER_SIZE);
                    filesize -= MessageSender.BUFFER_SIZE;
                }

                if (filesize > 0) {
                    bufferedInputStream.read(buffer, 0, (int) filesize);
                    dataOutputStream.write(buffer, 0, (int) filesize);
                }

                bufferedInputStream.close();
                dataOutputStream.close();
                dataInputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
