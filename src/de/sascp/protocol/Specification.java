package de.sascp.protocol;

/**
 * Created by Rene on 10.05.2016.
 */
public class Specification {
    public static final int CHLENGTH = 12;

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
    public static final int LOWESTMESSAGETYPE = 0;
    public static final int HIGHESTMESSAGETYPE = 10;
    public static final int REQFINDSERVER        = 1;
    public static final int RESFINDSERVER        = 2;
    public static final int REQLOGIN             = 3;
    public static final int UPDATECLIENT         = 4;
    public static final int SENDMSGGRP           = 5;
    public static final int SENDMSGUSR           = 6;
    public static final int REQHEARTBEAT         = 7;
    public static final int RESHEARTBEAT         = 8;
    public static final int ERRORMSGNOTDELIVERED = 9;
}
