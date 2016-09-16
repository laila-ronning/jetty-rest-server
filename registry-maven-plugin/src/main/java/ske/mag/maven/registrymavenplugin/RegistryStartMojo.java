package ske.mag.maven.registrymavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


/**
 * Start oppslagstjenesten til bruk i test.
 */
@Mojo(name="start", defaultPhase=LifecyclePhase.PRE_INTEGRATION_TEST)
public class RegistryStartMojo extends AbstractRegistryMojo {
    
    @Override
    protected void executeRegistryServer(RegistryServerRunner registryRunner) {
        System.setProperty("sikkerhet.utsted.saml.url", utstedSamlUrl);
        System.setProperty("sikkerhet.valider.saml.url", validerSamlUrl);
        System.setProperty("sikkerhet.sts.rest.sikkerhetspolicy", policy);
        registryRunner.fork();
    }
}
