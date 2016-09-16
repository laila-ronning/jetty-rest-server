package ske.registry.module;

import javax.inject.Singleton;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;
import mag.felles.metrics.MagMetricsUtil;
import mag.felles.ressurs.Apphome;
import mag.felles.sikkerhet.rest.filter.AutentiseringServerfilter;
import mag.felles.sikkerhet.rest.filter.SikkerhetspolicyProvider;
import mag.felles.sikkerhet.rest.provider.server.AutentiseringServerfilterProvider;
import mag.felles.sikkerhet.rest.token.TokenValidator;

import ske.registry.repository.InMemoryRegistryRepository;
import ske.registry.repository.RegistryRepository;
import ske.registry.service.RegistryService;
import ske.registry.service.RegistryServiceBean;

public class RegistryModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new KonfigModule(Apphome.getStandaloneApphome(), "registry", new RegistryModuleKonfig().getConfigKeys()));
        bind(RegistryService.class).to(RegistryServiceBean.class).in(Singleton.class);
        bind(RegistryRepository.class).to(InMemoryRegistryRepository.class).in(Singleton.class);

        bind(SikkerhetspolicyProvider.class).to(RegistrySikkerhetspolicyProvider.class).in(Singleton.class);
        bind(TokenValidator.class).toProvider(RegistryTokenValidatorProvider.class).in(Singleton.class);
        bind(AutentiseringServerfilter.class).toProvider(AutentiseringServerfilterProvider.class).in(Singleton.class);

        MetricRegistry metrics = MagMetricsUtil.lagDefaultMetrics();
        bind(MetricRegistry.class).toInstance(metrics);
        install(new MetricsInstrumentationModule(metrics));
    }

}
