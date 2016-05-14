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

import static de.sascp.protocol.Specification.VERSION;

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
    public static boolean buildMessage(reqFindServer chatMessage, OutputStream outputStream) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);

        DatagramPacket packet = null;
        packet = new DatagramPacket(outgoingMessage, outgoingMessage.length, Utility.getBroadcastIP(), 4242);
        DatagramSocket toSocket = null;
        try {
            toSocket = new DatagramSocket();
        } catch (SocketException e) {
            return false;
        }
        try {
            toSocket.send(packet);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean buildMessage(resFindServer chatMessage, OutputStream outputStream) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);

        try {
            outputStream.write(outgoingMessage);
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
