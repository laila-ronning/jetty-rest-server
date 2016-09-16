package ske.registry.util;

import static org.fest.assertions.Assertions.assertThat;
import static ske.registry.util.RegistreringHelper.erStatiskRegistrering;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Enhetstest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.repository.EntryMedTimestamp;
import ske.registry.server.RegistryServer;

@Category(Enhetstest.class)
public class RegistreringHelperTest {

    @Test
    public void skalGjenkjenneIkkeStatiskRegistreringUtenTjeneste() throws Exception {
        assertThat(erStatiskRegistrering(new RegistreringDTO.Builder("IkkeStatisk").bygg())).isFalse();
    }

    @Test
    public void skalGjenkjenneIkkeStatiskRegistrering() throws Exception {
        assertThat(erStatiskRegistrering(new RegistreringDTO.Builder("IkkeStatisk").tjeneste(
                "IkkeStatisk", null, null).bygg())).isFalse();
    }

    @Test
    public void skalGjenkjenneUtstedSamlSomStatiskRegistrering() throws Exception {
        assertThat(erStatiskRegistrering(new RegistreringDTO.Builder("Security Token Service").tjeneste(
                RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML, null, null).bygg())).isTrue();
    }

    @Test
    public void skalGjenkjenneValiderSamlSomStatiskRegistrering() throws Exception {
        assertThat(erStatiskRegistrering(new RegistreringDTO.Builder("Security Token Service").tjeneste(
                RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML, null, null).bygg())).isTrue();
    }

    @Test
    public void skalGjenkjenneIkkeStatiskRegistreringIEntry() throws Exception {
        RegistreringDTO registrering = new RegistreringDTO.Builder("IkkeStatisk").tjeneste(
                "IkkeStatisk", null, null).bygg();

        assertThat(erStatiskRegistrering(new EntryMedTimestamp<>(registrering))).isFalse();
    }

    @Test
    public void skalGjenkjenneUtstedSamlSomStatiskRegistreringIEntry() throws Exception {
        RegistreringDTO registrering = new RegistreringDTO.Builder("Security Token Service")
                .tjeneste(RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML, null, null)
                .tjeneste("IkkeStatisk", null, null).bygg();

        assertThat(erStatiskRegistrering(new EntryMedTimestamp<>(registrering))).isTrue();
    }
}
