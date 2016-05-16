package de.sascp.server;


import de.sascp.message.subTypes.reqFindServer;
import de.sascp.message.subTypes.reqLogin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * One instance of this thread will run for each client
 */
class ClientConnectionListener implements Runnable {
    // the socket where to listen/talk
    final Socket socket;
    // my unique id (easier for deconnection)
    final int id;
    private final Server parent;
    InputStream sInput;
    OutputStream sOutput;
    // the Username of the Client
    String username;
    private boolean keepGoing = true;

    // Constructore
    ClientConnectionListener(Socket socket, Server parent) {
        this.parent = parent;
        // a unique id
        id = ++Server.uniqueId;
        this.socket = socket;
            /* Creating both Data Stream */
        System.out.println("Thread trying to create Object Input/Output Streams");
        try {
            // create output first
            sOutput = socket.getOutputStream();
            sInput = socket.getInputStream();
        } catch (IOException e) {
            parent.display("Exception creating new Input/output Streams: " + e);
            return;
        }
    }

    // what will run forever
    public void run() {
        boolean lookingForCommonHeader = true;
        boolean lookingForPayload = true;
        int version = -1;
        int messageType = -1;
        int length = -1;

        // to loop until LOGOUT
        while (keepGoing) {
            while (lookingForCommonHeader) {
                byte[] headerBytes = new byte[CHLENGTH];
                try {
                    sInput.read(headerBytes);
                } catch (IOException e) {
                    // TODO connection failed
                    break;
                }

                version = intFromFourBytes(headerBytes, HEADERVERSIONOFFSET, 4); // Version number
                messageType = intFromFourBytes(headerBytes, HEADERTYPEOFFSET, 4); // MessageType
                length = intFromFourBytes(headerBytes, HEADERLENGTHOFFSET, 4);  // Length

                if (checkCommonHeader(version, messageType, length)) {
                    lookingForCommonHeader = false;
                }
            }
            while (lookingForPayload) {
                if (messageType == UPDATECLIENT) {
                    // TODO Updateclient List einlesen bitte danke
                } else {
                    byte[] payload = new byte[length];
                    try {
                        sInput.read(payload);
                    } catch (IOException e) {
                        // TODO connection failed
                        break;
                    }
                    switch (messageType) {
                        case (REQFINDSERVER):
                            parent.incomingMessageQueue.offer(new reqFindServer(socket.getInetAddress(), socket.getPort()));
                            break;
                        case (REQLOGIN):
                            String username = new String(payload, Charset.forName(CHARSET));
                            boolean usernameAlreadyTaken = false;
                            for (ClientConnectionListener ccl : parent.getListenerHashMap().values()) {
                                if (ccl.username == username) {
                                    usernameAlreadyTaken = true;
                                }
                                if (usernameAlreadyTaken) {
                                    parent.display("Login Request from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " -> " + username);
                                    parent.display("But his username was already taken - poor fella...");
                                    break;
                                }
                            }
                            if (!usernameAlreadyTaken) {
                                this.username = username;
                                if (parent.incomingMessageQueue.offer(new reqLogin(socket.getInetAddress(), username, socket.getPort()))) {
                                    parent.display("Login Request from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " -> " + username);
                                } else {
                                    parent.display("I was not able to put reqLogin into incoming message queue - forigve me senpai!");
                                }
                            }
                            break;
                        default:
                            parent.display("This is Microsoft Sam the Servers default switch-bracket");
                            break;
                    }
                }
                lookingForPayload = false;
            }
        }
        // remove myself from the arrayList containing the list of the
        // connected Clients
        parent.remove(id);
        close();
    }

    // try to close everything
    private void close() {
        // try to close the connection
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        }
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }

    /*
     * Write a String to the Client output stream
     */
    boolean writeMsg(String msg) {
        // if Client is still connected send the message to it
        if (!socket.isConnected()) {
            close();
            return false;
        }
        return true;
    }

    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }
}
