package de.sascp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static de.sascp.protocol.Specification.*;

/**
 * Created by Rene on 13.05.2016.
 */
public class Utility {
    public static final int CHLENGTH = 12;
    private static final String BROADCASTIP = "192.168.178.255";
    private static InetAddress broadcastIP;

    public static InetAddress getBroadcastIP() {
        if (broadcastIP == null) {
            try {
                broadcastIP = InetAddress.getByName(BROADCASTIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return broadcastIP;
    }

    public static int fromArray(byte[] payload, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, 4);
        return buffer.getInt();
    }

    /**
     * Checks the Common Header of a byte[] Stream against the protocol specification
     *
     * @return returns true if Common Header matches Protocol Specification
     */
    public static boolean checkCommonHeader(int version, int messageType, int length) {

        // TODO Anhand der Messagetype Länge prüfen

        return version == VERSION &&
                messageType > LOWESTMESSAGETYPE &&
                messageType < HIGHESTMESSAGETYPE &&
                length >= 0;

    }
}
