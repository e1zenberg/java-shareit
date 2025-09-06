package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Запрос на вещь.
 * На этом спринте класс присутствует как задел под будущую функциональность.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
