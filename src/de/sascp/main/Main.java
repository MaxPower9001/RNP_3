package de.sascp.main;


import de.sascp.client.ClientGUI;
import de.sascp.server.ServerGUI;

/**
 * Created by Rene on 10.05.2016.
 */

class Main {


    private Main() {
        // start Server GUI
        ServerGUI serverGUI = new ServerGUI();
        // start Client GUI
        ClientGUI clientGUI = new ClientGUI(serverGUI.getServer());
    }

    // to start the whole thing
    public static void main(String[] args) {
        new Main();
    }
}
