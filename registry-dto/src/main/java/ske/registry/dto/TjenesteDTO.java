package ske.registry.dto;

import java.net.URI;

public class TjenesteDTO {

    private String urn;
    private URI uri;
    private URI klientUri;
    private String beskrivelse;

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public URI getKlientUri() {
        return klientUri;
    }

    public void setKlientUri(URI klientUri) {
        this.klientUri = klientUri;
    }

}
