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
        // requesterId пока не используется, но оставлен для совместимости с дальнейшими спринтами
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        // проверим существование владельца для единообразия ошибок
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerId));
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of(); // возвращаем пустой список, если текст пуст
        }
        final String q = text.toLowerCase(Locale.ROOT);
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(i -> containsIgnoreCase(i.getName(), q) || containsIgnoreCase(i.getDescription(), q))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String src, String q) {
        return src != null && src.toLowerCase(Locale.ROOT).contains(q);
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
