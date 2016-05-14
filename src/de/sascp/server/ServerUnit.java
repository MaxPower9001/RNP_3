package de.sascp.server;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.util.MessageBuilder;

import static de.sascp.protocol.Specification.REQFINDSERVER;

/**
 * Created by Rene on 11.05.2016.
 */
class ServerUnit implements Runnable {
    private final Server parent;

    public ServerUnit(Server server) {
        this.parent = server;
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
                case (REQFINDSERVER):
                    answerRequestFindServer(currentMessage);
            }
        }
    }

    private void answerRequestFindServer(ChatMessage currentMessage) {
        MessageBuilder.buildMessage(new resFindServer(currentMessage.getSourceIP(), currentMessage.getSourcePort()), parent.getUdpServer().getSocket());
    }
}
