package musicplaylist.model;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private String name;
    private final UserRole role;

    public User(String name, UserRole role) {
        this(UUID.randomUUID(), name, role);
    }

    public User(UUID id, String name, UserRole role) {
        this.id = Objects.requireNonNull(id, "User ID must not be null");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name must not be empty");
        }
        this.name = name.trim();
        this.role = Objects.requireNonNull(role, "User role must not be null");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name must not be empty");
        }
        this.name = name.trim();
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof User)) return false;
        User user = (User) object;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
