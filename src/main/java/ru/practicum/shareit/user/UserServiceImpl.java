package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
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

        if (StringUtils.hasText(patch.getName())) {
            actual.setName(patch.getName());
        }
        if (StringUtils.hasText(patch.getEmail())) {
            if (!patch.getEmail().equalsIgnoreCase(actual.getEmail()) &&
                    userRepository.existsByEmail(patch.getEmail())) {
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
