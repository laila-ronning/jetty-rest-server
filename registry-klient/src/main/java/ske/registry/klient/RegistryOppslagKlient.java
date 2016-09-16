package ske.registry.klient;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface RegistryOppslagKlient {

    URI finnTjenesteURI(String urn);

    /**
     * Finner alle URIer som er registrert for urn. Denne metoden bruker ikke cache, og går rett på serveren for hvert kall. Hvis tjenesten
     * er overstyrt, brukes kun den overstyrte URIen. Serveren blir ikke kalt.
     * 
     * @param urn
     * @return Et set som inneholder alle URIer en tjeneste er registert på. Hvis tjenesten ikke finnes er set'et tomt.
     */
    Set<URI> finnAlleTjenesteURIer(String urn);

    Set<String> obligatoriskeOppslagSomMangler();

    Map<String, String> hentTjenesteAttributter(String urn);

}
