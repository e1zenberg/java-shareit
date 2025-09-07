package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

/**
 * Преобразование User <-> UserDto.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
