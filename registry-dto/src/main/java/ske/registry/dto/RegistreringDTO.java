package ske.registry.dto;

import java.net.URI;
import java.util.*;

import ske.registry.dto.admin.OppslagMedBeskrivelseDTO;

public class RegistreringDTO {

    private UUID tilbyderId = UUID.randomUUID();
    private String tilbyderNavn;
    private String applikasjonsversjon;
    private String applikasjonsgruppe;
    private String komponent;
    private String artefakt;
    private String hostOgPort;
    private URI adminsideUrl;
    private String helsetilstand;
    private URI helsesjekkUrl;
    private URI pingUrl;
    private Map<String, String> egendefinertInfo = new HashMap<>();
    private List<TjenesteDTO> tjenesteliste = new ArrayList<>();
    private String konfigurasjon;
    private List<OppslagMedBeskrivelseDTO> oppslag;

    private RegistreringDTO() {
        this.helsetilstand = "FRISK";
    }

    public static Builder medTilbyderNavn(String tilbyderNavn) {
        return new Builder(tilbyderNavn);
    }

    public UUID getTilbyderId() {
        return tilbyderId;
    }

    public void setTilbyderId(UUID tilbyderId) {
        this.tilbyderId = tilbyderId;
    }

    public List<TjenesteDTO> getTjenesteliste() {
        return tjenesteliste;
    }

    public void setTjenesteliste(List<TjenesteDTO> tjenesteliste) {
        this.tjenesteliste = tjenesteliste;
    }

    public String getKonfigurasjon() {
        return konfigurasjon;
    }

    public void setKonfigurasjon(String konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    public String getTilbyderNavn() {
        return tilbyderNavn;
    }

    public void setTilbyderNavn(String tilbyderNavn) {
        this.tilbyderNavn = tilbyderNavn;
    }

    public List<OppslagMedBeskrivelseDTO> getOppslag() {
        return oppslag;
    }

    public void setOppslag(List<OppslagMedBeskrivelseDTO> oppslag) {
        this.oppslag = oppslag;
    }

    public String getApplikasjonsversjon() {
        return applikasjonsversjon;
    }

    public void setApplikasjonsversjon(String applikasjonsversjon) {
        this.applikasjonsversjon = applikasjonsversjon;
    }

    public String getApplikasjonsgruppe() {
        return applikasjonsgruppe;
    }

    public void setApplikasjonsgruppe(String applikasjonsgruppe) {
        this.applikasjonsgruppe = applikasjonsgruppe;
    }

    public String getHostOgPort() {
        return hostOgPort;
    }

    public void setHostOgPort(String hostOgPort) {
        this.hostOgPort = hostOgPort;
    }

    public URI getAdminsideUrl() {
        return adminsideUrl;
    }

    public void setAdminsideUrl(URI adminsideUrl) {
        this.adminsideUrl = adminsideUrl;
    }

    public String getHelsetilstand() {
        return helsetilstand;
    }

    public void setHelsetilstand(String helsetilstand) {
        this.helsetilstand = helsetilstand;
    }

    public URI getHelsesjekkUrl() {
        return helsesjekkUrl;
    }

    public void setHelsesjekkUrl(URI helsesjekkUrl) {
        this.helsesjekkUrl = helsesjekkUrl;
    }

    public URI getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(URI pingUrl) {
        this.pingUrl = pingUrl;
    }

    public Map<String, String> getEgendefinertInfo() {
        return egendefinertInfo;
    }

    public void setEgendefinertInfo(Map<String, String> egendefinertInfo) {
        this.egendefinertInfo = egendefinertInfo;
    }

    public String getKomponent() {
        return komponent;
    }

    public String getArtefakt() {
        return artefakt;
    }

    public static final class Builder {

        private RegistreringDTO reg = new RegistreringDTO();

        public Builder(String tilbyderNavn) {
            reg.tilbyderNavn = tilbyderNavn;
        }

        public Builder konfigurasjon(String konfigurasjon) {
            reg.konfigurasjon = konfigurasjon;
            return this;
        }

        public Builder applikasjonsversjon(String applikasjonsversjon) {
            reg.applikasjonsversjon = applikasjonsversjon;
            return this;
        }

        public Builder applikasjonsgruppe(String applikasjonsgruppe) {
            reg.applikasjonsgruppe = applikasjonsgruppe;
            return this;
        }

        public Builder komponent(String komponent) {
            reg.komponent = komponent;
            return this;
        }

        public Builder artefakt(String artefakt) {
            reg.artefakt = artefakt;
            return this;
        }

        public Builder hostOgPort(String hostOgPort) {
            reg.hostOgPort = hostOgPort;
            return this;
        }

        public Builder adminsideUrl(URI adminsideUrl) {
            reg.adminsideUrl = adminsideUrl;
            return this;
        }

        public Builder helsesjekkUrl(URI helsesjekkUrl) {
            reg.helsesjekkUrl = helsesjekkUrl;
            return this;
        }

        public Builder pingUrl(URI pingUrl) {
            reg.pingUrl = pingUrl;
            return this;
        }

        public Builder leggTilEgendefinertInfo(String key, String value) {
            reg.egendefinertInfo.put(key, value);
            return this;
        }

        public Builder tjeneste(String urn, URI uri, String beskrivelse) {
            TjenesteDTO tjeneste = new TjenesteDTO();
            tjeneste.setUrn(urn);
            tjeneste.setUri(uri);
            tjeneste.setBeskrivelse(beskrivelse);
            reg.tjenesteliste.add(tjeneste);
            return this;
        }

        public Builder tjeneste(String urn, URI uri, URI klientUri, String beskrivelse) {
            TjenesteDTO tjeneste = new TjenesteDTO();
            tjeneste.setUrn(urn);
            tjeneste.setUri(uri);
            tjeneste.setKlientUri(klientUri);
            tjeneste.setBeskrivelse(beskrivelse);
            reg.tjenesteliste.add(tjeneste);
            return this;
        }

        public RegistreringDTO bygg() {
            return reg;
        }

    }

}
