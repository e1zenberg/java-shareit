package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern SIMPLE_EMAIL = Pattern.compile(".+@.+\\..+");

    private final UserRepository userRepository;

    @Override
    public User create(final User user) {
        if (user == null) {
            throw new ValidationException("User payload must not be null");
        }
        validateEmailOrThrow(user.getEmail());
        if (!StringUtils.hasText(user.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email already used: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public User update(final Long id, final User patch) {
        final User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (patch == null) {
            return existing;
        }
        if (StringUtils.hasText(patch.getName())) {
            existing.setName(patch.getName());
        }
        if (patch.getEmail() != null) {
            validateEmailOrThrow(patch.getEmail());
            if (!patch.getEmail().equals(existing.getEmail())
                    && userRepository.existsByEmail(patch.getEmail())) {
                throw new ConflictException("Email already used: " + patch.getEmail());
            }
            existing.setEmail(patch.getEmail());
        }
        return userRepository.save(existing);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    private void validateEmailOrThrow(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("email must not be blank");
        }
        if (!SIMPLE_EMAIL.matcher(email).matches()) {
            throw new ValidationException("email is invalid: " + email);
        }
    }
}
