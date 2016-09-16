package ske.registry.dto.admin;

import java.util.List;
import java.util.UUID;

public class KlientInfoDTO {

    private UUID tilbyderId;
    private String helsetilstand;
    private List<OppslagMedBeskrivelseDTO> oppslag;

    public List<OppslagMedBeskrivelseDTO> getOppslag() {
        return oppslag;
    }

    public void setOppslag(List<OppslagMedBeskrivelseDTO> oppslag) {
        this.oppslag = oppslag;
    }

    public UUID getTilbyderId() {
        return tilbyderId;
    }

    public void setTilbyderId(UUID tilbyderId) {
        this.tilbyderId = tilbyderId;
    }

    public String getHelsetilstand() {
        return helsetilstand;
    }

    public void setHelsetilstand(String helsetilstand) {
        this.helsetilstand = helsetilstand;
    }
}
