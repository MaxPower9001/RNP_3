package de.sascp.server;


import de.sascp.message.subTypes.reqHeartbeat;
import de.sascp.message.subTypes.reqLogin;
import de.sascp.message.subTypes.sendMsgGrp;
import de.sascp.message.subTypes.sendMsgUsr;
import de.sascp.util.MessageBuilder;
import de.sascp.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import static de.sascp.protocol.Specification.*;
import static de.sascp.util.Utility.*;

/**
 * One instance of this thread will run for each client
 */
class ClientConnectionListener implements Runnable {
    // the socket where to listen/talk
    final Socket socket;
    // my unique id (easier for deconnection)
    final int id;
    private final Server parent;
    InputStream sInput;
    OutputStream sOutput;
    // the Username of the Client
    String username = "";
    Timer sendHB;
    Timer checkHB;
    private boolean keepGoing = true;
    private boolean[] hbReceived = {false};



    // Constructore
    ClientConnectionListener(Socket socket, Server parent) {
        InetAddress targetIP = socket.getInetAddress();
        InetAddress sourceIP = socket.getLocalAddress();
        int targetPort = socket.getPort();
        int sourcePort = socket.getLocalPort();

        de.sascp.message.subTypes.reqHeartbeat reqHeartbeat = new reqHeartbeat(targetIP, targetPort, sourceIP, sourcePort);
        this.parent = parent;
        // a unique id
        id = ++Server.uniqueId;
        this.socket = socket;
            /* Creating both Data Stream */
        System.out.println("Thread trying to create Object Input/Output Streams");
        try {
            // create output first
            sOutput = socket.getOutputStream();
            sInput = socket.getInputStream();
        } catch (IOException e) {
            parent.display("Exception creating new Input/output Streams: " + e);
            return;
        }
//        resHeartbeat = new LinkedBlockingQueue<>();
//        heartbeat = new Heartbeat(this,resHeartbeat);
//        new Thread(heartbeat).start();
        sendHB = new Timer("sendHBCCL");
        sendHB.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    MessageBuilder.buildMessage(reqHeartbeat, sOutput);
                } catch (SocketException e) {
                    parent.display("Heartbeat failed to: " + socket.getInetAddress() + ":" + socket.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
        checkHB = new Timer("checkHBCCL");
        checkHB.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hbReceived[0] == false) {
                    goKillYourself();
                } else {
                    hbReceived[0] = false;
                }
            }
        }, 1000, TIMEOUT);
    }

    // what will run forever
    public void run() {
        boolean lookingForCommonHeader;
        boolean lookingForPayload;

        int version;
        int messageType;
        int length;



        // to loop until LOGOUT
        while (keepGoing) {
            lookingForCommonHeader = true;
            lookingForPayload = true;

            messageType = -1;
            length = -1;

            while (lookingForCommonHeader) {
                byte[] headerBytes = new byte[CHLENGTH];
                try {
                    sInput.read(headerBytes);
                } catch (IOException e) {
                    // TODO connection failed
                    break;
                }

                version = intFromFourBytes(headerBytes, HEADERVERSIONOFFSET, 4); // Version number
                messageType = intFromFourBytes(headerBytes, HEADERTYPEOFFSET, 4); // MessageType
                length = intFromFourBytes(headerBytes, HEADERLENGTHOFFSET, 4);  // Length

                if (checkCommonHeader(version, messageType, length)) {
                    lookingForCommonHeader = false;
                }
            }
            while (lookingForPayload) {
                if (messageType == UPDATECLIENT) {
                    // TODO Updateclient List einlesen bitte danke
                } else if (length >= 0) {
                    byte[] payload = new byte[length];
                    try {
                        sInput.read(payload);
                    } catch (IOException e) {
                        // TODO connection failed
                        break;
                    }
                    switch (messageType) {
                        case (REQLOGIN):
                            this.username = new String(payload, Charset.forName(CHARSET));
                            if (username.length() == 0 || parent.getListenerHashMap().containsKey(username)) {
//                                keepGoing = false;
                                parent.display("Login Request from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " -> " + username);
                                parent.display("But his username was already taken - poor fella...");
                            } else {
                                parent.getListenerHashMap().put(username, this);
                                if (parent.incomingMessageQueue.offer(new reqLogin(socket.getInetAddress(), username, socket.getPort()))) {
                                    parent.display("Login Request from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " -> " + username);
                                } else {
                                    parent.display("I was not able to put reqLogin into incoming message queue - forigve me senpai!");
                                }
                            }
                            break;
                        case (SENDMSGUSR):
                            int usrTextMessageId = Utility.intFromFourBytes(payload,0,4);
                            try {
                                InetAddress sourceIp = InetAddress.getByAddress(Utility.getByteArrayFragment(payload,4,4));
                                InetAddress targetIp = InetAddress.getByAddress(Utility.getByteArrayFragment(payload,8,4));
                                int sourcePort = Utility.intFromTwoBytes(payload,12);
                                int targetPort = Utility.intFromTwoBytes(payload,14);
                                String usrTextMessage = new String(Utility.getByteArrayFragment(payload,16,length - 16),Charset.forName(CHARSET));

                                sendMsgUsr message = new sendMsgUsr(targetIp, targetPort, sourceIp, sourcePort,usrTextMessageId, usrTextMessage);
                                parent.incomingMessageQueue.offer(message);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            break;
                        case (SENDMSGGRP):
                            int grpTextMessageId = Utility.intFromFourBytes(payload,0,4);
                            try {
                                InetAddress sourceIp = Inet4Address.getByAddress(Utility.getByteArrayFragment(payload,4,4));
                                InetAddress targetIp = Inet4Address.getByAddress(Utility.getByteArrayFragment(payload,8,4));
                                int sourcePort = Utility.intFromTwoBytes(payload,12);
                                int targetPort = Utility.intFromTwoBytes(payload,14);
                                String usrTextMessage = new String(Utility.getByteArrayFragment(payload,16,length - 16),Charset.forName(CHARSET));

                                sendMsgGrp message = new sendMsgGrp(targetIp, targetPort, sourceIp, sourcePort,grpTextMessageId, usrTextMessage);
                                parent.incomingMessageQueue.offer(message);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            break;
                        case (RESHEARTBEAT):
                            InetAddress sourceIP = socket.getInetAddress();
                            int sourcePort = socket.getPort();
                            parent.display("resHeartbeat: " + sourceIP + "|" + sourcePort);
                            hbReceived[0] = true;
                            break;
                        default:
                            parent.display("This is Microsoft Sam the Servers default switch-bracket");
                            break;
                    }
                }
                lookingForPayload = false;
            }
        }
    }

    // try to close everything
    private void close() {
        // try to close the connection
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        }
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }


    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }

    public void goKillYourself() {
        sendHB.cancel();
        checkHB.cancel();
        close();
        setKeepGoing(false);
        parent.remove(this);
    }
}
