package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

/**
 * Интеграционные тесты сервиса бронирований.
 * Профиль "test": H2 in-memory, schema создаёт Hibernate.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test") // <-- добавлено
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void create_shouldRejectOwnItemBooking() {
        User owner = userRepository.save(User.builder().name("O").email("o@test.io").build());
        Item item = itemRepository.save(Item.builder()
                .name("Перфоратор")
                .description("Отличный")
                .available(true)
                .owner(owner)
                .build());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), dto));
    }

    @Test
    void create_approve_flow_shouldWork() {
        User owner = userRepository.save(User.builder().name("Owner").email("owner2@test.io").build());
        User booker = userRepository.save(User.builder().name("User").email("user2@test.io").build());

        Item item = itemRepository.save(Item.builder()
                .name("Шуруповёрт")
                .description("С аккумулятором")
                .available(true)
                .owner(owner)
                .build());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)
        );

        BookingDto created = bookingService.create(booker.getId(), dto);
        assertThat(created.status()).isEqualTo(Booking.BookingStatus.WAITING);

        BookingDto approved = bookingService.approve(owner.getId(), created.id(), true);
        assertThat(approved.status()).isEqualTo(Booking.BookingStatus.APPROVED);

        List<BookingDto> forUser = bookingService.listForUser(booker.getId(), BookingState.ALL, 0, 10);
        assertThat(forUser).extracting(BookingDto::id).contains(approved.id());
    }

    @Test
    void create_shouldValidateDates() {
        User owner = userRepository.save(User.builder().name("O2").email("o2@test.io").build());
        User booker = userRepository.save(User.builder().name("U2").email("u2@test.io").build());
        Item item = itemRepository.save(Item.builder()
                .name("Стремянка")
                .description("5 ступеней")
                .available(true)
                .owner(owner)
                .build());

        BookingCreateDto bad = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)
        );

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), bad));
    }
}
