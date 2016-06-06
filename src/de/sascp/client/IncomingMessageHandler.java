package de.sascp.client;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.OHSHIT;

/**
 * Converts byte[] Streams checked by the Protocol Parser into ChatMessage Objects and answers Heartbeat-Requests
 */
class IncomingMessageHandler implements Runnable {

    private final Client parent;
    private boolean keepGoing;

    public IncomingMessageHandler(Client parent) {
        this.parent = parent;
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
            assert currentMessage != null;
            switch (currentMessage.getMessageType()) {
                case (OHSHIT):
                    break;
                case (RESFINDSERVER):
                    parent.incomingResFindServer.offer((resFindServer) currentMessage);
                    break;
                case (UPDATECLIENT):
                    updateClient updateClient = (updateClient) currentMessage;
                    if (updateClient.getSourceIP().equals(parent.socket.getInetAddress())) {
                        parent.setConnectedClients(updateClient.getClientInformation());
                    }
                    break;
                case (SENDMSGUSR): {
                    sendMsgUsr curMess = (sendMsgUsr) currentMessage;
                    String sourceUserName = "";
                    for (ClientInformation client : parent.getConnectedClients()) {
                        if(curMess.getSourceIP().equals(client.getClientIP()) && curMess.getSourcePort() == client.getClientPort()) {
                            sourceUserName = client.getClientUsername();
                            break;
                        }
                    }
                    parent.display(sourceUserName + ": " + curMess.getMessage());
                    break;
                }
                case (SENDMSGGRP): {
                    sendMsgGrp curMess = (sendMsgGrp) currentMessage;
                    String sourceUserName = "";
                    for (ClientInformation client : parent.getConnectedClients()) {
                        if(curMess.getSourceIP().equals(client.getClientIP()) && curMess.getSourcePort() == client.getClientPort()) {
                            sourceUserName = client.getClientUsername();
                            break;
                        }
                    }
                    parent.display(sourceUserName + ": " + curMess.getMessage());
                    break;
                }
                case (ERRORMSGNOTDELIVERED): {
                    errorMsgNotDelivered curMess = (errorMsgNotDelivered) currentMessage;
                    parent.displayError(curMess.getMessageId());
                    break;
                }
                default:
                    parent.display("Nothing found - I guess nobody wants to talk to you...");
                    break;
            }
            if (!keepGoing) {
                parent.display("IMH has left the building!");
            }
        }
    }

    public void stopRunning() {
        this.keepGoing = false;
        parent.incomingMessageQueue.offer(new shitIsAboutToGoDownMsg());
    }
}
