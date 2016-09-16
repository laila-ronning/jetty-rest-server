package ske.registry.klient;

import java.net.URI;

class OverstyrUrnDTO {

    public OverstyrUrnDTO(URI uri, String beskrivelse) {
        this.uri = uri;
        this.beskrivelse = beskrivelse;
    }

    OverstyrUrnDTO() {
    }

    private URI uri;
    private String beskrivelse;

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
