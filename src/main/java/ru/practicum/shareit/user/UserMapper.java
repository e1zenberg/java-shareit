package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

/**
 * Маппер между User и UserDto.
 */
public final class UserMapper {

    private UserMapper() {
        // utility
    }

    public static User toEntity(final UserDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto toDto(final User user) {
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
