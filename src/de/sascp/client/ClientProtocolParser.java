package de.sascp.client;

import de.sascp.message.subTypes.resFindServer;

import java.io.IOException;
import java.nio.ByteBuffer;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.CHLENGTH;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
public class ClientProtocolParser implements Runnable {

    Client parent;

    public ClientProtocolParser(Client parent) {
        this.parent = parent;
    }
    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */

    /**
     * Checks the Common Header of a byte[] Stream against the protocol specification
     * @return returns true if Common Header matches Protocol Specification
     */
    private static boolean checkCommonHeader(int version, int messageType, int length) {

        // TODO

        return false;
    }

    /**
     * Checks message type specific header against protocol specification
     * @param incomingData - byte[] Stream which will be checked
     * @param messageType - expected Message Type
     * @return returns true if Header matches Protocol Specification of given MessageType
     */
    public static boolean checkMessageHeader(byte[] incomingData, int messageType){

        // TODO

        return true;
    }

    public static int fromArray(byte[] payload, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, 4);
        return buffer.getInt();
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
                        parent.incomingMessageQueue.add(new resFindServer(parent.socket.getInetAddress(), parent.socket.getPort()));
                        parent.incomingMessageQueue.notify();
                        break;
                    case (REQHEARTBEAT):

                }
            }
        }
    }

    private void connectionFailed(IOException e) {
        parent.display("Server has close the connection: " + e);
        if (parent.clientGUI != null)
            parent.clientGUI.connectionFailed();
        return;
    }
}
