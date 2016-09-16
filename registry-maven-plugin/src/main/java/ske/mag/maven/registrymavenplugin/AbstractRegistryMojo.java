package ske.mag.maven.registrymavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractRegistryMojo
        extends AbstractMojo {

    private static final String DEFAULT_PORT = "20000";

    @Parameter(property = "registry.port", defaultValue = DEFAULT_PORT)
    private int port;

    @Parameter(property = "sikkerhet.utsted.saml.url", defaultValue = "http://xntg126.skead.no:30100/felles/sikkerhet/stsSikkerhet/v2/utstedSaml")
    protected String utstedSamlUrl;

    @Parameter(property = "sikkerhet.valider.saml.url", defaultValue = "http://xntg126.skead.no:30100/felles/sikkerhet/stsSikkerhet/v2/validerSaml")
    protected String validerSamlUrl;

    @Parameter(property = "sikkerhet.sts.rest.sikkerhetspolicy", defaultValue = "deaktivert")
    protected String policy;

    @Override
    public void execute() throws MojoExecutionException {
        System.setProperty("sikkerhet.utsted.saml.url", utstedSamlUrl);
        System.setProperty("sikkerhet.valider.saml.url", validerSamlUrl);
        System.setProperty("sikkerhet.sts.rest.sikkerhetspolicy", policy);
        RegistryServerRunner registryRunner = new RegistryServerRunner(port);
        executeRegistryServer(registryRunner);

    }

    protected abstract void executeRegistryServer(RegistryServerRunner registryRunner);
}
