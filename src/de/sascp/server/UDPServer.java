package de.sascp.server;

import de.sascp.message.subTypes.reqFindServer;

import java.io.IOException;
import java.net.*;

import static de.sascp.util.Utility.*;

public class UDPServer implements Runnable {
    Server parent;

    public UDPServer(Server parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(4242, InetAddress.getByName("127.0.0.1"));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        boolean lookingForCommonHeader = true;
        int version;
        int messageType;
        int length;

        while (lookingForCommonHeader) {
            DatagramPacket packet = new DatagramPacket(new byte[CHLENGTH], CHLENGTH);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] headerBytes = packet.getData();

            version = fromArray(headerBytes, 0); // Version number
            messageType = fromArray(headerBytes, 4); // MessageType
            length = fromArray(headerBytes, 8);  // Length

            if (checkCommonHeader(version, messageType, length)) {
                lookingForCommonHeader = false;
            }
        }
        parent.incomingMessageQueue.offer(new reqFindServer(socket.getInetAddress(), socket.getPort()));
    }
}