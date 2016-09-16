package ske.registry.module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mag.felles.konfig.KonfigFactory;
import mag.felles.konfig.KonfigKey;
import mag.felles.konfig.impl.KonfigLaster;
import mag.felles.konfig.impl.TypetListeKonfigKey;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

public class KonfigModule extends AbstractModule {

    private final KonfigFactory konfigFactory;

    public KonfigModule(String appHome, String propertyFilnavn, List<? extends KonfigKey<?>> konfigKeys) {
        konfigFactory = KonfigLaster.hentKonfigFraLastekjeden(appHome, propertyFilnavn, konfigKeys);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        Map<KonfigKey<?>, Object> konfigurasjoner = konfigFactory.hentAlleKonfigurasjoner();
        for (Entry<KonfigKey<?>, Object> entry : konfigurasjoner.entrySet()) {
            KonfigKey<?> key = entry.getKey();
            Class<Object> type = (Class<Object>) key.type();

            if (key instanceof TypetListeKonfigKey<?>) {
                TypeLiteral<List<?>> typeLiteral = (TypeLiteral<List<?>>) TypeLiteral.get(Types.listOf(type));
                bind(typeLiteral).annotatedWith(Names.named(key.getNoekkelNavn())).toInstance((List<?>) entry.getValue());
            }
            else {
                bind(type).annotatedWith(Names.named(key.getNoekkelNavn())).toInstance(entry.getValue());
            }
        }

        bind(KonfigFactory.class).toInstance(konfigFactory);
    }

    public KonfigFactory getKonfigFactory() {
        return this.konfigFactory;
    }

}
