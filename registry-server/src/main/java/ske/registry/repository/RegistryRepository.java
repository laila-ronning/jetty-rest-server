package ske.registry.repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;

public interface RegistryRepository {

    Collection<TjenesteDTO> aktiveTjenester();

    Collection<TjenesteDTO> inaktiveTjenester();

    Collection<TjenesteDTO> aktiveTjenesterMedURN(String urn);

    Collection<TjenesteDTO> inaktiveTjenesterMedURN(String urn);

    void registrerTjenester(RegistreringDTO registrering);

    void oppdaterTimestamp(UUID tilbyderId);

    void oppdaterKlientInfo(UUID tilbyderId, KlientInfoDTO klientinfo);

    void fjernAlleFraTilbyder(UUID tilbyderId);

    Collection<TimestampetRegistreringDTO> registreringer();

    TimestampetRegistreringDTO registrering(UUID tilbyderId);

    Collection<TimestampetRegistreringDTO> aktiveRegistreringer();

    Collection<TimestampetRegistreringDTO> inaktiveRegistreringer();

    void settRegistryStartet();

    boolean isInnenforGracePeriode();

    Map<String, String> finnEgendefinertInfoForTjeneste(String urn);

    void pollInaktiveRegistreringer();
}
