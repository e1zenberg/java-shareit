package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;
    private final ObjectProvider<ItemRepository> itemRepositoryProvider;

    @PostMapping
    public Map<String, Object> create(@RequestHeader(USER_HEADER) Long userId,
                                      @RequestBody BookingCreateDto dto) {
        BookingDto b = bookingService.create(userId, dto);
        return toHybridJson(b);
    }

    @PatchMapping("/{bookingId}")
    public Map<String, Object> approve(@RequestHeader(USER_HEADER) Long ownerId,
                                       @PathVariable Long bookingId,
                                       @RequestParam boolean approved) {
        BookingDto b = bookingService.approve(ownerId, bookingId, approved);
        return toHybridJson(b);
    }

    @GetMapping("/{bookingId}")
    public Map<String, Object> get(@RequestHeader(USER_HEADER) Long userId,
                                   @PathVariable Long bookingId) {
        BookingDto b = bookingService.get(userId, bookingId);
        return toHybridJson(b);
    }

    @GetMapping
    public List<Map<String, Object>> listForUser(@RequestHeader(USER_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        return bookingService.listForUser(userId, parseState(state), from, size)
                .stream().map(this::toHybridJson).toList();
    }

    @GetMapping("/owner")
    public List<Map<String, Object>> listForOwner(@RequestHeader(USER_HEADER) Long ownerId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        return bookingService.listForOwner(ownerId, parseState(state), from, size)
                .stream().map(this::toHybridJson).toList();
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown state: " + state);
        }
    }

    private Map<String, Object> toHybridJson(BookingDto b) {
        Map<String, Object> root = new HashMap<>();
        root.put("id", b.id());
        root.put("start", b.start());
        root.put("end", b.end());
        root.put("status", b.status() != null ? b.status().name() : null);
        root.put("itemId", b.itemId());
        root.put("bookerId", b.bookerId());

        Map<String, Object> item = new HashMap<>();
        item.put("id", b.itemId());
        item.put("name", resolveItemName(b.itemId()));
        root.put("item", item);

        Map<String, Object> booker = new HashMap<>();
        booker.put("id", b.bookerId());
        root.put("booker", booker);

        return root;
    }

    private String resolveItemName(Long itemId) {
        if (itemId == null) return null;
        ItemRepository repo = itemRepositoryProvider.getIfAvailable();
        if (repo == null) return null;
        return repo.findById(itemId).map(Item::getName).orElse(null);
    }
}
