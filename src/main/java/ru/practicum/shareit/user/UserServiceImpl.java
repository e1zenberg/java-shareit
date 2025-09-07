package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Простая вменяемая проверка: есть локальная часть, @, домен и точка в домене
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public UserDto create(UserDto userDto) {
        validateNew(userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }
        User saved = userRepository.save(User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long userId, UserDto patch) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // частичное обновление
        if (StringUtils.hasText(patch.getName())) {
            user.setName(patch.getName());
        }
        if (StringUtils.hasText(patch.getEmail())) {
            String newEmail = patch.getEmail();
            if (!isValidEmail(newEmail)) {
                throw new ValidationException("email is invalid");
            }
            boolean emailTaken = userRepository.findAll().stream()
                    .anyMatch(u -> !u.getId().equals(userId) && u.getEmail().equalsIgnoreCase(newEmail));
            if (emailTaken) {
                throw new ConflictException("Email already exists: " + newEmail);
            }
            user.setEmail(newEmail);
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void validateNew(UserDto dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new ValidationException("email must not be blank");
        }
        if (!isValidEmail(dto.getEmail())) {
            throw new ValidationException("email is invalid");
        }
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
