package ske.registry.module;

import java.util.List;

import com.google.common.collect.ImmutableList;

import mag.felles.konfig.KonfigKey;
import mag.felles.konfig.KonfigKeys;
import mag.felles.konfig.ModuleKonfig;
import mag.felles.konfig.impl.EnkelKonfigKey;

public class RegistryModuleKonfig implements ModuleKonfig {

    public static final String TIMEOUT_ID = "registry.timeout";
    public static final EnkelKonfigKey<Long> TIMEOUT_KEY = KonfigKeys.enkelKey(TIMEOUT_ID, Long.class,
            "Antall millisekunder før en registrering settes som inaktiv ved mangel på puls");

    public static final String GRACE_PERIODE_ID = "registry.graceperiode";
    public static final EnkelKonfigKey<Long> GRACE_PERIODE_KEY = KonfigKeys.enkelKey(GRACE_PERIODE_ID, Long.class,
            "Antall millisekunder etter oppstart hvor tilbydere skal få tid til å registere seg. I denne perioden vil klienter som ber om en "
                    + "tjeneste som ikke finnes motta en HTTP Error 503 - Service unavailable");

    public static final String REGISTRY_POLLING_INAKTIVE_ID = "registry.polling.inaktive";
    public static final EnkelKonfigKey<Long> REGISTRY_POLLING_INAKTIVE_KEY = KonfigKeys.enkelKey(REGISTRY_POLLING_INAKTIVE_ID, Long.class,
            "Antall millisekunder mellom hver polling av nye inaktive registreringer");

    public static final String STS_UTSTED_SAML_URL_ID = "sikkerhet.utsted.saml.url";
    public static final EnkelKonfigKey<String> STS_UTSTED_SAML_KEY = KonfigKeys.enkelKey(STS_UTSTED_SAML_URL_ID, String.class,
            "URL til utsteder av saml");

    public static final String STS_VALIDER_SAML_URL_ID = "sikkerhet.valider.saml.url";
    public static final EnkelKonfigKey<String> STS_VALIDER_SAML_KEY = KonfigKeys.enkelKey(STS_VALIDER_SAML_URL_ID, String.class,
            "URL til validator av saml");

    public static final String STS_SIKKERHETSPOLICY_ID = "sikkerhet.sts.rest.sikkerhetspolicy";
    public static final EnkelKonfigKey<String> STS_SIKKERHETSPOLICY_KEY = KonfigKeys.enkelKey(STS_SIKKERHETSPOLICY_ID, String.class,
            "Nivå for sikkerhet");

    public static final String STS_TOKENVALIDATOR_TIMEOUT_MILLS_ID = "sts.tokenvalidator.timeout.mills";
    public static final EnkelKonfigKey<Integer> STS_TOKENVALIDATOR_TIMEOUT_MILLS_KEY = KonfigKeys.enkelKey(STS_TOKENVALIDATOR_TIMEOUT_MILLS_ID,
            Integer.class, "Timeoutverdi for requests mot STS tjenesten. 5000 millisekunder er en passende verdi for mange applikasjoner.");

    public static final String STS_MAKS_ANTALL_AKTIVE_KALL_ID = "sts.maks.antall.aktive.kall";
    public static final EnkelKonfigKey<Integer> STS_MAKS_ANTALL_AKTIVE_KALL_KEY = KonfigKeys.enkelKey(STS_MAKS_ANTALL_AKTIVE_KALL_ID,
            Integer.class, "Antall aktive kall mot sts før vi feiler valideringen");

    @Override
    public List<KonfigKey<?>> getConfigKeys() {
        return ImmutableList.<KonfigKey<?>>of(
                TIMEOUT_KEY,
                GRACE_PERIODE_KEY,
                REGISTRY_POLLING_INAKTIVE_KEY,
                STS_UTSTED_SAML_KEY,
                STS_VALIDER_SAML_KEY,
                STS_SIKKERHETSPOLICY_KEY,
                STS_TOKENVALIDATOR_TIMEOUT_MILLS_KEY,
                STS_MAKS_ANTALL_AKTIVE_KALL_KEY
        );
    }

}
