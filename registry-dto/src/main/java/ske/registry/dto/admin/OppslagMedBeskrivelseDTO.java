package ske.registry.dto.admin;

import java.net.URI;

public class OppslagMedBeskrivelseDTO {

    private String urn;
    private URI uri;
    private String beskrivelse;

    public OppslagMedBeskrivelseDTO() {
    }

    public OppslagMedBeskrivelseDTO(String urn, URI uri, String beskrivelse) {
        this.urn = urn;
        this.uri = uri;
        this.beskrivelse = beskrivelse;
    }

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

}
