package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Хранение вещей в памяти.
 */
@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(seq.incrementAndGet());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwner() != null && Objects.equals(i.getOwner().getId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }
}
