package ske.registry.klient;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ske.mag.test.kategorier.Enhetstest;

@Category(Enhetstest.class)
@RunWith(MockitoJUnitRunner.class)
public class TjenesteklientForURNFactoryTest {

    private static final String URN_TEST = "urn:test";
    @Mock
    private RegistryKlient registryKlient;
    @Mock
    private Klientinterface klientinterface;

    @Before
    public void settopp() {
        when(registryKlient.finnTjenesteURI(URN_TEST)).thenReturn(URI.create("http://test1.uri"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalFeilHvisKlientIkkeImplementererInterface() {
        new KlientUtenInterface(registryKlient, "urn:test");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalFeilHvisOppslagstjenestenIkkeFinnerTjeneste() {
        Klientinterface tjenesteklient = new KlientSomBrukerURN(registryKlient, "urn:som:ikke:fins").getTjenesteklient();
        tjenesteklient.getUri();
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalFeilHvisOppslagstjenestenIkkeFinnerTjenesteVedAndreKall() {
        Klientinterface tjenesteklient = new KlientSomBrukerURN(registryKlient, URN_TEST).getTjenesteklient();
        tjenesteklient.getUri();

        when(registryKlient.finnTjenesteURI(URN_TEST)).thenReturn(null);
        tjenesteklient.getUri();
    }

    @Test
    public void skalLageDynamiskTjeneste() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteklient = tjenesteKlientSomOppdatererURI.getTjenesteklient();

        assertThat(Proxy.isProxyClass(tjenesteklient.getClass())).isTrue();
    }

    @Test
    public void skalLevereKorrektUri() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteklient = tjenesteKlientSomOppdatererURI.getTjenesteklient();

        assertThat(((Annetinterface) tjenesteklient).foo()).isEqualTo("bar:http://test1.uri");
    }

    @Test
    public void skalImplementereAlleInterfaces() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteklient = tjenesteKlientSomOppdatererURI.getTjenesteklient();

        assertThat(((Annetinterface) tjenesteklient).foo()).isEqualTo("bar:http://test1.uri");
        when(registryKlient.finnTjenesteURI("urn:test")).thenReturn(URI.create("http://test1.uri.endret"));

        assertThat(((Annetinterface) tjenesteklient).foo()).isEqualTo("bar:http://test1.uri.endret");
    }

    @Test
    public void skalIkkeLageNyInstansHvisUriErUendret() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteKlient = tjenesteKlientSomOppdatererURI.getTjenesteklient();
        tjenesteKlient.getUri();
        when(registryKlient.finnTjenesteURI("urn:test")).thenReturn(URI.create("http://test1.uri.endret"));
        tjenesteKlient.getUri();
        Object klient = ((TjenesteklientForURNFactory.Proxyinfo) tjenesteKlient).hentProxyinstans();
        tjenesteKlient.getUri();

        assertThat(klient).isSameAs(((TjenesteklientForURNFactory.Proxyinfo) tjenesteKlient).hentProxyinstans());
    }

    @Test
    public void skalLevereOppdatertTjenesteHvisURIErEndret() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteKlient = tjenesteKlientSomOppdatererURI.getTjenesteklient();
        when(registryKlient.finnTjenesteURI("urn:test")).thenReturn(URI.create("http://test1.uri.endret"));

        assertThat(tjenesteKlient.getUri()).isEqualTo(URI.create("http://test1.uri.endret"));
    }

    @Test(expected = MinKlientCheckedException.class)
    public void skalProxyeCheckedExceptoins() throws Exception {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        Klientinterface tjenesteklient = tjenesteKlientSomOppdatererURI.getTjenesteklient();

        tjenesteklient.getUriMedCheckedExcepton();
    }

    @Test(expected = MinKlientRuntimeException.class)
    public void skalProxyeRuntimeExceptions() throws Exception {
        when(klientinterface.getUri())
                .thenReturn(URI.create("urn:test"))
                .thenThrow(new MinKlientRuntimeException());
        TjenesteklientForURNFactory<Klientinterface> factory = new TjenesteklientForURNFactory<Klientinterface>(registryKlient, "urn:test") {

            @Override
            protected Klientinterface byggTjenesteklient(URI uri) {
                return klientinterface;
            }
        };

        Klientinterface tjenesteklient = factory.getTjenesteklient();
        try {
            tjenesteklient.getUri();
        } catch (Throwable e) {
            fail("skal ikke kaste exception ved første kall", e);
        }
        tjenesteklient.getUri();
    }

    @Test
    public void tjenesteSkalIkkeFinnesHvisOppslagstjenestenIkkeFinnerTjeneste() {
        KlientSomBrukerURN tjenesteklientProxy = new KlientSomBrukerURN(registryKlient, "urn:som:ikke:fins");

        assertThat(tjenesteklientProxy.finsTjenesteklient()).isFalse();
    }

    @Test
    public void tjenesteSkalFinnesHvisOppslagstjenestenFinnerTjeneste() {
        KlientSomBrukerURN tjenesteKlientSomOppdatererURI = new KlientSomBrukerURN(registryKlient, "urn:test");
        boolean fins = tjenesteKlientSomOppdatererURI.finsTjenesteklient();
        assertThat(tjenesteKlientSomOppdatererURI.finsTjenesteklient()).isTrue();
    }

    interface Klientinterface {

        URI getUri();

        URI getUriMedCheckedExcepton() throws MinKlientCheckedException;
    }

    interface Annetinterface {

        String foo();
    }

    class KlientSomBrukerURI implements Annetinterface, Klientinterface {

        private URI uri;

        public KlientSomBrukerURI(URI uri) {
            this.uri = uri;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public URI getUriMedCheckedExcepton() throws MinKlientCheckedException {
            throw new MinKlientCheckedException();
        }

        @Override
        public String foo() {
            return "bar:" + uri;
        }
    }

    class KlientSomBrukerURN extends TjenesteklientForURNFactory<Klientinterface> {

        public KlientSomBrukerURN(RegistryKlient registryKlient, String urn) {
            super(registryKlient, urn);
        }

        @Override
        protected Klientinterface byggTjenesteklient(URI uri) {
            return new KlientSomBrukerURI(uri);
        }
    }

    class KlasseSomIkkeImplementererInterface {

        public String foo() {
            return "bar";
        }
    }

    class KlientUtenInterface extends TjenesteklientForURNFactory<KlasseSomIkkeImplementererInterface> {

        public KlientUtenInterface(RegistryKlient registryKlient, String urn) {
            super(registryKlient, urn);
        }

        @Override
        protected KlasseSomIkkeImplementererInterface byggTjenesteklient(URI uri) {
            return new KlasseSomIkkeImplementererInterface();
        }
    }

    class MinKlientRuntimeException extends RuntimeException {
    }

    class MinKlientCheckedException extends Exception {
    }

}
