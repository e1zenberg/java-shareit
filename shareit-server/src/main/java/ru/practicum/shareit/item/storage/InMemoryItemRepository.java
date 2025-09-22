package ru.practicum.shareit.item.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

/**
 * Памятная реализация репозитория вещей (профиль: inmemory).
 * Оставлен abstract, чтобы не реализовывать весь контракт JpaRepository.
 */
@Repository
@Profile("inmemory")
public abstract class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public Item save(final Item item) {
        if (item.getId() == null) {
            item.setId(idSeq.incrementAndGet());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(final Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void deleteById(final Long id) {
        items.remove(id);
    }

    @Override
    public Page<Item> findByOwner_Id(final Long ownerId, final Pageable pageable) {
        final List<Item> data = items.values().stream()
                .filter(i -> i.getOwner() != null && Objects.equals(i.getOwner().getId(), ownerId))
                .collect(Collectors.toList());
        return new PageImpl<>(data, pageable, data.size());
    }

    @Override
    public Page<Item> search(final String text, final Pageable pageable) {
        final String q = text == null ? "" : text.toLowerCase(Locale.ROOT);
        final List<Item> data = items.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> (i.getName() != null && i.getName().toLowerCase(Locale.ROOT).contains(q))
                        || (i.getDescription() != null && i.getDescription().toLowerCase(Locale.ROOT).contains(q)))
                .collect(Collectors.toList());
        return new PageImpl<>(data, pageable, data.size());
    }
}
