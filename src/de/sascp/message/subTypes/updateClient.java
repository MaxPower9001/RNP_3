package de.sascp.message.subTypes;

import de.sascp.client.ClientInformation;
import de.sascp.message.ChatMessage;

import java.net.InetAddress;
import java.util.HashSet;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.UPDATECLIENT;

/**
 * Created by Rene on 11.05.2016.
 */
public class updateClient extends ChatMessage {
    private HashSet<ClientInformation> clientInformation = new HashSet<>();

    public updateClient(InetAddress targetIP, int targetPort, HashSet<ClientInformation> clientInformation) {
        super(targetIP, targetPort, null, PORT, UPDATECLIENT, 0);
        this.clientInformation = clientInformation;
        this.setLength(clientInformation.size());
    }

    public HashSet<ClientInformation> getClientInformation() {
        return clientInformation;
    }

    public int getLength() {
        return clientInformation.size();
    }
}
