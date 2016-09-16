package ske.registry.sikkerhet;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mag.felles.sikkerhet.rest.sts.HttpTokenValidatorService;
import mag.felles.sikkerhet.rest.sts.TokenValidatorService;
import mag.felles.sikkerhet.rest.token.TokenValidator;
import ske.registry.module.RegistryModuleKonfig;

public class ThrottledTokenValidatorService implements TokenValidatorService {
    private static final Logger logger = LoggerFactory.getLogger(ThrottledTokenValidatorService.class);

    private final TokenValidatorService innerService;
    private final AtomicInteger aktiveKall = new AtomicInteger(0);
    private final int maksAntallAktiveKall;

    @Inject
    public ThrottledTokenValidatorService(
            @Named(RegistryModuleKonfig.STS_MAKS_ANTALL_AKTIVE_KALL_ID) int maksAntallAktiveKall,
            @Named(RegistryModuleKonfig.STS_VALIDER_SAML_URL_ID) String validerSamlUri,
            @Named(RegistryModuleKonfig.STS_TOKENVALIDATOR_TIMEOUT_MILLS_ID) int timeout) {
        this(new HttpTokenValidatorService(validerSamlUri, timeout), maksAntallAktiveKall);
    }

    @VisibleForTesting
    ThrottledTokenValidatorService(TokenValidatorService innerService, int maksAntallAktiveKall) {
        this.innerService = innerService;
        this.maksAntallAktiveKall = maksAntallAktiveKall;
    }

    @Override
    public TokenValidator.TokenValideringstatus valider(String samlToken) {
        int antall = aktiveKall.incrementAndGet();
        try {
            if (antall > maksAntallAktiveKall) {
                logger.warn("Utfører ikke validering av token pga for mange samtidige kall mot STS tjenesten");
                return TokenValidator.TokenValideringstatus.FEILET;
            }
            return innerService.valider(samlToken);
        } finally {
            aktiveKall.decrementAndGet();
        }
    }

}
