package ske.registry.module;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import ske.registry.application.api.PingResource;
import ske.registry.module.RegistryModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GuiceTestServletConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new RegistryModule(),
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        Map<String, String> initParams = newHashMap();
                        initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES, PingResource.class.getPackage().getName());
                        initParams.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
                        filter("/*").through(GuiceContainer.class, initParams);
                    }
                }
                );
    }
}
