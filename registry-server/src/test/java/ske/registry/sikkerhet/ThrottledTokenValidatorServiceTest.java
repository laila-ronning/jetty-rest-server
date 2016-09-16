package ske.registry.sikkerhet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import mag.felles.sikkerhet.rest.sts.TokenValidatorService;
import mag.felles.sikkerhet.rest.token.TokenValidator;
import ske.mag.test.kategorier.Enhetstest;

@Category(Enhetstest.class)
public class ThrottledTokenValidatorServiceTest {

    private ExecutorService pool;

    @Before
    public void setUp() throws Exception {
        pool = Executors.newFixedThreadPool(20);
    }

    @After
    public void tearDown() throws Exception {
        try {
            pool.shutdownNow();
            pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (Exception ignore) {
        }
    }

    @Test
    public void skalKunneThrottleTilEnRequest() throws Exception {
        ThrottledTokenValidatorService service = new ThrottledTokenValidatorService(new SlowTokenValidatorService(200), 1);

        Future<TokenValidator.TokenValideringstatus> request1 = utfoerRequest(service);
        Future<TokenValidator.TokenValideringstatus> request2 = utfoerRequest(service);

        assertThat(request2.get(150, TimeUnit.MILLISECONDS), is(TokenValidator.TokenValideringstatus.FEILET));
        assertThat("request 1", request1.isDone(), is(false));
    }

    @Test(timeout = 60_000)
    public void skalKunneSendeNyeRequestEtterThrottlingErFerdig() throws Exception {
        ThrottledTokenValidatorService service = new ThrottledTokenValidatorService(new SlowTokenValidatorService(300), 1);
        Future<TokenValidator.TokenValideringstatus> request1 = utfoerRequest(service);
        Future<TokenValidator.TokenValideringstatus> request2 = utfoerRequest(service);

        assertThat("R 2", request2.get(100, TimeUnit.MILLISECONDS), is(TokenValidator.TokenValideringstatus.FEILET));
        assertThat("R 1", request1.get(), is(TokenValidator.TokenValideringstatus.GYLDIG));

        Future<TokenValidator.TokenValideringstatus> request3 = utfoerRequest(service);
        assertThat("R 3", request3.get(), is(TokenValidator.TokenValideringstatus.GYLDIG));
    }

    @Test
    public void skalFullfoereRequestSomIkkeErThrottlet() throws Exception {
        ThrottledTokenValidatorService service = new ThrottledTokenValidatorService(new SlowTokenValidatorService(100), 1);

        Future<TokenValidator.TokenValideringstatus> request1 = utfoerRequest(service);
        Future<TokenValidator.TokenValideringstatus> request2 = utfoerRequest(service);

        assertThat(request2.get(90, TimeUnit.MILLISECONDS), is(TokenValidator.TokenValideringstatus.FEILET));
        assertThat(request1.get(200, TimeUnit.MILLISECONDS), is(TokenValidator.TokenValideringstatus.GYLDIG));
    }

    @Test
    public void skalKunneThrottleTil8Request() throws Exception {
        int maksAntallAktiveKall = 8;
        ThrottledTokenValidatorService service = new ThrottledTokenValidatorService(new SlowTokenValidatorService(100), maksAntallAktiveKall);

        ArrayList<Future<TokenValidator.TokenValideringstatus>> tokens = new ArrayList<>();
        for (int i = 0; i < maksAntallAktiveKall; i++) {
            tokens.add(utfoerRequest(service));
        }

        Future<TokenValidator.TokenValideringstatus> request2 = utfoerRequest(service);
        assertThat(request2.get(70, TimeUnit.MILLISECONDS), is(TokenValidator.TokenValideringstatus.FEILET));
        for (Future<TokenValidator.TokenValideringstatus> token : tokens) {
            assertThat("request 1", token.isDone(), is(false));
        }
    }

    Future<TokenValidator.TokenValideringstatus> utfoerRequest(final ThrottledTokenValidatorService service) {
        return pool.submit(new Callable<TokenValidator.TokenValideringstatus>() {
            @Override
            public TokenValidator.TokenValideringstatus call() throws Exception {
                return service.valider("");

            }
        });
    }

    static class SlowTokenValidatorService implements TokenValidatorService {

        private final int responseTimeInMillis;

        SlowTokenValidatorService(int responseTimeInMillis) {
            this.responseTimeInMillis = responseTimeInMillis;
        }

        @Override
        public TokenValidator.TokenValideringstatus valider(String samlToken) {
            try {
                Thread.sleep(responseTimeInMillis);
            } catch (InterruptedException ignore) {
                // blir kastet når vi terminerer executin poolen
            }
            return TokenValidator.TokenValideringstatus.GYLDIG;
        }
    }

}