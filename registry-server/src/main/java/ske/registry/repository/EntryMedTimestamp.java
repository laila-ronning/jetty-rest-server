package ske.registry.repository;

import static ske.registry.util.RegistreringHelper.erStatiskRegistrering;

import java.util.Objects;

import ske.registry.dto.RegistreringDTO;

public class EntryMedTimestamp<T> {

    private final Object lock = new Object();

    private final long opprettetTimestamp;
    private final T entry;
    private long oppdatertTimestamp;

    public EntryMedTimestamp(T entry) {
        this.entry = entry;
        oppdater();
        opprettetTimestamp = oppdatertTimestamp;
    }

    public void oppdater() {
        synchronized (lock) {
            oppdatertTimestamp = System.currentTimeMillis();
        }
    }

    public boolean erAktiv(long registreringTimeout) {
        return (System.currentTimeMillis() - oppdatertTimestamp < registreringTimeout)
                || erStatiskRegistrering((RegistreringDTO) getEntry());
    }

    public long getOpprettetTimestamp() {
        return opprettetTimestamp;
    }

    public long getOppdatertTimestamp() {
        return oppdatertTimestamp;
    }

    public T getEntry() {
        return entry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntryMedTimestamp that = (EntryMedTimestamp) o;
        return Objects.equals(this.entry, that.entry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry);
    }

}
