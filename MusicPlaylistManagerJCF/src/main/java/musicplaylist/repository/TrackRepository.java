package musicplaylist.repository;

import musicplaylist.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** List registry + O(1) average-time primary-key index + category index. */
public class TrackRepository {
    private final List<Track> tracks = new ArrayList<>();
    private final Map<UUID, Track> byId = new HashMap<>();
    private final Map<String, Set<Track>> byCategory = new HashMap<>();
    private final Map<UUID, String> categoryById = new HashMap<>();

    public synchronized void add(Track track) {
        if (track == null) throw new IllegalArgumentException("Track must not be null");
        if (byId.containsKey(track.getId())) {
            throw new IllegalStateException("Track ID already exists: " + track.getId());
        }
        tracks.add(track);
        byId.put(track.getId(), track);
        addToCategoryIndex(track);
    }

    /** Replaces a track with the same UUID and returns the previous object. */
    public synchronized Track update(Track replacement) {
        if (replacement == null) throw new IllegalArgumentException("Track must not be null");
        Track oldTrack = byId.get(replacement.getId());
        if (oldTrack == null) {
            throw new java.util.NoSuchElementException("Track not found: " + replacement.getId());
        }
        int position = tracks.indexOf(oldTrack);
        // Playback statistics belong to the track identity and must survive metadata updates.
        replacement.setPlayCount(oldTrack.getPlayCount());
        removeFromCategoryIndex(oldTrack.getId());
        tracks.set(position, replacement);
        byId.put(replacement.getId(), replacement);
        addToCategoryIndex(replacement);
        return oldTrack;
    }

    public synchronized Optional<Track> remove(UUID id) {
        if (id == null) throw new IllegalArgumentException("Track ID must not be null");
        Track removed = byId.remove(id);
        if (removed == null) return Optional.empty();
        tracks.remove(removed);
        removeFromCategoryIndex(id);
        return Optional.of(removed);
    }

    public synchronized Optional<Track> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public synchronized List<Track> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) return Collections.emptyList();
        Set<Track> result = byCategory.get(normalize(category));
        return result == null ? Collections.emptyList() : new ArrayList<>(result);
    }

    public synchronized List<Track> findByArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) return Collections.emptyList();
        List<Track> result = new ArrayList<>();
        for (Track track : tracks) {
            if (track.getArtist().equalsIgnoreCase(artist.trim())) result.add(track);
        }
        return result;
    }

    public synchronized List<Track> findAll() {
        return new ArrayList<>(tracks);
    }

    public synchronized int size() {
        return tracks.size();
    }

    public synchronized Map<String, Integer> getCategoryCounts() {
        Map<String, Integer> result = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Set<Track>> entry : byCategory.entrySet()) {
            result.put(entry.getKey(), entry.getValue().size());
        }
        return result;
    }

    private void addToCategoryIndex(Track track) {
        String category = normalize(track.getCategoryKey());
        byCategory.computeIfAbsent(category, key -> new LinkedHashSet<>()).add(track);
        categoryById.put(track.getId(), category);
    }

    private void removeFromCategoryIndex(UUID id) {
        String oldCategory = categoryById.remove(id);
        if (oldCategory == null) return;
        Set<Track> categoryTracks = byCategory.get(oldCategory);
        if (categoryTracks != null) {
            categoryTracks.removeIf(track -> track.getId().equals(id));
            if (categoryTracks.isEmpty()) byCategory.remove(oldCategory);
        }
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category must not be empty");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
