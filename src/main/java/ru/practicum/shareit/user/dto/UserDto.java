package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

/**
 * DTO пользователя для REST.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE) // по умолчанию делаем поля приватными (исключаем дублирование модификаторов)
public class UserDto {
    Long id;
    String name;
    String email;
}
