package ske.registry.repository;

import static ske.registry.util.JsonHelper.tilJson;
import static ske.registry.util.RegistreringHelper.erStatiskRegistrering;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;
import ske.registry.module.RegistryModuleKonfig;

public class InMemoryRegistryRepository implements RegistryRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryRegistryRepository.class);
    private static final long INITIAL_POLLING_INAKTIVE_REGISTRERINGER_DELAY = 10000L;

    private final Map<UUID, EntryMedTimestamp<RegistreringDTO>> tilbyderRegistreringer = new ConcurrentHashMap<>();
    private final Multimap<String, EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>> tjenesteFraURN = Multimaps.synchronizedMultimap(HashMultimap.<String, EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>>create());
    private final Set<UUID> inaktiveRegistereringerHolder = new HashSet<>();
    private final long gracePeriodeLengde;
    private final long pollingInaktive;
    private long startTidspunkt;
    private long registreringTimeout;

    @Inject
    public InMemoryRegistryRepository(@Named(RegistryModuleKonfig.TIMEOUT_ID) Long registreringTimeout,
            @Named(RegistryModuleKonfig.GRACE_PERIODE_ID) Long gracePeriodeLengde,
            @Named(RegistryModuleKonfig.REGISTRY_POLLING_INAKTIVE_ID) Long pollingInaktive) {
        this.gracePeriodeLengde = gracePeriodeLengde;
        this.pollingInaktive = pollingInaktive;
        this.registreringTimeout = registreringTimeout == null ? 1000 : registreringTimeout;
        startTidspunkt = System.currentTimeMillis();
    }

    void setRegistreringTimeout(long registreringTimeout) {
        this.registreringTimeout = registreringTimeout;
    }

    @Override
    public Collection<TjenesteDTO> aktiveTjenester() {
        return pakkUt(finnAktive(tjenesteFraURN.values()));
    }

    @Override
    public Collection<TjenesteDTO> aktiveTjenesterMedURN(String urn) {
        return pakkUt(finnAktive(tjenesteFraURN.get(urn)));
    }

    @Override
    public Collection<TjenesteDTO> inaktiveTjenester() {
        return pakkUt(finnInaktive(tjenesteFraURN.values()));
    }

    @Override
    public Collection<TjenesteDTO> inaktiveTjenesterMedURN(String urn) {
        return pakkUt(finnInaktive(tjenesteFraURN.get(urn)));
    }

    @Override
    public Collection<TimestampetRegistreringDTO> registreringer() {
        return pakkUtRegistreringerMedTimestamp(tilbyderRegistreringer.values());
    }

    @Override
    public TimestampetRegistreringDTO registrering(UUID tilbyderId) {
        EntryMedTimestamp<RegistreringDTO> registrering = tilbyderRegistreringer.get(tilbyderId);
        if (registrering != null) {
            return new TimestampetRegistreringDTO(registrering.getEntry(), registrering.getOpprettetTimestamp(),
                    registrering.getOppdatertTimestamp(),
                    true);
        } else {
            return null;
        }
    }

    @Override
    public Collection<TimestampetRegistreringDTO> aktiveRegistreringer() {
        return pakkUtRegistreringerMedTimestamp(Collections2.filter(tilbyderRegistreringer.values(),
                new Predicate<EntryMedTimestamp<RegistreringDTO>>() {
                    @Override
                    public boolean apply(EntryMedTimestamp<RegistreringDTO> registreringMedTimestamp) {
                        return erRegistreringInnenforRegistreringTimeout(registreringMedTimestamp)
                                || erStatiskRegistrering(registreringMedTimestamp);
                    }
                }));
    }

    @Override
    public Collection<TimestampetRegistreringDTO> inaktiveRegistreringer() {
        return pakkUtRegistreringerMedTimestamp(Collections2.filter(tilbyderRegistreringer.values(),
                new Predicate<EntryMedTimestamp<RegistreringDTO>>() {
                    @Override
                    public boolean apply(EntryMedTimestamp<RegistreringDTO> registreringMedTimestamp) {
                        return erRegistreringUtenforRegistreringTimeout(registreringMedTimestamp)
                                && !erStatiskRegistrering(registreringMedTimestamp);
                    }
                }));
    }

    private boolean erRegistreringInnenforRegistreringTimeout(EntryMedTimestamp<RegistreringDTO> registreringMedTimestamp) {
        return System.currentTimeMillis() - registreringMedTimestamp.getOppdatertTimestamp() < registreringTimeout;
    }

    private boolean erRegistreringUtenforRegistreringTimeout(EntryMedTimestamp<RegistreringDTO> registreringMedTimestamp) {
        return System.currentTimeMillis() - registreringMedTimestamp.getOppdatertTimestamp() >= registreringTimeout;
    }

    @Override
    public void settRegistryStartet() {
        startTidspunkt = System.currentTimeMillis();
        logger.info("Starttidspunkt satt til {}, i grace-periode fram til {}.", new Date(startTidspunkt),
                new Date(startTidspunkt + gracePeriodeLengde));
    }

    @Override
    public boolean isInnenforGracePeriode() {
        return System.currentTimeMillis() - startTidspunkt < gracePeriodeLengde;
    }

    @Override
    public synchronized void registrerTjenester(RegistreringDTO registrering) {
        registrerAktivitet(registrering.getTilbyderId());
        fjernAlleFraTilbyder(registrering.getTilbyderId());
        EntryMedTimestamp<RegistreringDTO> registeringMedTimestamp = new EntryMedTimestamp<>(registrering);
        tilbyderRegistreringer.put(registrering.getTilbyderId(), registeringMedTimestamp);
        for (TjenesteDTO dto : registrering.getTjenesteliste()) {
            tjenesteFraURN.put(dto.getUrn(), new EntryMedTimestampetForelder<>(dto, registeringMedTimestamp));
        }
    }

    @Override
    public void oppdaterTimestamp(UUID tilbyderId) {
        registrerAktivitet(tilbyderId);
        if (!tilbyderRegistreringer.containsKey(tilbyderId)) {
            throw new RuntimeException("Har ingen registering fra tilbyder " + tilbyderId);
        }
        EntryMedTimestamp<RegistreringDTO> registrering = tilbyderRegistreringer.get(tilbyderId);
        if (registrering != null) {
            registrering.oppdater();
        }
    }

    @Override
    public void oppdaterKlientInfo(UUID tilbyderId, KlientInfoDTO klientinfo) {
        if (!tilbyderRegistreringer.containsKey(tilbyderId)) {
            throw new RuntimeException("Har ingen registering fra tilbyder " + tilbyderId);
        }
        EntryMedTimestamp<RegistreringDTO> registrering = tilbyderRegistreringer.get(tilbyderId);
        if (registrering != null) {
            registrering.getEntry().setOppslag(klientinfo.getOppslag());
            registrering.getEntry().setHelsetilstand(klientinfo.getHelsetilstand());
        }
    }

    @Override
    public synchronized void fjernAlleFraTilbyder(UUID tilbyderId) {
        final EntryMedTimestamp<RegistreringDTO> registrering = tilbyderRegistreringer.remove(tilbyderId);
        if (registrering != null) {
            synchronized (tjenesteFraURN) {
                Iterators.removeIf(tjenesteFraURN.values().iterator(),
                        new Predicate<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>>() {
                            public boolean apply(EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>> entry) {
                                return entry.getForelder().equals(registrering);
                            }
                        }
                );
            }
        }
    }

    @Override
    public Map<String, String> finnEgendefinertInfoForTjeneste(String urn) {
        for (TimestampetRegistreringDTO timestampetRegistrering : aktiveRegistreringer()) {
            for (TjenesteDTO tjeneste : timestampetRegistrering.getRegistrering().getTjenesteliste()) {
                if (tjeneste.getUrn().equals(urn)) {
                    return timestampetRegistrering.getRegistrering().getEgendefinertInfo();
                }
            }
        }
        return null;
    }

    private <T, U> Collection<T> pakkUt(Collection<EntryMedTimestampetForelder<T, EntryMedTimestamp<U>>> entries) {
        synchronized (tjenesteFraURN) {
            return ImmutableList.copyOf(Collections2.transform(entries,
                    new Function<EntryMedTimestampetForelder<T, EntryMedTimestamp<U>>, T>() {
                        public T apply(EntryMedTimestampetForelder<T, EntryMedTimestamp<U>> entry) {
                            return entry.getEntry();
                        }
                    }));
        }
    }

    private Collection<TimestampetRegistreringDTO> pakkUtRegistreringerMedTimestamp(Collection<EntryMedTimestamp<RegistreringDTO>> entries) {
        synchronized (tjenesteFraURN) {
            return ImmutableList.copyOf(Collections2.transform(entries,
                    new Function<EntryMedTimestamp<RegistreringDTO>, TimestampetRegistreringDTO>() {
                        public TimestampetRegistreringDTO apply(EntryMedTimestamp<RegistreringDTO> entry) {
                            return new TimestampetRegistreringDTO(entry.getEntry(), entry.getOpprettetTimestamp(), entry
                                    .getOppdatertTimestamp(), entry
                                    .erAktiv(registreringTimeout));
                        }
                    }
            ));
        }
    }

    private Collection<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>> finnAktive(
            Collection<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>> tjenester) {
        return Collections2.filter(tjenester,
                new Predicate<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>>() {
                    @Override
                    public boolean apply(EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>> tjeneste) {
                        return erRegistreringInnenforRegistreringTimeout(tjeneste.getForelder())
                                || erStatiskRegistrering(tjeneste.getForelder());
                    }
                }
        );
    }

    private Collection<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>> finnInaktive(
            Collection<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>> tjenester) {
        return Collections2.filter(tjenester,
                new Predicate<EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>>>() {
                    @Override
                    public boolean apply(EntryMedTimestampetForelder<TjenesteDTO, EntryMedTimestamp<RegistreringDTO>> tjeneste) {
                        return erRegistreringUtenforRegistreringTimeout(tjeneste.getForelder())
                                && !erStatiskRegistrering(tjeneste.getForelder());
                    }
                });
    }

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    @Override
    public void pollInaktiveRegistreringer() {
        final Runnable jobb = new Runnable() {
            public void run() {
                try {
                    oppdaterInaktiveRegistreringer();
                } catch (Exception e) {
                    logger.trace("Feil ved oppdatering av inaktive registreringer", e);
                }
            }
        };
        scheduler.scheduleAtFixedRate(jobb, INITIAL_POLLING_INAKTIVE_REGISTRERINGER_DELAY, pollingInaktive, TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    void oppdaterInaktiveRegistreringer() {
        Set<UUID> inaktiveRegistreringer = Sets.newHashSet(Collections2.transform(inaktiveRegistreringer(),
                new Function<TimestampetRegistreringDTO, UUID>() {
                    @Override
                    public UUID apply(TimestampetRegistreringDTO input) {
                        return input.getRegistrering().getTilbyderId();
                    }
                }));

        Sets.SetView<UUID> nyeInaktiveRegistreringer = Sets.difference(inaktiveRegistreringer, inaktiveRegistereringerHolder);

        if (!nyeInaktiveRegistreringer.isEmpty()) {
            for (UUID uuid : nyeInaktiveRegistreringer) {
                logger.warn(tilJson(
                        "beskrivelse", "Inaktiv registrering fra tilbyder",
                        "tilbyder", tilbyderRegistreringer.get(uuid).getEntry().getTilbyderNavn(),
                        "tilbyderId", uuid.toString()
                        ));
            }
            inaktiveRegistereringerHolder.addAll(nyeInaktiveRegistreringer);
        }
    }

    private void registrerAktivitet(UUID tilbyderId) {
        try {
            inaktiveRegistereringerHolder.remove(tilbyderId);
        } catch (Exception e) {
            logger.trace("Feil ved oppdatering av inaktive registreringer", e);
        }
    }

    @VisibleForTesting
    Set<UUID> getInaktiveRegistereringerHolder() {
        return inaktiveRegistereringerHolder;
    }

}
