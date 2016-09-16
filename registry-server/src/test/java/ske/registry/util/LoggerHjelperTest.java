package ske.registry.util;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URI;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Enhetstest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;

@Category(Enhetstest.class)
public class LoggerHjelperTest {

    @Test
    public void skalHenteUrnNaarTjenesteMangler() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("Registrering").bygg();

        assertThat(LoggerHjelper.hentUrn(registrering)).isEqualToIgnoringCase("(Ingen URN i tjenesteliste)");
    }

    @Test
    public void skalHenteUrnNaarRegistreringIkkeFinnes() throws Exception {
        assertThat(LoggerHjelper.hentUrn(null)).isEqualToIgnoringCase("(Ingen URN i tjenesteliste)");
    }

    @Test
    public void skalHenteUrnForTjeneste() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("Registrering")
                .tjeneste("Tjeneste-1", null, null, null)
                .tjeneste("Tjeneste-2", null, null, null)
                .bygg();

        assertThat(LoggerHjelper.hentUrn(registrering)).isEqualToIgnoringCase("Tjeneste-1");
    }

    @Test
    public void skalHenteUriNaarTjenesteMangler() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("Registrering").bygg();

        assertThat(LoggerHjelper.hentUri(registrering)).isEqualToIgnoringCase("(Ingen URI i tjenesteliste)");
    }

    @Test
    public void skalHenteUriNaarRegistreringIkkeFinnes() throws Exception {
        assertThat(LoggerHjelper.hentUri(null)).isEqualToIgnoringCase("(Ingen URI i tjenesteliste)");
    }

    @Test
    public void skalHenteUriForTjeneste() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("Registrering")
                .tjeneste("Tjeneste-1", URI.create("http://uri-1.no"), null, null)
                .tjeneste("Tjeneste-2", URI.create("http://uri-2.no"), null, null)
                .bygg();

        assertThat(LoggerHjelper.hentUri(registrering)).isEqualToIgnoringCase("http://uri-1.no");
    }

    @Test
    public void skalHenteTilbydernavn() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("Registrering").bygg();

        assertThat(LoggerHjelper.hentTilbydernavn(new TimestampetRegistreringDTO(registrering, 0L, 0L, true))).isEqualToIgnoringCase(
                "Registrering");
    }

    @Test
    public void skalHenteTilbydernavnNaarRegistreringMangler() throws Exception {
        assertThat(LoggerHjelper.hentTilbydernavn(new TimestampetRegistreringDTO(null, 0L, 0L, true))).isEqualToIgnoringCase(
                "(Registrering mangler tilbydernavn)");
    }

    @Test
    public void skalHenteTilbydernavnNaarTilbydernavnErNull() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn(null).bygg();

        assertThat(LoggerHjelper.hentTilbydernavn(new TimestampetRegistreringDTO(registrering, 0L, 0L, true))).isEqualToIgnoringCase(
                "(Registrering mangler tilbydernavn)");
    }

    @Test
    public void skalHenteTilbydernavnNaarTilbydernavnErBlank() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("").bygg();

        assertThat(LoggerHjelper.hentTilbydernavn(new TimestampetRegistreringDTO(registrering, 0L, 0L, true))).isEqualToIgnoringCase("");
    }
}
