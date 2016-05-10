package de.sascp.protocol;

/**
 * Created by Rene on 10.05.2016.
 */
public class Specification {
    // Timeout
    public static final int TIMEOUT = 2000;
    // Current Protocol Version
    public static final int VERSION = 1;
    // Port Number
    public static final int PORT = 4242;
    // Message Types

    // example types
    public static final int WHOISIN = 1000;
    public static final int MESSAGE = 1001;
    public static final int LOGOUT = 1002;

    // actual message types
    public static final int REQFINDSERVER = 1;
    public static final int RESFINDSERVER = 2;
}
