package musicplaylist.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Composition-owned history belonging to one playlist. */
public class PlaybackHistory {
    private final UUID id = UUID.randomUUID();
    private final List<String> entries = new ArrayList<>();

    public synchronized void record(String trackTitle) {
        if (trackTitle == null || trackTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Track title must not be empty");
        }
        entries.add(trackTitle.trim());
    }

    public synchronized List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized int size() {
        return entries.size();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PlaybackHistory)) return false;
        PlaybackHistory that = (PlaybackHistory) object;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
