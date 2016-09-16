package ske.registry.klient;

public interface RegistryKlient extends RegistryOppslagKlient, RegistryTjenesteregistreringKlient, HelsetilstandrapportererRegistryKlient {

    void start();

    void stopp();

}
