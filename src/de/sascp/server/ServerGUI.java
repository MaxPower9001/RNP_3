package de.sascp.server;

/**
 * Created by Rene on 10.05.2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/*
 * The server as a GUI
 */
public class ServerGUI extends JFrame implements WindowListener {

    // JTextArea for the chat room and the events
    private final JTextArea chat;
    private final JTextArea event;
    // my server
    private Server server;


    // server constructor that receive the port to listen to for connection as parameter
    public ServerGUI() {
        super("Chat Server");
        server = null;
        // in the NorthPanel the PortNumber the Start and Stop buttons
        JPanel north = new JPanel();
        add(north, BorderLayout.NORTH);

        // the event and chat room
        JPanel center = new JPanel(new GridLayout(2, 1));
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        appendRoom("Chat room.\n");
        center.add(new JScrollPane(chat));
        event = new JTextArea(80, 80);
        event.setEditable(false);
        appendEvent("Events log.\n");
        center.add(new JScrollPane(event));
        add(center);

        // need to be informed when the user click the close button on the frame
        addWindowListener(this);
        setSize(400, 600);
        server = new Server(this);
    }

    // append message to the two JTextArea
    // position at the end
    void appendRoom(String str) {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }

    void appendEvent(String str) {
        event.append(str);
        event.setCaretPosition(chat.getText().length() - 1);

    }

    /*
     * If the user click the X button to close the application
     * I need to close the connection with the server to free the port
     */
    public void windowClosing(WindowEvent e) {
        // if my Server exist
        if (server != null) {
            try {
                server.stop();            // ask the server to close the conection
            } catch (Exception eClose) {
            }
            server = null;
        }
        // dispose the frame
        dispose();
        System.exit(0);
    }

    // I can ignore the other WindowListener method
    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public Server getServer() {
        return server;
    }

}


