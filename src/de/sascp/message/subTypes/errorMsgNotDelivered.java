package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.ERRORMSGNOTDELIVERED;

/**
 * Created by Rene on 11.05.2016.
 */
public class errorMsgNotDelivered extends ChatMessage {
    private final int messageId;

    public errorMsgNotDelivered(InetAddress targetIP, int targetPort, InetAddress sourceIP, int sourcePort, int messageId) {
        // Length: 2 times IP, 2 times Port, messageID + length of actual message
        super(targetIP, targetPort, sourceIP, sourcePort, ERRORMSGNOTDELIVERED, (2 * 4 + 2 * 2 + 4));
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
