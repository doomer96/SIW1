package musicplaylist.service;

import musicplaylist.model.Playlist;
import musicplaylist.model.Track;

public class PlaylistExporter {
    public void export(Playlist playlist) {
        if (playlist == null) throw new IllegalArgumentException("Playlist must not be null");
        System.out.println("Playlist: " + playlist.getName());
        int index = 1;
        for (Track track : playlist.getTracks()) {
            System.out.printf("%d. %s - %s (%s)%n", index++, track.getArtist(),
                    track.getTitle(), track.getFormattedDuration());
        }
        System.out.println("Total: " + playlist.getTotalDurationSeconds() / 60 + " min");
    }
}
