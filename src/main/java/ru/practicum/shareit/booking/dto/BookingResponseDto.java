package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record BookingResponseDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        ItemNestedDto item,
        BookerNestedDto booker
) {
    public record ItemNestedDto(Long id, String name) {}
    public record BookerNestedDto(Long id) {}
}
