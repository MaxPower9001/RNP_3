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
    private static final String BROADCASTIP = "255.255.255.255";
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

    public static int intFromFourBytes(byte[] payload, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);
        return buffer.getInt();
    }

    public static int intFromTwoBytes(byte[] payload) {
        // return unsigned Integer
        return ((payload[1] & 0xFF) << 8) | payload[0] & 0xFF;
    }

    /**
     * Checks the Common Header against the protocol specification
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
