package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.protocol.Specification.REQLOGIN;

/**
 * Created by Rene on 11.05.2016.
 */
public class reqLogin extends ChatMessage {
    private final String username;

    public reqLogin(InetAddress targetIP, String username, int sourcePort) {
        super(targetIP, PORT, null, sourcePort, REQLOGIN, username.length());
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
