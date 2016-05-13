package de.sascp.util;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.reqFindServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static de.sascp.protocol.Specification.*;

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

        switch (chatMessage.getMessageType()) {
            case (REQFINDSERVER):
                reqFindServer messageToBeSent = new reqFindServer(chatMessage.getSourceIP(), chatMessage.getSourcePort());
                buildCommonHeader(outgoingMessage, messageToBeSent);

                DatagramPacket packet = new DatagramPacket(outgoingMessage, outgoingMessage.length, Utility.getBroadcastIP(), 4242);
                DatagramSocket toSocket = null;
                try {
                    toSocket = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                try {
                    toSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
        }

        // TODO

        return false;
    }

    private static void buildCommonHeader(byte[] byteToBeSent, ChatMessage chatMessage) {
        byteToBeSent[0] = (byte) VERSION;
        byteToBeSent[4] = (byte) chatMessage.getMessageType();
        byteToBeSent[8] = (byte) chatMessage.getLength();
    }
}
