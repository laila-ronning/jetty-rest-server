package ske.registry.klient;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.felles.exception.MagnetApplicationException;
import ske.mag.test.kategorier.Komponenttest;

@Category(Komponenttest.class)
public class RegistryKlientFakeImplOldTest {

    @Test
    public void skalReturnereFakeNaarProtokollIkkeErHttp() throws Exception {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med("file://oppslagstjeneste.properties")
                .bygg();

        assertThat(oppslagKlient.finnTjenesteURI("urn:skatteetaten:ekstkomm:v1")).isEqualTo(
                URI.create("http://localhost:8080/eksterntfilmottak"));
    }

    @Test
    public void skalReturnereNullNaarUrnIkkeFinnes() throws Exception {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med("file://oppslagstjeneste.properties")
                .bygg();

        assertThat(oppslagKlient.finnTjenesteURI("urn:skatteetaten:ekstkomm:v1wewe")).isNull();
    }

    @Test
    public void skalKasteExceptionVedUgyldigURI() {
        try {
            RegistryKlientImpl
                    .med("file://oppslagstjen%& -;e")
                    .bygg();

            fail("Skal kaste exception");
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
        }
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalKasteExceptionHvisFilIkkeFinnes() throws Exception {
        RegistryKlientImpl
                .med("file://finnesIkke.properties")
                .bygg();
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingOmFeilFiltypeVedFeilFiltype() {
        RegistryKlientImpl.med("file://tull.doc").bygg();
    }

    @Test(expected = MagnetApplicationException.class)
    public void skalGiFeilmeldingOmFeilFiltypeVedIngenFiltype() {
        RegistryKlientImpl.med("file://fake").bygg();
    }

}
