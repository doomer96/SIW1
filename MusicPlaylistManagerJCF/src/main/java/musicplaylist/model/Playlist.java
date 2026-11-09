package musicplaylist.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** A playlist keeps unique tracks while preserving their insertion order. */
public class Playlist {
    private final UUID id;
    private String name;
    private final Set<Track> tracks = new LinkedHashSet<>();
    private final PlaybackHistory history = new PlaybackHistory();

    public Playlist(String name) {
        this(UUID.randomUUID(), name);
    }

    public Playlist(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "Playlist ID must not be null");
        this.name = requireName(name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireName(name);
    }

    private static String requireName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist name must not be empty");
        }
        return name.trim();
    }

    public synchronized List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public synchronized Set<Track> getTrackSet() {
        return new LinkedHashSet<>(tracks);
    }

    public PlaybackHistory getHistory() {
        return history;
    }

    public synchronized boolean addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track must not be null");
        }
        return tracks.add(track);
    }

    public synchronized boolean removeTrack(UUID trackId) {
        Objects.requireNonNull(trackId, "Track ID must not be null");
        return tracks.removeIf(track -> track.getId().equals(trackId));
    }

    /** Backward-compatible removal by title from the first project. */
    public synchronized boolean removeTrack(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Track title must not be empty");
        }
        return tracks.removeIf(track -> track.getTitle().equalsIgnoreCase(title.trim()));
    }

    /** Replaces the object in this playlist without changing its identity or position semantics. */
    public synchronized void replaceTrack(Track oldTrack, Track newTrack) {
        Objects.requireNonNull(oldTrack, "Old track must not be null");
        Objects.requireNonNull(newTrack, "New track must not be null");
        if (tracks.contains(oldTrack)) {
            Set<Track> replacement = new LinkedHashSet<>();
            for (Track track : tracks) {
                replacement.add(track.getId().equals(oldTrack.getId()) ? newTrack : track);
            }
            tracks.clear();
            tracks.addAll(replacement);
        }
    }

    public synchronized boolean containsTrack(UUID trackId) {
        return tracks.stream().anyMatch(track -> track.getId().equals(trackId));
    }

    public synchronized int getTotalDurationSeconds() {
        int total = 0;
        for (Track track : tracks) {
            total = Math.addExact(total, track.getDurationSeconds());
        }
        return total;
    }

    public synchronized void playAll() {
        for (Track track : tracks) {
            if (track instanceof Playable) {
                ((Playable) track).play();
                history.record(track.getTitle());
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Playlist)) return false;
        Playlist playlist = (Playlist) object;
        return id.equals(playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
