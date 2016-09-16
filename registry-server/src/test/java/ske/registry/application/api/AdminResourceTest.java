package ske.registry.application.api;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import mag.felles.konfig.KonfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ske.mag.test.kategorier.Komponenttest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;
import ske.registry.repository.InMemoryRegistryRepository;
import ske.registry.service.RegistryService;
import ske.registry.service.RegistryServiceBean;

@Category(Komponenttest.class)
public class AdminResourceTest {

    @Mock
    private HttpServletRequest servletRequest;

    private RegistryService service;

    @Mock
    private KonfigFactory konfigFactory;

    @Before
    public void settopp() {
        MockitoAnnotations.initMocks(this);
        when(servletRequest.getRemoteHost()).thenReturn("localhost");
        service = new RegistryServiceBean(new InMemoryRegistryRepository(1000L, 500L, 2000L));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalIkkeGiConcurrentException() throws InterruptedException, BrokenBarrierException {
        final TjenesteResource resource = new TjenesteResource(service);
        final RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        final AdminResource adminResource = new AdminResource(service, null);
        final AtomicBoolean fikkConcurrentException = new AtomicBoolean(false);
        final int size = 500;

        final CyclicBarrier startBarrier = new CyclicBarrier(3);
        final CyclicBarrier assertBarrier = new CyclicBarrier(4);

        Runnable leggTilRegistreringer = new Runnable() {
            @Override
            public void run() {
                try {
                    startBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                for (int i = 0; i < size; i++) {
                    registrering.setTilbyderId(UUID.randomUUID());
                    resource.leggInnTjeneste(registrering, servletRequest);
                }
                try {
                    assertBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Runnable hentRegistreringer = new Runnable() {
            @Override
            public void run() {
                try {
                    startBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                try {
                    for (int i = 0; i < size; i++) {
                        adminResource.registreringer();
                    }
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                    fikkConcurrentException.set(true);
                }
                try {
                    assertBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new Thread(leggTilRegistreringer).start();
        new Thread(hentRegistreringer).start();
        new Thread(hentRegistreringer).start();

        assertBarrier.await();

        Collection<TimestampetRegistreringDTO> registreringer = (Collection<TimestampetRegistreringDTO>) adminResource.registreringer()
                .getEntity();

        assertThat(fikkConcurrentException.get()).isFalse();
        assertThat(registreringer.size()).isEqualTo(size);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalLevereRegistrering() {
        TjenesteResource resource = new TjenesteResource(service);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();

        resource.leggInnTjeneste(registrering, servletRequest);

        AdminResource adminResource = new AdminResource(service, null);
        Collection<TimestampetRegistreringDTO> registreringer = (Collection<TimestampetRegistreringDTO>) adminResource.registreringer()
                .getEntity();

        assertThat(registreringer.iterator().next().getRegistrering()).isEqualTo(registrering);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalLevereAktiveTjenester() {
        TjenesteResource resource = new TjenesteResource(service);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        RegistreringDTO registrering2 = lagRegistreringMedEnTjeneste();

        resource.leggInnTjeneste(registrering, servletRequest);
        resource.leggInnTjeneste(registrering2, servletRequest);

        AdminResource adminResource = new AdminResource(service, null);
        Collection<TjenesteDTO> tjenester = (Collection<TjenesteDTO>) adminResource.aktiveTjenester().getEntity();

        assertThat(tjenester).contains(registrering.getTjenesteliste().get(0), registrering2.getTjenesteliste().get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void registreringSkalBliInaktiv() throws InterruptedException {
        service = new RegistryServiceBean(new InMemoryRegistryRepository(1L, 500L, 2000L));
        TjenesteResource resource = new TjenesteResource(service);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        resource.leggInnTjeneste(registrering, servletRequest);
        AdminResource adminResource = new AdminResource(service, null);
        Thread.sleep(2);
        Collection<TjenesteDTO> tjenester = (Collection<TjenesteDTO>) adminResource.aktiveTjenester().getEntity();
        assertThat(tjenester).excludes(registrering.getTjenesteliste().get(0));
        tjenester = (Collection<TjenesteDTO>) adminResource.inaktiveTjenester().getEntity();
        assertThat(tjenester).contains(registrering.getTjenesteliste().get(0));
    }

    private RegistreringDTO lagRegistreringMedEnTjeneste() {
        return RegistreringDTO
                .medTilbyderNavn("test")
                .tjeneste("urn:test", URI.create("http://host/uri_til_test"), URI.create("http://host/uri_til_test"), null)
                .bygg();
    }

}
