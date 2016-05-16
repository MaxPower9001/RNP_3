package de.sascp.server;

import de.sascp.client.ClientInfomartion;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.sendMsgGrp;
import de.sascp.message.subTypes.sendMsgUsr;
import de.sascp.message.subTypes.updateClient;
import de.sascp.util.MessageBuilder;
import de.sascp.util.Utility;

import java.io.OutputStream;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.MessageBuilder.buildMessage;

/**
 * Created by Rene on 11.05.2016.
 */
class ServerUnit implements Runnable {
    private final Server parent;
    private boolean keepGoing;

    public ServerUnit(Server server) {
        this.parent = server;
        this.keepGoing = true;
    }

    @Override
    public void run() {
        while (keepGoing) {
            ChatMessage currentMessage = null;
            try {
                currentMessage = parent.incomingMessageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (currentMessage.getMessageType()) {
                case (REQFINDSERVER):
                    answerRequestFindServer(currentMessage);
                    break;
                case (REQLOGIN):
                    for (ClientConnectionListener ccl : parent.getListenerHashMap().values()) {
                        updateClient updateClient = new updateClient(ccl.socket.getInetAddress(), ccl.socket.getPort(), parent.getConnectedClients());
                        buildMessage(updateClient, ccl.sOutput);
                    }
                    break;
                case (SENDMSGUSR):
                    boolean targetStillInClientList = false;
                    OutputStream targetOutputStream = null;
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        if (clientConnection.socket.getInetAddress().equals(currentMessage.getDestinationIP()) &&
                            clientConnection.socket.getPort() == (currentMessage.getDestinationPort())    ) {
                            targetStillInClientList = true;
                            targetOutputStream = clientConnection.sOutput;
                            break;
                        }
                    }
                    if (targetStillInClientList) { // target found, relay message to target
                        buildMessage((sendMsgUsr) currentMessage, targetOutputStream);
                    } else {
                        // TODO handle error
                    }
                    break;
                case (SENDMSGGRP):
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        buildMessage((sendMsgGrp) currentMessage, clientConnection.sOutput);
                    }
                    break;
                default:
                    parent.display("The Queue had sth in it - sth unknown *scary noises*");
                    break;
            }
        }
    }

    private void answerRequestFindServer(ChatMessage currentMessage) {
        if (buildMessage(new resFindServer(
                        currentMessage.getSourceIP(),
                        currentMessage.getSourcePort(),
                        parent.getUdpServer().getSocket().getLocalAddress(),
                        parent.getUdpServer().getSocket().getLocalPort()),
                parent.getUdpServer().getSocket())) {
            parent.display("resFindServer send to: " + currentMessage.getSourceIP().getHostAddress() + "|" + currentMessage.getSourcePort());
        } else {
            parent.display("resFindServer was not sent - *sad trombone*");
        }
    }

    public void stopRunning() {
        this.keepGoing = false;
    }
}
