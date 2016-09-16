package ske.registry.module;

import mag.felles.sikkerhet.rest.filter.Sikkerhetspolicy;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Enhetstest;

@Category(Enhetstest.class)
public class RegistrySikkerhetspolicyProviderTest {

    RegistrySikkerhetspolicyProvider sikkerhetspolicyProvider;

    @Test
    public void skalReturnereEvaluering() throws Exception {
        sikkerhetspolicyProvider = new RegistrySikkerhetspolicyProvider(Sikkerhetspolicy.EVALUERING.toString().toLowerCase());
        org.fest.assertions.Assertions.assertThat(sikkerhetspolicyProvider.getSikkerhetspolicy()).isEqualTo(Sikkerhetspolicy.EVALUERING);
    }

    @Test
    public void skalReturnereDeaktivert() throws Exception {
        sikkerhetspolicyProvider = new RegistrySikkerhetspolicyProvider(Sikkerhetspolicy.DEAKTIVERT.toString().toLowerCase());
        org.fest.assertions.Assertions.assertThat(sikkerhetspolicyProvider.getSikkerhetspolicy()).isEqualTo(Sikkerhetspolicy.DEAKTIVERT);
    }

    @Test
    public void skalReturnereAktivert() throws Exception {
        sikkerhetspolicyProvider = new RegistrySikkerhetspolicyProvider(Sikkerhetspolicy.AKTIVERT.toString().toLowerCase());
        org.fest.assertions.Assertions.assertThat(sikkerhetspolicyProvider.getSikkerhetspolicy()).isEqualTo(Sikkerhetspolicy.AKTIVERT);
    }

    @Test
    public void skalReturnereDefaultAktivert() throws Exception {
        sikkerhetspolicyProvider = new RegistrySikkerhetspolicyProvider("rarpolicy");
        org.fest.assertions.Assertions.assertThat(sikkerhetspolicyProvider.getSikkerhetspolicy()).isEqualTo(Sikkerhetspolicy.AKTIVERT);
    }
}
