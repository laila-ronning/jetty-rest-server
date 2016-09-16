package ske.registry.util;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.repository.EntryMedTimestamp;
import ske.registry.server.RegistryServer;

public class RegistreringHelper {
    public RegistreringHelper() {
    }

    public static boolean erStatiskRegistrering(EntryMedTimestamp<RegistreringDTO> registreringMedTimestamp) {
        return erStatiskRegistrering(registreringMedTimestamp.getEntry());
    }

    public static boolean erStatiskRegistrering(RegistreringDTO registrering) {
        for (TjenesteDTO tjenesteDTO : registrering.getTjenesteliste()) {
            if (tjenesteDTO.getUrn().equals(RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML)
                    || tjenesteDTO.getUrn().equals(RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML)) {
                return true;
            }
        }
        return false;
    }
}
