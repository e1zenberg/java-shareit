package ru.practicum.shareit.item.dto;

import java.util.List;

public record ItemDetailsDto(
        Long id,
        String name,
        String description,
        Boolean available,
        Long ownerId,
        Long requestId,
        List<CommentDto> comments
) { }
