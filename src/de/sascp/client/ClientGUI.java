package de.sascp.client;

/**
 * Created by Rene on 10.05.2016.
 */

import de.sascp.server.Server;
import de.sascp.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

    // will first hold "Username:", later on "Enter message"
    private final JLabel label;
    // to hold the Username and later on the messages
    private final JTextField tf;
    // to hold the server address an the port number
    private final JTextField tfServer;
    private final JTextField tfLocalhost;
    // to Logout and get the list of the users
    private final JButton findServer;
    private final JButton login;
    private final JButton logout;
    private final JButton whoIsIn;
    // for the chat room
    private final JTextArea ta;
    private final String defaultHost;
    // the Client object
    private final Client client;
    // if it is for connection
    private boolean connected;

    // Constructor
    public ClientGUI(Server server) {
        super("Chat Client");
        defaultHost = "localhost";

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3, 2));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(2, 5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField("");
        tfServer.setEnabled(false);
        tfLocalhost = new JTextField("");
        tfLocalhost.setEnabled(true);


        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel(" "));
        serverAndPort.add(new JLabel("Local IP Adress:   "));
        serverAndPort.add(tfLocalhost);
        JLabel serverAdress = new JLabel("");
        serverAndPort.add(serverAdress);
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("Anonymous");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        findServer = new JButton("Find Server");
        findServer.addActionListener(this);
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);        // you have to login before being able to logout
        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);        // you have to login before being able to Who is in

        JPanel southPanel = new JPanel();
        southPanel.add(findServer);
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();


        // try creating a new Client with GUI
        client = new Client(this, server);

    }

    // called by the Client to append text in the TextArea
    public void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    public void connectionFailed() {
        findServer.setEnabled(true);
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Anonymous");
        // reset host name as a construction reqLoginTimer
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(true);
        tfLocalhost.setEditable(true);
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }

    /*
    * Button or JTextField clicked
    */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if (o == logout) {
            client.display("Nope...");
            return;
        }
        // if it the who is in button
        if (o == whoIsIn) {
            client.displayConnectedClients();
            return;
        }

        // ok it is coming from the JTextField
        if (connected) {
            // just have to send the message
            // TODO
            client.sendMsg(tf.getText());
            tf.setText("");
            return;
        }

        if (o == findServer) {
            try {
                Utility.setBroadcastIP(InetAddress.getByName(tfLocalhost.getText()));
            } catch (SocketException e1) {
                e1.printStackTrace();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
            client.reqFindServer();
        }

        if (o == login) {
            // ok it is a connection request
            String username = tf.getText().trim();
            // empty username ignore it
            if (username.length() == 0 || username.length() > 255) {
                append("The length of your username does not fit into protocol specification.");
                return;
            }
            client.setUsername(username);
            // test if we can start the Client
            if (!client.start())
                return;
            tf.setText("");
            label.setText("Enter your message below");
            connected = true;

            // disable login and findServer button
            findServer.setEnabled(false);
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server JTextField
            tfServer.setEditable(false);
            tfLocalhost.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
        }

    }

    public void setServerTextField(String serverIP) {
        tfServer.setText(serverIP);
    }

    public void enableFindServerButton(boolean enabled) {
        findServer.setEnabled(enabled);
    }

    public void enableLoginButton(boolean b) {
        login.setEnabled(b);

    }

    public void setMessageLabel(String string) {
        label.setText(string);
    }

    public void clearText(){
        ta.setText("");
    }
}


