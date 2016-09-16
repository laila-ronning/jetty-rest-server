package ske.registry.klient;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;

public class RegistryKlientImpl implements RegistryKlient {

    private static final Logger logger = LoggerFactory.getLogger(RegistryKlientImpl.class);

    public static final int STANDARD_READ_TIMEOUT = 1000;
    public static final int STANDARD_CONNECT_TIMEOUT = 1000;

    private final long registreringIntervall;
    private final long cacheRefreshIntervall;
    private final long timerStartDelay;
    private final OverstyringRepository overstyringRepository;
    private final boolean autoRefreshCachedeOppslag;
    private final Set<String> obligatoriskeOppslag;

    private RegistreringDTO tjenesteregistrering;
    private AtomicBoolean tjenesteregistreringRegistrert = new AtomicBoolean();
    private Timer cacheRefreshTimer;
    private Timer registreringTimer;
    private boolean startet;
    private String helsetilstand;

    RegistryRestKlient restKlient; // package private, tilgjengelig for test
    Map<String, URI> cachedeOppslag = new ConcurrentHashMap<>();

    /**
     * Bruk RegistryKlientBuilder for instansiering av Klienten
     */
    protected RegistryKlientImpl(RegistryKlientKonfig konfig, WebResource resource) {
        registreringIntervall = konfig.getRegistreringIntervall();
        cacheRefreshIntervall = konfig.getCacheRefreshIntervall();
        timerStartDelay = konfig.getTimerStartDelay();
        tjenesteregistrering = konfig.getTjenesteregistrering();
        autoRefreshCachedeOppslag = konfig.isAutoRefreshCachedeOppslag();
        obligatoriskeOppslag = konfig.getObligatoriskeOppslag();
        overstyringRepository = new OverstyringPropertiesReader(konfig.getOverstyringProperty());

        restKlient = new RegistryRestKlient(this, initialiserRestKlient(resource, konfig.getServerURI()),
                initialiserRestKlient(resource, konfig.getServerURI()), konfig.isFeilHvisRegistreringFeiler(), overstyringRepository);

        for (String urn : konfig.getObligatoriskeOppslag()) {
            URI uri = finnTjenesteURI(urn);
            if (uri == null) {
                logger.warn("RegistryKlient finner ikke URI for obligatorisk oppslag mot urn {}.", urn);
            }
        }
    }

    @Override
    public URI finnTjenesteURI(String urn) {
        URI uri = overstyringRepository.getOverstyrtTjeneste(urn);

        if (uri != null) {
            return uri;
        }

        uri = cachedeOppslag.get(urn);
        if (uri == null) {
            uri = restKlient.finnTjeneste(urn);
            if (uri != null) {
                cachedeOppslag.put(urn, uri);
                restKlient.sendKlientinfo(tjenesteregistreringRegistrert, cachedeOppslag, tjenesteregistrering, helsetilstand);
            }
        }
        return uri;
    }

    @Override
    public Set<URI> finnAlleTjenesteURIer(String urn) {
        Set<URI> urier = new LinkedHashSet<>();
        URI uri = overstyringRepository.getOverstyrtTjeneste(urn);

        if (uri != null) {
            urier.add(uri);
        } else {
            Set<URI> serverURIer = restKlient.finnAlleTjenesteURIer(urn);

            if (serverURIer != null && !serverURIer.isEmpty()) {
                urier.addAll(serverURIer);
            }
        }
        return urier;
    }

    @Override
    public Set<String> obligatoriskeOppslagSomMangler() {
        return ImmutableSet.copyOf(Sets.difference(obligatoriskeOppslag,
                Sets.union(cachedeOppslag.keySet(), overstyringRepository.getOverstyrteTjenester().keySet())));
    }

    @Override
    public void registrer(RegistreringDTO registrering) {
        this.tjenesteregistrering = registrering;
        this.helsetilstand = registrering.getHelsetilstand();
        if (startet) {
            restKlient.sendRegistrering(tjenesteregistrering);
        }
    }

