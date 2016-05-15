package de.sascp.client;

import java.net.InetAddress;

/**
 * Class used to store Client Information without storing the whole Client instance.
 * Holds    Client's IP,
 * Client's Port,
 * Client's Username
 * Every Client and Server holds a List of these to store Information about every connected Client.
 */
public class ClientInfomartion {
    final InetAddress clientIP;
    final int clientPort;
    final String clientUsername;
    final boolean isServer;

    public ClientInfomartion(InetAddress clientIP, int clientPort, String clientUsername, boolean isServer) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.clientUsername = clientUsername;
        this.isServer = isServer;
    }
}
