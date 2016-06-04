package de.sascp.main;


import de.sascp.client.ClientGUI;
import de.sascp.server.ServerGUI;
import de.sascp.util.Utility;

import java.net.SocketException;

/**
 * Created by Rene on 10.05.2016.
 */

class Main {


    private Main() {
        // start Server GUI
        ServerGUI serverGUI = new ServerGUI();
        // start Client GUI
        ClientGUI clientGUI = new ClientGUI(serverGUI.getServer());

        try {
            Utility.main(null);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // to start the whole thing
    public static void main(String[] args) {
        new Main();
    }
}
