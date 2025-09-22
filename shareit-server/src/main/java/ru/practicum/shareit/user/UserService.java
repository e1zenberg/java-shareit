package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User create(User user);

    User update(Long id, User patch);

    User getById(Long id);

    List<User> getAll();

    void deleteById(Long id);
}
