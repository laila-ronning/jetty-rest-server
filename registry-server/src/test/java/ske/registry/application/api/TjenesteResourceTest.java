package ske.registry.application.api;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML;
import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import mag.felles.konfig.KonfigFactory;
import mag.felles.konfigurasjon.STSTjenesteKonstanter;
import mag.felles.konfigurasjon.SikkerhetspolicyOppslagstjeneste;
import org.fest.assertions.Fail;
import org.fest.assertions.MapAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ske.mag.test.kategorier.Komponenttest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.PulsDTO;
import ske.registry.repository.InMemoryRegistryRepository;
import ske.registry.service.RegistryServiceBean;

@Category(Komponenttest.class)
public class TjenesteResourceTest {

    public static final String STS_REST_SIKKERHETSPOLICY = STSTjenesteKonstanter.STS_SIKKERHETSPOLICY_TJENESTEATTRIBUTT;
    public static final String AKTIVERT = SikkerhetspolicyOppslagstjeneste.AKTIVERT.getValue();

    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private KonfigFactory konfigFactory;
    @Mock
    private RegistryServiceBean registryServiceBeanMock;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void settopp() {
        MockitoAnnotations.initMocks(this);
        when(servletRequest.getRemoteHost()).thenReturn("localhost");
    }

    @Ignore("Skal midlertidig stokke alle uten å prioritere lik host")
    @Test
    public void skalStokkeTjenester() throws InterruptedException {

        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste("urn:test");
        RegistreringDTO registrering2 = lagRegistreringMedEnTjeneste("urn:test");
        RegistreringDTO registrering3 = lagRegistreringMedEnTjeneste("urn:test");
        RegistreringDTO registrering4 = lagRegistreringMedEnTjeneste("urn:test");

        registrering2.getTjenesteliste().get(0).setKlientUri(URI.create("http://localhost/test2"));
        registrering4.getTjenesteliste().get(0).setKlientUri(URI.create("http://localhost/test4"));

        resource.leggInnTjeneste(registrering, servletRequest);
        resource.leggInnTjeneste(registrering2, servletRequest);
        resource.leggInnTjeneste(registrering3, servletRequest);
        resource.leggInnTjeneste(registrering4, servletRequest);

        System.out.print(resource.aktiveTjenester("urn:test", servletRequest).getEntity());

        assertThat(resource.aktiveTjenester("urn:test", servletRequest).getEntity()).isNotNull();
        List<TjenesteDTO> tjenester = (List<TjenesteDTO>) resource.aktiveTjenester("urn:test", servletRequest).getEntity();

        assertThat(tjenester.indexOf(registrering2.getTjenesteliste().get(0))).isLessThan(
                tjenester.indexOf(registrering.getTjenesteliste().get(0)));

        assertThat(tjenester.indexOf(registrering2.getTjenesteliste().get(0))).isLessThan(
                tjenester.indexOf(registrering3.getTjenesteliste().get(0)));

        assertThat(tjenester.indexOf(registrering4.getTjenesteliste().get(0))).isLessThan(
                tjenester.indexOf(registrering.getTjenesteliste().get(0)));

        assertThat(tjenester.indexOf(registrering4.getTjenesteliste().get(0))).isLessThan(
                tjenester.indexOf(registrering3.getTjenesteliste().get(0)));
    }

    @Test
    public void skalReturnereEnTjenesteSelvOmDetFinnesToMedSammeNavn() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registrering = lagRegistreringMedToTjenester("urn:test", "urn:test");
        resource.leggInnTjeneste(registrering, servletRequest);

