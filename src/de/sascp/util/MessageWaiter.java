package de.sascp.util;

import de.sascp.message.ChatMessage;

public class MessageWaiter implements Runnable {
    ChatMessage waitingFor;

    public MessageWaiter(ChatMessage chatMessage) {
        this.waitingFor = chatMessage;
    }

    @Override
    public void run() {
        while (true) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
