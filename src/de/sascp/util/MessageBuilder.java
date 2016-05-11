package de.sascp.util;

import de.sascp.message.ChatMessage;

import java.io.OutputStream;

import static de.sascp.protocol.Specification.UPDATECLIENT;

/**
 * Message Builder presents functionality for converting Chat Message Objects into byte[] Streams
 */
public class MessageBuilder {
    /**
     * Converts an ChatMessage Object into Protocol-specific byte Stream
     * @param chatMessage - ChatMessage Object, which will be converted
     * @return - byte[] Stream for outgoing data
     */
    public static boolean buildMessage(ChatMessage chatMessage, OutputStream outputStream) {
        int size = 0;
        if (chatMessage.getMessageType() != UPDATECLIENT) {
            size = chatMessage.getLength();
        } else {

        }

        byte[] outgoingMessage = new byte[size];

        // TODO

        return false;
    }
}
