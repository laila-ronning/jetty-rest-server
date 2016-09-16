package ske.registry.klient;

import java.net.URI;
import java.util.Set;

import ske.registry.dto.RegistreringDTO;

public class RegistryKlientKonfig {
    private final Set<String> obligatoriskeOppslag;
    private final RegistreringDTO tjenesteregistrering;
    private final boolean feilHvisRegistreringFeiler;
    private final boolean autoRefreshCachedeOppslag;
    private final String overstyringProperty;
    private final long registreringIntervall;
    private final long timerStartDelay;
    private final long cacheRefreshIntervall;
    private final URI serverURI;

    public RegistryKlientKonfig(Set<String> obligatoriskeOppslag, RegistreringDTO tjenesteregistrering, boolean feilHvisRegistreringFeiler,
            boolean autoRefreshCachedeOppslag, String overstyringProperty, long registreringIntervall, long timerStartDelay,
            long cacheRefreshIntervall, URI serverURI) {
        this.obligatoriskeOppslag = obligatoriskeOppslag;
        this.tjenesteregistrering = tjenesteregistrering;
        this.feilHvisRegistreringFeiler = feilHvisRegistreringFeiler;
        this.autoRefreshCachedeOppslag = autoRefreshCachedeOppslag;
        this.overstyringProperty = overstyringProperty;
        this.registreringIntervall = registreringIntervall;
        this.timerStartDelay = timerStartDelay;
        this.cacheRefreshIntervall = cacheRefreshIntervall;
        this.serverURI = serverURI;
    }

    public Set<String> getObligatoriskeOppslag() {
        return obligatoriskeOppslag;
    }

    public RegistreringDTO getTjenesteregistrering() {
        return tjenesteregistrering;
    }

    public boolean isFeilHvisRegistreringFeiler() {
        return feilHvisRegistreringFeiler;
    }

    public boolean isAutoRefreshCachedeOppslag() {
        return autoRefreshCachedeOppslag;
    }

    public String getOverstyringProperty() {
        return overstyringProperty;
    }

    public long getRegistreringIntervall() {
        return registreringIntervall;
    }

    public long getTimerStartDelay() {
        return timerStartDelay;
    }

    public long getCacheRefreshIntervall() {
        return cacheRefreshIntervall;
    }

    public URI getServerURI() {
        return serverURI;
    }

}
