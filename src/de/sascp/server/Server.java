package de.sascp.server;

/**
 * Created by Rene on 10.05.2016.
 */

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import de.sascp.client.ClientInformation;
import de.sascp.marker.ChatProgramm;
import de.sascp.message.ChatMessage;
import de.sascp.util.Utility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static de.sascp.protocol.Specification.PORT;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server implements ChatProgramm, Runnable {
    // a unique ID for each connection
    static int uniqueId;
    public final LinkedBlockingQueue<ChatMessage> incomingMessageQueue;
    // A HashMap to keep the list of the Client
    private final HashMap<String, ClientConnectionListener> listenerHashMap;
    // if I am in a GUI
    private final ServerGUI serverGUI;
    // to display time
    private final SimpleDateFormat simpleDateFormat;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;

    private UDPServer udpServer;
    private ServerUnit serverUnit;


    public Server(ServerGUI serverGUI) {
        // GUI or not
        this.serverGUI = serverGUI;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        listenerHashMap = new HashMap<>();
        incomingMessageQueue = new LinkedBlockingQueue<>();
    }

    /*
     * For the GUI to stop the server
     */
    void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {
            new Socket(Utility.getLocalIP(), PORT);
        } catch (Exception e) {
            // nothing I can really do
        }
    }

    /* An SCTP channel can only control one SCTP association. An SCTPChannel is created by invoking one of the open methods of this class. A newly-created channel is open but not yet connected, that is, there is no association setup with a remote peer. An attempt to invoke an I/O operation upon an unconnected channel will cause a NotYetConnectedException to be thrown. An association can be setup by connecting the channel using one of its connect methods. Once connected, the channel remains connected until it is closed. Whether or not a channel is connected may be determined by invoking getRemoteAddresses.

SCTP channels support non-blocking connection: A channel may be created and the process of establishing the link to the remote socket may be initiated via the connect method for later completion by the finishConnect method. Whether or not a connection operation is in progress may be determined by invoking the isConnectionPending method.


     * Display an event (not a message) to the console or the GUI
     */
    void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        serverGUI.appendEvent(time + "\n");
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(ClientConnectionListener ccl) {
        if (listenerHashMap.containsValue(ccl)) {
            listenerHashMap.remove(ccl.username);
        }
        serverUnit.doUpdateClients();
    }

    public HashMap<String, ClientConnectionListener> getListenerHashMap() {
        return listenerHashMap;
    }

    public HashSet<ClientInformation> getConnectedClients() {
        HashSet<ClientInformation> returnValue = listenerHashMap.values().stream().map(ccl -> new ClientInformation(
                Utility.getRemoteAddress(ccl.channel),
                Utility.getRemotePort(ccl.channel),
                ccl.username,
                false)).collect(Collectors.toCollection(HashSet::new));
        return returnValue;
    }


    @Override
    public void run() {
        keepGoing = true;
        /* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            udpServer = new UDPServer(this);
            serverUnit = new ServerUnit(this);
            new Thread(udpServer, "UDP Server").start();
            new Thread(serverUnit, "Server Unit").start();
            //ServerSocket serverSocket = new ServerSocket(PORT);
            SctpServerChannel serverChannel =  SctpServerChannel.open().bind(new InetSocketAddress(PORT));
            //System.out.println("Server IP: " + serverSocket.getLocalSocketAddress());

            // infinite loop to wait for connections
            while (keepGoing) {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + PORT + ".");
                SctpChannel channel = serverChannel.accept();    // accept connection

                display("Socket opened to: " + Utility.getRemoteAddress(channel));
                // if I was asked to stop
                if (!keepGoing)
                    break;
                ClientConnectionListener t = new ClientConnectionListener(channel, this);  // make a thread of it

                new Thread(t, "CCL[" + Utility.getRemoteAddress(channel) + ":" + Utility.getRemotePort(channel) + "]").start();
            }
            // I was asked to stop
            try {
                serverChannel.close();
                for (Map.Entry<String, ClientConnectionListener> entry : listenerHashMap.entrySet()) {
                    try {
                        ClientConnectionListener tc = entry.getValue();
                        tc.channel.close();
                        //tc.sOutput.close();
                        //tc.socket.close();
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

    public UDPServer getUdpServer() {
        return udpServer;
    }

    public void showGUI() {
        this.serverGUI.setVisible(true);
    }

}




