package de.sascp.message.subTypes;

import de.sascp.message.ChatMessage;

import java.net.InetAddress;

import static de.sascp.protocol.Specification.REQLOGIN;

/**
 * Created by Rene on 11.05.2016.
 */
public class reqLogin extends ChatMessage {
    private final String username;

    public reqLogin(InetAddress destinationIP, String username) {
        super(destinationIP, de.sascp.protocol.Specification.PORT, null, 0, REQLOGIN, username.length());
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
