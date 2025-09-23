package ru.practicum.shareit.item.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

/**
 * Проверка поиска вещей: по имени/описанию, только доступные.
 * Профиль "test" включает H2 с ddl-auto=create.
 */
@DataJpaTest
@ActiveProfiles("test")
class ItemRepositorySearchTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void search_shouldFindByNameOrDescription_andOnlyAvailable() {
        User owner = userRepository.save(
                User.builder().name("Owner").email("owner@test.io").build()
        );

        Item matchAvailable = itemRepository.save(
                Item.builder()
                        .name("Дрель ударная")
                        .description("Мощная ударная дрель")
                        .available(true)
                        .owner(owner)
                        .build()
        );
        Item matchUnavailable = itemRepository.save(
                Item.builder()
                        .name("Дрель мини")
                        .description("Компактная, но не доступна")
                        .available(false)
                        .owner(owner)
                        .build()
        );
        Item notMatch = itemRepository.save(
                Item.builder()
                        .name("Лобзик")
                        .description("Для дерева")
                        .available(true)
                        .owner(owner)
                        .build()
        );

        var page = itemRepository.search("дрель", org.springframework.data.domain.PageRequest.of(0, 10));
        List<Item> result = page.getContent();

        assertThat(result)
                .contains(matchAvailable)
                .doesNotContain(matchUnavailable)
                .doesNotContain(notMatch);
    }
}
