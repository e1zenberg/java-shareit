package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Простое DAO для пользователей.
 */
public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Collection<User> findAll();

    void deleteById(Long id);

    boolean existsByEmail(String email);
}
