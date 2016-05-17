package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.SENDMSGGRP;

/**
 * Created by Rene on 11.05.2016.
 */
public class sendMsgGrp extends ChatMessage {
    private final String message;
    private final int messageId;

    public sendMsgGrp(InetAddress destinationIP, int destinationPort, InetAddress sourceIP, int sourcePort, int messageId, String message) {
        // Length: 2 times IP, 2 times Port, messageID + length of actual message
        super(destinationIP, destinationPort, sourceIP, sourcePort, SENDMSGGRP, (2*4 + 2*2 + 4 + message.length()));
        this.message = message;
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }
    public int getMessageId() {
        return messageId;
    }

}
