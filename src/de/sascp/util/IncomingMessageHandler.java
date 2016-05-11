package de.sascp.util;

import de.sascp.client.Client;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.resHeartbeat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Converts byte[] Streams checked by the Protocol Parser into ChatMessage Objects and answers Heartbeat-Requests
 */
public class IncomingMessageHandler implements Runnable {

    Client parent;

    public IncomingMessageHandler(Client parent) {
        this.parent = parent;
    }


    public static void handleIncomingMessage(ChatMessage chatMessage) {

        // TODO

    }

    /**
     * Directly answers Heartbeat Package with the given OutputStream
     * @param outputStream - OutputStream needed to send the Message
     * @return returns true, if message send successfully. returns false,
     *          if IOException thrown
     */
    public static boolean answerHeartbeat(int targetIP, int targetPort, OutputStream outputStream) {
        try{
            outputStream.write(MessageBuilder.buildMessage(new resHeartbeat(targetIP, targetPort)));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        while (true) {

        }
    }
}
