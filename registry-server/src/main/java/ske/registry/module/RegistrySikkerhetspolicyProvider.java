package ske.registry.module;

import javax.inject.Inject;
import javax.inject.Named;

import mag.felles.konfigurasjon.SikkerhetspolicyOppslagstjeneste;
import mag.felles.sikkerhet.rest.filter.Sikkerhetspolicy;
import mag.felles.sikkerhet.rest.filter.SikkerhetspolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrySikkerhetspolicyProvider implements SikkerhetspolicyProvider {
    private final static Logger log = LoggerFactory.getLogger(RegistrySikkerhetspolicyProvider.class);
    private String policy;

    @Inject
    public RegistrySikkerhetspolicyProvider(@Named(RegistryModuleKonfig.STS_SIKKERHETSPOLICY_ID) String policy) {
        this.policy = policy;
    }

    @Override
    public Sikkerhetspolicy getSikkerhetspolicy() {
        log.debug("Konfigurert sikkerhetspolicy: {}", policy);

        Sikkerhetspolicy sikkerhetspolicy = Sikkerhetspolicy.AKTIVERT;
        if (SikkerhetspolicyOppslagstjeneste.EVALUERING.getValue().equals(policy)) {
            sikkerhetspolicy = Sikkerhetspolicy.EVALUERING;
        } else if (SikkerhetspolicyOppslagstjeneste.DEAKTIVERT.getValue().equals(policy)) {
            sikkerhetspolicy = Sikkerhetspolicy.DEAKTIVERT;
        } else if (SikkerhetspolicyOppslagstjeneste.AKTIVERT.getValue().equals(policy)) {
            sikkerhetspolicy = Sikkerhetspolicy.AKTIVERT;
        } else {
            log.info("Ukjent sikkerhetspolicy {}. Bruker standard policy: {}",
                    policy, sikkerhetspolicy);
        }
        return sikkerhetspolicy;
    }

}
