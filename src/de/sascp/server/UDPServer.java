package de.sascp.server;

import de.sascp.message.subTypes.reqFindServer;

import java.io.IOException;
import java.net.*;

import static de.sascp.protocol.Specification.*;
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
            try {
                socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
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

                version = intFromFourBytes(headerBytes, HEADERVERSIONOFFSET, 4); // Version number
                messageType = intFromFourBytes(headerBytes, HEADERTYPEOFFSET, 4); // MessageType
                length = intFromFourBytes(headerBytes, HEADERLENGTHOFFSET, 4);  // Length

                if (checkCommonHeader(version, messageType, length)) {
                    lookingForCommonHeader = false;
                }
            }

            if (parent.incomingMessageQueue.offer(new reqFindServer(((InetSocketAddress) packet.getSocketAddress()).getAddress(), (((InetSocketAddress) packet.getSocketAddress())
                    .getPort())))) {
                parent.display("reqFindServer received and added to incoming Message Queue");
            } else {
                parent.display("reqFindServer received and thrown away!");
            }
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}