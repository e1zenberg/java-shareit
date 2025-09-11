package ru.practicum.shareit.user.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

/**
 * Памятная реализация репозитория пользователей (профиль: inmemory).
 * Сделан abstract, чтобы не реализовывать все методы JpaRepository.
 */
@Repository
@Profile("inmemory")
public abstract class InMemoryUserRepository implements UserRepository {

    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public User save(final User user) {
        if (user.getId() == null) {
            user.setId(idSeq.incrementAndGet());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(final Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(final Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
