package musicplaylist.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Common entity for songs, podcasts and audiobooks.
 * UUID is the immutable business key used by equals() and hashCode().
 */
public abstract class Track implements Comparable<Track> {
    private final UUID id;
    private String title;
    private String artist;
    private int durationSeconds;
    private int playCount;

    protected Track() {
        this(UUID.randomUUID(), "Unknown", "Unknown", 1);
    }

    protected Track(String title, String artist, int durationSeconds) {
        this(UUID.randomUUID(), title, artist, durationSeconds);
    }

    protected Track(UUID id, String title, String artist, int durationSeconds) {
        this.id = Objects.requireNonNull(id, "Track ID must not be null");
        this.title = requireText(title, "Title");
        this.artist = requireText(artist, "Artist");
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.durationSeconds = durationSeconds;
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireText(title, "Title");
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = requireText(artist, "Artist");
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.durationSeconds = durationSeconds;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        if (playCount < 0) {
            throw new IllegalArgumentException("Play count must not be negative");
        }
        this.playCount = playCount;
    }

    public void incrementPlayCount() {
        playCount++;
    }

    public String getFormattedDuration() {
        return String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
    }

    public void displayInfo() {
        System.out.printf("[%s] %s - %s (%s), plays: %d%n",
                getType(), artist, title, getFormattedDuration(), playCount);
    }

    /** Category used by the repository index. Songs are grouped by genre. */
    public String getCategoryKey() {
        return getType();
    }

    public abstract String getType();

    public abstract double calculateRoyalty();

    /** Natural order: title, then artist, then ID for a deterministic result. */
    @Override
    public int compareTo(Track other) {
        Objects.requireNonNull(other, "Track to compare must not be null");
        int result = String.CASE_INSENSITIVE_ORDER.compare(title, other.title);
        if (result == 0) {
            result = String.CASE_INSENSITIVE_ORDER.compare(artist, other.artist);
        }
        if (result == 0) {
            result = id.compareTo(other.id);
        }
        return result;
    }

    /** All Track subclasses share the globally unique UUID identity contract. */
    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Track)) {
            return false;
        }
        Track track = (Track) object;
        return id.equals(track.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
