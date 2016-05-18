package de.sascp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;

import static de.sascp.protocol.Specification.*;

/**
 * Created by Rene on 13.05.2016.
 */
public class Utility {
    public static final int CHLENGTH = 12;
    private static final String BROADCASTIP = "127.255.255.255";
    private static final String LOCALIP = "localhost";
    private static InetAddress broadcastIP;
    private static InetAddress localIP;


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
    public static InetAddress getLocalIP() {
        if (localIP == null) {
            try {
                localIP = InetAddress.getByName(LOCALIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return localIP;
    }

    public static int intFromFourBytes(byte[] payload, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);
        return buffer.getInt();
    }

    public static int intFromTwoBytes(byte[] payload, int offset) {
        // return unsigned Integer
        return ((payload[offset + 1] & 0xFF) << 8) | payload[offset] & 0xFF;
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

    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static int getMessageId() {
        return (int) Instant.now().getEpochSecond();
    }

    public static byte[] intPortToByteArray(int port) {
        byte[] hans = new byte[2];
        hans[0] = (byte) (port & 0x00FF);
        hans[1] = (byte) ((port >> 8) & 0x00FF);
        return hans;
    }

    public static byte[] intToByteArray(int integer) {
        return ByteBuffer.allocate(4).putInt(integer).array();
    }

    public static byte[] getByteArrayFragment(byte[] dataarray, int offset, int length) {
        byte[] returnValue = new byte[length];
        int counter = 0;
        for(int i = offset ; i < offset+length; i++){
            returnValue[counter] = dataarray[i];
            counter++;
        }
        return returnValue;
    }
}
