package musicplaylist.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A listener's request. Higher priority is processed first; equal priorities are FIFO. */
public class PlaybackRequest implements Comparable<PlaybackRequest> {
    private final UUID id;
    private final UUID requesterId;
    private final String requesterName;
    private final UUID trackId;
    private final int priority;
    private final Instant submittedAt;
    private final long sequenceNumber;
    private RequestStatus status;

    public PlaybackRequest(UUID requesterId, String requesterName, UUID trackId, int priority) {
        this(requesterId, requesterName, trackId, priority, 0L);
    }

    public PlaybackRequest(UUID requesterId, String requesterName, UUID trackId,
                           int priority, long sequenceNumber) {
        this(UUID.randomUUID(), requesterId, requesterName, trackId, priority,
                Instant.now(), sequenceNumber);
    }

    public PlaybackRequest(UUID id, UUID requesterId, String requesterName,
                           UUID trackId, int priority, Instant submittedAt,
                           long sequenceNumber) {
        this.id = Objects.requireNonNull(id, "Request ID must not be null");
        this.requesterId = Objects.requireNonNull(requesterId, "Requester ID must not be null");
        if (requesterName == null || requesterName.trim().isEmpty()) {
            throw new IllegalArgumentException("Requester name must not be empty");
        }
        this.requesterName = requesterName.trim();
        this.trackId = Objects.requireNonNull(trackId, "Track ID must not be null");
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }
        this.priority = priority;
        this.submittedAt = Objects.requireNonNull(submittedAt, "Submission time must not be null");
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number must not be negative");
        }
        this.sequenceNumber = sequenceNumber;
        this.status = RequestStatus.PENDING;
    }

    public UUID getId() { return id; }
    public UUID getRequesterId() { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public UUID getTrackId() { return trackId; }
    public int getPriority() { return priority; }
    public Instant getSubmittedAt() { return submittedAt; }
    public long getSequenceNumber() { return sequenceNumber; }
    public RequestStatus getStatus() { return status; }

    public void markProcessed() {
        requirePending();
        status = RequestStatus.PROCESSED;
    }

    public void markCancelled() {
        requirePending();
        status = RequestStatus.CANCELLED;
    }

    private void requirePending() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can change status");
        }
    }

    @Override
    public int compareTo(PlaybackRequest other) {
        Objects.requireNonNull(other, "Request to compare must not be null");
        int result = Integer.compare(other.priority, priority);
        if (result == 0) {
            result = Long.compare(sequenceNumber, other.sequenceNumber);
        }
        if (result == 0) {
            result = submittedAt.compareTo(other.submittedAt);
        }
        if (result == 0) {
            result = id.compareTo(other.id);
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PlaybackRequest)) return false;
        PlaybackRequest that = (PlaybackRequest) object;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
