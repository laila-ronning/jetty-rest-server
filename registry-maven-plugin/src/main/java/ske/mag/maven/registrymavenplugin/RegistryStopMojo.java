package ske.mag.maven.registrymavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


/**
 * Stop oppslagstjenesten startet igjennom registry:start.
 */
@Mojo(name="stop", defaultPhase=LifecyclePhase.POST_INTEGRATION_TEST)
public class RegistryStopMojo extends AbstractRegistryMojo {

    @Override
    protected void executeRegistryServer(RegistryServerRunner registryRunner) {
        if(!registryRunner.isRunning()) {
            getLog().warn("Tjenesteregisteret er ikke startet i dette bygget");
        }   
        registryRunner.stop();
        System.clearProperty("sikkerhet.utsted.saml.url");
        System.clearProperty("sikkerhet.valider.saml.url");
        System.clearProperty("sikkerhet.sts.rest.sikkerhetspolicy");
    }
}
