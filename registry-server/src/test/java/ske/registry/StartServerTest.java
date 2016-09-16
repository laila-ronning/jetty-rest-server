package ske.registry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Integrasjonstest;
import ske.registry.server.RegistryServer;

@Category(Integrasjonstest.class)
public class StartServerTest {

    @Test
    public void skalKunneStarteServer() throws Exception {
        final RegistryServer registryServer = new RegistryServer(RegistryServer.DEFAULT_HTTP_PORT);
        final AtomicBoolean harFeil = new AtomicBoolean(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    registryServer.kjoerServer();
                } catch (Exception e) {
                    e.printStackTrace();
                   harFeil.set(true);
                }
            }
        });
        thread.start();

        Thread.sleep(2000);
        assertThat(thread.isAlive(), is(true));
        assertThat(harFeil.get(), is(false));
    }

}
