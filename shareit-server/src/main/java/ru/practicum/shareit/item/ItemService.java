package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto dto);
    ItemDto update(Long ownerId, Long itemId, ItemDto patch);

    ItemDetailsDto getById(Long requesterId, Long itemId);

    List<ItemWithBookingsDto> getByOwner(Long ownerId);

    List<ItemDto> search(String text);

    // Комментарии
    CommentDto addComment(Long userId, Long itemId, CommentCreateDto comment);
}
