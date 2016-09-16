package ske.registry.module;

import static com.google.common.collect.Maps.newHashMap;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.reader.ClassReaders;
import mag.felles.konfig.impl.KonfigUtils;
import mag.felles.rest.filter.server.TraceRequestFilter;
import mag.felles.sikkerhet.rest.filter.AutentiseringServerfilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.application.api.PingResource;
import ske.registry.server.ServletExceptionLoggingFilter;

public class GuiceServletConfig extends GuiceServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Injector getInjector() {
        final String pakker = Joiner.on(",").join(ImmutableList.of(
                PingResource.class.getPackage().getName(),
                "com.wordnik.swagger.jersey.listing"
                ));

        Injector injector = Guice.createInjector(new RegistryModule(),
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {

                        Map<String, String> initParams = newHashMap();
                        initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES, pakker);
                        initParams.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

                        bootstrapSwagger();

                        // Sikkerhet STS skal gjelder kun for tjenesteregistreringer
                        filterRegex("/tjeneste").through(AutentiseringServerfilter.class);

                        filter("/*").through(ServletExceptionLoggingFilter.class);
                        filter("*").through(TraceRequestFilter.class);
                        bind(ServletExceptionLoggingFilter.class).in(Scopes.SINGLETON);
                        filter("/*").through(GuiceContainer.class, initParams);
                    }
                });
        if (KonfigUtils.terminerEtterKonfigLoggingFlagSatt()) {
            logger.info("Flagg satt for å terminere etter konfig har blitt logget, avslutter applikasjonen");
            // Ikke helt pent å kalle System.exit. Men her har ThreadPools blitt opprettet av Grizzly.
            System.exit(0);
        }
        return injector;
    }

    private void bootstrapSwagger() {
        ApiInfo info = new ApiInfo(
                "Oppslagstjenesten REST-API", /* title */
                "Dette er dokumentasjonen til Oppslagstjenesten REST-API", /* Description */
                "", /* TOS URL */
                "mag@skatteetaten.no", /* Contact */
                "", /* license */
                "" /* license URL */
        );

        OverrideConverter converter = new OverrideConverter();
        converter.add(URI.class.getName(), "{\"id\":\"URI\"}");
        converter.add(UUID.class.getName(), "{\"id\":\"UUID\"}");
        ModelConverters.addConverter(converter, true);

        ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage(PingResource.class.getPackage().getName());
        ScannerFactory.setScanner(scanner);

        SwaggerConfig config = ConfigFactory.config();
        config.setApiVersion("0.0");
        config.setApiInfo(info);
        config.setBasePath("http://localhost:10101/registry");
        ConfigFactory.setConfig(config);
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());
    }

}
