package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.RESFINDSERVER;

/**
 * Created by Rene on 11.05.2016.
 */
public class resFindServer extends ChatMessage {
    public resFindServer(InetAddress destinationIP, int destinationPort) {
        super(destinationIP, destinationPort, null, 0, RESFINDSERVER, 0);
    }
}
