package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking.BookingStatus;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        Long itemId,
        Long bookerId,
        BookingStatus status
) { }
