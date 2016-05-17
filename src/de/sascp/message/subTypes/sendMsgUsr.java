package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.SENDMSGUSR;

/**
 * Created by Rene on 11.05.2016.
 */
public class sendMsgUsr extends ChatMessage {
    private final String message;
    private final int messageId;

    public sendMsgUsr(InetAddress targetIP, int targetPort, InetAddress sourceIP, int sourcePort, int messageId, String message) {
        // Length: 2 times IP, 2 times Port, messageID + length of actual message
        super(targetIP, targetPort, sourceIP, sourcePort, SENDMSGUSR, (2 * 4 + 2 * 2 + 4 + message.length()));
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