    @Override
    public void avregistrer(String urn) {
        boolean fjernet = false;
        for (Iterator<TjenesteDTO> i = tjenesteregistrering.getTjenesteliste().iterator(); i.hasNext();) {
            TjenesteDTO dto = i.next();
            if (dto.getUrn().equals(urn)) {
                i.remove();
                fjernet = true;
                break;
            }
        }
        if (fjernet) {
            restKlient.sendRegistrering(tjenesteregistrering);
        }
    }

    public void slettCachedeOppslag() {
        cachedeOppslag.clear();
    }

    @Override
    public void start() {
        if (startet) {
            stopp();
        }
        startet = true;

        if (tjenesteregistrering != null) {
            startRegistrering();
            restKlient.sendKlientinfo(tjenesteregistreringRegistrert, cachedeOppslag, tjenesteregistrering, helsetilstand);
        }

        if (autoRefreshCachedeOppslag) {
            startCacheRefresh();
        }

    }

    @Override
    public void start(ClientFilter registreringKlientFilter) {
        restKlient.leggTilFilter(registreringKlientFilter);
        start();
    }

    @Override
    public void stopp() {
        startet = false;

        if (tjenesteregistrering != null) {
            restKlient.sendAvregistrering(tjenesteregistrering);
        }

        if (registreringTimer != null) {
            registreringTimer.cancel();
        }
        if (cacheRefreshTimer != null) {
            cacheRefreshTimer.cancel();
        }
    }

