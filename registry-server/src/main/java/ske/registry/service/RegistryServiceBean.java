package ske.registry.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;
import ske.registry.repository.RegistryRepository;

public class RegistryServiceBean implements RegistryService {

    private final RegistryRepository registryRepository;

    @Inject
    public RegistryServiceBean(RegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    @Override
    public Collection<TjenesteDTO> aktiveTjenesterMedURN(String urn) {
        return registryRepository.aktiveTjenesterMedURN(urn);
    }

    @Override
    public Collection<TjenesteDTO> inaktiveTjenesterMedURN(String urn) {
        return registryRepository.inaktiveTjenesterMedURN(urn);
    }

    @Override
    public void registrerTjenester(RegistreringDTO registrering) {
        registryRepository.registrerTjenester(registrering);
    }

    @Override
    public void oppdaterTimestamp(UUID tilbyderId) {
        registryRepository.oppdaterTimestamp(tilbyderId);
    }

    @Override
    public void oppdaterKlientInfo(UUID tilbyderId, KlientInfoDTO klientinfo) {
        registryRepository.oppdaterKlientInfo(tilbyderId, klientinfo);
    }

    @Override
    public void fjernAlleFraTilbyder(UUID tilbyderId) {
        registryRepository.fjernAlleFraTilbyder(tilbyderId);
    }

    @Override
    public Collection<TjenesteDTO> aktiveTjenester() {
        return registryRepository.aktiveTjenester();
    }

    @Override
    public Collection<TjenesteDTO> inaktiveTjenester() {
        return registryRepository.inaktiveTjenester();
    }

    @Override
    public Collection<TimestampetRegistreringDTO> registreringer() {
        return registryRepository.registreringer();
    }

    @Override
    public TimestampetRegistreringDTO registrering(UUID tilbyderId) {
        return registryRepository.registrering(tilbyderId);
    }

    @Override
    public Collection<TimestampetRegistreringDTO> aktiveRegistreringer() {
        return registryRepository.aktiveRegistreringer();
    }

    @Override
    public Collection<TimestampetRegistreringDTO> inaktiveRegistreringer() {
        return registryRepository.inaktiveRegistreringer();
    }

    @Override
    public boolean isInnenforGracePeriode() {
        return registryRepository.isInnenforGracePeriode();
    }

    @Override
    public Map<String, String> finnEgendefinertInfoForTjeneste(String urn) {
        return registryRepository.finnEgendefinertInfoForTjeneste(urn);
    }

}
