package ru.practicum.shareit.request;

import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequest toEntity(ItemRequestCreateDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(java.time.LocalDateTime.now())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest entity, List<Item> items) {
        List<ItemRequestDto.ItemShortDto> shortItems = items.stream()
                .map(i -> new ItemRequestDto.ItemShortDto(
                        i.getId(),
                        i.getName(),
                        i.getOwner() != null ? i.getOwner().getId() : null
                ))
                .collect(Collectors.toList());

        return new ItemRequestDto(
                entity.getId(),
                entity.getDescription(),
                entity.getCreated(),
                shortItems
        );
    }
}
