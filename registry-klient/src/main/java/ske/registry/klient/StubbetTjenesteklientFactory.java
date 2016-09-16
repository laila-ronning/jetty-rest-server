package ske.registry.klient;

import java.net.URI;

public class StubbetTjenesteklientFactory<T> extends TjenesteklientForURNFactory<T> {

    private T stubbetTjeneste;

    public StubbetTjenesteklientFactory(T stubbetTjeneste) {
        super(null,null);
        this.stubbetTjeneste = stubbetTjeneste;
    }

    @Override
    public T getTjenesteklient() {
        return stubbetTjeneste;
    }

    @Override
    protected T byggTjenesteklient(URI uri) {
        return stubbetTjeneste;
    }
}
