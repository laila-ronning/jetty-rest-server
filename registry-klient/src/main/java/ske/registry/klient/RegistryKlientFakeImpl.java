package ske.registry.klient;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.filter.ClientFilter;
import mag.felles.konfigurasjon.STSTjenesteKonstanter;
import mag.felles.konfigurasjon.SikkerhetspolicyOppslagstjeneste;
import mag.felles.ressurs.Apphome;
import mag.felles.ressurs.Lastekjede;
import mag.felles.ressurs.MagRessurslaster;
import mag.felles.ressurs.Ressurs;
import mag.felles.sikkerhet.rest.filter.Sikkerhetspolicy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.mag.felles.exception.MagnetApplicationExceptionBuilder;
import ske.registry.dto.RegistreringDTO;

public class RegistryKlientFakeImpl implements RegistryKlient {
    private static final Logger logger = LoggerFactory.getLogger(RegistryKlientFakeImpl.class);

    private SikkerhetspolicyOppslagstjeneste sikkerhetspolicy = SikkerhetspolicyOppslagstjeneste.DEAKTIVERT;
    private Map<String, URI> oppslag = new HashMap<>();

    public RegistryKlientFakeImpl(RegistryKlientKonfig konfig) {
        String stubbetOppslagstjenestefil = konfig.getServerURI().getAuthority();

        if (stubbetOppslagstjenestefil == null) {
            return;
        }

        int plasseringForExtension = stubbetOppslagstjenestefil.lastIndexOf('.');
        if (plasseringForExtension < 1) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FEIL_FILTYPE_STUBBING).build();
        }

        String stubbetPrefix = stubbetOppslagstjenestefil.substring(0, plasseringForExtension);
        String stubbetExtension = stubbetOppslagstjenestefil.substring(plasseringForExtension + 1);

        if (!stubbetExtension.equalsIgnoreCase("properties")) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FEIL_FILTYPE_STUBBING).build();
        }

        MagRessurslaster stubbetRessurs = new MagRessurslaster(finnAppHome(), null, Lastekjede.KONFIG);
        Ressurs lastRessurs = stubbetRessurs.lastRessurs(stubbetPrefix, stubbetExtension);

        if (lastRessurs == null) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FINNER_IKKE_FIL_FOR_STUBBING).build();
        } else {
            try (Reader reader = lastRessurs.erFilBasert() ? new FileReader(lastRessurs.getFile()) : new InputStreamReader(
                    lastRessurs.getInputStream());) {

                Properties props = new Properties();
                props.load(reader);

                for (String urn : props.stringPropertyNames()) {
                    String verdi = props.getProperty(urn);
                    if (StringUtils.isBlank(verdi)) {
                        logger.error("Feil format i stubfil for nøkkel: {}.", urn);
                        continue;
                    }
                    if (STSTjenesteKonstanter.STS_SIKKERHETSPOLICY_TJENESTEATTRIBUTT.equals(urn)) {
                        sikkerhetspolicy = SikkerhetspolicyOppslagstjeneste.valueOf(verdi.toUpperCase());
                    } else {
                        oppslag.put(urn, new URI(verdi));
                    }
                }
            } catch (Exception e) {
                logger.error("Feil format i stubbet oppslagstjenestefil.", e);
            }
        }
    }

    private String finnAppHome() {
        if (System.getProperty("app.homepath.magnet") != null || System.getProperty("app.homepath") != null) {
            return Apphome.getGlassfishApphome();
        }
        return Apphome.getStandaloneApphome();
    }

    @Override
    public URI finnTjenesteURI(String urn) {
        return oppslag.get(urn);
    }

    @Override
    public Set<URI> finnAlleTjenesteURIer(String urn) {
        Set<URI> urier = new LinkedHashSet<>();
        URI uri = oppslag.get(urn);

        if (uri != null) {
            urier.add(uri);
        }
        return urier;
    }

    @Override
    public Set<String> obligatoriskeOppslagSomMangler() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> hentTjenesteAttributter(String urn) {
        if (STSTjenesteKonstanter.URN_STS_UTSTED_SAML.equals(urn) || STSTjenesteKonstanter.URN_STS_VALIDER_SAML.equals(urn)) {
            return ImmutableMap.of(STSTjenesteKonstanter.STS_SIKKERHETSPOLICY_TJENESTEATTRIBUTT, sikkerhetspolicy.getValue());
        }
        return Maps.newHashMap();
    }

    @Override
    public void registrer(RegistreringDTO registrering) {
    }

    @Override
    public void avregistrer(String urn) {
    }

    @Override
    public void start(ClientFilter registreringKlientFilter) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stopp() {
    }

    @Override
    public void settHelsetilstand(String tilstandskode) {
    }

}
