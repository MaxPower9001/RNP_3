package de.sascp.message;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private final int version;
    private final int messageType;
    private final int length;

    private ChatMessage() {
        this.version = -1;
        this.messageType = -1;
        this.length = -1;
    }

    // constructor
    public ChatMessage(int version, int messageType, int length) {
        this.version = version;
        this.messageType = messageType;
        this.length = length;
    }

    // getters

    public long getVersion() {
        return version;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getLength() {
        return length;
    }
}


