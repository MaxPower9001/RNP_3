package de.sascp.util;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import de.sascp.client.ClientInfomartion;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
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

                int version = intFromFourBytes(headerBytes, HEADERVERSIONOFFSET, 4); // Version number
                int messageType = intFromFourBytes(headerBytes, HEADERTYPEOFFSET, 4); // MessageType
                int length = intFromFourBytes(headerBytes, HEADERLENGTHOFFSET, 4);  // Length

                if (checkCommonHeader(version, messageType, length)) {
                    incomingMessageQueue.offer(new resFindServer(toSocket.getLocalAddress(), toSocket.getLocalPort(), ((InetSocketAddress) incomingPacket.getSocketAddress()).getAddress(), ((
                            (InetSocketAddress) incomingPacket.getSocketAddress())
                            .getPort())));
                }
            }
        }
        toSocket.close();
        return true;
    }

    public static boolean buildMessage(resFindServer resFindServer, DatagramSocket toSocket) {
        byte[] outgoingMessage = buildCommonHeader(resFindServer);
        DatagramPacket packet = new DatagramPacket(outgoingMessage, outgoingMessage.length, resFindServer.getDestinationIP(), resFindServer.getDestinationPort());
        try {
            toSocket.send(packet);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean buildMessage(resHeartbeat resHeartbeat, OutputStream outputStream) {
        return false;
    }

    public static boolean buildMessage(reqLogin chatMessage, OutputStream outputStream) {
        byte[] outgoingMessage = buildCommonHeader(chatMessage);

        byte[] username = chatMessage.getUsername().getBytes(Charset.forName(CHARSET));

        byte[] combined = new byte[outgoingMessage.length + username.length];

        System.arraycopy(outgoingMessage, 0, combined, 0, outgoingMessage.length);
        System.arraycopy(username, 0, combined, outgoingMessage.length, username.length);

        try {
            outputStream.write(combined);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean buildMessage(updateClient chatMessage, OutputStream outputStream) {
        ByteOutputStream byteOutputStream = new ByteOutputStream();
        byte[] commonHeader = buildCommonHeader(chatMessage);
        byteOutputStream.write(commonHeader);
        for (ClientInfomartion clientInfomartion : chatMessage.getClientInfomartion()) {
            byte[] recordIP;
            byte[] recordPort = new byte[2];
            byte[] recordUsernameLength = new byte[1];
            byte[] recordReserved = new byte[1];
            byte[] recordUsername;

            recordIP = clientInfomartion.getClientIP().getAddress();

            recordPort[0] = (byte) (clientInfomartion.getClientPort() & 0x00FF);
            recordPort[1] = (byte) ((clientInfomartion.getClientPort() >> 8) & 0x00FF);

            recordUsernameLength[0] = (byte) (clientInfomartion.getClientUsername().length() & 0xFF);

            recordReserved[0] = 0;

            recordUsername = clientInfomartion.getClientUsername().getBytes();

            byteOutputStream.write(recordIP);
            byteOutputStream.write(recordPort);
            byteOutputStream.write(recordUsernameLength);
            byteOutputStream.write(recordReserved);
            byteOutputStream.write(recordUsername);
        }
        byte[] outBytes = byteOutputStream.getBytes();

        try {
            outputStream.write(outBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
}
