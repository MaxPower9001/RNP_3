package de.sascp.util;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.reqFindServer;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.resHeartbeat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * Message Builder presents functionality for converting Chat Message Objects into byte[] Streams
 */
public class MessageBuilder {
    /**
     * Converts an ChatMessage Object into Protocol-specific byte Stream
     *
     * @param chatMessage - ChatMessage Object, which will be converted
     * @return - byte[] Stream for outgoing data
     */
    public static boolean buildMessage(reqFindServer chatMessage, ConcurrentLinkedQueue incomingMessageQueue) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);

        DatagramPacket outgoingPacket = new DatagramPacket(outgoingMessage, outgoingMessage.length, Utility.getBroadcastIP(), PORT);
        DatagramPacket incomingPacket = new DatagramPacket(new byte[CHLENGTH], CHLENGTH);
        DatagramSocket toSocket;
        try {
            toSocket = new DatagramSocket();
        } catch (SocketException e) {
            return false;
        }
        try {
            toSocket.send(outgoingPacket);
        } catch (IOException t) {

        }
        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTime < TIMEOUT) {
            try {
                toSocket.setSoTimeout(TIMEOUT);
                toSocket.receive(incomingPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] headerBytes = incomingPacket.getData();

            int version = fromArray(headerBytes, 0); // Version number
            int messageType = fromArray(headerBytes, 4); // MessageType
            int length = fromArray(headerBytes, 8);  // Length

            if (checkCommonHeader(version, messageType, length)) {
                incomingMessageQueue.offer(new resFindServer(incomingPacket.getAddress(), incomingPacket.getPort()));
            }
        }
        toSocket.close();
        return true;
    }

    public static boolean buildMessage(resFindServer chatMessage, DatagramSocket toSocket) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);
        DatagramPacket packet = new DatagramPacket(outgoingMessage, outgoingMessage.length, chatMessage.getDestinationIP(), chatMessage.getDestinationPort());
        try {
            toSocket.send(packet);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static byte[] buildCommonHeader(ChatMessage chatMessage) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(intToByteArray(VERSION));
            outputStream.write(intToByteArray(chatMessage.getMessageType()));
            outputStream.write(intToByteArray(chatMessage.getLength()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private static byte[] intToByteArray(int intToTransform) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(intToTransform);
        return byteBuffer.array();
    }

    public static boolean buildMessage(resHeartbeat resHeartbeat, OutputStream outputStream) {
        return false;
    }
}
