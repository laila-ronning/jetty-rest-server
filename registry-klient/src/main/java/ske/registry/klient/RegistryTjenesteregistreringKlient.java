package ske.registry.klient;

import com.sun.jersey.api.client.filter.ClientFilter;

import ske.registry.dto.RegistreringDTO;

public interface RegistryTjenesteregistreringKlient {

    void registrer(RegistreringDTO registrering);

    void avregistrer(String urn);

    void start(ClientFilter registreringKlientFilter);

}
