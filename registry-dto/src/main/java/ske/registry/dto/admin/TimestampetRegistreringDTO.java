package ske.registry.dto.admin;

import java.util.Date;

import ske.registry.dto.RegistreringDTO;

public class TimestampetRegistreringDTO {
    private final String opprettet;
    private final String oppdatert;
    private final boolean aktiv;
    private final RegistreringDTO registrering;

    public TimestampetRegistreringDTO(RegistreringDTO registrering, long opprettet, long oppdatert, boolean aktiv) {
        this.registrering = registrering;
        this.opprettet = new Date(opprettet).toString();
        this.oppdatert = new Date(oppdatert).toString();
        this.aktiv = aktiv;
    }

    public String getOpprettet() {
        return opprettet;
    }

    public String getOppdatert() {
        return oppdatert;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public RegistreringDTO getRegistrering() {
        return registrering;
    }

}
