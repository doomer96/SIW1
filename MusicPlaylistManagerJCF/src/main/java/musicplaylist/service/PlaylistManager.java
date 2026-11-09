package musicplaylist.service;

import musicplaylist.model.Playable;
import musicplaylist.model.PlaybackRequest;
import musicplaylist.model.Playlist;
import musicplaylist.model.RequestStatus;
import musicplaylist.model.Track;
import musicplaylist.model.User;
import musicplaylist.model.UserRole;
import musicplaylist.repository.PlaylistRepository;
import musicplaylist.repository.TrackRepository;
import musicplaylist.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

/**
 * Application service. Public operations synchronize repository, playlist and queue state.
 * HashMap lookups provide average O(1) access by UUID; the PriorityQueue processes
 * higher-priority requests first and keeps FIFO order for equal priorities.
 */
public class PlaylistManager {
    private final TrackRepository trackRepository = new TrackRepository();
    private final PlaylistRepository playlistRepository = new PlaylistRepository();
    private final UserRepository userRepository = new UserRepository();
    private final Queue<PlaybackRequest> playbackQueue = new PriorityQueue<>();
    private final Map<UUID, PlaybackRequest> requestsById = new HashMap<>();
    private long nextRequestSequence = 0L;


    public synchronized void addTrack(Track track) {
        trackRepository.add(track);
    }

    /** Replaces a track and updates every playlist reference and category index. */
    public synchronized void updateTrack(Track replacement) {
        Track oldTrack = trackRepository.update(replacement);
        for (Playlist playlist : playlistRepository.findAll()) {
            playlist.replaceTrack(oldTrack, replacement);
        }
    }

    /** Also removes pending requests for the deleted track. */
    public synchronized boolean removeTrack(UUID trackId) {
        Optional<Track> removed = trackRepository.remove(trackId);
        if (!removed.isPresent()) return false;
        for (Playlist playlist : playlistRepository.findAll()) {
            playlist.removeTrack(trackId);
        }
        cancelRequests(request -> request.getTrackId().equals(trackId));
        return true;
    }

    public synchronized Optional<Track> findTrackById(UUID id) {
        return trackRepository.findById(id);
    }

    public synchronized List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public synchronized List<Track> findTracksByCategory(String category) {
        return trackRepository.findByCategory(category);
    }

    public synchronized List<Track> findTracksByArtist(String artist) {
        return trackRepository.findByArtist(artist);
    }

    public synchronized List<Track> getTracksSorted(Comparator<Track> comparator) {
        if (comparator == null) throw new IllegalArgumentException("Comparator must not be null");
        List<Track> result = trackRepository.findAll();
        result.sort(comparator);
        return result;
    }

    public synchronized Map<String, Integer> getCategoryCounts() {
        return trackRepository.getCategoryCounts();
    }


    public synchronized void addPlaylist(Playlist playlist) {
        playlistRepository.add(playlist);
    }

    public synchronized void renamePlaylist(UUID playlistId, String newName) {
        Playlist playlist = requirePlaylist(playlistId);
        playlist.setName(newName);
    }

    public synchronized boolean removePlaylist(UUID playlistId) {
        return playlistRepository.remove(playlistId).isPresent();
    }

    public synchronized Optional<Playlist> findPlaylistById(UUID id) {
        return playlistRepository.findById(id);
    }

    public synchronized Optional<Playlist> findPlaylistByName(String name) {
        return playlistRepository.findByName(name);
    }

    /** Backward-compatible lookup method from the first project. */
    public synchronized Playlist findByName(String name) {
        return playlistRepository.findByName(name).orElse(null);
    }

    public synchronized List<Playlist> getPlaylists() {
        return playlistRepository.findAll();
    }

    public synchronized void addTrackToPlaylist(UUID playlistId, UUID trackId) {
        Playlist playlist = requirePlaylist(playlistId);
        Track track = requireTrack(trackId);
        playlist.addTrack(track); // Set prevents a duplicate track in the same playlist.
    }

    public synchronized boolean removeTrackFromPlaylist(UUID playlistId, UUID trackId) {
        return requirePlaylist(playlistId).removeTrack(trackId);
    }


    public synchronized void addUser(User user) {
        userRepository.add(user);
    }

    public synchronized void updateUser(User replacement) {
        userRepository.update(replacement);
    }

    /** Removing a user also cancels that user's pending requests. */
    public synchronized boolean removeUser(UUID userId) {
        if (!userRepository.remove(userId).isPresent()) return false;
        cancelRequests(request -> request.getRequesterId().equals(userId));
        return true;
    }

