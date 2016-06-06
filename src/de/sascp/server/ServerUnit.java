package de.sascp.server;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;

import java.io.OutputStream;

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
                    doUpdateClients();
                    break;
                case (SENDMSGUSR):
                    boolean targetStillInClientList = false;
                    OutputStream targetOutputStream = null;
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        if (clientConnection.socket.getInetAddress().equals(currentMessage.getTargetIP()) &&
                                clientConnection.socket.getPort() == (currentMessage.getTargetPort())) {
                            targetStillInClientList = true;
                            targetOutputStream = clientConnection.sOutput;
                        }
                    }
                    if (targetStillInClientList) { // target found, relay message to target
                        buildMessage((sendMsgUsr) currentMessage, targetOutputStream);
                        for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                            if (clientConnection.socket.getInetAddress().equals(currentMessage.getSourceIP()) &&
                                    clientConnection.socket.getPort() == (currentMessage.getSourcePort())) {
                                targetOutputStream = clientConnection.sOutput;
                            }
                        }
                        errorMsgNotDelivered errMsg = new errorMsgNotDelivered(
                                currentMessage.getTargetIP(),
                                currentMessage.getTargetPort(),
                                currentMessage.getSourceIP(),
                                currentMessage.getTargetPort(),
                                ((sendMsgUsr) currentMessage).getMessageId());
                        buildMessage(errMsg, targetOutputStream);
                    } else {
                        for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                            if (clientConnection.socket.getInetAddress().equals(currentMessage.getSourceIP()) &&
                                    clientConnection.socket.getPort() == (currentMessage.getSourcePort())) {
                                targetOutputStream = clientConnection.sOutput;
                            }
                        }
                        errorMsgNotDelivered errMsg = new errorMsgNotDelivered(
                                currentMessage.getTargetIP(),
                                currentMessage.getTargetPort(),
                                currentMessage.getSourceIP(),
                                currentMessage.getTargetPort(),
                                ((sendMsgUsr) currentMessage).getMessageId());
                        buildMessage(errMsg, targetOutputStream);
                    }
                    break;
                case (SENDMSGGRP):
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        sendMsgGrp messageToBeSent = (sendMsgGrp) currentMessage;
                        buildMessage(messageToBeSent, clientConnection.sOutput);
                    }
                    break;
                default:
                    parent.display("The Queue had sth in it - sth unknown *scary noises*");
                    break;
            }
        }
    }

    public void doUpdateClients() {
        for (ClientConnectionListener ccl : parent.getListenerHashMap().values()) {
            updateClient updateClient = new updateClient(ccl.socket.getInetAddress(), ccl.socket.getPort(), parent.getConnectedClients());
            buildMessage(updateClient, ccl.sOutput);
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
