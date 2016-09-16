package ske.registry.kompabilitet;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URI;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Integrasjonstest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.klient.RegistryKlient;
import ske.registry.klient.RegistryKlientBuilder;

@Category(Integrasjonstest.class)
public class R11KompabilitetsTest {

    @BeforeClass
    public static void sleep() throws InterruptedException {
        // Sover i 2 sekunder for å la registry:run starte
        Thread.sleep(2000);
    }

    @Test
    public void skalRegistrereTjenesteOgSlaaDenOpp() throws Exception {
        RegistryKlientBuilder registryKlientBuilder = new RegistryKlientBuilder("http://localhost:20000/registry/tjeneste");
        RegistryKlient registryKlient = registryKlientBuilder.byggRegistryKlient();

        registryKlient.start();

        RegistreringDTO registreringDTO = RegistreringDTO
                .medTilbyderNavn("Registrering")
                .tjeneste("urn:tjeneste:en", URI.create("http://www.tjeneste.en.no"), URI.create("http://www.tjeneste.en.no"),
                        "beskrivelse tjeneste en")
                .tjeneste("urn:tjeneste:to", URI.create("http://www.tjeneste.to.no"), URI.create("http://www.tjeneste.to.no"),
                        "beskrivelse tjeneste to")
                .bygg();

        registryKlient.registrer(registreringDTO);

        URI uri = registryKlient.finnTjenesteURI("urn:tjeneste:en");

        assertThat(uri.toString()).isEqualTo("http://www.tjeneste.en.no");
    }

    @Test
    public void skalHenteUtTjenesteattributter() throws Exception {
        RegistryKlientBuilder registryKlientBuilder = new RegistryKlientBuilder("http://localhost:20000/registry/tjeneste");
        RegistryKlient registryKlient = registryKlientBuilder.byggRegistryKlient();

        Map<String, String> tjenesteAttributter = registryKlient.hentTjenesteAttributter("urn:skatteetaten:drift:sts:rest:v2:validerSaml");
        assertThat(tjenesteAttributter.size()).isEqualTo(1);
        assertThat(tjenesteAttributter.get("sts.rest.sikkerhetspolicy")).isEqualTo("deaktivert");

        tjenesteAttributter = registryKlient.hentTjenesteAttributter("urn:skatteetaten:drift:sts:rest:v2:utstedSaml");
        assertThat(tjenesteAttributter.size()).isEqualTo(1);
        assertThat(tjenesteAttributter.get("sts.rest.sikkerhetspolicy")).isEqualTo("deaktivert");
    }
}
