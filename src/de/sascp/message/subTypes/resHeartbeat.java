package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.RESHEARTBEAT;

/**
 * Created by Rene on 11.05.2016.
 */
public class resHeartbeat extends ChatMessage {
    public resHeartbeat(InetAddress targetIP, int targetPort) {
        super(targetIP, targetPort, null, 0, RESHEARTBEAT, 0);
    }
}
