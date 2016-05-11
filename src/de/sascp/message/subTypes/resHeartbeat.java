package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import static de.sascp.protocol.Specification.RESHEARTBEAT;

/**
 * Created by Rene on 11.05.2016.
 */
public class resHeartbeat extends ChatMessage {
    public resHeartbeat(int targetIP, int targetPort) {
        super(targetIP, targetPort, RESHEARTBEAT, 0);
    }
}
