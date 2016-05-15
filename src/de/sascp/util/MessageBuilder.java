package de.sascp.util;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.reqFindServer;
import de.sascp.message.subTypes.reqLogin;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.resHeartbeat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentLinkedQueue;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * Message Builder presents functionality for converting Chat Message Objects into byte[] Streams
 */
public class MessageBuilder {
    /**
     *
     * @param chatMessage
     * @param incomingMessageQueue
     * @return
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
            } catch (SocketTimeoutException timeoutException) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (incomingPacket.getData().length == CHLENGTH) {
                byte[] headerBytes = incomingPacket.getData();

                int version = fromArray(headerBytes, HEADERVERSIONOFFSET); // Version number
                int messageType = fromArray(headerBytes, HEADERTYPEOFFSET); // MessageType
                int length = fromArray(headerBytes, HEADERLENGTHOFFSET);  // Length

                if (checkCommonHeader(version, messageType, length)) {
                    incomingMessageQueue.offer(new resFindServer(incomingPacket.getAddress(), incomingPacket.getPort()));
                }
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

    public static boolean buildMessage(reqLogin chatMessage, OutputStream outputStream) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);

        byte[] username = chatMessage.getUsername().getBytes(Charset.forName(CHARSET));

        ByteOutputStream byteOutputStream = new ByteOutputStream();

        byteOutputStream.write(outgoingMessage);
        byteOutputStream.write(username);

        try {
            outputStream.write(byteOutputStream.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
