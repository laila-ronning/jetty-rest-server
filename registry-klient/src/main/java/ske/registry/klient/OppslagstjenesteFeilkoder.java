package ske.registry.klient;

import ske.mag.felles.exception.Feilkode;

public enum OppslagstjenesteFeilkoder implements Feilkode {

    FEIL_FILTYPE_TJENESTEOVERSTYRING("OPPSL_ERROR_0001", "Feil filtype for overstyring av url for tjenester. Forventet en .properties-fil"),
    FEIL_LESING_AV_TJENESTEOVERSTYRING("OPPSL_ERROR_0002", "Feil ved les av fila for tjenesteoverstyring."),
    FINNER_IKKE_FIL_FOR_TJENESTEOVERSTYRING("OPPSL_ERROR_0003", "Fant ikke fila for tjenesteoverstyring."),
    FEIL_FILTYPE_STUBBING("OPPSL_ERROR_0004", "Feil filtype for stubbing av oppslagstjenesten. Forventet en .properties-fil"),
    FINNER_IKKE_FIL_FOR_STUBBING("OPPSL_ERROR_0005", "Fant ikke fila for stubbing av oppslagstjenesten.");

    private final String kode;
    private final String melding;

    private OppslagstjenesteFeilkoder(String kode, String melding) {
        this.kode = kode;
        this.melding = melding;
    }

    @Override
    public String getKode() {
        return null;
    }

    @Override
    public String getMelding() {
        return null;
    }
}
