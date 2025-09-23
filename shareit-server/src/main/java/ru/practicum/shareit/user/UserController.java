package ru.practicum.shareit.user;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody final UserDto dto) {
        final User created = userService.create(UserMapper.toEntity(dto));
        return UserMapper.toDto(created);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable(name = "id") final Long id,
                          @RequestBody final UserDto dto) {
        final User updated = userService.update(id, UserMapper.toEntity(dto));
        return UserMapper.toDto(updated);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable(name = "id") final Long id) {
        return UserMapper.toDto(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name = "id") final Long id) {
        userService.deleteById(id);
    }
}
