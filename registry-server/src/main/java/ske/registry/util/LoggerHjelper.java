package ske.registry.util;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;

public class LoggerHjelper {

    private static final String INGEN_URN = "(Ingen URN i tjenesteliste)";
    private static final String INGEN_URI = "(Ingen URI i tjenesteliste)";
    private static final String INGEN_TILBYDERNAVN = "(Registrering mangler tilbydernavn)";

    public static String hentUrn(RegistreringDTO registrering) {
        return registrering != null && registrering.getTjenesteliste() != null && registrering.getTjenesteliste().size() > 0 ? registrering
                .getTjenesteliste().get(0).getUrn() : INGEN_URN;
    }

    public static String hentUri(RegistreringDTO registrering) {
        return registrering != null && registrering.getTjenesteliste() != null && registrering.getTjenesteliste().size() > 0
                && registrering.getTjenesteliste().get(0).getUri() != null ? registrering.getTjenesteliste().get(0).getUri()
                .toString() : INGEN_URI;
    }

    public static String hentTilbydernavn(TimestampetRegistreringDTO registrering) {
        return registrering != null && registrering.getRegistrering() != null && registrering.getRegistrering().getTilbyderNavn() != null ? registrering
                .getRegistrering().getTilbyderNavn()
                : INGEN_TILBYDERNAVN;
    }
}
