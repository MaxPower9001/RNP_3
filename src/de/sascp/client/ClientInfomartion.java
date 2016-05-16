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
    private final InetAddress clientIP;
    private final int clientPort;
    private final String clientUsername;
    private final boolean isServer;

    public ClientInfomartion(InetAddress clientIP, int clientPort, String clientUsername, boolean isServer) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.clientUsername = clientUsername;
        this.isServer = isServer;
    }

    public InetAddress getClientIP() {
        return clientIP;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public boolean isServer() {
        return isServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientInfomartion that = (ClientInfomartion) o;

        if (clientPort != that.clientPort) return false;
        if (isServer != that.isServer) return false;
        if (!clientIP.equals(that.clientIP)) return false;
        return clientUsername.equals(that.clientUsername);

    }

    @Override
    public int hashCode() {
        int result = clientIP.hashCode();
        result = 31 * result + clientPort;
        result = 31 * result + clientUsername.hashCode();
        result = 31 * result + (isServer ? 1 : 0);
        return result;
    }
}
