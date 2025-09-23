package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) long userId,
                                         @RequestBody @Valid BookItemRequestDto requestDto) {
        return bookingClient.bookItem(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(USER_HEADER) long ownerId,
                                          @PathVariable long bookingId,
                                          @RequestParam("approved") boolean approved) {
        return bookingClient.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> get(@RequestHeader(USER_HEADER) long userId,
                                      @PathVariable long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getForUser(@RequestHeader(USER_HEADER) long userId,
                                             @RequestParam(name = "state", defaultValue = "ALL") String state,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState parsed = BookingState.valueOf(state);
        return bookingClient.getBookings(userId, parsed, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getForOwner(@RequestHeader(USER_HEADER) long ownerId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String state,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState parsed = BookingState.valueOf(state);
        return bookingClient.getBookingsOwner(ownerId, parsed, from, size);
    }
}
