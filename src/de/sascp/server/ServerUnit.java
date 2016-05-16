package de.sascp.server;

import de.sascp.message.ChatMessage;
import de.sascp.message.subTypes.reqLogin;
import de.sascp.message.subTypes.resFindServer;
import de.sascp.message.subTypes.updateClient;

import static de.sascp.protocol.Specification.REQFINDSERVER;
import static de.sascp.protocol.Specification.REQLOGIN;
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
                    if (parent.getListenerHashMap().keySet().contains(((reqLogin) currentMessage).getUsername())) {
                        break;
                    } else {
                        for (ClientConnectionListener ccl : parent.getListenerHashMap().values()) {
                            updateClient updateClient = new updateClient(ccl.socket.getInetAddress(), ccl.socket.getPort(), parent.getConnectedClients());
                            buildMessage(updateClient, ccl.sOutput);
                        }
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
