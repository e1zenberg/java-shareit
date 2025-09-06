package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Простое DAO для вещей.
 */
public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    Collection<Item> findAll();

    List<Item> findByOwnerId(Long ownerId);

    void deleteById(Long id);
}
