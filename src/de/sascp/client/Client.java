package de.sascp.client;

import de.sascp.marker.ChatProgramm;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;
import de.sascp.server.Server;
import de.sascp.util.MessageBuilder;
import de.sascp.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
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
    // Server instance, which can be used if needed
    private final Server server;
    long uniqueID = Instant.EPOCH.getEpochSecond();
    // for I/O
    Socket socket;
    InputStream sInput;
    OutputStream sOutput;
    // Timer and boolean for heartbeat
    Timer checkHB;
    Timer reqLoginTimer;
    Timer resFindServer;
    ClientInformation myInformation;
    // Threads for handling incoming messages
    private IncomingMessageHandler incomingMessageHandler;
    private ClientProtocolParser clientProtocolParser;
    // List of all connected Clients in the current session
    private HashSet<ClientInformation> connectedClients;
    private boolean[] hbReceived = {false};
    // The server ip and the client's username
    private String serverip;
    private String username;


    public Client(ClientGUI clientGUI, Server server) {
        // Instantiate Lists and Queues
        connectedClients = new HashSet<>();
        incomingResFindServer = new ConcurrentLinkedQueue<>();
        incomingMessageQueue = new LinkedBlockingQueue<>();

        // Instantiate IMH and PP, will be started when needed
        this.incomingMessageHandler = new IncomingMessageHandler(this);
        this.clientProtocolParser = new ClientProtocolParser(this);

        reqLoginTimer = new Timer("reqLoginTimer");
        checkHB = new Timer("checkHBClient");
        resFindServer = new Timer("resFindServer");

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
        if (socket == null || socket.isClosed()) {
            // establishing socket to server for TCP communication
            try {
                socket = new Socket(serverip, PORT);
            } catch (Exception ec) {
                display("Error connectiong to serverip:" + ec);
                return false;
            }

		/* Creating both Data Streams */
            try {
                sInput = socket.getInputStream();
                sOutput = socket.getOutputStream();
            } catch (IOException eIO) {
                display("Exception creating new Input/output Streams: " + eIO);
                return false;
            }

            // Create and start Threads for IMH and PP
            new Thread(clientProtocolParser, "ClientProtocolParser " + uniqueID).start();
            new Thread(incomingMessageHandler, "IncomingMessageHandler " + uniqueID).start();
        }
        /*
         Send Login Message according to Protocol specification
         If failed, close socket and return false
        */
        reqLogin();
        return true;
    }

    /**
     * Sends reqLogin Package to Server. Checks if updateClient
     *
     * @return
     */
    private void reqLogin() {
        reqLogin loginMessage = null;
        try {
            loginMessage = new reqLogin(InetAddress.getByName(this.serverip), this.username, 0);
        } catch (UnknownHostException e) {
            display("Unable to resolve Server IP");
        }
        MessageBuilder.buildMessage(loginMessage, sOutput);
        reqLoginTimer = new Timer();
        reqLoginTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connectedClients.isEmpty() || !findOwnUsername()) {
                    display("Bad Login - maybe try another Username");
                    checkHB.cancel();
                    clientGUI.connectionFailed();
                } else {
                    display("Logged in - you did it!");
                    clientGUI.clearText();
                    startHeartbeatTimer();
                }
            }
        }, TIMEOUT);
    }

    void startHeartbeatTimer() {
        checkHB = new Timer();
        checkHB.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hbReceived[0] == false) {
                    checkHB.cancel();
                    disconnect();
                    display("Server died! Let me handle dat...");
                    findNewServer();
                } else {
                    hbReceived[0] = false;
                }
            }
        }, TIMEOUT / 2, TIMEOUT);
    }

    void findNewServer() {
        connectedClients.removeIf(clientInformation -> clientInformation.getClientUsername() == "");
        InetAddress lowestIP = Utility.getBroadcastIP();
        int lowestPort = Integer.MAX_VALUE;
        for (ClientInformation clientInformation :
                connectedClients) {
            int compVal = clientInformation.getClientIP().getHostAddress().compareTo(lowestIP.getHostAddress());
            if (compVal == -1) {
                lowestIP = clientInformation.getClientIP();
                lowestPort = clientInformation.getClientPort();
            } else if (compVal == 0) {
                if (clientInformation.getClientPort() < lowestPort) {
                    lowestIP = clientInformation.getClientIP();
                    lowestPort = clientInformation.getClientPort();
                }
            }
        }
        if (lowestIP.equals(myInformation.getClientIP()) && lowestPort == myInformation.getClientPort()) {
            startLocalServer();
        } else {
            try {
                synchronized (this) {
                    wait(TIMEOUT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reqFindServer();
        }
        connectedClients.clear();
    }

    // /w hans tolle nachricht
    void sendMsg(String message) {
        if(message.startsWith("/w")) {
            String target = message.split(" ")[1];
            ArrayList<String> messageText = new ArrayList<String>(Arrays.asList(message.split(" ")));
            messageText.remove(0);
            messageText.remove(0);
            String actualMessage = "";

            for (String s : messageText)
            {
                actualMessage += s + " ";
            }
            for (ClientInformation client : connectedClients) {
                if (client.getClientUsername().equals(target)) {
                    InetAddress targetIP = client.getClientIP();
                    int targetPort = client.getClientPort();
                    MessageBuilder.buildMessage(new sendMsgUsr(targetIP, targetPort,socket.getLocalAddress(),socket.getLocalPort(),Utility.getMessageId(),actualMessage),sOutput);
                    break;
                }
            }
        }
        else {
            MessageBuilder.buildMessage(new sendMsgGrp(Utility.getBroadcastIP(), 0,socket.getLocalAddress(),socket.getLocalPort(),Utility.getMessageId(),message),sOutput);
        }
    }

    private boolean findOwnUsername() {
        for (ClientInformation clientInformation : connectedClients) {
            if (
                    clientInformation.getClientIP().equals(socket.getLocalAddress()) &&
                            clientInformation.getClientPort() == socket.getLocalPort() &&
                            clientInformation.getClientUsername().equals(username)) {
                myInformation = clientInformation;
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
        incomingMessageHandler.stopRunning();
        clientProtocolParser.stopRunning();
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
        clientGUI.connectionFailed();
        checkHB.cancel();
    }

    public void reqFindServer() {
        sendMessage(new reqFindServer(Utility.getBroadcastIP(), PORT));
        resFindServer = new Timer();
        resFindServer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (incomingResFindServer.isEmpty()) {
                    startLocalServer();
                } else {
                    InetAddress lowestIP = Utility.getBroadcastIP();
                    for (resFindServer r : incomingResFindServer) {
                        if (r.getSourceIP().toString().compareTo(lowestIP.toString()) == -1) {
                            lowestIP = r.getSourceIP();
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

    void startLocalServer() {
        display("No server found!");
        display("Starting own server...");
        new Thread(server, "ServerThread").start();
        server.showGUI();
        display("Server started");
        clientGUI.setServerTextField("127.0.0.1");
        clientGUI.enableFindServerButton(false);
    }

    public void displayConnectedClients() {
        display("Connected Nodes:");
        for (ClientInformation clientInformation : connectedClients) {
            if (!clientInformation.isServer() && clientInformation.getClientUsername() != username) {
                display(clientInformation.getClientIP().getHostAddress() + ":" + clientInformation.getClientPort() + " | " + clientInformation.getClientUsername());
            } else {
                display(clientInformation.getClientIP().getHostAddress() + ":" + clientInformation.getClientPort() + " | " + "SERVER");
            }
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashSet<ClientInformation> getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(HashSet<ClientInformation> connectedClients) {
        this.connectedClients = connectedClients;
    }

    public boolean[] getHbReceived() {
        return hbReceived;
    }
}


