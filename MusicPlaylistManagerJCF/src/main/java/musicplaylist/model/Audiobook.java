package musicplaylist.model;

import java.util.UUID;

public class Audiobook extends Track implements Playable, Downloadable {
    private String narrator;
    private int totalChapters;
    private int currentChapter;

    public Audiobook() {
        super();
        narrator = "Unknown";
        totalChapters = 1;
        currentChapter = 1;
    }

    public Audiobook(String title, String artist, int durationSeconds, String narrator, int totalChapters) {
        super(title, artist, durationSeconds);
        this.narrator = requireText(narrator, "Narrator");
        if (totalChapters <= 0) {
            throw new IllegalArgumentException("Chapters must be positive");
        }
        this.totalChapters = totalChapters;
        currentChapter = 1;
    }

    public Audiobook(UUID id, String title, String artist, int durationSeconds,
                     String narrator, int totalChapters) {
        super(id, title, artist, durationSeconds);
        this.narrator = requireText(narrator, "Narrator");
        if (totalChapters <= 0) {
            throw new IllegalArgumentException("Chapters must be positive");
        }
        this.totalChapters = totalChapters;
        currentChapter = 1;
    }

    public String getNarrator() {
        return narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = requireText(narrator, "Narrator");
    }

    public int getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(int totalChapters) {
        if (totalChapters <= 0) {
            throw new IllegalArgumentException("Chapters must be positive");
        }
        this.totalChapters = totalChapters;
        if (currentChapter > totalChapters) {
            currentChapter = totalChapters;
        }
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(int currentChapter) {
        if (currentChapter < 1 || currentChapter > totalChapters) {
            throw new IllegalArgumentException(
                    "Chapter must be between 1 and " + totalChapters);
        }
        this.currentChapter = currentChapter;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("    Narrator: " + narrator + ", Chapter: "
                + currentChapter + "/" + totalChapters);
    }

    @Override
    public String getType() {
        return "Audiobook";
    }

    @Override
    public double calculateRoyalty() {
        return getPlayCount() * 0.05;
    }

    @Override
    public void play() {
        incrementPlayCount();
        System.out.println("Playing audiobook: " + getTitle() + ", chapter " + currentChapter);
    }

    @Override
    public void stop() {
        System.out.println("Bookmark saved at chapter " + currentChapter + " of " + getTitle());
    }

    @Override
    public double getFileSizeMb() {
        return getDurationSeconds() * 8 / 1024.0;
    }

    @Override
    public void download() {
        System.out.printf("Downloading audiobook %s (%.2f MB)%n", getTitle(), getFileSizeMb());
    }

    public void nextChapter() {
        if (currentChapter < totalChapters) {
            currentChapter++;
        }
    }

    public double getProgressPercent() {
        return currentChapter * 100.0 / totalChapters;
    }

    public int estimateDaysToFinish(int minutesPerDay) {
        if (minutesPerDay <= 0) {
            throw new IllegalArgumentException("Minutes per day must be positive");
        }
        return (int) Math.ceil(getDurationSeconds() / 60.0 / minutesPerDay);
    }
}
