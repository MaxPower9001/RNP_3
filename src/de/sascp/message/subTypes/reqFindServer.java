package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.REQFINDSERVER;

/**
 * Created by Rene on 11.05.2016.
 */
public class reqFindServer extends ChatMessage {
    public reqFindServer(InetAddress sourceIP, int sourcePort) {
        super(sourceIP, sourcePort, REQFINDSERVER, 0);
    }
}
