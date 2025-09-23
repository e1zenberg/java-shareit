package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.*;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingCreateDto dto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto get(Long userId, Long bookingId);

    List<BookingDto> listForUser(Long userId, BookingState state, int from, int size);

    List<BookingDto> listForOwner(Long ownerId, BookingState state, int from, int size);
}
