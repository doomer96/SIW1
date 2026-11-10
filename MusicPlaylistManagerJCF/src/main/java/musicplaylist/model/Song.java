package musicplaylist.model;

import java.util.UUID;

public class Song extends Track implements Playable, Downloadable {
    private String album;
    private String genre;

    public Song() {
        super();
        album = "Single";
        genre = "Unknown";
    }

    public Song(String title, String artist, int durationSeconds, String album, String genre) {
        super(title, artist, durationSeconds);
        this.album = requireText(album, "Album");
        this.genre = requireText(genre, "Genre");
    }

    public Song(UUID id, String title, String artist, int durationSeconds, String album, String genre) {
        super(id, title, artist, durationSeconds);
        this.album = requireText(album, "Album");
        this.genre = requireText(genre, "Genre");
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = requireText(album, "Album");
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = requireText(genre, "Genre");
    }

    @Override
    public String getCategoryKey() {
        return genre;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("    Album: " + album + ", Genre: " + genre);
    }

    @Override
    public String getType() {
        return "Song";
    }

    @Override
    public double calculateRoyalty() {
        return getPlayCount() * 0.004;
    }

    @Override
    public void play() {
        incrementPlayCount();
        System.out.println("Playing song: " + getTitle());
    }

    @Override
    public void stop() {
        System.out.println("Stopped song: " + getTitle());
    }

    @Override
    public double getFileSizeMb() {
        return getDurationSeconds() * 16 / 1024.0;
    }

    @Override
    public void download() {
        System.out.printf("Downloading song %s (%.2f MB)%n", getTitle(), getFileSizeMb());
    }

    public boolean isLongSong() {
        return getDurationSeconds() > 300;
    }

    public boolean isHit() {
        return getPlayCount() >= 1000;
    }

    public String createRemixTitle() {
        return getTitle() + " (Remix)";
    }
}
