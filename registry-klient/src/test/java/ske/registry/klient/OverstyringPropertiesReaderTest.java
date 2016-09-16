package ske.registry.klient;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.felles.exception.MagnetApplicationException;
import ske.mag.test.kategorier.Komponenttest;

@Category(Komponenttest.class)
public class OverstyringPropertiesReaderTest {

    public static final String OVERSTYRTE_TJENESTER_TESTFIL = "overstyrte-tjenester-testfil.properties";
    private OverstyringPropertiesReader overstyringPropertiesReader;

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingOmFeilFiltypeVedFeilFiltype() {
        overstyringPropertiesReader = new OverstyringPropertiesReader("tull.doc");
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingOmFeilFiltypeVedIngenFiltype() {
        overstyringPropertiesReader = new OverstyringPropertiesReader("tull");
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingOmFeilFiltypeMedFiltypeBarePunktum() {
        overstyringPropertiesReader = new OverstyringPropertiesReader("tull.");
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingNaarAngittFilIkkeEksisterer() {
        overstyringPropertiesReader = new OverstyringPropertiesReader("tull.csv");
    }

    @Test
    public void skalReturnereNaarDefaultfilFilIkkeEksisterer() {
        try {
            overstyringPropertiesReader = new OverstyringPropertiesReader(OverstyringPropertiesReader.OVERSTYRTE_TJENESTER_FILNAVN);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void skalLeseGyldigFil() {
        overstyringPropertiesReader = new OverstyringPropertiesReader(OVERSTYRTE_TJENESTER_TESTFIL);

        assertThat(overstyringPropertiesReader.getOverstyrteTjenester()).hasSize(2);
    }

    @Test
    public void skalReturnereUrn() {
        overstyringPropertiesReader = new OverstyringPropertiesReader(OVERSTYRTE_TJENESTER_TESTFIL);
        URI overstyrtTjeneste = overstyringPropertiesReader.getOverstyrtTjeneste("urn:ske.skatteetaten.eksempel");

        assertThat(overstyrtTjeneste.toString()).isEqualTo("www.skatteetaten.no");
    }

    @Test
    public void skalFaaNullVedUgyldigUrn() {
        overstyringPropertiesReader = new OverstyringPropertiesReader(OVERSTYRTE_TJENESTER_TESTFIL);
        URI overstyrtTjeneste = overstyringPropertiesReader.getOverstyrtTjeneste("tull.urn");

        assertNull(overstyrtTjeneste);
    }

}
