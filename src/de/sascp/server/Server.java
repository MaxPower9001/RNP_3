package de.sascp.server;

/**
 * Created by Rene on 10.05.2016.
 */

import de.sascp.marker.ChatProgramm;
import de.sascp.message.ChatMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static de.sascp.protocol.Specification.PORT;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server implements ChatProgramm, Runnable {
    // a unique ID for each connection
    static int uniqueId;
    public final LinkedBlockingQueue<ChatMessage> incomingMessageQueue = new LinkedBlockingQueue<>();
    // an ArrayList to keep the list of the Client
    private final HashMap<String, ClientConnectionListener> listenerHashMap;
    // if I am in a GUI
    private final ServerGUI serverGUI;
    // to display time
    private final SimpleDateFormat simpleDateFormat;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;


    public Server(ServerGUI serverGUI) {
        // GUI or not
        this.serverGUI = serverGUI;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        listenerHashMap = new HashMap<>();
        new Thread(new UDPServer(this)).start();
    }

    /*
     * For the GUI to stop the server
     */
    void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {
            new Socket("localhost", PORT);
        } catch (Exception e) {
            // nothing I can really do
        }
    }

    /*
     * Display an event (not a message) to the console or the GUI
     */
    void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        if (serverGUI == null)
            System.out.println(time);
        else
            serverGUI.appendEvent(time + "\n");
    }

    /*
     *  to broadcast a message to all Clients
     */
    synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = simpleDateFormat.format(new Date());
        String messageLf = time + " " + message + "\n";
        // display message on console or GUI
        if (serverGUI == null)
            System.out.print(messageLf);
        else
            serverGUI.appendRoom(messageLf);     // append in the room window

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = listenerHashMap.size(); --i >= 0; ) {
            ClientConnectionListener ct = listenerHashMap.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(messageLf)) {
                listenerHashMap.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < listenerHashMap.size(); ++i) {
            ClientConnectionListener ct = listenerHashMap.get(i);
            // found it
            if (ct.id == id) {
                listenerHashMap.remove(i);
                return;
            }
        }
    }

    public HashMap<String, ClientConnectionListener> getListenerHashMap() {
        return listenerHashMap;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    @Override
    public void run() {
        keepGoing = true;
        /* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(PORT);

            // infinite loop to wait for connections
            while (keepGoing) {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + PORT + ".");

                Socket socket = serverSocket.accept();    // accept connection
                // if I was asked to stop
                if (!keepGoing)
                    break;
                ClientConnectionListener t = new ClientConnectionListener(socket, this);  // make a thread of it
                listenerHashMap.put(t.socket.getInetAddress().toString() + t.socket.getPort(), t);                                    // save it in the ArrayList
                Thread thread = new Thread(t);
                thread.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for (Map.Entry<String, ClientConnectionListener> entry : listenerHashMap.entrySet()) {
                    try {
                        ClientConnectionListener tc = entry.getValue();
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        // not much I can do
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = simpleDateFormat.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }
}




