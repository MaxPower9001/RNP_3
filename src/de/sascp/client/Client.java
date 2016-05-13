package de.sascp.client;

import de.sascp.marker.ChatProgramm;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.reqFindServer;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.util.MessageBuilder;
import de.sascp.util.Utility;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.TIMEOUT;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client implements ChatProgramm {
    public final IncomingMessageHandler incomingMessageHandler;
    // if I use a GUI or not
    public final ClientGUI clientGUI;
    private final ClientProtocolParser clientProtocolParser;
    public ConcurrentLinkedQueue<ChatMessage> incomingMessageQueue = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<resFindServer> incomingResFindServer = new ConcurrentLinkedQueue<>();
    // for I/O
    public InputStream sInput;        // to read from the socket TODO ändern in InputStream
    public Socket socket;
    OutputStream sOutput;        // to write on the socket TODO ändern in OutputStream
    // the server and the username
    private String server;
    private String username;
    private ArrayList<Runnable> waiterList = new ArrayList<>();


    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter is null
     */
    public Client(ClientGUI clientGUI) {
        this.incomingMessageHandler = new IncomingMessageHandler(this);
        this.clientProtocolParser = new ClientProtocolParser(this);
        this.server = "";
        this.username = "";
        // save if we are in GUI mode or not
        this.clientGUI = clientGUI;
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
        new Thread(clientProtocolParser).start();
        new Thread(incomingMessageHandler).start();
        return false;
    }

    /*
     * To send a message to the console or the GUI
     */
    public void display(String msg) {
        if (clientGUI == null)
            System.out.println(msg);      // println in console mode
        else
            clientGUI.append(msg + "\n");        // append to the ClientGUI JTextArea (or whatever)
    }

    /*
     * To send a message to the server // TODO vorher umwandeln in byte[] Stream
     */
    void sendMessage(ChatMessage msg) {
        MessageBuilder.buildMessage(msg, sOutput);
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
        if (clientGUI != null)
            clientGUI.connectionFailed();

    }

    public void reqFindServer() {
        sendMessage(new reqFindServer(Utility.getBroadcastIP(), 0));
        incomingResFindServer.clear();

        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (incomingResFindServer.isEmpty()) {
                    clientGUI.append("Keinen Server gefunden!\n");
                    clientGUI.append("Starte eigenen Server...");
                    startOwnServer();
                    clientGUI.setServerTextField("localhost");
                } else {
                    InetAddress lowestIP = Utility.getBroadcastIP();
                    for (resFindServer r : incomingResFindServer) {
                        if (r.getSourceIP().toString().compareTo(lowestIP.toString()) == -1) {
                            lowestIP = r.getSourceIP();
                        }
                    }
                    clientGUI.append("Server gefunden!");
                    clientGUI.setServerTextField(lowestIP.toString());
                }
            }
        }, TIMEOUT);
    }

    private void connectToOwnServer() {
        // TODO
    }

    private void startOwnServer() {
        // TODO
    }

    public void displayConnectedClients() {
        // TODO
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setServer(String server) {
        this.server = server;
    }
}


