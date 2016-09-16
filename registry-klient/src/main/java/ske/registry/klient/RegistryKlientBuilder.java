package ske.registry.klient;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.sun.jersey.api.client.WebResource;

import ske.registry.dto.RegistreringDTO;

public class RegistryKlientBuilder {
    private URI serverURI;
    private Set<String> obligatoriskeOppslag = new HashSet<>();
    private RegistreringDTO tjenesteregistrering;
    private boolean feilHvisRegistreringFeiler;
    private boolean autoRefreshCachedeOppslag = true;
    private String overstyringProperty = "true";

    public static final long STANDARD_REGISTRERING_INTERVALL = 10000;
    public static final long STANDARD_CACHE_REFRESH_INTERVALL = 10000;
    public static final long STANDARD_TIMER_START_DELAY = 5000;

    private long registreringIntervall = STANDARD_REGISTRERING_INTERVALL;
    private long cacheRefreshIntervall = STANDARD_CACHE_REFRESH_INTERVALL;
    private long timerStartDelay = STANDARD_TIMER_START_DELAY;
    private WebResource webResource;

    public RegistryKlientBuilder(URI serverURI) {
        this.serverURI = serverURI;
    }

    public RegistryKlientBuilder(String serverURI) {
        this.serverURI = URI.create(serverURI);
    }

    public RegistryKlientBuilder(WebResource webResource) {
        this.webResource = webResource;
        this.serverURI = webResource.getURI();
    }

    public RegistryKlientBuilder tjenesteregistrering(RegistreringDTO registrering) {
        this.tjenesteregistrering = registrering;
        return this;
    }

    public RegistryKlientBuilder feilHvisRegistreringFeiler(boolean verdi) {
        this.feilHvisRegistreringFeiler = verdi;
        return this;
    }

    public RegistryKlientBuilder obligatoriskOppslag(String urn) {
        checkNotNull(urn, "Obligatorisk oppslag kan ikke være null.");
        obligatoriskeOppslag.add(urn);
        return this;
    }

    public RegistryKlientBuilder overstyringProperty(String overstyringProperty) {
        this.overstyringProperty = overstyringProperty;
        return this;
    }

    public RegistryKlientBuilder autoRefreshCachedeOppslag(boolean verdi) {
        this.autoRefreshCachedeOppslag = verdi;
        return this;
    }

    public RegistryKlientBuilder registreringIntervall(long millisekunder) {
        this.registreringIntervall = millisekunder;
        return this;
    }

    public RegistryKlientBuilder timerStartDelay(long millisekunder) {
        this.timerStartDelay = millisekunder;
        return this;
    }

    public RegistryKlientBuilder cacheRefreshIntervall(long millisekunder) {
        this.cacheRefreshIntervall = millisekunder;
        return this;
    }

    public RegistryKlientKonfig byggKonfigurasjon() {
        checkNotNull(serverURI, "Server URI kan ikke være null.");
        checkNotNull(overstyringProperty, "overstyringProperty kan ikke overtyres med null verdi");
        return new RegistryKlientKonfig(obligatoriskeOppslag, tjenesteregistrering, feilHvisRegistreringFeiler, autoRefreshCachedeOppslag,
                overstyringProperty, registreringIntervall, timerStartDelay, cacheRefreshIntervall, serverURI);
    }

    public RegistryKlient byggRegistryKlient() {
        RegistryKlientKonfig konfig = byggKonfigurasjon();
        if (konfig.getServerURI() != null && "file".equalsIgnoreCase(konfig.getServerURI().getScheme())) {
            return new RegistryKlientFakeImpl(konfig);
        }
        return new RegistryKlientImpl(konfig, webResource);
    }

}