    public synchronized Optional<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public synchronized List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }


    /** LISTENER workflow: submit a request for an existing playable track. */
    public synchronized PlaybackRequest submitPlaybackRequest(User requester, UUID trackId, int priority) {
        if (requester == null) throw new IllegalArgumentException("Requester must not be null");
        User registeredRequester = userRepository.findById(requester.getId())
                .orElseThrow(() -> new NoSuchElementException("Requester is not registered"));
        if (registeredRequester.getRole() != UserRole.LISTENER) {
            throw new IllegalStateException("Only LISTENER users can submit playback requests");
        }
        Track track = requireTrack(trackId);
        if (!(track instanceof Playable)) {
            throw new IllegalStateException("This track cannot be played");
        }
        PlaybackRequest request = new PlaybackRequest(registeredRequester.getId(),
                registeredRequester.getName(), trackId, priority, nextRequestSequence++);
        playbackQueue.offer(request);
        requestsById.put(request.getId(), request);
        return request;
    }

    /** OPERATOR workflow: poll and execute the highest-priority pending request. */
    public synchronized Optional<PlaybackRequest> processNextPlaybackRequest(User operator) {
        requireRole(operator, UserRole.OPERATOR);
        PlaybackRequest request = playbackQueue.poll();
        if (request == null) return Optional.empty();

        requestsById.remove(request.getId());
        Optional<Track> trackOptional = trackRepository.findById(request.getTrackId());
        if (!trackOptional.isPresent() || !(trackOptional.get() instanceof Playable)) {
            request.markCancelled();
            return Optional.of(request);
        }

        Track track = trackOptional.get();
        ((Playable) track).play();
        request.markProcessed();
        for (Playlist playlist : playlistRepository.findAll()) {
            if (playlist.containsTrack(track.getId())) {
                playlist.getHistory().record(track.getTitle());
            }
        }
        return Optional.of(request);
    }

    /** A listener can cancel their own request; an operator can cancel any request. */
    public synchronized boolean cancelPlaybackRequest(UUID requestId, User actor) {
        if (requestId == null || actor == null) {
            throw new IllegalArgumentException("Request ID and actor must not be null");
        }
        User registeredActor = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NoSuchElementException("Actor is not registered"));
        PlaybackRequest request = requestsById.get(requestId);
        if (request == null) return false;
        boolean isOperator = registeredActor.getRole() == UserRole.OPERATOR;
        boolean isOwner = request.getRequesterId().equals(registeredActor.getId());
        if (!isOperator && !isOwner) {
            throw new IllegalStateException("User can cancel only their own requests");
        }
        playbackQueue.remove(request);
        requestsById.remove(requestId);
        request.markCancelled();
        return true;
    }

    public synchronized Optional<PlaybackRequest> findRequestById(UUID requestId) {
        return Optional.ofNullable(requestsById.get(requestId));
    }

    /** Returns a priority-ordered snapshot without exposing the live queue. */
    public synchronized List<PlaybackRequest> getPendingRequests() {
        PriorityQueue<PlaybackRequest> copy = new PriorityQueue<>(playbackQueue);
        List<PlaybackRequest> result = new ArrayList<>();
        while (!copy.isEmpty()) result.add(copy.poll());
        return result;
    }

    public synchronized int getPendingRequestCount() {
        return playbackQueue.size();
    }

    // ---------- Analytics ----------

    /** Backward-compatible count: counts entries across all playlists. */
    public synchronized int getTotalTracks() {
        return getTotalPlaylistEntries();
    }

    /** Number of distinct tracks registered in the library repository. */
    public synchronized int getLibraryTrackCount() {
        return trackRepository.size();
    }

    /** Counts playlist entries; a track present in two playlists is counted twice. */
    public synchronized int getTotalPlaylistEntries() {
        int total = 0;
        for (Playlist playlist : playlistRepository.findAll()) {
            total += playlist.getTracks().size();
        }
        return total;
    }

    public synchronized long getTotalLibraryDurationSeconds() {
        long total = 0;
        for (Track track : trackRepository.findAll()) total += track.getDurationSeconds();
        return total;
    }

    public synchronized double getTotalEstimatedRoyalties() {
        double total = 0.0;
        for (Track track : trackRepository.findAll()) total += track.calculateRoyalty();
        return total;
    }

    public synchronized Map<String, Integer> getPlaylistTrackCounts() {
        Map<String, Integer> report = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Playlist playlist : playlistRepository.findAll()) {
            report.put(playlist.getName(), playlist.getTracks().size());
        }
        return report;
    }


    private Track requireTrack(UUID id) {
        if (id == null) throw new IllegalArgumentException("Track ID must not be null");
        return trackRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Track not found: " + id));
    }

    private Playlist requirePlaylist(UUID id) {
        if (id == null) throw new IllegalArgumentException("Playlist ID must not be null");
        return playlistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found: " + id));
    }

    private void requireRole(User actor, UserRole requiredRole) {
        if (actor == null) throw new IllegalArgumentException("User must not be null");
        User registered = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NoSuchElementException("User is not registered"));
        if (registered.getRole() != requiredRole) {
            throw new IllegalStateException("This operation requires role " + requiredRole);
        }
    }

    private interface RequestFilter {
        boolean matches(PlaybackRequest request);
    }

    private void cancelRequests(RequestFilter filter) {
        List<PlaybackRequest> toCancel = new ArrayList<>();
        for (PlaybackRequest request : playbackQueue) {
            if (filter.matches(request)) toCancel.add(request);
        }
        for (PlaybackRequest request : toCancel) {
            playbackQueue.remove(request);
            requestsById.remove(request.getId());
            if (request.getStatus() == RequestStatus.PENDING) request.markCancelled();
        }
    }
}
