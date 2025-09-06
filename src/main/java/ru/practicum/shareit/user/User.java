package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Простейшая модель пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    // уникальный идентификатор пользователя
    private Long id;
    // отображаемое имя/логин
    private String name;
    // адрес электронной почты (уникальный в системе)
    private String email;
}
