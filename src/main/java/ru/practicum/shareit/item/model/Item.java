package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * Модель вещи.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    // уникальный идентификатор вещи
    private Long id;
    // краткое название
    private String name;
    // развёрнутое описание
    private String description;
    // доступна ли вещь для аренды
    private Boolean available;
    // владелец вещи
    private User owner;
    // ссылка на запрос, по которому была создана вещь (может отсутствовать)
    private ItemRequest request;
}
