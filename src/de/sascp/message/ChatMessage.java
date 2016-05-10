package de.sascp.message;

/**
 * Created by Rene on 10.05.2016.
 */

import java.io.Serializable;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

    private final byte[] version = new byte[4];
    private final byte[] messageType = new byte[4];
    private final byte[] length = new byte[4];

    private final int type;
    private final String message;

    // constructor
    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    // getters
    public int getType() {
        return type;
    }
    public String getMessage() {
        return message;
    }
}


