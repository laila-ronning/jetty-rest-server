package ske.registry.server;

import static ske.registry.module.RegistryModuleKonfig.STS_SIKKERHETSPOLICY_KEY;
import static ske.registry.module.RegistryModuleKonfig.STS_UTSTED_SAML_KEY;
import static ske.registry.module.RegistryModuleKonfig.STS_VALIDER_SAML_KEY;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.UriBuilder;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import mag.felles.konfig.KonfigFactory;
import mag.felles.konfig.KonfigKey;
import mag.felles.konfigurasjon.STSTjenesteKonstanter;
import mag.felles.log.LoggingUtil;
import mag.felles.ressurs.Apphome;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.servlet.WebappContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.dto.RegistreringDTO;
import ske.registry.module.GuiceServletConfig;
import ske.registry.repository.RegistryRepository;

public final class RegistryServer {

    private static final String PROPERTY_SOM_HENTES_UT_I_LOGGING_UTIL = "LOG_APP_NAME";

    public static final String URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML = STSTjenesteKonstanter.URN_STS_UTSTED_SAML;
    public static final String URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML = STSTjenesteKonstanter.URN_STS_VALIDER_SAML;
    public static final String SIKKERHETSPOLICY_TJENESTEATTRIBUTT = STSTjenesteKonstanter.STS_SIKKERHETSPOLICY_TJENESTEATTRIBUTT;
    public static final int DEFAULT_HTTP_PORT = 20180;

    private static final Logger logger = LoggerFactory.getLogger(RegistryServer.class);

    private final int httpPort;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private CountDownLatch startUpLatch = new CountDownLatch(1);

    public RegistryServer(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void kjoerServer() throws Exception {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            URI uri = UriBuilder.fromUri("http://" + hostName).port(httpPort).build();
            HttpServer httpServer = GrizzlyServerFactory.createHttpServer(uri, (HttpHandler) null);
            httpServer.addListener(new NetworkListener("local", "localhost", httpPort));

            WebappContext ctx = new WebappContext("Registry", "/registry");
            ctx.addListener(GuiceServletConfig.class);
            ctx.addFilter("GuiceFilter", GuiceFilter.class).addMappingForUrlPatterns(null, "/*");
            ctx.addFilter("ServletErrorLoggingFilter", ServletExceptionLoggingFilter.class).addMappingForUrlPatterns(null, "/*");
            ctx.addServlet("default", new HttpServlet() {
                private static final long serialVersionUID = 4216706630095432734L;

                @SuppressWarnings("unused")
                public void service() throws ServletException {
                }
            }).addMapping("");

            ctx.deploy(httpServer);

            httpServer.start();

            Injector injector = (Injector) ctx.getAttribute(Injector.class.getName());

            injector.getInstance(RegistryRepository.class).settRegistryStartet();
            injector.getInstance(RegistryRepository.class).pollInaktiveRegistreringer();
            injector.getInstance(RegistryRepository.class).registrerTjenester(lagRegistrering(injector));

            logger.info("{ \"beskrivelse\" : \"Oppslagstjenesten startet på port {}.\",\"port\":\"{}\" }", httpPort, httpPort);

            PrintWriter console = new PrintWriter(System.out);
            console.println("Lytter på http-port " + httpPort + ".");
            console.println("Trykk CTRL + c eller send SIGTERM (kill <pid>) for å stoppe.");
            console.flush();

            try {
                startUpLatch.countDown();
                shutdownLatch.await();
            } catch (InterruptedException e) {
                logger.error("{ \"beskrivelse\" : \"Oppslagstjenesten avbrutt.\" }", e);
            }
            httpServer.stop();
        } finally {
            logger.info("{ \"beskrivelse\" : \"Oppslagstjenesten stoppet.\" }");
        }
    }

    public void stopp() {
        shutdownLatch.countDown();
    }

    // Brukes av Zookeeper
    public void ventTilHarStartet() {
        try {
            startUpLatch.await();
        } catch (InterruptedException e) {
            logger.error("Feil ved venting på at RegistryServer skulle starte.", e);
        }
    }

    public static void main(String[] args) throws Exception {
        logging();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("{ \"beskrivelse\" : \"Oppslagstjenesten stoppet.\" }");
            }
        });
        lagOgStartServer(args);
    }

    private static void logging() {
        Map<String, String> loggKontekst = new HashMap<>();
        String loggnavn = System.getProperty(PROPERTY_SOM_HENTES_UT_I_LOGGING_UTIL);
        loggKontekst.put("loggnavn", (loggnavn == null || loggnavn.equals("")) ? "registry-server" : loggnavn);
        LoggingUtil.konfigurerLogbackLogging(Apphome.getStandaloneApphome(), loggKontekst);
    }

    private static void lagOgStartServer(String[] args) throws Exception {
        RegistryServer server = new RegistryServer(hentPort(args));
        server.kjoerServer();
    }

    private static Integer hentPort(String[] args) {
        return args.length < 1 ? DEFAULT_HTTP_PORT : Integer.valueOf(args[0]);
    }

    private RegistreringDTO lagRegistrering(Injector injector) {
        Map<KonfigKey<?>, Object> konfigKeyObjectMap = injector.getInstance(KonfigFactory.class).hentAlleKonfigurasjoner();

        String utstedSamlUri = konfigKeyObjectMap.get(STS_UTSTED_SAML_KEY).toString();
        String validerSamlUri = konfigKeyObjectMap.get(STS_VALIDER_SAML_KEY).toString();
        String sikkerhetspolicy = konfigKeyObjectMap.get(STS_SIKKERHETSPOLICY_KEY).toString();

        String host = "";
        String port = "";

        try {
            URI uri = new URI(validerSamlUri);
            host = uri.getHost().split("\\.")[0];
            port = String.valueOf(uri.getPort());
        } catch (URISyntaxException e) {
            logger.error("STS-tjenesten har ugyldig format", e);
        }

        return RegistreringDTO
                .medTilbyderNavn("Security Token Service")
                .applikasjonsgruppe("Drift")
                .komponent("Security Token Service")
                .hostOgPort(host + ":" + port)
                .leggTilEgendefinertInfo(SIKKERHETSPOLICY_TJENESTEATTRIBUTT, sikkerhetspolicy)
                .tjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML, null, URI.create(utstedSamlUri),
                        "Tjeneste for å utstede SAML-token")
                .tjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML, null, URI.create(validerSamlUri),
                        "Tjeneste for å validere SAML-token")
                .bygg();
    }

}
