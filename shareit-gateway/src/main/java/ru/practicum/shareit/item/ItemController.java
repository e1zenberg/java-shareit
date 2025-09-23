package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) long ownerId,
                                         @RequestBody @Valid ItemDto dto) {
        return itemClient.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_HEADER) long ownerId,
                                         @PathVariable long itemId,
                                         @RequestBody ItemDto patch) {
        return itemClient.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) long requesterId,
                                          @PathVariable long itemId) {
        return itemClient.getById(requesterId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader(USER_HEADER) long ownerId) {
        return itemClient.getByOwner(ownerId, 0, 10);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "text") String text) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_HEADER) long userId,
                                             @PathVariable long itemId,
                                             @RequestBody @Valid CommentCreateDto comment) {
        return itemClient.addComment(userId, itemId, comment);
    }
}
