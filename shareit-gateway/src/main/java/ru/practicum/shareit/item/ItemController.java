package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) long ownerId,
                                         @RequestBody @Valid ItemDto dto) {
        log.info("Create item by userId={}", ownerId);
        return itemClient.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_HEADER) long ownerId,
                                         @PathVariable(name = "itemId") long itemId,
                                         @RequestBody ItemDto patch) {
        log.info("Update item {} by userId={}", itemId, ownerId);
        return itemClient.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) long requesterId,
                                          @PathVariable(name = "itemId") long itemId) {
        return itemClient.getById(requesterId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader(USER_HEADER) long ownerId,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.getByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "text") String text) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.ok().body(java.util.List.of());
        }
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_HEADER) long userId,
                                             @PathVariable(name = "itemId") long itemId,
                                             @RequestBody @Valid CommentCreateDto comment) {
        return itemClient.addComment(userId, itemId, comment);
    }
}
