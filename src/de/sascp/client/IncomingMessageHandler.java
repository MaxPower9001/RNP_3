package de.sascp.client;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.*;
import de.sascp.util.MessageBuilder;

import java.io.OutputStream;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.*;

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


    public static void handleIncomingMessage(ChatMessage chatMessage) {

        // TODO

    }

    /**
     * Directly answers Heartbeat Package with the given OutputStream
     *
     * @param outputStream - OutputStream needed to send the Message
     * @return returns true, if message send successfully. returns false,
     * if IOException thrown
     */
    private boolean answerHeartbeat(InetAddress targetIP, int targetPort, OutputStream outputStream) {
        return MessageBuilder.buildMessage(new resHeartbeat(targetIP, targetPort), outputStream);
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
                case (REQHEARTBEAT):
                    answerHeartbeat(parent.socket.getInetAddress(), parent.socket.getPort(), parent.sOutput);
                    break;
                case (RESFINDSERVER):
                    parent.incomingResFindServer.offer((resFindServer) currentMessage);
                    break;
                case (UPDATECLIENT):
                    updateClient updateClient = (updateClient) currentMessage;
                    if (updateClient.getSourceIP().equals(parent.socket.getInetAddress())) {
                        parent.setConnectedClients(updateClient.getClientInfomartion());
                    }
                    break;
                case (SENDMSGUSR): {
                    sendMsgUsr curMess = (sendMsgUsr) currentMessage;
                    String sourceUserName = "";
                    for (ClientInfomartion client : parent.getConnectedClients()) {
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
                    for (ClientInfomartion client : parent.getConnectedClients()) {
                        if(curMess.getSourceIP().equals(client.getClientIP()) && curMess.getSourcePort() == client.getClientPort()) {
                            sourceUserName = client.getClientUsername();
                            break;
                        }
                    }
                    parent.display(sourceUserName + ": " + curMess.getMessage());
                    break;
                }
                default:
                    parent.display("Nothing found - I guess nobody wants to talk to you...");
                    break;
            }
        }
    }

    public void stopRunning() {
        this.keepGoing = false;
    }
}
