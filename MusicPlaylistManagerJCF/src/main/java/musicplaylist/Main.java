package musicplaylist;

import musicplaylist.model.Audiobook;
import musicplaylist.model.PlaybackRequest;
import musicplaylist.model.Playlist;
import musicplaylist.model.Podcast;
import musicplaylist.model.Song;
import musicplaylist.model.Track;
import musicplaylist.model.User;
import musicplaylist.model.UserRole;
import musicplaylist.service.PlaylistExporter;
import musicplaylist.service.PlaylistManager;
import musicplaylist.service.TrackComparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Executable demonstration of the JCF-enhanced Music Playlist Manager. */
public class Main {
    public static void main(String[] args) {
        PlaylistManager manager = new PlaylistManager();

        User listener = new User("Davlet", UserRole.LISTENER);
        User operator = new User("Music Operator", UserRole.OPERATOR);
        manager.addUser(listener);
        manager.addUser(operator);

        Song song1 = new Song("Bohemian Rhapsody", "Queen", 354,
                "A Night at the Opera", "Rock");
        Song song2 = new Song("Blinding Lights", "The Weeknd", 200,
                "After Hours", "Pop");
        Podcast podcast = new Podcast("Tech Talk", "Radio Tech", 3900,
                "Alex Brown", 42);
        Audiobook audiobook = new Audiobook("1984", "George Orwell", 39600,
                "Simon Prebble", 24);

        manager.addTrack(song1);
        manager.addTrack(song2);
        manager.addTrack(podcast);
        manager.addTrack(audiobook);

        Playlist favorites = new Playlist("Favorites");
        Playlist study = new Playlist("Study");
        manager.addPlaylist(favorites);
        manager.addPlaylist(study);
        manager.addTrackToPlaylist(favorites.getId(), song1.getId());
        manager.addTrackToPlaylist(favorites.getId(), song2.getId());
        manager.addTrackToPlaylist(favorites.getId(), podcast.getId());
        manager.addTrackToPlaylist(study.getId(), song2.getId());
        manager.addTrackToPlaylist(study.getId(), audiobook.getId());
        manager.addTrackToPlaylist(study.getId(), song2.getId()); // Set rejects duplicate.

        System.out.println("=== PRIMARY-KEY LOOKUP (HashMap) ===");
        System.out.println(manager.findTrackById(song1.getId()).get().getTitle());

        System.out.println("\n=== EQUALITY AND HASHING (UUID business key) ===");
        java.util.Set<Track> identityCheck = new java.util.HashSet<>();
        identityCheck.add(song1);
        identityCheck.add(new Song(song1.getId(), "Bohemian Rhapsody", "Queen", 354,
                "A Night at the Opera", "Rock"));
        System.out.println("Same UUID objects in HashSet: " + identityCheck.size());

        System.out.println("\n=== CATEGORY INDEX (Map<String, Set<Track>>) ===");
        System.out.println("Rock tracks: " + manager.findTracksByCategory("rock").size());
        System.out.println("Category counts: " + manager.getCategoryCounts());

        System.out.println("\n=== NATURAL ORDER (Comparable: title) ===");
        List<Track> naturalOrder = manager.getAllTracks();
        Collections.sort(naturalOrder);
        printTrackTitles(naturalOrder);

        System.out.println("\n=== CUSTOM COMPARATORS ===");
        System.out.println("By artist, then title:");
        printTrackTitles(manager.getTracksSorted(TrackComparators.BY_ARTIST_THEN_TITLE));
        System.out.println("By duration, longest first:");
        printTrackTitles(manager.getTracksSorted(TrackComparators.BY_DURATION_DESC));
        System.out.println("By play count, highest first:");
        printTrackTitles(manager.getTracksSorted(TrackComparators.BY_PLAY_COUNT_DESC));

        System.out.println("\n=== REQUEST QUEUE (PriorityQueue) ===");
        manager.submitPlaybackRequest(listener, song1.getId(), 3);
        manager.submitPlaybackRequest(listener, podcast.getId(), 9);
        manager.submitPlaybackRequest(listener, song2.getId(), 9);
        for (PlaybackRequest request : manager.getPendingRequests()) {
            System.out.println("priority=" + request.getPriority()
                    + ", trackId=" + request.getTrackId()
                    + ", requester=" + request.getRequesterName());
        }

        System.out.println("\n=== OPERATOR PROCESSES REQUESTS ===");
        manager.processNextPlaybackRequest(operator);
        manager.processNextPlaybackRequest(operator);
        System.out.println("Requests still pending: " + manager.getPendingRequestCount());

        System.out.println("\n=== SYNCHRONIZED UPDATE ===");
        Song updatedSong = new Song(song1.getId(), "Bohemian Rhapsody - Remastered",
                "Queen", 360, "A Night at the Opera (Remastered)", "Classic Rock");
        manager.updateTrack(updatedSong);
        System.out.println("Updated repository title: "
                + manager.findTrackById(song1.getId()).get().getTitle());
        System.out.println("Updated title in Favorites: "
                + favorites.getTracks().get(0).getTitle());
        System.out.println("New category index size: "
                + manager.findTracksByCategory("classic rock").size());

        System.out.println("\n=== SYNCHRONIZED DELETE ===");
        manager.removeTrack(song2.getId());
        System.out.println("Blinding Lights still in Study: "
                + study.containsTrack(song2.getId()));
        System.out.println("Total tracks after deletion: " + manager.getLibraryTrackCount());

        System.out.println("\n=== PLAYBACK HISTORY AND EXPORT ===");
        System.out.println("Favorites history: " + favorites.getHistory().getEntries());
        new PlaylistExporter().export(favorites);

        System.out.println("\n=== ANALYTICS REPORT ===");
        System.out.println("Library tracks: " + manager.getLibraryTrackCount());
        System.out.println("Playlist entries: " + manager.getTotalPlaylistEntries());
        System.out.println("Library duration (seconds): " + manager.getTotalLibraryDurationSeconds());
        System.out.printf("Estimated royalties: %.3f%n", manager.getTotalEstimatedRoyalties());
        System.out.println("Playlist sizes: " + manager.getPlaylistTrackCounts());
        System.out.println("Registered listeners: " + manager.findUsersByRole(UserRole.LISTENER).size());

        System.out.println("\n=== VALIDATION EXAMPLE ===");
        try {
            manager.submitPlaybackRequest(operator, audiobook.getId(), 5);
        } catch (IllegalStateException exception) {
            System.out.println("Expected role validation: " + exception.getMessage());
        }
    }

    private static void printTrackTitles(List<Track> tracks) {
        for (Track track : tracks) {
            System.out.println("- " + track.getTitle() + " | " + track.getArtist()
                    + " | " + track.getFormattedDuration());
        }
    }
}
