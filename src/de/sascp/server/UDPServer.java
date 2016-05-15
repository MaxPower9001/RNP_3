package de.sascp.server;

import de.sascp.message.subTypes.reqFindServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static de.sascp.protocol.Specification.PORT;
import static de.sascp.util.Utility.*;

public class UDPServer implements Runnable {
    private final Server parent;
    private DatagramSocket socket;

    public UDPServer(Server parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
            try {
                socket = new DatagramSocket(PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        boolean keepGoing = true;
        while (keepGoing) {

            DatagramPacket packet = new DatagramPacket(new byte[CHLENGTH], CHLENGTH);
            boolean lookingForCommonHeader = true;
            int version;
            int messageType;
            int length;
            while (lookingForCommonHeader) {
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
            parent.incomingMessageQueue.offer(new reqFindServer(packet.getAddress(), packet.getPort()));
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}