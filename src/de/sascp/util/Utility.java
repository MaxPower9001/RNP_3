package de.sascp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Rene on 13.05.2016.
 */
public class Utility {
    public static final String BROADCASTIP = "255.255.255.255";
    public static final int CHLENGTH = 12;
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
}
