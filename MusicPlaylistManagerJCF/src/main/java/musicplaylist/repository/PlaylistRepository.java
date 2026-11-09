package musicplaylist.repository;

import musicplaylist.model.Playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlaylistRepository {
    private final List<Playlist> playlists = new ArrayList<>();
    private final Map<UUID, Playlist> byId = new HashMap<>();

    public synchronized void add(Playlist playlist) {
        if (playlist == null) throw new IllegalArgumentException("Playlist must not be null");
        if (byId.containsKey(playlist.getId())) {
            throw new IllegalStateException("Playlist ID already exists: " + playlist.getId());
        }
        playlists.add(playlist);
        byId.put(playlist.getId(), playlist);
    }

    public synchronized Playlist update(Playlist replacement) {
        if (replacement == null) throw new IllegalArgumentException("Playlist must not be null");
        Playlist old = byId.get(replacement.getId());
        if (old == null) throw new java.util.NoSuchElementException("Playlist not found: " + replacement.getId());
        playlists.set(playlists.indexOf(old), replacement);
        byId.put(replacement.getId(), replacement);
        return old;
    }

    public synchronized Optional<Playlist> remove(UUID id) {
        if (id == null) throw new IllegalArgumentException("Playlist ID must not be null");
        Playlist removed = byId.remove(id);
        if (removed == null) return Optional.empty();
        playlists.remove(removed);
        return Optional.of(removed);
    }

    public synchronized Optional<Playlist> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public synchronized Optional<Playlist> findByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        return playlists.stream()
                .filter(playlist -> playlist.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    public synchronized List<Playlist> findAll() {
        return new ArrayList<>(playlists);
    }

    public synchronized int size() {
        return playlists.size();
    }
}
