package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import static de.sascp.protocol.Specification.REQFINDSERVER;
import static de.sascp.protocol.Specification.VERSION;

/**
 * Created by Rene on 11.05.2016.
 */
class reqFindServer extends ChatMessage {
    public reqFindServer() {
        super(VERSION, REQFINDSERVER, 0);
    }
}
