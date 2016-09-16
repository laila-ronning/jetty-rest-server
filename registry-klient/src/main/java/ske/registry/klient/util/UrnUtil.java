package ske.registry.klient.util;

/**
 * Skal ikke brukes. URN ligger i REST klienter eller i dokumentasjon til tjenesten
 */
@Deprecated
public class UrnUtil {

    @Deprecated
    public String urnMagnetApi(int versjon) {
        return String.format("urn:skatteetaten:magnet:api:v%s", Integer.toString(versjon));
    }

    @Deprecated
    public String urnPartsregisterIdentifikasjonWebService(int versjon) {
        return String.format("urn:skatteetaten:part:identifikasjon:partsidentifikasjon:v%s", Integer.toString(versjon));
    }

    @Deprecated
    public String urnEksterntFilmottak(int versjon) {
        return String.format("urn:skatteetaten:ekstkomm:v%s", Integer.toString(versjon));
    }
}