    private void startRegistrering() {
        tjenesteregistreringRegistrert.set(restKlient.sendRegistrering(tjenesteregistrering));

        boolean isDaemon = true;
        registreringTimer = new Timer("RegistryKlientTimer", isDaemon);
        registreringTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    registreringEllerPulsTask();
                } catch (Exception e) {
                    logger.warn("Timer registrering/puls feilet.", e);

                } catch (Throwable t) {
                    logger.error("Timer registrering/puls feilet med Throwable.", t);
                }
            }

        }, timerStartDelay, registreringIntervall);
    }

    void registreringEllerPulsTask() {
        if (!startet) {
            return;
        }
        if (!tjenesteregistreringRegistrert.get()) {
            tjenesteregistreringRegistrert.set(restKlient.sendRegistrering(tjenesteregistrering));
        } else {
            tjenesteregistreringRegistrert.set(restKlient.sendPuls(tjenesteregistrering));
        }
    }

    void cacheRefreshTask() {
        if (startet) {
            boolean endretInnhold = false;

            Set<String> oppslagViSkalProeve = ImmutableSet.copyOf(Sets.union(cachedeOppslag.keySet(), obligatoriskeOppslagSomMangler()));

            if (!oppslagViSkalProeve.isEmpty()) {
                Map<String, URI> oppslagViFinner = restKlient.finnTjenester(oppslagViSkalProeve);
                if (oppslagViFinner != null) {
                    for (Map.Entry<String, URI> oppslag : oppslagViFinner.entrySet()) {
                        URI gammelVerdi = cachedeOppslag.put(oppslag.getKey(), oppslag.getValue());
                        if (gammelVerdi == null) {
                            endretInnhold = true;
                        } else if (!gammelVerdi.equals(oppslag.getValue())) {
                            logger.trace("Fikk oppdatert verdi for cachet oppslag {} = {}, gammel verdi {}.", oppslag.getKey(),
                                    oppslag.getValue(),
                                    gammelVerdi);
                            endretInnhold = true;
                        }
                    }
                    for (String urn : cachedeOppslag.keySet()) {
                        if (oppslagViFinner.get(urn) == null) {
                            cachedeOppslag.remove(urn);
                            logger.warn(
                                    "Fikk null fra registry ved forsøk på refresh av cachet oppslag {} = {}, fjerner oppslag fra cache.",
                                    urn,
                                    cachedeOppslag.get(urn));
                            endretInnhold = true;
                        }
                    }

                    if (endretInnhold) {
                        restKlient.sendKlientinfo(tjenesteregistreringRegistrert, cachedeOppslag, tjenesteregistrering, helsetilstand);
                        Set oppslagSomMangler = obligatoriskeOppslagSomMangler();
                        if (!oppslagSomMangler.isEmpty()) {
                            logger.trace("Obligatoriske oppslag som mangler etter cache refresh: {}", oppslagSomMangler);
                        }
                    }
                }
            }
        }
    }

    private void startCacheRefresh() {
        boolean isDaemon = true;
        cacheRefreshTimer = new Timer("RegistryCacheRefreshTimer", isDaemon);
        cacheRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    cacheRefreshTask();
                } catch (Exception e) {
                    logger.warn("Cache refresh feilet.", e);
                } catch (Throwable t) {
                    logger.error("Cache refresh feilet med Throwable.", t);
                }
            }
        }, timerStartDelay, cacheRefreshIntervall);
    }

    @Override
    public void settHelsetilstand(String tilstandskode) {
        boolean endret = !StringUtils.equals(this.helsetilstand, tilstandskode);
        this.helsetilstand = tilstandskode;
        if (endret) {
            restKlient.sendKlientinfo(tjenesteregistreringRegistrert, cachedeOppslag, tjenesteregistrering, helsetilstand);
        }
    }

    @Override
    public Map<String, String> hentTjenesteAttributter(String urn) {
        return restKlient.hentTjenesteAttributter(urn);
    }

    public static Builder med(URI serverURI) {
        return new Builder(serverURI);
    }

    public static Builder med(String serverURI) {
        return med(URI.create(serverURI));
    }

    public static Builder med(WebResource serverResource) {
        return new Builder(serverResource);
    }

    /**
     * Bruk RegistryKlientBuilder og RegistryKlientBuilder#byggRegistryKlient()
     * 
     * @see RegistryKlientBuilder
     */
    @Deprecated
    public static final class Builder {

        private RegistryKlientBuilder konfigBuilder;

        public Builder(URI serverURI) {
            konfigBuilder = new RegistryKlientBuilder(serverURI);
        }

        public Builder(WebResource serverResource) {
            konfigBuilder = new RegistryKlientBuilder(serverResource);
        }

        public Builder tjenesteregistrering(RegistreringDTO registrering) {
            konfigBuilder.tjenesteregistrering(registrering);
            return this;
        }

        public Builder feilHvisRegistreringFeiler(boolean verdi) {
            konfigBuilder.feilHvisRegistreringFeiler(verdi);
            return this;
        }

        public Builder obligatoriskOppslag(String urn) {
            konfigBuilder.obligatoriskOppslag(urn);
            return this;
        }

        public Builder autoRefreshCachedeOppslag(boolean verdi) {
            konfigBuilder.autoRefreshCachedeOppslag(verdi);
            return this;
        }

        public Builder timerStartDelay(long millisekunder) {
            konfigBuilder.timerStartDelay(millisekunder);
            return this;
        }

        public Builder registreringIntervall(long millisekunder) {
            konfigBuilder.registreringIntervall(millisekunder);
            return this;
        }

        public Builder overstyringProperty(String overstyringProperty) {
            konfigBuilder.overstyringProperty(overstyringProperty);
            return this;
        }

        public Builder cacheRefreshIntervall(long millisekunder) {
            konfigBuilder.cacheRefreshIntervall(millisekunder);
            return this;
        }

        public RegistryKlient bygg() {
            return konfigBuilder.byggRegistryKlient();
        }

    }

    private WebResource initialiserRestKlient(WebResource serverResource, URI serverURI) {
        if (serverResource == null) {
            ClientConfig cc = new DefaultClientConfig();
            cc.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, STANDARD_CONNECT_TIMEOUT);
            cc.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, STANDARD_READ_TIMEOUT);
            cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

            return Client.create(cc).resource(serverURI);
        }
        return serverResource;
    }

}
