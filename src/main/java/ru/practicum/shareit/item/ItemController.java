package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.storage.ItemRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final ObjectProvider<ItemRepository> itemRepositoryProvider;
    private final ObjectProvider<BookingRepository> bookingRepositoryProvider;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) Long ownerId,
                          @RequestBody ItemDto dto) {
        return itemService.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto patch) {
        return itemService.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public Map<String, Object> getById(@RequestHeader(USER_HEADER) Long requesterId,
                                       @PathVariable Long itemId) {
        ItemDetailsDto dto = itemService.getById(requesterId, itemId);

        Map<String, Object> json = new HashMap<>();
        json.put("id", dto.id());
        json.put("name", dto.name());
        json.put("description", dto.description());
        json.put("available", dto.available());
        json.put("ownerId", dto.ownerId());
        json.put("requestId", dto.requestId());
        json.put("comments", dto.comments());

        ItemRepository itemRepo = itemRepositoryProvider.getIfAvailable();
        BookingRepository bookingRepo = bookingRepositoryProvider.getIfAvailable();

        if (itemRepo != null && bookingRepo != null) {
            itemRepo.findById(itemId).ifPresent(item -> {
                if (item.getOwner() != null && item.getOwner().getId() != null
                        && item.getOwner().getId().equals(requesterId)) {
                    LocalDateTime now = LocalDateTime.now();
                    Booking last = bookingRepo.findFirstByItem_IdAndStartBeforeAndStatusOrderByEndDesc(
                            itemId, now, Booking.BookingStatus.APPROVED);
                    Booking next = bookingRepo.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, Booking.BookingStatus.APPROVED);
                    BookingShortDto lastShort = BookingMapper.toShort(last);
                    BookingShortDto nextShort = BookingMapper.toShort(next);
                    if (lastShort != null) {
                        json.put("lastBooking", lastShort);
                    }
                    if (nextShort != null) {
                        json.put("nextBooking", nextShort);
                    }
                }
            });
        }

        return json;
    }

    @GetMapping
    public List<ItemWithBookingsDto> getByOwner(@RequestHeader(USER_HEADER) Long ownerId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        validatePage(from, size);
        List<ItemWithBookingsDto> all = itemService.getByOwner(ownerId);
        int start = Math.min(from, Math.max(0, all.size()));
        int end = Math.min(start + size, all.size());
        return all.subList(start, end);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentCreateDto comment) {
        return itemService.addComment(userId, itemId, comment);
    }

    private void validatePage(Integer from, Integer size) {
        if (from == null || from < 0) {
            throw new ValidationException("'from' must be >= 0");
        }
        if (size == null || size <= 0) {
            throw new ValidationException("'size' must be > 0");
        }
    }
}