        List<TjenesteDTO> resultat = (List<TjenesteDTO>) resource.aktiveTjenester("urn:test", servletRequest).getEntity();
        assertThat(resultat).hasSize(2);
    }

    @Test
    public void skalReturnereEnTjeneste() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste("urn:test");
        resource.leggInnTjeneste(registrering, servletRequest);

        List<TjenesteDTO> resultat = (List<TjenesteDTO>) resource.aktiveTjenester("urn:test", servletRequest).getEntity();
        assertThat(resultat).hasSize(1);
    }

    @Test
    public void skalReturnereServiceUnavailableNaarInnenforGraceperiode() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste("urn:finnesIkke");
        resource.leggInnTjeneste(registrering, servletRequest);

        assertThat(resource.aktiveTjenester("urn:test", servletRequest).getStatus()).isEqualTo(
                Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void skalReturnereNotFoundNaarUtenforGraceperiode() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 0L, 2000L))
                );
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste("urn:finnesIkke");
        resource.leggInnTjeneste(registrering, servletRequest);

        assertThat(resource.aktiveTjenester("urn:test", servletRequest).getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void skalReturnereTjenesteAttributter() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );

        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste("urn:test");
        resource.leggInnTjeneste(registreringDTO, servletRequest);

        Map<String, String> tjenesteAttributter = (Map<String, String>) resource.hentTjenesteattributter("urn:test", servletRequest)
                .getEntity();
        assertThat(tjenesteAttributter).isNotNull();
        assertThat(tjenesteAttributter).includes(MapAssert.entry(STS_REST_SIKKERHETSPOLICY, AKTIVERT));

        tjenesteAttributter = (Map<String, String>) resource.hentTjenesteattributter("urn:finnesIkke", servletRequest).getEntity();
        assertThat(tjenesteAttributter).isNull();
    }

    @Test
    public void skalGiStatusForbiddenVedValiderSamlURN() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );

        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML);
        Response response = resource.leggInnTjeneste(registreringDTO, servletRequest);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getEntity())
                .isEqualTo("Ikke lov å registrere tjeneste med urn: urn:skatteetaten:drift:sts:rest:v2:validerSaml");
    }

    @Test
    public void skalGiStatusForbiddenVedUtstedSamlURN() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );

        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML);
        Response response = resource.leggInnTjeneste(registreringDTO, servletRequest);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getEntity()).isEqualTo("Ikke lov å registrere tjeneste med urn: urn:skatteetaten:drift:sts:rest:v2:utstedSaml");
    }

    @Test
    public void skalIkkeGiUspesifisertFeilVedFeilUri() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registrering = RegistreringDTO
                .medTilbyderNavn("test")
                .tjeneste("urn:test", URI.create("http://host:8080//uri_til_test/bla"), null, null)
                .bygg();
        Response response = resource.leggInnTjeneste(registrering, servletRequest);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void skalIkkeGiUspesifisertFeilVedVanligRegistrering() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registreringDTO = RegistreringDTO
                .medTilbyderNavn("hei")
                .konfigurasjon(konfigFactory.formaterInnholdPaaJsonFormat())
                .tjeneste("urn:skatteetaten:fastsetting:dvh:grunnlagsdata:startstopp", URI.create("http://test/test"),
                        "tekst med mellomrom")
                .bygg();
        Response response = resource.leggInnTjeneste(registreringDTO, servletRequest);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void skalSendePuls() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L))
                );
        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste("urn:test");
        resource.leggInnTjeneste(registreringDTO, servletRequest);

        PulsDTO pulsDTO = new PulsDTO();
        pulsDTO.setTilbyderId(registreringDTO.getTilbyderId());

        Response response = resource.puls(pulsDTO);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getMetadata().containsKey("Location")).isTrue();
    }

    @Test
    public void skalIkkeFeileVedPulsUkjentTjeneste() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 0L, 2000L)));

        PulsDTO puls = new PulsDTO();
        puls.setTilbyderId(UUID.randomUUID());

        try {
            Response response = resource.puls(puls);
            assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        } catch (NullPointerException e) {
            Fail.fail();
        }
    }

    @Test
    public void skalReturnereBadRequestVedPuls() {
        TjenesteResource resource = new TjenesteResource(registryServiceBeanMock);
        doThrow(new RuntimeException()).when(registryServiceBeanMock).oppdaterTimestamp(any(UUID.class));

        assertThat(resource.puls(new PulsDTO()).getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skalSendeStopp() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 0L, 2000L)));
        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste("urn:test");
        resource.leggInnTjeneste(registreringDTO, servletRequest);

        assertThat(resource.aktiveTjenester("urn:test", servletRequest).getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        PulsDTO pulsDTO = new PulsDTO();
        pulsDTO.setTilbyderId(registreringDTO.getTilbyderId());

        Response response = resource.stopp(pulsDTO);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getMetadata().containsKey("Location")).isTrue();

        // Skal fjernes fra tilbydere etter stopp
        assertThat(resource.aktiveTjenester("urn:test", servletRequest).getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void skalIkkeFeileVedStoppUkjentTjeneste() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 0L, 2000L)));

        PulsDTO puls = new PulsDTO();
        puls.setTilbyderId(UUID.randomUUID());

        try {
            Response response = resource.stopp(puls);
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        } catch (NullPointerException e) {
            Fail.fail();
        }
    }

    @Test
    public void skalOppdatereInfo() {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L)));
        RegistreringDTO registreringDTO = lagRegistreringMedEnTjeneste("urn:test");
        resource.leggInnTjeneste(registreringDTO, servletRequest);

        KlientInfoDTO klientInfo = new KlientInfoDTO();
        klientInfo.setTilbyderId(registreringDTO.getTilbyderId());

        Response response = resource.oppdaterInfo(klientInfo);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getMetadata().containsKey("Location")).isTrue();
    }

    @Test
    public void skalReturnereBadRequestVedOppdatereInfo() {
        TjenesteResource resource = new TjenesteResource(registryServiceBeanMock);
        doThrow(new RuntimeException()).when(registryServiceBeanMock).oppdaterKlientInfo(any(UUID.class), any(KlientInfoDTO.class));

        assertThat(resource.oppdaterInfo(new KlientInfoDTO()).getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skalReturnereAktiveTjenesterForFlereUrn() throws Exception {
        TjenesteResource resource = new TjenesteResource(new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 0L, 2000L)));
        RegistreringDTO registreringDTO = lagRegistreringMedToTjenester("urn:test:en", "urn:test:to");
        resource.leggInnTjeneste(registreringDTO, servletRequest);

        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.put("urnListe", Lists.newArrayList("urn:test:en", "urn:test:to", "urn:finnesIkke"));
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

        assertThat(resource.aktiveTjenesterForFlereUrn(uriInfo, servletRequest).getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());

        Map<String, Collection<TjenesteDTO>> aktiveTjenesterMedURN = (Map<String, Collection<TjenesteDTO>>) resource
                .aktiveTjenesterForFlereUrn(uriInfo, servletRequest).getEntity();

        assertThat(aktiveTjenesterMedURN.size()).isEqualTo(2);

        assertThat(aktiveTjenesterMedURN.get("urn:test:en").iterator().next().getUri()).isEqualTo(
                registreringDTO.getTjenesteliste().get(0).getKlientUri());

        assertThat(aktiveTjenesterMedURN.get("urn:test:to").iterator().next().getUri()).isEqualTo(
                registreringDTO.getTjenesteliste().get(1).getKlientUri());
    }

    private RegistreringDTO lagRegistreringMedEnTjeneste(String tjenesteURN) {
        RegistreringDTO registrering = RegistreringDTO
                .medTilbyderNavn("test")
                .leggTilEgendefinertInfo(STS_REST_SIKKERHETSPOLICY, AKTIVERT)
                .tjeneste(tjenesteURN, URI.create("http://host/uri_til_test"), URI.create("http://host/uri_til_test"), null)
                .bygg();
        return registrering;
    }

    private RegistreringDTO lagRegistreringMedToTjenester(String tjenesteURN, String tjenesteURN_to) {
        RegistreringDTO registrering = RegistreringDTO
                .medTilbyderNavn("test")
                .leggTilEgendefinertInfo(STS_REST_SIKKERHETSPOLICY, AKTIVERT)
                .tjeneste(tjenesteURN, URI.create("http://host/uri_til_test"), URI.create("http://host/uri_til_test"), null)
                .tjeneste(tjenesteURN_to, URI.create("http://host/uri_til_test"), URI.create("http://host/uri_til_test"), null)
                .bygg();
        return registrering;
    }

}
