package de.sascp.message;

import java.io.Serializable;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.VERSION;

public abstract class ChatMessage implements Serializable {

    private final InetAddress destinationIP;
    private final int destinationPort;
    private final int version;
    private final int messageType;
    private InetAddress sourceIP;
    private int sourcePort;
    private int length;

    // constructor
    protected ChatMessage(InetAddress destinationIP, int destinationPort, InetAddress sourceIP, int sourcePort, int messageType, int length) {
        this.destinationIP = destinationIP;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.version = VERSION;
        this.messageType = messageType;
        this.length = length;
    }

    // getters

    public int getVersion() {
        return version;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getLength() {
        return length;
    }

    protected void setLength(int length) {
        this.length = length;
    }

    public InetAddress getDestinationIP() {
        return destinationIP;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public InetAddress getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(InetAddress sourceIP) {
        this.sourceIP = sourceIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }
}


