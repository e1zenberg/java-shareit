package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

public final class BookingMapper {
    private BookingMapper() {}

    public static BookingDto toDto(Booking b) {
        return new BookingDto(
                b.getId(),
                b.getStart(),
                b.getEnd(),
                b.getItem() != null ? b.getItem().getId() : null,
                b.getBooker() != null ? b.getBooker().getId() : null,
                b.getStatus()
        );
    }

    public static BookingShortDto toShort(Booking b) {
        if (b == null) return null;
        return new BookingShortDto(b.getId(), b.getBooker() != null ? b.getBooker().getId() : null);
    }
}
