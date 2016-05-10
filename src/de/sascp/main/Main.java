package de.sascp.main;


import de.sascp.client.ClientGUI;
import de.sascp.server.ServerGUI;

/**
 * Created by Rene on 10.05.2016.
 */

class Main {


    private Main() {
        // start Client GUI
        ClientGUI clientGUI = new ClientGUI();


        ServerGUI serverGUI = new ServerGUI();
    }

    // to start the whole thing
    public static void main(String[] args) {
        new Main();
    }
}
