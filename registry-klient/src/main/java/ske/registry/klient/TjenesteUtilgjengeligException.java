package ske.registry.klient;

public class TjenesteUtilgjengeligException extends RuntimeException {

    public TjenesteUtilgjengeligException(String urn, String message) {
        super(message + " for URN: " + urn);
    }

    public TjenesteUtilgjengeligException(String urn, String message, Throwable cause) {
        super(message + " for URN: " + urn, cause);
    }
}
