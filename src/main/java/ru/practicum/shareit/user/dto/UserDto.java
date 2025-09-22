package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;

/**
 * DTO пользователя для слоёв представления/транспорта.
 */
@Value
@Builder
public class UserDto {
    Long id;
    String name;
    String email;
}
