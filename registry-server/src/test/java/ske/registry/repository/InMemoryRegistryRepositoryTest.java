package ske.registry.repository;

import static org.fest.assertions.Assertions.assertThat;
import static ske.registry.server.RegistryServer.SIKKERHETSPOLICY_TJENESTEATTRIBUTT;
import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML;
import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import mag.felles.konfigurasjon.SikkerhetspolicyOppslagstjeneste;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ske.mag.test.kategorier.Komponenttest;
import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;

@Category(Komponenttest.class)
public class InMemoryRegistryRepositoryTest {

    private InMemoryRegistryRepository repo = new InMemoryRegistryRepository(1000L, 500L, 2000L);

    @Test
    public void skalLeggeInnAlleTjenesterFraTilbyder() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();

        repo.registrerTjenester(registrering);

        assertThat(repo.aktiveTjenesterMedURN("urn:test")).contains(registrering.getTjenesteliste().get(0));
    }

    @Test
    public void skalRegistrereTilbyderMedAttributter() throws Exception {
        RegistreringDTO registrering = RegistreringDTO.medTilbyderNavn("tilbydernavn")
                .hostOgPort("host:port")
                .adminsideUrl(URI.create("http://adminside"))
                .tjeneste("urn:tjeneste", URI.create("http://tjeneste"), null)
                .pingUrl(URI.create("http://ping"))
                .helsesjekkUrl(URI.create("http://helsesjekk"))
                .leggTilEgendefinertInfo("nokkel", "verdi")
                .applikasjonsgruppe("applikasjonsgruppe")
                .komponent("komponent")
                .artefakt("komponent-versjon-Leveransepakke-zip").bygg();

        repo.registrerTjenester(registrering);
        assertThat(repo.registreringer().iterator().next().getRegistrering()).isEqualTo(registrering);

        RegistreringDTO registreringFraRepo = repo.registreringer().iterator().next().getRegistrering();

        assertThat(registreringFraRepo.getTilbyderNavn()).isEqualTo("tilbydernavn");
        assertThat(registreringFraRepo.getHostOgPort()).isEqualTo("host:port");
        assertThat(registreringFraRepo.getAdminsideUrl().toString()).isEqualTo("http://adminside");
        assertThat(registreringFraRepo.getTjenesteliste().iterator().next().getUrn()).isEqualTo("urn:tjeneste");
        assertThat(registreringFraRepo.getTjenesteliste().iterator().next().getUri().toString()).isEqualTo("http://tjeneste");
        assertThat(registreringFraRepo.getPingUrl().toString()).isEqualTo("http://ping");
        assertThat(registreringFraRepo.getHelsesjekkUrl().toString()).isEqualTo("http://helsesjekk");
        assertThat(registreringFraRepo.getEgendefinertInfo().containsKey("nokkel")).isTrue();
        assertThat(registreringFraRepo.getEgendefinertInfo().containsValue("verdi")).isTrue();
        assertThat(registreringFraRepo.getApplikasjonsgruppe()).isEqualTo("applikasjonsgruppe");
        assertThat(registreringFraRepo.getKomponent()).isEqualTo("komponent");
        assertThat(registreringFraRepo.getArtefakt()).isEqualTo("komponent-versjon-Leveransepakke-zip");

        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).isEmpty();
    }

    @Test
    public void skalReturnererRegistreringer() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.registreringer().iterator().next().getRegistrering()).isEqualTo(registrering);
        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).isEmpty();
    }

    @Test
    public void skalReturnererRegistreringForTilbyder() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.registrering(registrering.getTilbyderId()).getRegistrering()).isEqualTo(registrering);
        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).isEmpty();
    }

    @Test
    public void skalReturnererTjenesterSomErAktiveForVanligeRegistreringer() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.aktiveTjenester()).contains(registrering.getTjenesteliste().get(0));
        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).isEmpty();
    }

    @Test
    public void skalSkilleMellomNormalOgStatiskTjeneste() throws Exception {
        RegistreringDTO normalReg = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(normalReg);

        RegistreringDTO statiskReg = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(statiskReg);

        assertThat(repo.aktiveTjenester()).hasSize(3);

        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).hasSize(2);
    }

    @Test
    public void skalReturnererTjenesterSomErAktiveForStatiskeRegistreringer() {
        RegistreringDTO registrering = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(registrering);
        assertThat(repo.aktiveTjenester()).contains(registrering.getTjenesteliste().get(0));

        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenester()).contains(registrering.getTjenesteliste().get(0));
    }

    @Test
    public void skalReturnereRegistreringerSomErAktiveForVanligeRegistreringer() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);

        Collection<TimestampetRegistreringDTO> timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).isNotEmpty();

        repo.setRegistreringTimeout(0L);

        timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).isEmpty();
    }

    @Test
    public void skalReturnereRegistreringerSomErAktiveForStatiskeRegistreringer() {
        RegistreringDTO registreringValider = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(registreringValider);

        Collection<TimestampetRegistreringDTO> timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).isNotEmpty();

        repo.setRegistreringTimeout(0L);

        timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).isNotEmpty();
    }

    @Test
    public void skalSkilleMellomNormalOgStatiskRegistrering() throws Exception {
        RegistreringDTO normalReg = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(normalReg);

        RegistreringDTO statiskReg = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(statiskReg);

        Collection<TimestampetRegistreringDTO> timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).hasSize(2);

        repo.setRegistreringTimeout(0L);

        timestampetRegistreringDTOs = repo.aktiveRegistreringer();
        assertThat(timestampetRegistreringDTOs).hasSize(1);
    }

    @Test
    public void skalReturnererTjenesterSomErAktiveForUrn() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.aktiveTjenesterMedURN("urn:test")).contains(registrering.getTjenesteliste().get(0));
        repo.setRegistreringTimeout(0L);
        assertThat(repo.aktiveTjenesterMedURN("urn:test")).isEmpty();
    }

    @Test
    public void skalReturnererTjenesterSomErInaktiveForUrn() {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.inaktiveTjenesterMedURN("urn:test")).contains(registrering.getTjenesteliste().get(0));
    }

    @Test
    public void skalReturnererTjenesterSomErInaktive() {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.inaktiveTjenester()).contains(registrering.getTjenesteliste().get(0));
    }

    @Test
    public void statiskRegistreringTjenesteSkalAldriBliInaktiv() {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(registrering);
        assertThat(repo.inaktiveTjenester()).isEmpty();
    }

    @Test
    public void skalReturnereRegistreringerSomErInaktive() {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.inaktiveRegistreringer()).isNotEmpty();
    }

    @Test
    public void statiskRegistreringSkalAldriBliInaktiv() {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedToStatiskeTjenester();
        repo.registrerTjenester(registrering);
        assertThat(repo.inaktiveRegistreringer()).isEmpty();
    }

    @Test
    public void skalVareTraadSikkerVedSpoering() throws Exception {
        repo.setRegistreringTimeout(10L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        final AtomicBoolean run = new AtomicBoolean(true);
        int antall = 200;
        final CountDownLatch latch = new CountDownLatch(antall);
        Runnable aktiveTjenester = new Runnable() {
            @Override
            public void run() {
                latch.countDown();
                while (run.get()) {

                    repo.aktiveTjenesterMedURN("urn:test");
                }
            }
        };
        ArrayList<Thread> ts = new ArrayList<>();
        for (int i = 0; i < antall; i++) {
            Thread thread = new Thread(aktiveTjenester);
            ts.add(thread);
            thread.start();
        }

        latch.await();
        repo.registrerTjenester(registrering);
        Thread.sleep(10);

        for (Thread t : ts) {
            assertThat(t.isAlive()).isTrue();
        }
        run.set(false);
    }

    @Test
    public void skalOppdatereTimestamp() throws InterruptedException {
        repo.setRegistreringTimeout(10L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        Thread.sleep(20);
        assertThat(repo.inaktiveTjenesterMedURN("urn:test")).contains(registrering.getTjenesteliste().get(0));
        repo.oppdaterTimestamp(registrering.getTilbyderId());
        assertThat(repo.aktiveTjenesterMedURN("urn:test")).contains(registrering.getTjenesteliste().get(0));
    }

    @Test(expected = RuntimeException.class)
    public void skalFeileHvisOppdatereTimestampForTilbyderSomIkkeErRegisterert() throws InterruptedException {
        repo.oppdaterTimestamp(UUID.randomUUID());
    }

    @Test
    public void skalFjerneAlleTjenesterFraTilbyder() {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        RegistreringDTO registrering2 = lagRegistreringMedEnTjeneste();

        repo.registrerTjenester(registrering);
        repo.registrerTjenester(registrering2);

        repo.fjernAlleFraTilbyder(registrering.getTilbyderId());

        assertThat(repo.aktiveTjenesterMedURN("urn:test")).excludes(registrering.getTjenesteliste().get(0)).contains(
                registrering2.getTjenesteliste().get(0));
    }

    @Test
    public void skalFinneEgendefinertInfoForTjeneste() throws Exception {
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        RegistreringDTO registreringMedEgendefinertInfo = lagRegistreringMedEnTjenesteOgEgendefinertInfo();
        repo.registrerTjenester(registrering);
        repo.registrerTjenester(registreringMedEgendefinertInfo);

        Map<String, String> stringStringMap = repo.finnEgendefinertInfoForTjeneste("urn:test");
        assertThat(stringStringMap).isEmpty();

        stringStringMap = repo.finnEgendefinertInfoForTjeneste("urn:finnesIkke");
        assertThat(stringStringMap).isNull();

        stringStringMap = repo.finnEgendefinertInfoForTjeneste("urn:testMedEgendefinertInfo");
        assertThat(stringStringMap).isNotEmpty();
        assertThat(stringStringMap.get(SIKKERHETSPOLICY_TJENESTEATTRIBUTT)).isEqualToIgnoringCase(
                SikkerhetspolicyOppslagstjeneste.AKTIVERT.getValue());
    }

    @Test
    public void skalOppdatereNyeInaktiveRegistreringer() throws Exception {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        assertThat(repo.getInaktiveRegistereringerHolder()).isEmpty();

        repo.oppdaterInaktiveRegistreringer();

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(1);

        RegistreringDTO registrering2 = lagRegistreringMedEnTjeneste();

        repo.registrerTjenester(registrering2);
        repo.oppdaterInaktiveRegistreringer();

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(2);
        assertThat(repo.getInaktiveRegistereringerHolder()).contains(registrering.getTilbyderId(), registrering2.getTilbyderId());
    }

    @Test
    public void skalToemmeInaktiveRegistreringerVedPuls() throws Exception {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        repo.oppdaterInaktiveRegistreringer();

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(1);

        repo.setRegistreringTimeout(1000L);
        repo.oppdaterTimestamp(registrering.getTilbyderId());

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(0);
    }

    @Test
    public void skalToemmeInaktiveRegistreringerVedNyRegistrering() throws Exception {
        repo.setRegistreringTimeout(0L);
        RegistreringDTO registrering = lagRegistreringMedEnTjeneste();
        repo.registrerTjenester(registrering);
        repo.oppdaterInaktiveRegistreringer();

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(1);

        repo.setRegistreringTimeout(1000L);
        repo.registrerTjenester(registrering);

        assertThat(repo.getInaktiveRegistereringerHolder().size()).isEqualTo(0);
    }

    private RegistreringDTO lagRegistreringMedEnTjeneste() {
        return RegistreringDTO.medTilbyderNavn("test").tjeneste("urn:test", URI.create("http://test"), null).bygg();
    }

    private RegistreringDTO lagRegistreringMedToStatiskeTjenester() {
        return RegistreringDTO.medTilbyderNavn("test")
                .tjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML, URI.create("http://test"), null)
                .tjeneste(URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML, URI.create("http://test"), null)
                .bygg();
    }

    private RegistreringDTO lagRegistreringMedEnTjenesteOgEgendefinertInfo() {
        return RegistreringDTO.medTilbyderNavn("test").tjeneste("urn:testMedEgendefinertInfo", URI.create("http://test"), null)
                .leggTilEgendefinertInfo(SIKKERHETSPOLICY_TJENESTEATTRIBUTT, SikkerhetspolicyOppslagstjeneste.AKTIVERT.getValue()).bygg();
    }

}
