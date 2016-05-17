package de.sascp.message;

import java.io.Serializable;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.VERSION;

public abstract class ChatMessage implements Serializable {

    private final InetAddress targetIP;
    private final int targetPort;
    private final int version;
    private final int messageType;
    private InetAddress sourceIP;
    private int sourcePort;
    private int length;
    private byte[] payload;

    // constructor
    protected ChatMessage(InetAddress targetIP, int targetPort, InetAddress sourceIP, int sourcePort, int messageType, int length) {
        this.targetIP = targetIP;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
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

    public void setLength(int length) {
        this.length = length;
    }

    public InetAddress getTargetIP() {
        return targetIP;
    }

    public int getTargetPort() {
        return targetPort;
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.length = length + payload.length;
    }
}


