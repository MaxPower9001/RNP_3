package de.sascp.message;

import java.io.Serializable;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.VERSION;

public class ChatMessage implements Serializable {

    private final int sourceIP;
    private final int sourcePort;

    private final int version;
    private final int messageType;
    private final int length;

    // constructor
    public ChatMessage(InetAddress sourceIP, int sourcePort, int messageType, int length) {
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.version = VERSION;
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


