package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * REST-контроллер вещей.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    // во избежание дубляжей общие строки вынесены в константы
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private static final String ITEM_ID_PATH = "/{itemId}";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) Long userId,
                          @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping(ITEM_ID_PATH)
    public ItemDto update(@RequestHeader(USER_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto patch) {
        return itemService.update(userId, itemId, patch);
    }

    @GetMapping(ITEM_ID_PATH)
    public ItemDto getById(@RequestHeader(USER_HEADER) Long userId,
                           @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getByOwner(@RequestHeader(USER_HEADER) Long userId) {
        return itemService.getByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        return itemService.search(text);
    }
}
