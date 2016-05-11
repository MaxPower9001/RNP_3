package de.sascp.server;

/**
 * Created by Rene on 11.05.2016.
 */
public class ServerUnit {


    case(REQFINDSERVER):
            parent.incomingMessageQueue.add(new

    reqFindServer(parent.socket.getInetAddress(),parent

    .socket.getPort()))
    parent.notify()
    break
}
