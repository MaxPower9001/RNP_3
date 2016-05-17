package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.RESFINDSERVER;

/**
 * Created by Rene on 11.05.2016.
 */
public class resFindServer extends ChatMessage {
    public resFindServer(InetAddress targetIP, int targetPort, InetAddress sourceIP, int sourcePort) {
        super(targetIP, targetPort, sourceIP, sourcePort, RESFINDSERVER, 0);
    }
}
