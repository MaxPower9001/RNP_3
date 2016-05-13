package de.sascp.server;

/**
 * Created by Rene on 11.05.2016.
 */
class ServerUnit implements Runnable {
    private final ClientConnectionListener parent;

    public ServerUnit(ClientConnectionListener server) {
        this.parent = server;
    }

    @Override
    public void run() {

    }
}
