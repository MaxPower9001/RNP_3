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

    public static boolean buildMessage(sendMsgUsr sendMsgUsr, OutputStream outputStream) {
        byte[] usrTextMessage = sendMsgUsr.getMessage().getBytes();
        byte[] usrTextMessageId = Utility.intToByteArray(sendMsgUsr.getMessageId());
        byte[] sourceIp = sendMsgUsr.getSourceIP().getAddress();
        byte[] targetIp = sendMsgUsr.getDestinationIP().getAddress();
        byte[] sourcePort = intPortToByteArray(sendMsgUsr.getSourcePort());
        byte[] targetPort = intPortToByteArray(sendMsgUsr.getDestinationPort());

        byte[] messageToBeSent = buildCommonHeader(sendMsgUsr);
        messageToBeSent = concat(messageToBeSent,usrTextMessageId);
        messageToBeSent = concat(messageToBeSent,sourceIp);
        messageToBeSent = concat(messageToBeSent,targetIp);
        messageToBeSent = concat(messageToBeSent,sourcePort);
        messageToBeSent = concat(messageToBeSent,targetPort);
        messageToBeSent = concat(messageToBeSent,usrTextMessage);

        try {
            outputStream.write(messageToBeSent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean buildMessage(sendMsgGrp sendMsgGrp, OutputStream outputStream) {
        byte[] usrTextMessage = sendMsgGrp.getMessage().getBytes();
        byte[] usrTextMessageId = Utility.intToByteArray(sendMsgGrp.getMessageId());
        byte[] sourceIp = sendMsgGrp.getSourceIP().getAddress();
        byte[] targetIp = sendMsgGrp.getDestinationIP().getAddress();
        byte[] sourcePort = intPortToByteArray(sendMsgGrp.getSourcePort());
        byte[] targetPort = intPortToByteArray(sendMsgGrp.getDestinationPort());

        byte[] messageToBeSent = buildCommonHeader(sendMsgGrp);
        messageToBeSent = concat(messageToBeSent,usrTextMessageId);
        messageToBeSent = concat(messageToBeSent,sourceIp);
        messageToBeSent = concat(messageToBeSent,targetIp);
        messageToBeSent = concat(messageToBeSent,sourcePort);
        messageToBeSent = concat(messageToBeSent,targetPort);
        messageToBeSent = concat(messageToBeSent,usrTextMessage);

        try {
            outputStream.write(messageToBeSent);
        } catch (IOException e) {
            e.printStackTrace();
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
        byte[] messageToBeSent = concat(new byte[0],buildCommonHeader(chatMessage));
        for (ClientInfomartion clientInfomartion : chatMessage.getClientInfomartion()) {
            byte[] recordIP;
            byte[] recordPort = new byte[2];
            byte[] recordUsernameLength = new byte[1];
            byte[] recordReserved = new byte[1];
            byte[] recordUsername;

            recordIP = clientInfomartion.getClientIP().getAddress();

            recordPort = intPortToByteArray(clientInfomartion.getClientPort());

            recordUsernameLength[0] = (byte) (clientInfomartion.getClientUsername().length() & 0xFF);

            recordReserved[0] = 0;

            recordUsername = clientInfomartion.getClientUsername().getBytes();

            messageToBeSent = concat(messageToBeSent,recordIP);
            messageToBeSent = concat(messageToBeSent,recordPort);
            messageToBeSent = concat(messageToBeSent,recordUsernameLength);
            messageToBeSent = concat(messageToBeSent,recordReserved);
            messageToBeSent = concat(messageToBeSent,recordUsername);
        }

        try {
            outputStream.write(messageToBeSent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static byte[] buildCommonHeader(ChatMessage chatMessage) {
        byte[] header = intToByteArray(VERSION);
        header = concat(header,intToByteArray(chatMessage.getMessageType()));
        header = concat(header,intToByteArray(chatMessage.getLength()));
        return header;
    }

    private static byte[] intToByteArray(int intToTransform) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(intToTransform);
        return byteBuffer.array();
    }
}
