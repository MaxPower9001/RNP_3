package de.sascp.client;

import de.sascp.marker.ChatProgramm;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;
import de.sascp.server.Server;
import de.sascp.util.MessageBuilder;
import de.sascp.util.Utility;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.TIMEOUT;

class Client implements ChatProgramm {
    // Client Gui Component
    final ClientGUI clientGUI;

    // Queues for incoming Messages
    final LinkedBlockingQueue<ChatMessage> incomingMessageQueue;
    final ConcurrentLinkedQueue<resFindServer> incomingResFindServer;
    final ConcurrentLinkedQueue<updateClient> incomingUpdateClient;
    // Threads for handling incoming messages
    private final IncomingMessageHandler incomingMessageHandler;
    private final ClientProtocolParser clientProtocolParser;
    // Server instance, which can be used if needed
    private final Server server;
    // for I/O
    Socket socket;
    InputStream sInput;
    OutputStream sOutput;
    // List of all connected Clients in the current session
    private ArrayList<ClientInfomartion> connectedClients;
    // The server ip and the client's username
    private String serverip;
    private String username;


    public Client(ClientGUI clientGUI, Server server) {
        // Instantiate Lists and Queues
        connectedClients = new ArrayList<>();
        incomingUpdateClient = new ConcurrentLinkedQueue<>();
        incomingResFindServer = new ConcurrentLinkedQueue<>();
        incomingMessageQueue = new LinkedBlockingQueue<>();

        // Instantiate IMH and PP, will be started when needed
        this.incomingMessageHandler = new IncomingMessageHandler(this);
        this.clientProtocolParser = new ClientProtocolParser(this);

        this.server = server;
        this.clientGUI = clientGUI;

        this.username = "";
        this.serverip = "";
    }

    /**
     * Called when login button on GUI is pressed.
     *
     * @return returns <code>false</code> if:
     * <p>- not able to create Socket to Server</p>
     * <p>- Server IP faulty</p>
     * <p>- Input or Output Stream not created</p>
     * <p>- reqLogin function returns with false</p>
     */
    boolean start() {
        // establishing socket to server for TCP communication
        try {
            socket = new Socket(serverip, PORT);
        } catch (Exception ec) {
            display("Error connectiong to serverip:" + ec);
            return false;
        }

        String msg = "Socket established to " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

		/* Creating both Data Streams */
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // Create and start Threads for IMH and PP
        new Thread(clientProtocolParser).start();
        new Thread(incomingMessageHandler).start();
        /*
         Send Login Message according to Protocol specification
         If failed, close socket and return false
        */
        if (!reqLogin()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sends reqLogin Package to Server. Checks if updateClient
     *
     * @return
     */
    private boolean reqLogin() {
        final boolean[] connected = {false};
        reqLogin loginMessage;
        try {
            loginMessage = new reqLogin(InetAddress.getByName(this.serverip), this.username);
        } catch (UnknownHostException e) {
            display("Unable to resolve Server IP");
            return connected[0];
        }
        MessageBuilder.buildMessage(loginMessage, sOutput);
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connectedClients.isEmpty() || !findOwnUsername()) {
                    display("Bad Login - maybe try another Username");
                } else {
                    display("Logged in - you did it!");

                    connected[0] = true;
                }
                incomingUpdateClient.clear();
            }
        }, TIMEOUT);
        return connected[0];
    }

    private boolean findOwnUsername() {
        for (ClientInfomartion clientInfomartion : connectedClients) {
            if (
                    clientInfomartion.clientIP == socket.getLocalAddress() &&
                            clientInfomartion.clientPort == socket.getLocalPort() &&
                            clientInfomartion.clientUsername == username) {
                return true;
            }
        }
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
     * To send a message to the serverip // TODO vorher umwandeln in byte[] Stream
     */
    private void sendMessage(reqFindServer msg) {
        MessageBuilder.buildMessage(msg, incomingResFindServer);
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        incomingMessageQueue.offer(new resHeartbeat(null, 0));
        incomingMessageHandler.stopRunning();
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        } // not much else I can do

        // inform the GUI
        if (clientGUI != null)
            clientGUI.connectionFailed();
        clientProtocolParser.stopRunning();

    }

    public void reqFindServer() {
        sendMessage(new reqFindServer(Utility.getBroadcastIP(), PORT));

        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (incomingResFindServer.isEmpty()) {
                    display("No server found!");
                    display("Starting own server...");
                    new Thread(server).start();
                    server.showGUI();
                    display("Server started");
                    clientGUI.setServerTextField("localhost");
                    clientGUI.disableFindServerButton();
                } else {
                    InetAddress lowestIP = Utility.getBroadcastIP();
                    for (resFindServer r : incomingResFindServer) {
                        if (r.getDestinationIP().toString().compareTo(lowestIP.toString()) == -1) {
                            lowestIP = r.getDestinationIP();
                        }
                    }
                    display("Server found!");
                    serverip = lowestIP.getHostAddress();
                    clientGUI.setServerTextField(lowestIP.getHostAddress());
                }
                incomingResFindServer.clear();
            }
        }, TIMEOUT);
    }

    private void connectToOwnServer() {
        // TODO
    }

    public void displayConnectedClients() {
        // TODO
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServerip() {
        return serverip;
    }

    public void setServerip(String serverip) {
        this.serverip = serverip;
    }

    public void setConnectedClients(ArrayList<ClientInfomartion> connectedClients) {
        this.connectedClients = connectedClients;
    }
}


