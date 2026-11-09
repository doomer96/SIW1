package musicplaylist.model;

import java.util.UUID;

public class Podcast extends Track implements Playable {
    private String host;
    private int episodeNumber;

    public Podcast() {
        super();
        host = "Unknown";
        episodeNumber = 1;
    }

    public Podcast(String title, String artist, int durationSeconds, String host, int episodeNumber) {
        super(title, artist, durationSeconds);
        this.host = requireText(host, "Host");
        if (episodeNumber <= 0) {
            throw new IllegalArgumentException("Episode number must be positive");
        }
        this.episodeNumber = episodeNumber;
    }

    public Podcast(UUID id, String title, String artist, int durationSeconds, String host, int episodeNumber) {
        super(id, title, artist, durationSeconds);
        this.host = requireText(host, "Host");
        if (episodeNumber <= 0) {
            throw new IllegalArgumentException("Episode number must be positive");
        }
        this.episodeNumber = episodeNumber;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = requireText(host, "Host");
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        if (episodeNumber <= 0) {
            throw new IllegalArgumentException("Episode number must be positive");
        }
        this.episodeNumber = episodeNumber;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("    Host: " + host + ", Episode: " + episodeNumber);
    }

    @Override
    public String getType() {
        return "Podcast";
    }

    @Override
    public double calculateRoyalty() {
        return getPlayCount() * 0.002 + 5.0;
    }

    @Override
    public void play() {
        incrementPlayCount();
        System.out.println("Playing podcast: " + getTitle() + " #" + episodeNumber);
    }

    @Override
    public void stop() {
        System.out.println("Stopped podcast: " + getTitle());
    }

    public void skipIntro() {
        System.out.println("Skipped 30 seconds of intro in " + getTitle());
    }

    public int getNextEpisodeNumber() {
        return episodeNumber + 1;
    }

    public boolean isLongEpisode() {
        return getDurationSeconds() > 3600;
    }
}
