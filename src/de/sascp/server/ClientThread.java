package de.sascp.server;

/**
 * Created by Rene on 10.05.2016.
 */

import de.sascp.message.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

import static de.sascp.protocol.Specification.*;

/**
 * One instance of this thread will run for each client
 */
class ClientThread implements Runnable {
    // the socket where to listen/talk
    final Socket socket;
    // my unique id (easier for deconnection)
    final int id;
    private final Server parent;
    ObjectInputStream sInput; //TODO ändern in InputStream
    ObjectOutputStream sOutput; //TODO ändern in OutputStream
    // the Username of the Client
    String username;
    // the date I connect
    private String date;

    // Constructore
    ClientThread(Socket socket, Server parent) {
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
        // to loop until LOGOUT
        boolean keepGoing = true;
        while (keepGoing) {
            // read a String (which is an object)
            ChatMessage cm;
            try {
                cm = (ChatMessage) sInput.readObject();
            } catch (IOException e) {
                parent.display(username + " Exception reading Streams: " + e);
                break;
            } catch (ClassNotFoundException e2) {
                break;
            }
            // the messaage part of the ChatMessage
            String message = cm.getMessage();

            // Switch on the type of message receive
            switch (cm.getType()) {
                case REQFINDSERVER:
                    // TODO Antwort auf REQFINDSERVER
                    break;
                case MESSAGE:
                    parent.broadcast(username + ": " + message);
                    break;
                case LOGOUT:
                    parent.display(username + " disconnected with a LOGOUT message.");
                    keepGoing = false;
                    break;
                case WHOISIN:
                    writeMsg("List of the users connected at " + parent.getSdf().format(new Date()) + "\n");
                    // scan al the users connected
                    for (int i = 0; i < parent.getAl().size(); ++i) {
                        ClientThread ct = parent.getAl().get(i);
                        writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                    }
                    break;
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
