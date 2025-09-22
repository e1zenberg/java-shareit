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
    public User create(User user) {
        if (user == null || !StringUtils.hasText(user.getEmail()) || !SIMPLE_EMAIL.matcher(user.getEmail()).matches()) {
            throw new ValidationException("email is invalid");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("email already used: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User patch) {
        User actual = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (patch == null) {
            return actual;
        }
        if (StringUtils.hasText(patch.getName())) {
            actual.setName(patch.getName());
        }
        if (patch.getEmail() != null) {
            if (!StringUtils.hasText(patch.getEmail()) || !SIMPLE_EMAIL.matcher(patch.getEmail()).matches()) {
                throw new ValidationException("email is invalid");
            }
            if (!patch.getEmail().equalsIgnoreCase(actual.getEmail())
                    && userRepository.existsByEmail(patch.getEmail())) {
                throw new ConflictException("email already used: " + patch.getEmail());
            }
            actual.setEmail(patch.getEmail());
        }
        return userRepository.save(actual);
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
}
