package de.sascp.client;

import java.io.IOException;

/**
 * Created by Rene on 11.05.2016.
 */
public class ServerListener implements Runnable{
    Client parent;

    ServerListener(Client parent){
        this.parent = parent;
    }
    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */

        public void run() {
            while(true) {
                try {
                    String msg = String.valueOf(parent.sInput.read());
                    // if console mode print the message and add back the prompt
                    if(parent.cg == null) {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                    else {
                        parent.cg.append(msg);
                    }
                }
                catch(IOException e) {
                    parent.display("Server has close the connection: " + e);
                    if(parent.cg != null)
                        parent.cg.connectionFailed();
                    break;
                }
            }
        }

}
