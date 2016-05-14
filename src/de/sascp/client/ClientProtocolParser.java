package de.sascp.client;

import de.sascp.message.subTypes.resFindServer;

import java.io.IOException;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
class ClientProtocolParser implements Runnable {

    private final Client parent;

    public ClientProtocolParser(Client parent) {
        this.parent = parent;
    }
    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */

    /**
     * Checks message type specific header against protocol specification
     *
     * @param incomingData - byte[] Stream which will be checked
     * @param messageType  - expected Message Type
     * @return returns true if Header matches Protocol Specification of given MessageType
     */
    public static boolean checkMessageHeader(byte[] incomingData, int messageType) {

        // TODO

        return true;
    }

    public void run() {
        boolean lookingForCommonHeader = true;
        boolean lookingForPayload = true;
        int version = -1;
        int messageType = -1;
        int length = -1;

        while (lookingForCommonHeader) {
            byte[] headerBytes = new byte[CHLENGTH];
            try {
                parent.sInput.read(headerBytes);
            } catch (IOException e) {
                connectionFailed(e);
                break;
            }

            version = fromArray(headerBytes, 0); // Version number
            messageType = fromArray(headerBytes, 4); // MessageType
            length = fromArray(headerBytes, 8);  // Length

            if (checkCommonHeader(version, messageType, length)) {
                lookingForCommonHeader = false;
            }
        }

        while (lookingForPayload) {
            if (messageType == UPDATECLIENT) {
                // TODO Updateclient List einlesen bitte danke
            } else {
                byte[] payload = new byte[length];
                try {
                    parent.sInput.read(payload);
                } catch (IOException e) {
                    connectionFailed(e);
                    break;
                }
                switch (messageType) {
                    case (RESFINDSERVER):
                        try {
                            parent.incomingMessageQueue.put(new resFindServer(parent.socket.getInetAddress(), parent.socket.getPort()));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case (REQHEARTBEAT):

                }
            }
            lookingForPayload = false;
        }
    }

    private void connectionFailed(IOException e) {
        parent.display("Server has close the connection: " + e);
        if (parent.clientGUI != null)
            parent.clientGUI.connectionFailed();
    }
}
