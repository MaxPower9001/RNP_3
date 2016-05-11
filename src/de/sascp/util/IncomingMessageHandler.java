package de.sascp.util;

import de.sascp.message.ChatMessage;

import java.io.IOException;
import java.io.OutputStream;

import static de.sascp.protocol.Specification.RESHEARTBEAT;

/**
 * Converts byte[] Streams checked by the Protocol Parser into ChatMessage Objects and answers Heartbeat-Requests
 */
public class IncomingMessageHandler {
    /**
     * Converts a previously checked byte[] Stream into a ChatMessage Object
     * @param incomingData - Protocol conform byte[] Stream
     * @return ChatMessage Object containing byte[] Stream Data
     */
    public static ChatMessage convertIncomingByteStream(byte[] incomingData){
        int messageType = 0;
        String messageString = "";

        // TODO

        return new ChatMessage(messageType, messageString);
    }

    /**
     * Directly answers Heartbeat Package with the given OutputStream
     * @param outputStream - OutputStream needed to send the Message
     * @return returns true, if message send successfully. returns false,
     *          if IOException thrown
     */
    public static boolean answerHeartbeat(OutputStream outputStream){
        try{
            outputStream.write(MessageBuilder.buildMessage(new ChatMessage(RESHEARTBEAT,"")));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
