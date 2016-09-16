package ske.registry.sikkerhet;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;

import mag.felles.sikkerhet.rest.sts.TokenValidatorListener;
import mag.felles.sikkerhet.rest.token.TokenValidator;

public class MetricTokenValidatorListener implements TokenValidatorListener {

    private final Map<TokenValidator.TokenValideringstatus, Timer> tokenTimers;
    private final Counter cacheHit;

    @Inject
    public MetricTokenValidatorListener(MetricRegistry metricRegistry) {
        tokenTimers = lagTimersForTokenStatuser(metricRegistry);
        cacheHit = metricRegistry.counter("tokenvalidator.cachehit");
    }

    private Map<TokenValidator.TokenValideringstatus, Timer> lagTimersForTokenStatuser(MetricRegistry metricRegistry) {
        ImmutableMap.Builder<TokenValidator.TokenValideringstatus, Timer> timers = ImmutableMap.builder();
        for (TokenValidator.TokenValideringstatus tokenValideringstatus : TokenValidator.TokenValideringstatus.values()) {
            Timer timer = metricRegistry.timer("tokenvalidator." + tokenValideringstatus.name().toLowerCase());
            timers.put(tokenValideringstatus, timer);
        }
        return timers.build();
    }

    @Override
    public void validationEvent(long durationNanos, TokenValidator.TokenValideringstatus valideringstatus) {
        tokenTimers.get(valideringstatus).update(durationNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void cacheHitEvent() {
        cacheHit.inc();
    }

}
