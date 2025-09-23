package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

public final class BookingMapper {
    private BookingMapper() {

    }

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem() != null ? booking.getItem().getId() : null,
                booking.getBooker() != null ? booking.getBooker().getId() : null,
                booking.getStatus()
        );
    }

    public static BookingShortDto toShort(Booking booking) {
        if (booking == null) return null;
        return new BookingShortDto(
                booking.getId(),
                booking.getBooker() != null ? booking.getBooker().getId() : null
        );
    }

    public static BookingResponseDto fromDto(BookingDto dto, String itemName) {
        if (dto == null) return null;
        BookingResponseDto.ItemNestedDto item = null;
        if (dto.itemId() != null) {
            item = new BookingResponseDto.ItemNestedDto(dto.itemId(), itemName);
        }
        BookingResponseDto.BookerNestedDto booker = null;
        if (dto.bookerId() != null) {
            booker = new BookingResponseDto.BookerNestedDto(dto.bookerId());
        }
        String status = dto.status() != null ? dto.status().name() : null;
        return new BookingResponseDto(dto.id(), dto.start(), dto.end(), status, item, booker);
    }
}
