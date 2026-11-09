package musicplaylist.repository;

import musicplaylist.model.User;
import musicplaylist.model.UserRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** User registry with both primary-key and role indexes. */
public class UserRepository {
    private final List<User> users = new ArrayList<>();
    private final Map<UUID, User> byId = new HashMap<>();
    private final Map<UserRole, Set<User>> byRole = new HashMap<>();
    private final Map<UUID, UserRole> roleById = new HashMap<>();

    public synchronized void add(User user) {
        if (user == null) throw new IllegalArgumentException("User must not be null");
        if (byId.containsKey(user.getId())) {
            throw new IllegalStateException("User ID already exists: " + user.getId());
        }
        users.add(user);
        byId.put(user.getId(), user);
        addToRoleIndex(user);
    }

    public synchronized User update(User replacement) {
        if (replacement == null) throw new IllegalArgumentException("User must not be null");
        User old = byId.get(replacement.getId());
        if (old == null) throw new java.util.NoSuchElementException("User not found: " + replacement.getId());
        removeFromRoleIndex(old.getId());
        users.set(users.indexOf(old), replacement);
        byId.put(replacement.getId(), replacement);
        addToRoleIndex(replacement);
        return old;
    }

    public synchronized Optional<User> remove(UUID id) {
        if (id == null) throw new IllegalArgumentException("User ID must not be null");
        User removed = byId.remove(id);
        if (removed == null) return Optional.empty();
        users.remove(removed);
        removeFromRoleIndex(id);
        return Optional.of(removed);
    }

    public synchronized Optional<User> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public synchronized List<User> findByRole(UserRole role) {
        if (role == null) throw new IllegalArgumentException("Role must not be null");
        Set<User> result = byRole.get(role);
        return result == null ? new ArrayList<>() : new ArrayList<>(result);
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(users);
    }

    private void addToRoleIndex(User user) {
        byRole.computeIfAbsent(user.getRole(), key -> new LinkedHashSet<>()).add(user);
        roleById.put(user.getId(), user.getRole());
    }

    private void removeFromRoleIndex(UUID id) {
        UserRole oldRole = roleById.remove(id);
        if (oldRole == null) return;
        Set<User> roleUsers = byRole.get(oldRole);
        if (roleUsers != null) {
            roleUsers.removeIf(user -> user.getId().equals(id));
            if (roleUsers.isEmpty()) byRole.remove(oldRole);
        }
    }
}
