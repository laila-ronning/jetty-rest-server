package ske.registry.klient;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.fest.assertions.MapAssert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import ske.mag.test.kategorier.Komponenttest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.PulsDTO;

@Category(Komponenttest.class)
public class RegistryKlientImplOldTest {

    @Mock
    private WebResource mockWebResource;
    @Mock
    private WebResource.Builder mockWebResourceBuilder;
    @Mock
    private RegistryRestKlient klient;

    @BeforeClass
    public static void stopLogging() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistryKlientImpl.class);
        logger.getLoggerContext().reset();
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(mockWebResource.getURI()).thenReturn(URI.create("http://dummyserver"));
        when(mockWebResource.queryParam(anyString(), anyString())).thenReturn(mockWebResource);
        when(mockWebResource.queryParams(any(MultivaluedMap.class))).thenReturn(mockWebResource);
        when(mockWebResource.path(anyString())).thenReturn(mockWebResource);
        when(mockWebResource.entity(anyObject(), anyString())).thenReturn(mockWebResourceBuilder);
    }

    @Test
    public void skalReturnereHvisFinnesIRegistry() {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty("false")
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        assertThat(oppslagKlient.finnTjenesteURI("urn:tjeneste")).isEqualTo(URI.create("http://tjeneste"));
    }

    @Test
    public void skalReturnereTjenesteattributter() throws Exception {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty("false")
                .bygg();

        ImmutableMap<String, String> tjenesteattributter = ImmutableMap.of("sts.rest.sikkerhetspolicy", "aktivert");
        when(mockWebResource.get(any(GenericType.class))).thenReturn(tjenesteattributter);

        assertThat(oppslagKlient.hentTjenesteAttributter("urn:tjeneste")).includes(
                MapAssert.entry("sts.rest.sikkerhetspolicy", "aktivert"));
    }

    @Test
    public void skalIkkeReturnereTjenesteattributter() throws Exception {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty("false")
                .bygg();

        when(mockWebResource.get(any(GenericType.class))).thenReturn(null);

        assertThat(oppslagKlient.hentTjenesteAttributter("urn:tjeneste")).isNull();
    }

    @Test
    public void skalKasteExceptionVedKallMotReturnereTjenesteattributter() throws Exception {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty("false")
                .bygg();

        when(mockWebResource.get(any(GenericType.class))).thenThrow(new ClientHandlerException());

        assertThat(oppslagKlient.hentTjenesteAttributter("urn:tjeneste")).isNull();
    }

    @Test
    public void skalReturnereAlleHvisFinnesIRegistry() {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty("false")
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        TjenesteDTO dto2 = new TjenesteDTO();
        dto2.setKlientUri(URI.create("http://tjeneste2"));

        when(mockWebResource.get(any(GenericType.class))).thenReturn(Arrays.asList(dto, dto2));

        LinkedHashSet<URI> alleURIer = new LinkedHashSet<>(Arrays.asList(dto.getKlientUri(), dto2.getKlientUri()));
        assertThat(oppslagKlient.finnAlleTjenesteURIer("urn:tjeneste")).isEqualTo(alleURIer);
    }

    @Test
    public void skalReturnereOverstyrtUriHvisDenFinnes() {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty(OverstyringPropertiesReaderTest.OVERSTYRTE_TJENESTER_TESTFIL)
                .bygg();

        assertThat(oppslagKlient.finnTjenesteURI("urn:ske.skatteetaten.eksempel")).isEqualTo(URI.create("www.skatteetaten.no"));
    }

    @Test
    public void skalUtfoereObligatoriskOppslag() {
        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl
                .med(mockWebResource)
                .obligatoriskOppslag("urn:oppslag")
                .bygg();

        verify(mockWebResource).get(any(GenericType.class));

    }

    @Test
    public void skalFeileHvisObligatoriskOppslagFeiler() {
        RegistryKlient klient = RegistryKlientImpl
                .med(mockWebResource)
                .obligatoriskOppslag("urn:oppslag-som-feiler")
                .bygg();

        assertThat(klient.obligatoriskeOppslagSomMangler()).contains("urn:oppslag-som-feiler");
    }

    @Test
    public void skalIkkeFeilrapportereManglendeObligatoriskeOppslag() {
        RegistryKlient klient = RegistryKlientImpl
                .med(mockWebResource)
                .overstyringProperty(OverstyringPropertiesReaderTest.OVERSTYRTE_TJENESTER_TESTFIL)
                .obligatoriskOppslag("urn:oppslag-som-feiler")
                .obligatoriskOppslag("urn:ske.skatteetaten.eksempel")
                .bygg();

        assertThat(klient.obligatoriskeOppslagSomMangler()).containsOnly("urn:oppslag-som-feiler");
    }

    @Test
    public void skalReturnereNullHvisIkkeFinnesIRegistry() {
        RegistryOppslagKlient oppslagKlient = RegistryKlientImpl
                .med(mockWebResource)
                .bygg();

        when(mockWebResource.get(any(GenericType.class))).thenReturn(null);

        assertThat(oppslagKlient.finnTjenesteURI("urn:tjeneste")).isNull();

    }

    @Test
    public void skalSendeRegistreringHvisTilbyrTjeneste() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .bygg();

        klient.start();
        klient.stopp();

        verify(mockWebResource).entity(registrering, MediaType.APPLICATION_JSON);

    }

    @Test
    public void skalSendeRegistreringHvisNyRegistrering() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .bygg();

        klient.start();

        verify(mockWebResource).entity(registrering, MediaType.APPLICATION_JSON);

        registrering = lagRegistreringDTO();
        klient.registrer(registrering);

        klient.stopp();

        verify(mockWebResource).entity(registrering, MediaType.APPLICATION_JSON);

    }

    @Test
    public void skalSendeAvregistrering() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .bygg();

        klient.start();
        klient.stopp();
        klient.avregistrer("urn:tjeneste");
        assertThat(registrering.getTjenesteliste()).isEmpty();
        verify(mockWebResource, atLeast(2)).entity(registrering, MediaType.APPLICATION_JSON);
    }

    @Test
    public void skalIkkeSendeAvregistreringHvisIkkeFjernet() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .bygg();

        klient.start();
        klient.stopp();
        klient.avregistrer("urn:ikke_registrert");
        assertThat(registrering.getTjenesteliste()).isNotEmpty();
        verify(mockWebResource).entity(registrering, MediaType.APPLICATION_JSON);
    }

    @Test
    public void skalSendeStopp() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .bygg();

        klient.start();
        klient.stopp();
        verify(mockWebResource, atLeast(1)).entity(registrering, MediaType.APPLICATION_JSON);
        verify(mockWebResource, atLeast(2)).entity(anyObject(), anyString());
    }

    @Test
    public void skalResendeRegistreringHvisFeilet() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .bygg();

        when(mockWebResource.entity(registrering, MediaType.APPLICATION_JSON)).thenThrow(new ClientHandlerException());
        klient.start();
        klient.registreringEllerPulsTask();
        klient.stopp();
        verify(mockWebResource, atLeast(2)).entity(registrering, MediaType.APPLICATION_JSON);

    }

    @Test(expected = RuntimeException.class)
    public void skalFeileHvisRegistreringFeiletOgKonfigurertTilAAFeile() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .feilHvisRegistreringFeiler(true)
                .timerStartDelay(0)
                .bygg();

        when(mockWebResource.entity(registrering, MediaType.APPLICATION_JSON)).thenThrow(new ClientHandlerException());
        klient.start();

    }

    @Test
    public void skalResendeRegistreringMedTimer() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .bygg();

        when(mockWebResource.entity(registrering, MediaType.APPLICATION_JSON)).thenThrow(new ClientHandlerException());
        klient.start();
        Thread.sleep(50);
        klient.stopp();
        verify(mockWebResource, atLeast(2)).entity(registrering, MediaType.APPLICATION_JSON);

    }

    @Test
    public void skalSendePulsHvisTilbyrTjeneste() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .bygg();

        klient.start();
        klient.registreringEllerPulsTask();
        klient.stopp();
        verify(mockWebResource, atLeastOnce()).entity(any(PulsDTO.class), anyString());
    }

    @Test
    public void skalSendeKlientinfo() {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .bygg();

        klient.start();
        klient.registreringEllerPulsTask();
        klient.stopp();
        verify(mockWebResource, atLeastOnce()).entity(any(KlientInfoDTO.class), anyString());
    }

    @Test
    public void skalSendePulsMedTimer() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .bygg();

        klient.start();
        Thread.sleep(50);
        klient.stopp();
        verify(mockWebResource, atLeastOnce()).entity(any(PulsDTO.class), anyString());

    }

    @Test
    public void skalFortsattSendePulsSelvOmFeiler() throws InterruptedException {
        RegistreringDTO registrering = lagRegistreringDTO();

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .tjenesteregistrering(registrering)
                .timerStartDelay(0)
                .registreringIntervall(10)
                .bygg();

        doThrow(new ClientHandlerException()).when(mockWebResource).post();
        klient.start();
        Thread.sleep(50);
        klient.stopp();
        verify(mockWebResource, atLeast(2)).entity(any(PulsDTO.class), anyString());

    }

    @Test
    public void skalRefresheCacheMedTimer() throws InterruptedException {
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .timerStartDelay(0)
                .cacheRefreshIntervall(10)
                .autoRefreshCachedeOppslag(true)
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        klient.start();
        klient.finnTjenesteURI("urn:tjeneste");
        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:tjeneste", Collections.singletonList(dto)));
        Thread.sleep(50);
        klient.stopp();
        verify(mockWebResource, atLeast(2)).get(any(GenericType.class));

    }

    @Test
    public void skalRefresheCache() {
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        klient.start();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));
        klient.finnTjenesteURI("urn:tjeneste");
        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:tjeneste", Collections.singletonList(dto)));
        klient.cacheRefreshTask();
        klient.stopp();
        verify(mockWebResource, atLeast(2)).get(any(GenericType.class));

    }

    @Test
    public void skalFjerneRegistreringFraCache() {
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        klient.start();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));
        klient.finnTjenesteURI("urn:tjeneste");
        assertThat(klient.cachedeOppslag.containsKey("urn:tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.emptyMap());
        klient.cacheRefreshTask();
        klient.stopp();
        assertThat(klient.cachedeOppslag).isEmpty();
    }

    @Test
    public void skalIkkeFjerneRegistreringFraCacheDersomHentingFraServerFeiler() {
        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .bygg();

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));

        klient.start();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));
        klient.finnTjenesteURI("urn:tjeneste");
        assertThat(klient.cachedeOppslag.containsKey("urn:tjeneste"));
        doThrow(mock(UniformInterfaceException.class)).when(mockWebResource).get(any(GenericType.class));
        klient.cacheRefreshTask();
        klient.stopp();
        assertThat(klient.cachedeOppslag).isNotEmpty();
    }

    @Test
    public void skalSendeKlientInfoHvisCachetOppslagIkkeLengerErTilgjengelig() {

        final TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .autoRefreshCachedeOppslag(false)
                .tjenesteregistrering(lagRegistreringDTO())
                .bygg();

        klient.finnTjenesteURI("urn:tjeneste_som_skal_i_cache");

        klient.start();

        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.emptyMap());

        klient.cacheRefreshTask();

        verify(mockWebResource, times(2)).path("klientinfo");

        klient.stopp();
    }

    @Test
    public void skalIkkeSendeKlientInfoHvisCacheIkkeEndresEtterRefresh() {

        final TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .autoRefreshCachedeOppslag(false)
                .tjenesteregistrering(lagRegistreringDTO())
                .bygg();

        klient.finnTjenesteURI("urn:tjeneste_som_skal_i_cache");
        klient.start();
        verify(mockWebResource, times(1)).path("klientinfo");

        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:tjeneste_som_skal_i_cache", Collections.singletonList(dto)));

        klient.cacheRefreshTask();

        verify(mockWebResource, times(1)).path("klientinfo");

        klient.stopp();
    }

    @Test
    public void skalSendeKlientInfoHvisCacheEndresEtterRefresh() {

        final TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .autoRefreshCachedeOppslag(false)
                .tjenesteregistrering(lagRegistreringDTO())
                .bygg();

        klient.finnTjenesteURI("urn:tjeneste_som_skal_i_cache");
        klient.start();
        verify(mockWebResource, times(1)).path("klientinfo");

        dto.setKlientUri(URI.create("http://endret_uri"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:tjeneste_som_skal_i_cache", Collections.singletonList(dto)));

        klient.cacheRefreshTask();

        verify(mockWebResource, times(2)).path("klientinfo");

        klient.stopp();
    }

    @Test
    public void skalFortsetteAaSjekkeObligatoriskeOppslagSelvOmDetHarVaertBorte() {

        final TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .obligatoriskOppslag("urn:obligatorisk_tjeneste")
                .autoRefreshCachedeOppslag(false)
                .bygg();

        klient.finnTjenesteURI("urn:annen_tjeneste_som_skal_i_cache");

        klient.start();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isEmpty();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:annen_tjeneste_som_skal_i_cache", Collections.singletonList(dto)));

        klient.cacheRefreshTask();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isNotEmpty();

        when(mockWebResource.get(any(GenericType.class))).thenReturn(ImmutableMap.builder()
                .put("urn:annen_tjeneste_som_skal_i_cache", Collections.singletonList(dto))
                .put("urn:obligatorisk_tjeneste", Collections.singletonList(dto))
                .build());

        klient.cacheRefreshTask();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isEmpty();
        klient.stopp();
    }

    @Test
    public void skalFortsetteAaSjekkeObligatoriskeOppslagSelvOmDetHarVaertBorteOgDetIkkeErAndreOppslag() {

        TjenesteDTO dto = new TjenesteDTO();
        dto.setKlientUri(URI.create("http://tjeneste"));
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.singletonList(dto));

        RegistryKlientImpl klient = (RegistryKlientImpl) RegistryKlientImpl
                .med(mockWebResource)
                .obligatoriskOppslag("urn:tjeneste")
                .autoRefreshCachedeOppslag(false)
                .bygg();

        klient.start();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isEmpty();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(Collections.emptyMap());
        klient.cacheRefreshTask();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isNotEmpty();
        when(mockWebResource.get(any(GenericType.class))).thenReturn(
                Collections.singletonMap("urn:tjeneste", Collections.singletonList(dto)));
        klient.cacheRefreshTask();
        assertThat(klient.obligatoriskeOppslagSomMangler()).isEmpty();
        klient.stopp();
    }

    @Test
    public void skalVareTraadsikkerForCaching() {
        final RegistryKlientImpl oppslagKlient = (RegistryKlientImpl) RegistryKlientImpl.med(mockWebResource)
                .overstyringProperty("false").autoRefreshCachedeOppslag(false)
                .bygg();
        TjenesteDTO dto = new TjenesteDTO();

        int antallIterasjoner = 20;
        oppslagKlient.restKlient = klient;
        oppslagKlient.start();

        when(klient.finnTjeneste("urn:tjeneste")).thenReturn(URI.create("tjeneste"));
        HashMap<String, URI> value = new HashMap<>();
        for (int i = 0; i < antallIterasjoner; i++) {
            value.put("urn:tjeneste" + i, URI.create("tjeneste" + i));
            when(klient.finnTjeneste("urn:tjeneste" + i)).thenReturn(URI.create("tjeneste" + i));
        }
        when(klient.finnTjenester(anyCollection())).thenReturn(value);

        RunIt target = new RunIt() {
            private volatile boolean kjoer = true;
            private volatile Exception ex;

            @Override
            public void run() {
                while (kjoer) {
                    try {
                        oppslagKlient.cacheRefreshTask();
                    } catch (Exception e) {
                        ex = e;
                        throw new RuntimeException("cacheRefreshTask()");
                    }
                }
            }

            @Override
            public void stopp() {
                kjoer = false;
            }

            @Override
            public boolean harex() {
                return ex != null;
            }

        };
        Thread thread = new Thread(target);
        try {
            thread.start();
            for (int i = 0; i < antallIterasjoner; i++) {
                dto.setKlientUri(URI.create("http://tjeneste"));
                oppslagKlient.finnTjenesteURI("urn:tjeneste" + i);
            }
        } catch (Exception e) {
            fail("Uventet ex");
        } finally {
            target.stopp();
        }
        if (target.harex()) {
            fail("cacheRefreshTask skal ikke kaste exception");
        }

    }

    private RegistreringDTO lagRegistreringDTO() {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("test").tjeneste("urn:tjeneste", null, null).bygg();
        return registrering;
    }

    interface RunIt extends Runnable {
        void stopp();

        boolean harex();
    }

}
