package de.sascp.server;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Protocol Parser presents functionality for checking Common Header and Message Type specific Header Information
 * against the Protocol Specification
 */
class ServerProtocolParser {

    private final Server parent;

    public ServerProtocolParser(Server parent) {
        this.parent = parent;
    }
    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */

    /**
     * Checks the Common Header of a byte[] Stream against the protocol specification
     *
     * @return returns true if Common Header matches Protocol Specification
     */
    private static boolean checkCommonHeader(int version, int messageType, int length) {

        // TODO

        return false;
    }

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

    private static int fromArray(byte[] payload, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, 4);
        return buffer.getInt();
    }

    public void connectionFailed(IOException e) {

    }


}
