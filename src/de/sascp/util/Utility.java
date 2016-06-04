package de.sascp.util;

import java.net.*;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;

import static de.sascp.protocol.Specification.*;
import static java.lang.System.out;

/**
 * Created by Rene on 13.05.2016.
 */
public class Utility {
    public static final int OHSHIT = -9001;
    public static final int CHLENGTH = 12;
    private static InetAddress broadcastIP;
    private static InetAddress localIP;

    private static final boolean showAllNIC = false;

    private static final String NIC = "eth1";


    public static InetAddress getBroadcastIP() {
        return broadcastIP;
    }

    public static InetAddress getLocalIP() {
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
        byte[] c = new byte[aLen + bLen];
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
        for (int i = offset; i < offset + length; i++) {
            returnValue[counter] = dataarray[i];
            counter++;
        }
        return returnValue;
    }

    public static void main(String args[]) throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
            displayInterfaceInformation(netint);
        }
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        if(showAllNIC){
            out.printf("Display name: %s\n", netint.getDisplayName());
            out.printf("Name: %s\n", netint.getName());
        }
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            if (showAllNIC) {
                out.printf("InetAddress: %s\n", inetAddress);
            }
            if(netint.getName().equals(NIC) && inetAddress.getClass() == Inet4Address.class && !inetAddress.isLoopbackAddress()){
                localIP = inetAddress;
                broadcastIP = netint.getInterfaceAddresses().get(0).getBroadcast();
                System.out.println("Local IP: " + localIP);
                System.out.println("Broadcast IP: " + broadcastIP);
            }
        }
        out.printf("\n");
    }
    public static int compare(InetAddress adr1, InetAddress adr2) {
        byte[] ba1 = adr1.getAddress();
        byte[] ba2 = adr2.getAddress();

        // general ordering: ipv4 before ipv6
        if(ba1.length < ba2.length) return -1;
        if(ba1.length > ba2.length) return 1;

        // we have 2 ips of the same type, so we have to compare each byte
        for(int i = 0; i < ba1.length; i++) {
            int b1 = unsignedByteToInt(ba1[i]);
            int b2 = unsignedByteToInt(ba2[i]);
            if(b1 == b2)
                continue;
            if(b1 < b2)
                return -1;
            else
                return 1;
        }
        return 0;
    }

    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
