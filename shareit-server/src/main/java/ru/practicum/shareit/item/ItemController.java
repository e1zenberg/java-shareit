package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(name = USER_HEADER) Long ownerId,
                          @RequestBody ItemDto dto) {
        return itemService.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(name = USER_HEADER) Long ownerId,
                          @PathVariable(name = "itemId") Long itemId,
                          @RequestBody ItemDto patch) {
        return itemService.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getById(@RequestHeader(name = USER_HEADER) Long requesterId,
                                       @PathVariable(name = "itemId") Long itemId) {
        List<ItemWithBookingsDto> owned = itemService.getByOwner(requesterId);
        for (ItemWithBookingsDto it : owned) {
            if (it.id().equals(itemId)) {
                return it;
            }
        }
        ItemDetailsDto d = itemService.getById(requesterId, itemId);
        return new ItemWithBookingsDto(
                d.id(),
                d.name(),
                d.description(),
                d.available(),
                d.ownerId(),
                d.requestId(),
                null,
                null,
                d.comments()
        );
    }

    @GetMapping
    public List<ItemWithBookingsDto> getByOwner(@RequestHeader(name = USER_HEADER) Long ownerId) {
        return itemService.getByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(name = USER_HEADER) Long userId,
                                 @PathVariable(name = "itemId") Long itemId,
                                 @RequestBody CommentCreateDto comment) {
        return itemService.addComment(userId, itemId, comment);
    }
}
