package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public record ItemWithBookingsDto(
        Long id,
        String name,
        String description,
        Boolean available,
        Long ownerId,
        Long requestId,
        BookingShortDto lastBooking,
        BookingShortDto nextBooking,
        List<CommentDto> comments
) { }
