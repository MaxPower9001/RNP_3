package de.sascp.util;

import de.sascp.client.ClientInformation;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;

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

                System.out.println("UDP Local IP: " + toSocket.getLocalAddress() + ":" + toSocket.getLocalPort());
                System.out.println("UDP Remote IP: " + incomingPacket.getAddress() + ":" + incomingPacket.getPort());

                if (checkCommonHeader(version, messageType, length)) {
                    incomingMessageQueue.offer(
                            new resFindServer(
                                    toSocket.getLocalAddress(),
                                    toSocket.getLocalPort(),
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort()));
                }
            }
        }
        toSocket.close();
        return true;
    }

    public static boolean buildMessage(resFindServer resFindServer, DatagramSocket toSocket) {
        byte[] outgoingMessage = buildCommonHeader(resFindServer);
        DatagramPacket packet = new DatagramPacket(outgoingMessage, outgoingMessage.length, resFindServer.getTargetIP(), resFindServer.getTargetPort());
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
        byte[] targetIp = sendMsgUsr.getTargetIP().getAddress();
        byte[] sourcePort = intPortToByteArray(sendMsgUsr.getSourcePort());
        byte[] targetPort = intPortToByteArray(sendMsgUsr.getTargetPort());

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
        byte[] targetIp = sendMsgGrp.getTargetIP().getAddress();
        byte[] sourcePort = intPortToByteArray(sendMsgGrp.getSourcePort());
        byte[] targetPort = intPortToByteArray(sendMsgGrp.getTargetPort());

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

    public static boolean buildMessage(reqHeartbeat reqHeartbeat, OutputStream outputStream) throws IOException {
        byte[] outgoingMessage = buildCommonHeader(reqHeartbeat);
        outputStream.write(outgoingMessage);

        return true;
    }
    public static boolean buildMessage(resHeartbeat resHeartbeat, OutputStream outputStream) {
        byte[] outgoingMessage = buildCommonHeader(resHeartbeat);
        try {
            outputStream.write(outgoingMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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
        for (ClientInformation clientInformation : chatMessage.getClientInformation()) {
            byte[] recordIP;
            byte[] recordPort;
            byte[] recordUsernameLength = new byte[1];
            byte[] recordReserved = new byte[1];
            byte[] recordUsername;

            recordIP = clientInformation.getClientIP().getAddress();

            recordPort = intPortToByteArray(clientInformation.getClientPort());

            recordUsernameLength[0] = (byte) (clientInformation.getClientUsername().length() & 0xFF);

            recordReserved[0] = 0;

            recordUsername = clientInformation.getClientUsername().getBytes();

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

    public static boolean buildMessage(errorMsgNotDelivered errMessage, OutputStream outputStream) {
        byte[] messageToBeSent = concat(new byte[0], buildCommonHeader(errMessage));

        byte[] messageID = new byte[4];
        byte[] sourceIP;
        byte[] targetIP;
        byte[] sourcePort;
        byte[] targetPort;

        messageID[0] = (byte) errMessage.getMessageId();
        sourceIP = errMessage.getSourceIP().getAddress();
        targetIP = errMessage.getTargetIP().getAddress();
        sourcePort = intPortToByteArray(errMessage.getSourcePort());
        targetPort = intPortToByteArray(errMessage.getTargetPort());

        messageToBeSent = concat(messageToBeSent, messageID);
        messageToBeSent = concat(messageToBeSent, sourceIP);
        messageToBeSent = concat(messageToBeSent, targetIP);
        messageToBeSent = concat(messageToBeSent, sourcePort);
        messageToBeSent = concat(messageToBeSent, targetPort);

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
