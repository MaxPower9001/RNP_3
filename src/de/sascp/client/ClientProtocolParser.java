package de.sascp.client;

import de.sascp.message.subTypes.sendMsgGrp;
import de.sascp.message.subTypes.sendMsgUsr;
import de.sascp.message.subTypes.updateClient;
import de.sascp.util.Utility;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashSet;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
class ClientProtocolParser implements Runnable {

    private final Client parent;
    private boolean keepGoing = true;
    private boolean notLoggedIn = true;

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
            // As long the client is not logged in, we wait for an resFindServer Package transfered via UDP
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
                version = intFromFourBytes(headerBytes, HEADERVERSIONOFFSET, 4); // Version number
                messageType = intFromFourBytes(headerBytes, HEADERTYPEOFFSET, 4); // MessageType
                length = intFromFourBytes(headerBytes, HEADERLENGTHOFFSET, 4);  // Length

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
                    HashSet<ClientInfomartion> clientList = new HashSet<>();
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
                        byte[] recordUsername = new byte[recordUsernameLength[0]];
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
                        clientList.add(new ClientInfomartion(ip, intFromTwoBytes(recordPort,0), username, parent.socket.getInetAddress() == ip));
                    }
                    clientList.add(new ClientInfomartion(parent.socket.getInetAddress(), parent.socket.getPort(), "", true));
                    // after all the ClientInformation are added, create new updateClient Message and push to parent
                    updateClient updateClient = new updateClient(null, 0, clientList);
                    updateClient.setSourceIP(parent.socket.getInetAddress());
                    updateClient.setSourcePort(parent.socket.getPort());
                    if (parent.incomingMessageQueue.offer(updateClient)) {
                        parent.display("Update Client List received");
                    } else {
                        parent.display("Update Client List thrown away!");
                    }
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
                        case (REQHEARTBEAT):
                            break;
                        case (SENDMSGUSR):
                            int usrTextMessageId = Utility.intFromFourBytes(payload,0,4);
                            try {
                                InetAddress sourceIp = InetAddress.getByAddress(Utility.getByteArrayFragment(payload,4,4));
                                InetAddress targetIp = InetAddress.getByAddress(Utility.getByteArrayFragment(payload,8,4));
                                int sourcePort = Utility.intFromTwoBytes(payload,12);
                                int targetPort = Utility.intFromTwoBytes(payload,14);
                                String usrTextMessage = new String(Utility.getByteArrayFragment(payload,16,payload.length - 16),Charset.forName(CHARSET));

                                sendMsgUsr message = new sendMsgUsr(targetIp, targetPort, sourceIp, sourcePort,usrTextMessageId, usrTextMessage);
                                parent.incomingMessageQueue.offer(message);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            break;
                        case (SENDMSGGRP):
                            int grpTextMessageId = Utility.intFromFourBytes(payload,0,4);
                            try {
                                InetAddress sourceIp = Inet4Address.getByAddress(Utility.getByteArrayFragment(payload,4,4));
                                InetAddress targetIp = Inet4Address.getByAddress(Utility.getByteArrayFragment(payload,8,4));
                                int sourcePort = Utility.intFromTwoBytes(payload,12);
                                int targetPort = Utility.intFromTwoBytes(payload,14);
                                String usrTextMessage = new String(Utility.getByteArrayFragment(payload,16,payload.length - 16), Charset.forName(CHARSET));

                                sendMsgGrp message = new sendMsgGrp(targetIp, targetPort, sourceIp, sourcePort,grpTextMessageId, usrTextMessage);
                                parent.incomingMessageQueue.offer(message);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            parent.display("Nothing found - I guess nobody wants to talk to you...");
                            break;

                    }
                }
                lookingForPayload = false;
                lookingForCommonHeader = true;
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
