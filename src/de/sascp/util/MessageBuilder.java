package de.sascp.util;

import de.sascp.message.ChatMessage;

/**
 * Message Builder presents functionality for converting Chat Message Objects into byte[] Streams
 */
public class MessageBuilder {
    /**
     * Converts an ChatMessage Object into Protocol-specific byte Stream
     * @param chatMessage - ChatMessage Object, which will be converted
     * @return - byte[] Stream for outgoing data
     */
    public static byte[] buildMessage(ChatMessage chatMessage){
        int size = 0;
        byte[] outgoingMessage = new byte[size];

        // TODO

        return outgoingMessage;
    }
}
