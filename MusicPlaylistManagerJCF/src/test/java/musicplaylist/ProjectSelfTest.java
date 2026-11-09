package musicplaylist;

import musicplaylist.model.PlaybackRequest;
import musicplaylist.model.Playlist;
import musicplaylist.model.Song;
import musicplaylist.model.Track;
import musicplaylist.model.User;
import musicplaylist.model.UserRole;
import musicplaylist.service.PlaylistManager;
import musicplaylist.service.TrackComparators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProjectSelfTest {
    private ProjectSelfTest() { }

    public static void main(String[] args) {
        testEqualsAndHashCode();
        testTrackUpdateSynchronizesPlaylistAndCategoryIndex();
        testQueuePriorityAndFifo();
        testTrackDeletionCleansRelatedData();
        testRoleValidation();
        testComparatorSorting();
        System.out.println("ALL SELF-TESTS PASSED");
    }

    private static void testEqualsAndHashCode() {
        Song original = new Song("Song A", "Artist A", 100, "Album A", "Rock");
        Song sameIdentity = new Song(original.getId(), "Different title", "Different artist",
                120, "Different album", "Pop");
        Set<Track> set = new HashSet<>();
        set.add(original);
        set.add(sameIdentity);
        check(original.equals(sameIdentity), "Tracks with the same UUID must be equal");
        check(original.hashCode() == sameIdentity.hashCode(), "Equal tracks must share hash code");
        check(set.size() == 1, "HashSet must not store duplicate UUID identities");
    }

    private static void testTrackUpdateSynchronizesPlaylistAndCategoryIndex() {
        PlaylistManager manager = new PlaylistManager();
        Song song = new Song("Original", "Artist", 100, "Album", "Rock");
        song.setPlayCount(17);
        manager.addTrack(song);
        Playlist playlist = new Playlist("Mix");
        manager.addPlaylist(playlist);
        manager.addTrackToPlaylist(playlist.getId(), song.getId());

        Song replacement = new Song(song.getId(), "Updated", "Artist", 120, "Album 2", "Jazz");
        manager.updateTrack(replacement);

        check("Updated".equals(playlist.getTracks().get(0).getTitle()),
                "Playlist reference must be updated");
        check(playlist.getTracks().get(0).getPlayCount() == 17,
                "Playback statistics must survive metadata updates");
        check(manager.findTracksByCategory("Rock").isEmpty(), "Old category index must be cleared");
        check(manager.findTracksByCategory("Jazz").size() == 1, "New category index must be populated");
    }

    private static void testQueuePriorityAndFifo() {
        PlaylistManager manager = new PlaylistManager();
        User listener = new User("Listener", UserRole.LISTENER);
        User operator = new User("Operator", UserRole.OPERATOR);
        manager.addUser(listener);
        manager.addUser(operator);
        Song first = new Song("First", "Artist", 100, "Album", "Pop");
        Song second = new Song("Second", "Artist", 110, "Album", "Pop");
        Song high = new Song("High", "Artist", 120, "Album", "Pop");
        manager.addTrack(first);
        manager.addTrack(second);
        manager.addTrack(high);

        PlaybackRequest firstRequest = manager.submitPlaybackRequest(listener, first.getId(), 5);
        manager.submitPlaybackRequest(listener, second.getId(), 5);
        manager.submitPlaybackRequest(listener, high.getId(), 9);

        List<PlaybackRequest> pending = manager.getPendingRequests();
        check(pending.get(0).getTrackId().equals(high.getId()), "Higher priority must be first");
        check(pending.get(1).getId().equals(firstRequest.getId()), "Equal priorities must be FIFO");

        PlaybackRequest processedHigh = manager.processNextPlaybackRequest(operator).get();
        PlaybackRequest processedFirst = manager.processNextPlaybackRequest(operator).get();
        check(processedHigh.getTrackId().equals(high.getId()), "Operator must process high priority first");
        check(processedFirst.getId().equals(firstRequest.getId()), "Operator must preserve FIFO for equal priorities");
    }

    private static void testTrackDeletionCleansRelatedData() {
        PlaylistManager manager = new PlaylistManager();
        User listener = new User("Listener", UserRole.LISTENER);
        manager.addUser(listener);
        Song song = new Song("To delete", "Artist", 100, "Album", "Rock");
        manager.addTrack(song);
        Playlist playlist = new Playlist("Mix");
        manager.addPlaylist(playlist);
        manager.addTrackToPlaylist(playlist.getId(), song.getId());
        manager.submitPlaybackRequest(listener, song.getId(), 3);

        check(manager.removeTrack(song.getId()), "Existing track should be deleted");
        check(!manager.findTrackById(song.getId()).isPresent(), "Deleted track must leave primary index");
        check(!playlist.containsTrack(song.getId()), "Deleted track must leave all playlists");
        check(manager.getPendingRequestCount() == 0, "Pending requests for deleted track must be cancelled");
    }

    private static void testRoleValidation() {
        PlaylistManager manager = new PlaylistManager();
        User operator = new User("Operator", UserRole.OPERATOR);
        manager.addUser(operator);
        Song song = new Song("Song", "Artist", 100, "Album", "Rock");
        manager.addTrack(song);
        boolean rejected = false;
        try {
            manager.submitPlaybackRequest(operator, song.getId(), 2);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(rejected, "OPERATOR must not submit listener requests");
    }

    private static void testComparatorSorting() {
        PlaylistManager manager = new PlaylistManager();
        Song shortSong = new Song("A", "Artist", 100, "Album", "Pop");
        Song longSong = new Song("B", "Artist", 300, "Album", "Pop");
        manager.addTrack(shortSong);
        manager.addTrack(longSong);
        List<Track> sorted = manager.getTracksSorted(TrackComparators.BY_DURATION_DESC);
        check(sorted.get(0).getId().equals(longSong.getId()), "Duration comparator must sort descending");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
