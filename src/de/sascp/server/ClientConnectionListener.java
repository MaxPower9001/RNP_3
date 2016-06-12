package de.sascp.server;


import com.sun.nio.sctp.SctpChannel;
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
import java.nio.ByteBuffer;
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
    //final Socket socket;
    SctpChannel channel;
    // my unique id (easier for deconnection)
    final int id;
    private final Server parent;
    //InputStream sInput;
    //OutputStream sOutput;
    // the Username of the Client
    String username = "";
    Timer sendHB;
    Timer checkHB;
    private boolean keepGoing = true;
    private boolean[] hbReceived = {false};
    reqHeartbeat reqHeartbeat;



    // Constructore
    ClientConnectionListener(SctpChannel channel, Server parent) {
        InetAddress targetIP = Utility.getRemoteAddress(channel);
        InetAddress sourceIP = Utility.getLocalAddress(channel);
        int targetPort = Utility.getRemotePort(channel);
        int sourcePort = Utility.getLocalPort(channel);

        reqHeartbeat = new reqHeartbeat(targetIP, targetPort, sourceIP, sourcePort);
        this.parent = parent;
        // a unique id
        id = ++Server.uniqueId;
        this.channel = channel;
            /* Creating both Data Stream */
/*        System.out.println("Thread trying to create Object Input/Output Streams");
        try {
            // create output first
            sOutput = socket.getOutputStream();
            sInput = socket.getInputStream();
        } catch (IOException e) {
            parent.display("Exception creating new Input/output Streams: " + e);
            return;
        }*/
    }

    private void startSendCheckHB(final SctpChannel channel, final Server parent, final reqHeartbeat reqHeartbeat) {
        sendHB = new Timer("sendHBCCL");
        sendHB.schedule(new TimerTask() {
            @Override
            public void run() {
//                try {
                    MessageBuilder.buildMessage(reqHeartbeat, channel);
//                } catch (SocketException e) {
//                    parent.display("Heartbeat failed to: " + Utility.getRemoteAddress(channel) + ":" + Utility.getRemotePort(channel));
//                    sendHB.cancel();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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
                    ByteBuffer headerByteBuffer = ByteBuffer.allocate(headerBytes.length);
                    System.out.println(channel.receive(headerByteBuffer, null, null));
                    headerByteBuffer.flip();
                    headerByteBuffer.get(headerBytes);
                    headerByteBuffer.clear();
                    //sInput.read(headerBytes);
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
                ByteBuffer buff;
                if (messageType == UPDATECLIENT) {
                    // TODO Updateclient List einlesen bitte danke
                } else if (length >= 0) {
                    byte[] payload = new byte[length];
                    try {
                        buff = ByteBuffer.allocate(payload.length);
                        channel.receive(buff, null, null);
                        buff.flip();
                        buff.get(payload);
                        buff.clear();
                        //sInput.read(payload);
                    } catch (IOException e) {
                        // TODO connection failed
                        break;
                    }
                    switch (messageType) {
                        case (REQLOGIN):
                            this.username = new String(payload, Charset.forName(CHARSET));
                            if (username.length() == 0 || parent.getListenerHashMap().containsKey(username)) {
                                parent.display("Login Request from: " + Utility.getRemoteAddress(channel).getHostAddress() + ":" + Utility.getRemotePort(channel) + " -> " + username);
                                parent.display("But his username was already taken - poor fella...");
                                this.username = null;
                            } else {
                                parent.getListenerHashMap().put(username, this);
                                parent.display(username + " added to Clients");
                                if (parent.incomingMessageQueue.offer(new reqLogin(Utility.getRemoteAddress(channel), username, Utility.getRemotePort(channel)))) {
                                    parent.display("Login Request from: " + Utility.getRemoteAddress(channel).getHostAddress() + ":" + Utility.getRemotePort(channel) + " -> " + username);
                                    startSendCheckHB(channel,parent,reqHeartbeat);
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
                            InetAddress sourceIP = Utility.getRemoteAddress(channel);
                            int sourcePort = Utility.getRemotePort(channel);
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
/*        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        }
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }*/
        try {
            if (channel != null) channel.close();
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
