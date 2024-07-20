package com.voice.network;


import java.io.Serializable;

public class PlayerAudioSendPacket implements Serializable {
    private String sender;
    private byte[] buffer;
    private int bufferSize;

    public PlayerAudioSendPacket(String sender, byte[] buffer, int bufferSize) {
        this.sender = sender;
        this.buffer = buffer;
        this.bufferSize = bufferSize;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
