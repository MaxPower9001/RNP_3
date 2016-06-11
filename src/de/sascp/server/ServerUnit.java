package de.sascp.server;

import com.sun.nio.sctp.SctpChannel;
import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;
import de.sascp.util.Utility;

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
                    SctpChannel targetChannel = null;
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        if (Utility.getRemoteAddress(clientConnection.channel).equals(currentMessage.getTargetIP()) &&
                                Utility.getRemotePort(clientConnection.channel) == (currentMessage.getTargetPort())) {
                            targetStillInClientList = true;
                            targetChannel = clientConnection.channel;
                        }
                    }
                    if (targetStillInClientList) { // target found, relay message to target
                        buildMessage((sendMsgUsr) currentMessage, targetChannel);
                        for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                            if (Utility.getRemoteAddress(clientConnection.channel).equals(currentMessage.getSourceIP()) &&
                                    Utility.getRemotePort(clientConnection.channel) == (currentMessage.getSourcePort())) {
                                targetChannel = clientConnection.channel;
                            }
                        }
                    } else {
                        for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                            if (Utility.getRemoteAddress(clientConnection.channel).equals(currentMessage.getSourceIP()) &&
                                    Utility.getRemotePort(clientConnection.channel) == (currentMessage.getSourcePort())) {
                                targetChannel = clientConnection.channel;
                            }
                        }
                        errorMsgNotDelivered errMsg = new errorMsgNotDelivered(
                                currentMessage.getTargetIP(),
                                currentMessage.getTargetPort(),
                                currentMessage.getSourceIP(),
                                currentMessage.getTargetPort(),
                                ((sendMsgUsr) currentMessage).getMessageId());
                        buildMessage(errMsg, targetChannel);
                    }
                    break;
                case (SENDMSGGRP):
                    for (ClientConnectionListener clientConnection : parent.getListenerHashMap().values()) {
                        sendMsgGrp messageToBeSent = (sendMsgGrp) currentMessage;
                        buildMessage(messageToBeSent, clientConnection.channel);
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
            updateClient updateClient = new updateClient(Utility.getRemoteAddress(ccl.channel), Utility.getRemotePort(ccl.channel), parent.getConnectedClients());
            buildMessage(updateClient, ccl.channel);
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
