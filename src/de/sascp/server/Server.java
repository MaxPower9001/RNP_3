package de.sascp.server;

/**
 * Created by Rene on 10.05.2016.
 */

import de.sascp.marker.ChatProgramm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static de.sascp.protocol.Specification.PORT;

/*
 * The server that can be run both as a console application or a GUI
 */
class Server implements ChatProgramm {
    // a unique ID for each connection
    static int uniqueId;
    // an ArrayList to keep the list of the Client
    private final ArrayList<ClientConnectionListener> al;
    // if I am in a GUI
    private final ServerGUI sg;
    // to display time
    private final SimpleDateFormat sdf;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;


    public Server(ServerGUI sg) {
        // GUI or not
        this.sg = sg;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<>();
    }

    public void start() {
        keepGoing = true;
		/* create socket server and wait for connection requests */
        try
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(PORT);

            // infinite loop to wait for connections
            while(keepGoing)
            {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + PORT + ".");

                Socket socket = serverSocket.accept();  	// accept connection
                // if I was asked to stop
                if(!keepGoing)
                    break;
                ClientConnectionListener t = new ClientConnectionListener(socket, this);  // make a thread of it
                al.add(t);									// save it in the ArrayList
                Thread thread = new Thread(t);
                thread.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for (ClientConnectionListener tc : al) {
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        // not much I can do
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
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
        }
        catch(Exception e) {
            // nothing I can really do
        }
    }
    /*
     * Display an event (not a message) to the console or the GUI
     */
    void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        if(sg == null)
            System.out.println(time);
        else
            sg.appendEvent(time + "\n");
    }
    /*
     *  to broadcast a message to all Clients
     */
    synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        // display message on console or GUI
        if(sg == null)
            System.out.print(messageLf);
        else
            sg.appendRoom(messageLf);     // append in the room window

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = al.size(); --i >= 0;) {
            ClientConnectionListener ct = al.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientConnectionListener ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    public ArrayList<ClientConnectionListener> getAl() {
        return al;
    }

    public SimpleDateFormat getSdf() {
        return sdf;
    }
}




