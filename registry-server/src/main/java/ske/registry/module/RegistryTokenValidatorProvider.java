package ske.registry.module;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mag.felles.sikkerhet.rest.sts.STSTokenValidator;
import mag.felles.sikkerhet.rest.token.TokenValidator;
import ske.registry.sikkerhet.MetricTokenValidatorListener;
import ske.registry.sikkerhet.ThrottledTokenValidatorService;

public class RegistryTokenValidatorProvider implements Provider<TokenValidator> {

    private final Logger log = LoggerFactory.getLogger(RegistryTokenValidatorProvider.class);
    private final ThrottledTokenValidatorService validatorService;
    private final MetricTokenValidatorListener metricTokenValidatorListener;

    @Inject
    public RegistryTokenValidatorProvider(
            ThrottledTokenValidatorService validatorService,
            MetricTokenValidatorListener metricTokenValidatorListener) {
        this.validatorService = validatorService;
        this.metricTokenValidatorListener = metricTokenValidatorListener;
    }

    @Override
    public TokenValidator get() {
        log.info("Oppretter STSTokenValidator");
        return new STSTokenValidator(validatorService, metricTokenValidatorListener);
    }

}
