package ske.registry.klient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TjenesteklientForURNFactory<T> {

    private static final Logger logger = LoggerFactory.getLogger(TjenesteklientForURNFactory.class);
    private final RegistryKlient registryKlient;
    private final String urn;
    private final T tjenesteklientProxy;

    /**
     * Merk: Forutsetter at klienten implementerer minst ett interface.
     *
     * @param registryKlient
     * @param urn            (logisk navn) til tjenesten som klienten skal spørre oppslagstjenesten om.
     */
    public TjenesteklientForURNFactory(RegistryKlient registryKlient, String urn) {
        this(registryKlient, urn, null);
    }

    /**
     * @param registryKlient
     * @param urn               (logisk navn) til tjenesten som klienten skal spørre oppslagstjenesten om.
     * @param tjenesteinterface Interface som klienten av typen T implementerer
     */
    public TjenesteklientForURNFactory(RegistryKlient registryKlient, String urn, Class tjenesteinterface) {
        this.registryKlient = registryKlient;
        this.urn = urn;
        this.tjenesteklientProxy = lagProxy(tjenesteinterface != null ? new Class[]{tjenesteinterface} : null);
    }

    public T getTjenesteklient() {
        return tjenesteklientProxy;
    }
    
    /**
     * Sjekk om tjenesteklienten finnes. {@link #getTjenesteklient()} returnerer et proxy-objekt som ikke gir mulighet
     * for å sjekke om objektet proxyen representerer er null, dvs om det fins i oppslagstjenesten.
     * <p>
     * <b>Sideeffekter:</b> man gjør et oppslag mot oppslagstjenesten for å finne den gjeldende verdi 
     * </p>
     * @return true hvis tjenesteklienten fins
     */
    public boolean finsTjenesteklient() {
        Proxyinfo proxyinfo = (Proxyinfo)tjenesteklientProxy;
        boolean finsTjenesteklient = false;
        if (proxyinfo != null) {
            try {
                proxyinfo.oppdaterProxyinstans();
                Object instans = proxyinfo.hentProxyinstans();
                finsTjenesteklient = instans != null;
            } catch (TjenesteUtilgjengeligException e) {
                finsTjenesteklient = false;
            }
        } 
        return finsTjenesteklient;
    }

    @SuppressWarnings("unchecked")
    private T lagProxy(Class[] tjenesteinterfaces) {
        if (tjenesteinterfaces == null || tjenesteinterfaces.length == 0) {
            tjenesteinterfaces = byggTjenesteklient(URI.create("http://dummy.uri")).getClass().getInterfaces();
        }
        if (tjenesteinterfaces.length == 0) {
            throw new IllegalArgumentException("Klienten må implementere et interface");
        }

        ArrayList<Class> interfaces = new ArrayList<>();
        Collections.addAll(interfaces, tjenesteinterfaces);
        interfaces.add(Proxyinfo.class);

        return (T) Proxy.newProxyInstance(tjenesteinterfaces[0].getClassLoader(),
                interfaces.toArray(new Class[interfaces.size()]),
                new OppslagstjenesteInvocationHandler()
        );
    }

    private class OppslagstjenesteInvocationHandler implements InvocationHandler {

        private URI aktivUri;
        private T tjenesteklient;

        private synchronized T oppdatertTjenesteklient() {
            if (tjenesteklient == null) {
                byggTjenesteklientFoersteGang();
            } else {
                URI ferskURI = registryKlient.finnTjenesteURI(urn);
                if (ferskURI == null) {
                    throw new TjenesteUtilgjengeligException(urn, "Tjeneste-uri er ikke lenger tilgjengelig");
                } else if (!ferskURI.equals(aktivUri)) {
                    aktivUri = ferskURI;
                    logger.trace("Lager ny instans av klient (for uri {}).", aktivUri);
                    tjenesteklient = byggTjenesteklient(aktivUri);
                }
            }
            return tjenesteklient;
        }

        private void byggTjenesteklientFoersteGang() {
            aktivUri = registryKlient.finnTjenesteURI(urn);
            if (aktivUri == null) {
                throw new TjenesteUtilgjengeligException(urn, "Ingen tilgjengelig tjeneste-uri ved første oppslag");
            }
            logger.trace("Oppretter instans av klient (for uri {}).", aktivUri);
            tjenesteklient = byggTjenesteklient(aktivUri);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("oppdaterProxyinstans".equals(method.getName())) {
                oppdatertTjenesteklient();
                return null;
            }
            if ("hentProxyinstans".equals(method.getName())) {
                return tjenesteklient;
            }
            try {
                return method.invoke(oppdatertTjenesteklient(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    protected abstract T byggTjenesteklient(URI uri);

    interface Proxyinfo {
        void oppdaterProxyinstans();
        Object hentProxyinstans();
    }

}
