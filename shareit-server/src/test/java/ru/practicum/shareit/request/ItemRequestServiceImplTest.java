package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ItemRequestServiceImplTest {

    ItemRequestRepository itemRequestRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;
    ItemRequestServiceImpl service;

    @BeforeEach
    void setup() {
        itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
        itemRepository = Mockito.mock(ItemRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        service = new ItemRequestServiceImpl(itemRequestRepository, itemRepository, userRepository);
    }

    @Test
    void create_ok() {
        User u = User.builder().id(1L).name("A").email("a@a").build();
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        Mockito.when(itemRequestRepository.save(any())).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequestDto result = service.create(1L, dto);

        assertEquals(10L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void create_blankDescription_throws() {
        User u = User.builder().id(1L).name("A").email("a@a").build();
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        ItemRequestCreateDto dto = new ItemRequestCreateDto("  ");
        assertThrows(ValidationException.class, () -> service.create(1L, dto));
    }

    @Test
    void getOwn_sortedDesc_andItemsAttached() {
        User u = User.builder().id(1L).name("A").email("a@a").build();
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));

        ItemRequest r1 = ItemRequest.builder().id(1L).description("A").requestor(u).created(LocalDateTime.now().minusHours(1)).build();
        ItemRequest r2 = ItemRequest.builder().id(2L).description("B").requestor(u).created(LocalDateTime.now()).build();

        Mockito.when(itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(1L))
                .thenReturn(List.of(r2, r1));

        Item i = Item.builder().id(100L).name("Дрель").owner(User.builder().id(9L).build()).request(r2).build();
        Mockito.when(itemRepository.findByRequest_IdIn(List.of(2L, 1L)))
                .thenReturn(List.of(i));

        List<ItemRequestDto> res = service.getOwn(1L);
        assertEquals(2, res.size());
        assertEquals(2L, res.get(0).getId());
        assertEquals(1, res.get(0).getItems().size());
        assertEquals(0, res.get(1).getItems().size());
    }

    @Test
    void getAll_excludesOwn_andSortedDesc_withPagination() {
        User u = User.builder().id(1L).name("A").email("a@a").build();
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));

        ItemRequest r1 = ItemRequest.builder().id(3L).description("X").requestor(User.builder().id(2L).build()).created(LocalDateTime.now().minusDays(1)).build();
        ItemRequest r2 = ItemRequest.builder().id(4L).description("Y").requestor(User.builder().id(3L).build()).created(LocalDateTime.now()).build();

        Mockito.when(itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(r2, r1)));

        Mockito.when(itemRepository.findByRequest_IdIn(List.of(4L, 3L))).thenReturn(List.of());

        List<ItemRequestDto> res = service.getAll(1L, 0, 10);
        assertEquals(2, res.size());
        assertEquals(4L, res.get(0).getId());
        assertEquals(3L, res.get(1).getId());
    }

    @Test
    void getById_ok_withItems() {
        User u = User.builder().id(1L).name("A").email("a@a").build();
        ItemRequest r = ItemRequest.builder().id(7L).description("Q").requestor(u).created(LocalDateTime.now()).build();

        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        Mockito.when(itemRequestRepository.findById(7L)).thenReturn(java.util.Optional.of(r));

        Item i = Item.builder().id(200L).name("Лестница").owner(User.builder().id(5L).build()).request(r).build();
        Mockito.when(itemRepository.findByRequest_Id(7L)).thenReturn(List.of(i));

        ItemRequestDto dto = service.getById(1L, 7L);
        assertEquals(7L, dto.getId());
        assertEquals(1, dto.getItems().size());
        assertEquals(200L, dto.getItems().get(0).getId());
        assertEquals(5L, dto.getItems().get(0).getOwnerId());
    }

    @Test
    void getById_notFound_throws() {
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(User.builder().id(1L).build()));
        Mockito.when(itemRequestRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(1L, 99L));
    }
}
