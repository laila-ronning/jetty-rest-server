package ske.registry.klient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.OppslagMedBeskrivelseDTO;
import ske.registry.dto.admin.PulsDTO;

class RegistryRestKlient {

    private static final Logger logger = LoggerFactory.getLogger(RegistryRestKlient.class);

    private final WebResource serverResource;
    private final WebResource registreringServerResource;
    private final boolean feilHvisRegistreringFeiler;
    private final OverstyringRepository overstyringRepository;
    private RegistryKlientImpl registryKlient;

    RegistryRestKlient(RegistryKlientImpl registryKlient, WebResource serverResource, WebResource registreringServerResource,
            boolean feilHvisRegistreringFeiler,
            OverstyringRepository overstyringRepository) {
        this.registryKlient = registryKlient;
        this.serverResource = serverResource;
        this.registreringServerResource = registreringServerResource;
        this.feilHvisRegistreringFeiler = feilHvisRegistreringFeiler;
        this.overstyringRepository = overstyringRepository;
    }

    void sendAvregistrering(RegistreringDTO tjenesteregistrering) {
        if (tjenesteregistrering != null) {
            logger.info("Sender stopp");
            PulsDTO dto = new PulsDTO();
            dto.setTilbyderId(tjenesteregistrering.getTilbyderId());
            try {
                serverResource.path("stopp").entity(dto, MediaType.APPLICATION_JSON).post();
            } catch (UniformInterfaceException | ClientHandlerException e) {
                logger.warn("Sending av stoppmelding til registry server {} feilet.", serverResource.getURI(), e);
            }
        }
    }

    Map<String, URI> finnTjenester(Collection<String> urnCollection) {
        Map<String, URI> uriMap = new HashMap<>();
        Map<String, Collection<TjenesteDTO>> res;
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        List<String> urnListe = new ArrayList<>();
        urnListe.addAll(urnCollection);
        queryParams.put("urnListe", urnListe);
        try {
            res = serverResource.path("liste").queryParams(queryParams).get(new GenericType<Map<String, Collection<TjenesteDTO>>>() {
            });
        } catch (UniformInterfaceException | ClientHandlerException e) {
            logger.warn("Oppslag mot registry server {} feilet for urnCollection {}.", serverResource.getURI(), urnCollection, e);
            return null;
        }
        if (res != null) {
            for (String urn : res.keySet()) {
                uriMap.put(urn, res.get(urn).iterator().next().getKlientUri());
            }
        }
        return uriMap;
    }

    URI finnTjeneste(String urn) {
        Set<URI> urier = finnAlleTjenesteURIer(urn);
        URI uri = null;
        if (urier.size() > 0)
            uri = urier.iterator().next();
        return uri;
    }

    Set<URI> finnAlleTjenesteURIer(String urn) {
        List<TjenesteDTO> res = null;
        LinkedHashSet<URI> tjenesteURIer = new LinkedHashSet<>();
        try {
            res = serverResource.path(urn).get(new GenericType<List<TjenesteDTO>>() {
            });
        } catch (UniformInterfaceException | ClientHandlerException e) {
            logger.warn("Oppslag mot registry server {} feilet for urn {}.", serverResource.getURI(), urn, e);
        }
        if (res != null) {
            for (TjenesteDTO t : res) {
                URI uri = t.getKlientUri();
                tjenesteURIer.add(uri);
            }
        }
        return tjenesteURIer;
    }

    boolean sendPuls(RegistreringDTO tjenesteregistrering) {
        if (tjenesteregistrering != null) {
            logger.trace("Sender puls");
            PulsDTO dto = new PulsDTO();
            dto.setTilbyderId(tjenesteregistrering.getTilbyderId());
            try {
                serverResource.path("puls").entity(dto, MediaType.APPLICATION_JSON).post();
                return true;
            } catch (UniformInterfaceException | ClientHandlerException e) {
                logger.warn("Sending av puls til registry server {} feilet.", serverResource.getURI(), e);
            }
        }
        return false;
    }

    boolean sendRegistrering(RegistreringDTO tjenesteregistrering) {
        boolean res = false;
        if (tjenesteregistrering != null) {
            try {
                registreringServerResource.entity(tjenesteregistrering, MediaType.APPLICATION_JSON).post();
                res = true;
            } catch (UniformInterfaceException | ClientHandlerException e) {
                if (feilHvisRegistreringFeiler) {
                    throw new RuntimeException("Registrering av tjenester mot registry server " + registreringServerResource.getURI()
                            + " feilet, og registryklient er konfigurert til å feile hvis dette skjer.", e);
                }
                logger.warn("Registrering av tjenester mot registry server {} feilet", registreringServerResource.getURI(), e);
            }
        }
        return res;
    }

    boolean sendKlientinfo(AtomicBoolean tjenesteregistreringRegistrert, Map<String, URI> cachedeOppslag,
            RegistreringDTO tjenesteregistrering, String helsetilstand) {
        if (tjenesteregistreringRegistrert.get()) {
            logger.debug("Sender klientinfo");
            KlientInfoDTO dto = new KlientInfoDTO();
            dto.setTilbyderId(tjenesteregistrering.getTilbyderId());

            List<OppslagMedBeskrivelseDTO> oppslag = new ArrayList<>();
            oppslag.addAll(Collections2.transform(cachedeOppslag.entrySet(),
                    new Function<Map.Entry<String, URI>, OppslagMedBeskrivelseDTO>() {
                        @Override
                        public OppslagMedBeskrivelseDTO apply(Map.Entry<String, URI> input) {
                            return new OppslagMedBeskrivelseDTO(input.getKey(), input.getValue(),
                                    "cachet oppslag fra oppslagstjeneste på " + registryKlient.restKlient.serverResource.getURI());
                        }
                    }));
            oppslag.addAll(Collections2.transform(overstyringRepository.getOverstyrteTjenester().entrySet(),
                    new Function<Map.Entry<String,
                    OverstyrUrnDTO>, OppslagMedBeskrivelseDTO>() {
                        @Override
                        public OppslagMedBeskrivelseDTO apply(Map.Entry<String, OverstyrUrnDTO> input) {
                            return new OppslagMedBeskrivelseDTO(input.getKey(), input.getValue().getUri(),
                                    "Fra filnavn: " + overstyringRepository.getFilnavnTilLogging()
                                            + ". Beskrivelse: " + input.getValue().getBeskrivelse());
                        }
                    }));

            dto.setOppslag(oppslag);
            dto.setHelsetilstand(helsetilstand);
            try {
                serverResource.path("klientinfo").entity(dto, MediaType.APPLICATION_JSON).post();
                return true;

            } catch (UniformInterfaceException e) {
                logger.warn(String.format("Sending av klientinfo til registry server %s feilet. Melding: %s",
                        serverResource.getURI(), e.getResponse().getEntity(String.class)), e);
            } catch (ClientHandlerException e) {
                logger.warn(String.format("Sending av klientinfo til registry server %s feilet.", serverResource.getURI()), e);
            }
        }
        return false;
    }

    Map<String, String> hentTjenesteAttributter(String urn) {
        try {
            return serverResource.path(urn).path("tjenesteattributter")
                    .get(new GenericType<Map<String, String>>() {
                    });
        } catch (UniformInterfaceException | ClientHandlerException e) {
            logger.warn("Henting av tjenesteattributter mot registry server {} feilet for urn {}.", serverResource.getURI(), urn, e);
            return null;
        }
    }

    void leggTilFilter(ClientFilter registreringKlientFilter) {
        registreringServerResource.addFilter(registreringKlientFilter);
    }
}
