package de.sascp.message.subTypes;

import de.sascp.client.ClientInfomartion;
import de.sascp.message.ChatMessage;

import java.net.InetAddress;
import java.util.ArrayList;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.UPDATECLIENT;

/**
 * Created by Rene on 11.05.2016.
 */
public class updateClient extends ChatMessage {
    private ArrayList<ClientInfomartion> clientInfomartion = new ArrayList<>();

    public updateClient(InetAddress destinationIP, int destinationPort, ArrayList<ClientInfomartion> clientInfomartion) {
        super(destinationIP, destinationPort, null, PORT, UPDATECLIENT, 0);
        this.clientInfomartion = clientInfomartion;
        this.setLength(clientInfomartion.size());
    }

    public ArrayList<ClientInfomartion> getClientInfomartion() {
        return clientInfomartion;
    }
}
