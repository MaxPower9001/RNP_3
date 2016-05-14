package de.sascp.server;


import de.sascp.message.subTypes.reqFindServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

import static de.sascp.protocol.Specification.REQFINDSERVER;
import static de.sascp.protocol.Specification.UPDATECLIENT;
import static de.sascp.util.Utility.*;

/**
 * One instance of this thread will run for each client
 */
public class ClientConnectionListener implements Runnable {
    // the socket where to listen/talk
    final Socket socket;
    // my unique id (easier for deconnection)
    final int id;
    private final Server parent;
    public ObjectInputStream sInput; //TODO ändern in InputStream
    ObjectOutputStream sOutput; //TODO ändern in OutputStream
    // the Username of the Client
    String username;
    // the date I connect
    private String date;

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
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sInput = new ObjectInputStream(socket.getInputStream());
            // read the username
            username = (String) sInput.readObject();
            parent.display(username + " just connected.");
        } catch (IOException e) {
            parent.display("Exception creating new Input/output Streams: " + e);
            return;
        }
        // have to catch ClassNotFoundException
        // but I read a String, I am sure it will work
        catch (ClassNotFoundException e) {
        }
        date = new Date().toString() + "\n";
    }

    // what will run forever
    public void run() {
        boolean lookingForCommonHeader = true;
        boolean lookingForPayload = true;
        int version = -1;
        int messageType = -1;
        int length = -1;

        // to loop until LOGOUT
        boolean keepGoing = true;
        while (lookingForCommonHeader) {
            byte[] headerBytes = new byte[CHLENGTH];
            try {
                sInput.read(headerBytes);
            } catch (IOException e) {
                // TODO connection failed
                break;
            }

            version = fromArray(headerBytes, 0); // Version number
            messageType = fromArray(headerBytes, 4); // MessageType
            length = fromArray(headerBytes, 8);  // Length

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
                }
            }
            lookingForPayload = false;
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
        // write the message to the stream
        try {
            sOutput.writeObject(msg);
        }
        // if an error occurs, do not abort just inform the user
        catch (IOException e) {
            parent.display("Error sending message to " + username);
            parent.display(e.toString());
        }
        return true;
    }
}
