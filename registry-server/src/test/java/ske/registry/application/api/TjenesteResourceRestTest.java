package ske.registry.application.api;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Komponenttest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.klient.RegistryKlientImpl;
import ske.registry.module.GuiceTestServletConfig;

@Category(Komponenttest.class)
public class TjenesteResourceRestTest extends JerseyTest {

    public TjenesteResourceRestTest() {
        super(new WebAppDescriptor.Builder()
                .filterClass(GuiceFilter.class)
                .contextListenerClass(GuiceTestServletConfig.class)
                .build());
    }

    @Test
    public void skalFinneFlereAktiveTjenesterMedKlient() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedFlereTjenester();
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(1000)
                .bygg();

        klient.start();
        klient.finnTjenesteURI("urn:test1");
        klient.finnTjenesteURI("urn:test2");
        assertThat(klient.finnTjenesteURI("urn:test1")).isEqualTo(URI.create("http://uri_til_test1"));
        klient.stopp();
        klient.slettCachedeOppslag();
        assertThat(klient.finnTjenesteURI("urn:test1")).isNull();
    }

    @Test
    public void skalRapportereObligatoriskeOppslagSomFeiler() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedFlereTjenester();

        RegistryKlientImpl klientMedRegistrering = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(1000)
                .bygg();
        klientMedRegistrering.start();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .obligatoriskOppslag("urn:test1")
                .obligatoriskOppslag("urn:test2")
                .obligatoriskOppslag("urn:test3")
                .bygg();

        klientMedRegistrering.stopp();
        assertThat(klient.obligatoriskeOppslagSomMangler()).containsOnly("urn:test3");
    }


    @Test
    public void skalIkkeBliTilgjengeligForOppslagFoerGracePeriodeUtloep() throws InterruptedException {
        try {
            jsonResource().path("tjeneste").path("urn:ikke_registrert").get(new GenericType<List<TjenesteDTO>>() {
            });
        } catch (UniformInterfaceException e) {
        }
        try {
            jsonResource().path("tjeneste").path("urn:ikke_registrert").get(new GenericType<List<TjenesteDTO>>() {
            });
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        }
    }

    @Test
    public void skalBliTilgjengeligForOppslagEtterGracePeriodeUtloep() throws InterruptedException {
        try {
            jsonResource().path("tjeneste").path("urn:ikke_registrert").get(new GenericType<List<TjenesteDTO>>() {
            });
        } catch (UniformInterfaceException e) {
        }

        Thread.sleep(500);
        try {
            jsonResource().path("tjeneste").path("urn:ikke_registrert").get(new GenericType<List<TjenesteDTO>>() {
            });
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }


    @Test
    public void skalFinneAktiveTjenesterMedKlient() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(1000)
                .bygg();

        klient.start();

        assertThat(klient.finnTjenesteURI("urn:test")).isEqualTo(URI.create("http://uri_til_test"));

        klient.stopp();
        klient.slettCachedeOppslag();
        assertThat(klient.finnTjenesteURI("urn:test")).isNull();
    }

    @Test
    public void skalSendeKlientInfo() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(1000)
                .bygg();

        klient.start();
        assertThat(klient.finnTjenesteURI("urn:test")).isEqualTo(URI.create("http://uri_til_test"));
        assertThat(resource().path("admin/registrering").get(String.class)).contains("oppslag\":[{\"urn\":\"urn:test\"," +
                "\"uri\":\"http://uri_til_test");

        klient.stopp();
    }


    @Test
    public void skalFaaUriBasertPaaServerensAdresse() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        registrering.getTjenesteliste().get(0).setKlientUri(null);
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(1000)
                .bygg();

        klient.start();

        assertThat(klient.finnTjenesteURI("urn:test")).isNotNull();
        assertThat(klient.finnTjenesteURI("urn:test")).isNotEqualTo(URI.create("http://uri_til_test"));

        klient.stopp();
        klient.slettCachedeOppslag();
        assertThat(klient.finnTjenesteURI("urn:test")).isNull();
    }

    @Test
    public void skalStokkeTjenester() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        registrering.getTjenesteliste().get(0).setKlientUri(null);
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(60000)
                .bygg();

        RegistreringDTO registrering2 = lagRegistreringMedEnTjeneste();
        registrering2.getTjenesteliste().get(0).setKlientUri(null);
        registrering2.setTilbyderId(UUID.randomUUID());
        registrering2.getTjenesteliste().get(0).setUri(URI.create("http://uri2_til_test"));
        RegistryKlientImpl klient2 = (RegistryKlientImpl) RegistryKlientImpl
                .med(resource().getURI() + "tjeneste")
                .tjenesteregistrering(registrering2)
                .timerStartDelay(0)
                .registreringIntervall(60000)
                .bygg();

        klient.start();
        klient2.start();

        assertThat(klient.finnTjenesteURI("urn:test")).isNotNull();

        List<TjenesteDTO> res = jsonResource().path("tjeneste").path("urn:test").get(new GenericType<List<TjenesteDTO>>() {
        });

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getUri()).isNotEqualTo(res.get(1).getUri());
        assertThat(res.get(0).getUri().toString()).isIn("http://uri2_til_test", "http://uri_til_test");

        klient.stopp();
        klient2.stopp();
        klient.slettCachedeOppslag();
        klient2.slettCachedeOppslag();
        assertThat(klient.finnTjenesteURI("urn:test")).isNull();
    }

    @Test
    public void skalKallePing() {
        assertThat(resource().path("ping").get(String.class)).isEqualTo("pong");
    }

    @Test
    public void skalReturnereTomListeAvTjenester() {
        assertThat(resource().path("admin/tjeneste/aktiv").get(String.class)).isEqualTo("[]");
    }

    @Test
    public void skalFinneAktiveTjenester() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();

        jsonResource().path("tjeneste").entity(registrering, MediaType.APPLICATION_JSON).post(String.class);
        assertThat(jsonResource().path("admin/tjeneste/aktiv").get(String.class)).contains("http://uri_til_test");
    }

    private RegistreringDTO lagRegistreringMedEnTjeneste() {
        return RegistreringDTO
                .medTilbyderNavn("test")
                .tjeneste("urn:test", URI.create("http://uri_til_test"), URI.create("http://uri_til_test"), null)
                .bygg();
    }

    private RegistreringDTO lagRegistreringMedFlereTjenester() {
        return RegistreringDTO
                .medTilbyderNavn("test")
                .konfigurasjon("testkonfig")
                .tjeneste("urn:test1", URI.create("http://uri_til_test1"), URI.create("http://uri_til_test1"), null)
                .tjeneste("urn:test2", URI.create("http://uri_til_test2"), URI.create("http://uri_til_test2"), null)
                .tjeneste("urn:test2", URI.create("http://uri2_til_test2"), URI.create("http://uri2_til_test2"), null)
                .bygg();
    }


    private WebResource jsonResource() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        return client.resource(resource().getURI());
    }

}
