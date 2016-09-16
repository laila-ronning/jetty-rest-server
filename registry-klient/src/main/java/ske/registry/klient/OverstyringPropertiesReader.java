package ske.registry.klient;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import mag.felles.ressurs.Apphome;
import mag.felles.ressurs.Lastekjede;
import mag.felles.ressurs.MagRessurslaster;
import mag.felles.ressurs.Ressurs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.mag.felles.exception.MagnetApplicationExceptionBuilder;

public class OverstyringPropertiesReader implements OverstyringRepository {
    public static final String OVERSTYRTE_TJENESTER_FILNAVN = "overstyrte-tjenester.properties";

    private static final Logger logger = LoggerFactory.getLogger(OverstyringPropertiesReader.class);

    private final String overstyringProperty;

    private Map<String, OverstyrUrnDTO> overstyrteTjenester = new HashMap<>();

    private String filnavnTilLogging;

    public OverstyringPropertiesReader(String overstyringProperty) {
        this.overstyringProperty = overstyringProperty;

        try {
            lagMapForOverstyrtTjeneste();
        } catch (IOException e) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FEIL_LESING_AV_TJENESTEOVERSTYRING).aarsak(e)
                    .build();
        }
    }

    @Override
    public Map<String, OverstyrUrnDTO> getOverstyrteTjenester() {
        return overstyrteTjenester;
    }

    @Override
    public String getFilnavnTilLogging() {
        return filnavnTilLogging;
    }

    @Override
    public URI getOverstyrtTjeneste(String urn) {
        if (overstyrteTjenester.containsKey(urn)) {
            logger.info(String.format("Urn %s er overstyrt med URI %s fra fil %s. Beskrivelse for overstyring er: %s.", urn,
                    overstyrteTjenester.get(urn).getUri().toString(), filnavnTilLogging, overstyrteTjenester.get(urn).getBeskrivelse()));
            return overstyrteTjenester.get(urn).getUri();
        }
        return null;
    }

    private void lagMapForOverstyrtTjeneste() throws IOException {
        String overstyringsfil = settNavnPaaOverstyringsfil();

        if (overstyringsfil == null) {
            return;
        }

        int plasseringForExtension = overstyringsfil.lastIndexOf('.');
        if (plasseringForExtension < 1) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FEIL_FILTYPE_TJENESTEOVERSTYRING).build();
        }

        String overstyringsprefix = overstyringsfil.substring(0, plasseringForExtension);
        String overstyringsextension = overstyringsfil.substring(plasseringForExtension + 1);

        if (!overstyringsextension.equalsIgnoreCase("properties")) {
            throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FEIL_FILTYPE_TJENESTEOVERSTYRING).build();
        }

        MagRessurslaster overstyringsRessurs = new MagRessurslaster(finnAppHome(), null, Lastekjede.KONFIG);
        Ressurs lastRessurs = overstyringsRessurs.lastRessurs(overstyringsprefix, overstyringsextension);
        if (lastRessurs == null) {
            if (!overstyringsfil.equalsIgnoreCase(OVERSTYRTE_TJENESTER_FILNAVN)) {
                throw MagnetApplicationExceptionBuilder.feilkode(OppslagstjenesteFeilkoder.FINNER_IKKE_FIL_FOR_TJENESTEOVERSTYRING).build();
            }
            return;
        }
        filnavnTilLogging = lastRessurs.getLokasjon();

        try (Reader reader = lastRessurs.erFilBasert() ? new FileReader(lastRessurs.getFile()) : new InputStreamReader(
                lastRessurs.getInputStream()
                )) {

            Properties props = new Properties();
            props.load(reader);

            for (String urn : props.stringPropertyNames()) {
                String[] urnInfo = StringUtils.split(props.getProperty(urn), '|');
                if (urnInfo.length != 2) {
                    logger.error("Feil format i overstyringsfil: {}.", props.getProperty(urn));
                    continue;
                }
                OverstyrUrnDTO dto = new OverstyrUrnDTO(URI.create(urnInfo[0]), urnInfo[1]);
                overstyrteTjenester.put(urn, dto);
            }
        } catch (Exception e) {
            logger.error("Feil format i overstyringsfil.", e);
        }
    }

    private String finnAppHome() {
        if (System.getProperty("app.homepath.magnet") != null || System.getProperty("app.homepath") != null) {
            return Apphome.getGlassfishApphome();
        }
        return Apphome.getStandaloneApphome();
    }

    private String settNavnPaaOverstyringsfil() {
        if (overstyringProperty.equalsIgnoreCase("true")) {
            return OVERSTYRTE_TJENESTER_FILNAVN;
        }

        if (overstyringProperty.equalsIgnoreCase("false")) {
            return null;
        }

        return overstyringProperty;
    }

}
