package musicplaylist.service;

import musicplaylist.model.Track;

import java.util.Comparator;

/** Reusable dynamic sorting strategies required by the JCF assignment. */
public final class TrackComparators {
    private TrackComparators() { }

    public static final Comparator<Track> BY_TITLE =
            Comparator.comparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Track::getId);

    public static final Comparator<Track> BY_ARTIST_THEN_TITLE =
            Comparator.comparing(Track::getArtist, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Track::getId);

    public static final Comparator<Track> BY_DURATION_DESC =
            Comparator.comparingInt(Track::getDurationSeconds).reversed()
                    .thenComparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER);

    public static final Comparator<Track> BY_PLAY_COUNT_DESC =
            Comparator.comparingInt(Track::getPlayCount).reversed()
                    .thenComparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER);
}
