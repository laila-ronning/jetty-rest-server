package ske.registry.klient;

import java.net.URI;
import java.util.Map;

public interface OverstyringRepository {

    Map<String, OverstyrUrnDTO> getOverstyrteTjenester();

    String getFilnavnTilLogging();

    URI getOverstyrtTjeneste(String urn);
}
