package de.sascp.client;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.resHeartbeat;
import de.sascp.util.MessageBuilder;

import java.io.OutputStream;
import java.net.InetAddress;

import static de.sascp.protocol.Specification.REQHEARTBEAT;
import static de.sascp.protocol.Specification.RESFINDSERVER;

/**
 * Converts byte[] Streams checked by the Protocol Parser into ChatMessage Objects and answers Heartbeat-Requests
 */
class IncomingMessageHandler implements Runnable {

    private final Client parent;

    public IncomingMessageHandler(Client parent) {
        this.parent = parent;
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
        while (true) {
            ChatMessage currentMessage = null;
            try {
                currentMessage = parent.incomingMessageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (currentMessage.getMessageType()) {
                case (REQHEARTBEAT):
                    answerHeartbeat(parent.socket.getInetAddress(), parent.socket.getPort(), parent.sOutput);
                    break;
                case (RESFINDSERVER):
                    parent.incomingResFindServer.offer((resFindServer) currentMessage);
                    break;
            }
        }
    }
}
