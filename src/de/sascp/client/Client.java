package de.sascp.client;

import de.sascp.message.ChatMessage;

import java.io.*;
import java.net.Socket;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.REQLOGIN;
import static de.sascp.util.MessageBuilder.buildMessage;

/*
 * The Client that can be run both as a console or a GUI
 */
class Client  {
    private final ServerListener serverListener;
    // if I use a GUI or not
    final ClientGUI cg;
    // the server and the username
    private final String server;
    private final String username;
    // for I/O
    InputStream sInput;		// to read from the socket TODO ändern in InputStream
    private OutputStream sOutput;		// to write on the socket TODO ändern in OutputStream
    private Socket socket;


    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter is null
     */
    Client(String server, String username, ClientGUI cg) {
        this.serverListener = new ServerListener(this);
        this.server = server;
        this.username = username;
        // save if we are in GUI mode or not
        this.cg = cg;
    }

    /*
     * To start the dialog
     */
    boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, PORT);
        }
        // if it failed not much I can so
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return true;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

		/* Creating both Data Stream */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return true;
        }

        // creates the Thread to listen from the server
        new Thread(serverListener).start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try
        {
            sOutput.write(buildMessage(new ChatMessage(REQLOGIN,username)));
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return true;
        }
        // success we inform the caller that it worked
        return false;
    }

    /*
     * To send a message to the console or the GUI
     */
    void display(String msg) {
        if(cg == null)
            System.out.println(msg);      // println in console mode
        else
            cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
    }

    /*
     * To send a message to the server // TODO vorher umwandeln in byte[] Stream
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.write(buildMessage(msg));
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do

        // inform the GUI
        if(cg != null)
            cg.connectionFailed();

    }
}


