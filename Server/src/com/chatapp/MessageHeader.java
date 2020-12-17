package com.chatapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageHeader {
    private byte[] senderId;
    private byte[] receiverId;
    private byte[] contentLength;

    public MessageHeader() {
        senderId = new byte[4];
        receiverId = new byte[4];
        contentLength = new byte[4];
    }

    public void setSenderId(int senderId) {
        this.senderId = ByteBuffer.allocate(4).putInt(senderId).array();
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = ByteBuffer.allocate(4).putInt(receiverId).array();
    }

    public void setContentLength(int contentLength) {
        this.contentLength = ByteBuffer.allocate(4).putInt(contentLength).array();
    }

    public int getSenderId() {
        return ByteBuffer.wrap(senderId).getInt();
    }

    public int getReceiverId() {
        return ByteBuffer.wrap(receiverId).getInt();
    }

    public int getContentLength() {
        return ByteBuffer.wrap(contentLength).getInt();
    }


    public void read(DataInputStream dataInputStream) throws IOException {
        dataInputStream.read(senderId, 0, 4);
        dataInputStream.read(receiverId, 0, 4);
        dataInputStream.read(contentLength, 0, 4);
    }

    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(senderId, 0, 4);
        dataOutputStream.write(receiverId, 0, 4);
        dataOutputStream.write(contentLength, 0, 4);
    }
}
