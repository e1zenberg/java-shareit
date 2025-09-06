package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Сервис работы с вещами.
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        validateNew(dto);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerId));
        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(Boolean.TRUE.equals(dto.getAvailable()))
                .owner(owner)
                .build();
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        if (item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Only owner can edit the item");
        }

        if (StringUtils.hasText(patch.getName())) {
            item.setName(patch.getName());
        }
        if (StringUtils.hasText(patch.getDescription())) {
            item.setDescription(patch.getDescription());
        }
        if (patch.getAvailable() != null) {
            item.setAvailable(patch.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long requesterId, Long itemId) {
        // requesterId оставлен для будущих спринтов
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerId));
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of(); // пустой поисковый текст -> пустой результат
        }
        final String query = text.toLowerCase(Locale.ROOT); // говорящая переменная
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> containsIgnoreCase(item.getName(), query)
                        || containsIgnoreCase(item.getDescription(), query))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private void validateNew(ItemDto dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("description must not be blank");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("available must not be null");
        }
    }
}
