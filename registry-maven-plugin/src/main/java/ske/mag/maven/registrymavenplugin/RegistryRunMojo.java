package ske.mag.maven.registrymavenplugin;

import org.apache.maven.plugins.annotations.Mojo;


/**
 * Kjør oppslagstjenesten.
 */
@Mojo(name="run", requiresDirectInvocation=true)
public class RegistryRunMojo extends AbstractRegistryMojo {

    @Override
    protected void executeRegistryServer(RegistryServerRunner registryRunner) {
        System.setProperty("sikkerhet.utsted.saml.url", utstedSamlUrl);
        System.setProperty("sikkerhet.valider.saml.url", validerSamlUrl);
        System.setProperty("sikkerhet.sts.rest.sikkerhetspolicy", policy);
        registryRunner.run();
        System.clearProperty("sikkerhet.utsted.saml.url");
        System.clearProperty("sikkerhet.valider.saml.url");
        System.clearProperty("sikkerhet.sts.rest.sikkerhetspolicy");
    }
}
