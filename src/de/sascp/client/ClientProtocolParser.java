package de.sascp.client;

import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.updateClient;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
class ClientProtocolParser implements Runnable {

    private final Client parent;
    private boolean keepGoing = true;

    public ClientProtocolParser(Client parent) {
        this.parent = parent;
    }

    public void run() {
        while (keepGoing) {
            // Initialise variables for current run
            boolean lookingForCommonHeader = true;
            boolean lookingForPayload = true;
            int version = -1;
            int messageType = -1;
            int length = -1;

            // Look for Common Header, until one received
            while (lookingForCommonHeader) {
                // Headerbyte with specific length
                byte[] headerBytes = new byte[CHLENGTH];
                try {
                    parent.sInput.read(headerBytes);
                } catch (IOException e) {
                    parent.display("Error reading header bytes - pls do sth");
                    e.printStackTrace();
                    // Stop looking for header data
                    break;
                }
                // Seperate values inside headerBytes
                version = fromArray(headerBytes, HEADERVERSIONOFFSET); // Version number
                messageType = fromArray(headerBytes, HEADERTYPEOFFSET); // MessageType
                length = fromArray(headerBytes, HEADERLENGTHOFFSET);  // Length

                // Check if correct Common Header
                if (checkCommonHeader(version, messageType, length)) {
                    lookingForCommonHeader = false;
                }
            }
            // If Common Header found, wait for payload
            while (lookingForPayload) {
                // Message tyoe UPDATECLIENT's length meaning differs from other packets
                if (messageType == UPDATECLIENT) {
                    // Prepare list to store seperate client record as ClientInformation
                    ArrayList<ClientInfomartion> clientList = new ArrayList<>();
                    // Iterate over each record, represented by message's length
                    for (int i = 1; i <= length; i++) {
                        // prepare byte[] for Client's ip, port, usernamelength and reserved bits
                        byte[] recordIP = new byte[4];
                        byte[] recordPort = new byte[2];
                        byte[] recordUsernameLength = new byte[1];
                        byte[] recordReserved = new byte[1];

                        // read into each byte[] from input stream
                        try {
                            parent.sInput.read(recordIP);
                            parent.sInput.read(recordPort);
                            parent.sInput.read(recordUsernameLength);
                            parent.sInput.read(recordReserved);
                        } catch (IOException e) {
                            parent.display("Error reading payload bytes for UpdateClient Package - this is getting out of hand!");
                            // Stop looking for payload
                            break;
                        }
                        // after username length is acquired prepare byte[] to read username into
                        byte[] recordUsername = new byte[fromArray(recordUsernameLength, 0)];
                        // read username from input stream
                        try {
                            parent.sInput.read(recordUsername);
                        } catch (IOException e) {
                            parent.display("Error reading username bytes for UpdateClient Package - fix it, now!");
                        }
                        // convert byte[] ip into proper InetAdress
                        InetAddress ip = null;
                        try {
                            ip = Inet4Address.getByAddress(recordIP);
                        } catch (UnknownHostException e) {
                            parent.display("Error converting IP into String for UpdateClient Package - shame on you!");
                        }
                        // convert byte[] username into proper String
                        String username = new String(recordUsername);

                        // add new ClientInformation to list for updateClient Message
                        clientList.add(new ClientInfomartion(ip, fromArray(recordPort, 0), username, parent.getServerip() == ip.getHostAddress()));
                    }
                    // after all the ClientInformation are added, create new updateClient Message and push to parent
                    parent.incomingUpdateClient.add(new updateClient(parent.socket.getInetAddress(), parent.socket.getPort(), clientList));
                }
                // Every other Message type's length
                else if (length > 0) {
                    // prepare payload byte[] according to message's length parameter
                    byte[] payload = new byte[length];

                    // read into byte[] from input stream
                    try {
                        parent.sInput.read(payload);
                    } catch (IOException e) {
                        parent.display("Error reading payload for non UpdateClient package - git gud");
                        // stop looking for payload
                        break;
                    }
                    // according to message type put message into incomingMessageQueue for IMH
                    switch (messageType) {
                        case (RESFINDSERVER):
                            parent.incomingMessageQueue.offer(new resFindServer(parent.socket.getInetAddress(), parent.socket.getPort()));
                            break;
                        case (REQHEARTBEAT):
                            break;
                        default:
                            parent.display("Nothing found - I guess nobody wants to talk to you...");
                            break;

                    }
                }
                lookingForPayload = false;
            }
        }
    }

    private void connectionFailed(IOException e) {
        parent.display("Server has close the connection: " + e);
        if (parent.clientGUI != null)
            parent.clientGUI.connectionFailed();
    }

    public void stopRunning() {
        this.keepGoing = false;
    }
}
