package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ItemServiceImplTest {

    ItemRepository itemRepository;
    CommentRepository commentRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;
    ItemRequestRepository itemRequestRepository;
    ItemServiceImpl service;

    @BeforeEach
    void setup() {
        itemRepository = Mockito.mock(ItemRepository.class);
        commentRepository = Mockito.mock(CommentRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
        service = new ItemServiceImpl(itemRepository, commentRepository, bookingRepository, userRepository, itemRequestRepository);
    }

    @Test
    void create_withoutRequestId_ok() {
        User owner = User.builder().id(1L).name("A").email("a@a").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenAnswer(inv -> {
            Item it = inv.getArgument(0);
            it.setId(10L);
            return it;
        });

        ItemDto dto = ItemDto.builder().name("Дрель").description("Ударная").available(true).build();
        ItemDto saved = service.create(1L, dto);

        assertEquals(10L, saved.getId());
        assertNull(saved.getRequestId());
    }

    @Test
    void create_withRequestId_ok() {
        User owner = User.builder().id(1L).name("A").email("a@a").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(7L)).thenReturn(Optional.of(ItemRequest.builder().id(7L).build()));
        when(itemRepository.save(any())).thenAnswer(inv -> {
            Item it = inv.getArgument(0);
            it.setId(11L);
            return it;
        });

        ItemDto dto = ItemDto.builder().name("Лобзик").description("По дереву").available(true).requestId(7L).build();
        ItemDto saved = service.create(1L, dto);

        assertEquals(11L, saved.getId());

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        Mockito.verify(itemRepository).save(captor.capture());
        Item persisted = captor.getValue();
        assertNotNull(persisted.getRequest());
        assertEquals(7L, persisted.getRequest().getId());
    }

    @Test
    void create_withRequestId_notFound_throws() {
        User owner = User.builder().id(1L).name("A").email("a@a").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        ItemDto dto = ItemDto.builder().name("Стремянка").description("2м").available(true).requestId(99L).build();
        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
    }
}
