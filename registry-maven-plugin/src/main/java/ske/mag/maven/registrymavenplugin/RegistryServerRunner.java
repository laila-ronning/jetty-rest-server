package ske.mag.maven.registrymavenplugin;

import ske.registry.server.RegistryServer;

public class RegistryServerRunner {
    private final int port;
    private static Thread forkedRegistryServer;
    private RegistryServer server;

    public RegistryServerRunner(int port) {
        this.port = port;
    }

    public void run() {
        try {
            server = new RegistryServer(port);
            server.kjoerServer();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void fork() {
        final RegistryServerRunner self = this;
        forkedRegistryServer = new Thread(new Runnable() {

            @Override
            public void run() {
                self.run();
            }
        });
        forkedRegistryServer.start();
    }
    
    public void stop() {
        server.stopp();
    }

    public boolean isRunning() {
        return forkedRegistryServer != null;
    }
}
