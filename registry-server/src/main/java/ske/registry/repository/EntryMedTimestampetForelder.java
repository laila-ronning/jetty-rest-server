package ske.registry.repository;

import java.util.Objects;

public final class EntryMedTimestampetForelder<T, U extends EntryMedTimestamp<?>> {

    private final T entry;
    private final U forelder;

    public EntryMedTimestampetForelder(T entry, U forelder) {
        this.entry = entry;
        this.forelder = forelder;
    }

    public T getEntry() {
        return entry;
    }

    public U getForelder() {
        return forelder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntryMedTimestampetForelder that = (EntryMedTimestampetForelder) o;
        return Objects.equals(this.entry, that.entry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry);
    }

}
