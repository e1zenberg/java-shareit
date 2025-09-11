package ru.practicum.shareit.item;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

/**
 * Web-slice тест ItemController:
 *  - поднимается только MVC-слой (без БД, без schema.sql)
 *  - сервис замокан
 */
@WebMvcTest(controllers = ItemController.class)
@ActiveProfiles("test")
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("POST /items — создаёт вещь и возвращает DTO")
    void create_shouldReturnItemDto() throws Exception {
        ItemDto payload = ItemDto.builder()
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .build();

        ItemDto response = ItemDto.builder()
                .id(100L)
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .build();

        Mockito.when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(response);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @DisplayName("PATCH /items/{id} — частичное обновление вещи")
    void update_shouldReturnItemDto() throws Exception {
        long itemId = 55L;

        ItemDto patch = ItemDto.builder()
                .name("Дрель PRO")
                .build();

        ItemDto updated = ItemDto.builder()
                .id(itemId)
                .name("Дрель PRO")
                .description("Ударная")
                .available(true)
                .build();

        Mockito.when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updated);

        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) itemId)))
                .andExpect(jsonPath("$.name", is("Дрель PRO")));
    }

    @Test
    @DisplayName("GET /items/{id} — возвращает детали вещи")
    void getById_shouldReturnDetailsDto() throws Exception {
        long itemId = 77L;

        // Комментарий: (id, text, authorName, created)
        CommentDto c1 = new CommentDto(1L, "Отличный!", "Иван", (LocalDateTime) null);

        // ItemDetailsDto: (id, name, description, available, ownerId, requestId, comments)
        ItemDetailsDto details = new ItemDetailsDto(
                itemId,
                "Лобзик",
                "Для дерева",
                Boolean.TRUE,
                2L,                // ownerId
                null,              // requestId (может быть null)
                List.of(c1)        // comments
        );

        Mockito.when(itemService.getById(anyLong(), anyLong())).thenReturn(details);

        mvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) itemId)))
                .andExpect(jsonPath("$.name", is("Лобзик")))
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @Test
    @DisplayName("GET /items — список вещей владельца")
    void getByOwner_shouldReturnListWithBookings() throws Exception {
        // BookingShortDto обычно: (id, bookerId) — нам сейчас не нужен, ставим null
        BookingShortDto last = null;
        BookingShortDto next = null;

        // ItemWithBookingsDto:
        // (id, name, description, available, ownerId, requestId, lastBooking, nextBooking, comments)
        ItemWithBookingsDto it = new ItemWithBookingsDto(
                5L,
                "Стремянка",
                "5 ступеней",
                Boolean.TRUE,
                10L,                           // ownerId
                null,                          // requestId
                last,
                next,
                List.<CommentDto>of()          // важно: явно типизируем
        );

        Mockito.when(itemService.getByOwner(anyLong())).thenReturn(List.of(it));

        mvc.perform(get("/items")
                        .header(USER_HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(5)));
    }

    @Test
    @DisplayName("POST /items/{id}/comment — добавляет комментарий")
    void addComment_shouldReturnCommentDto() throws Exception {
        long itemId = 321L;

        CommentCreateDto payload = new CommentCreateDto("Топовая вещь!");
        CommentDto response = new CommentDto(9L, "Топовая вещь!", "Пётр", (LocalDateTime) null);

        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(response);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9)))
                .andExpect(jsonPath("$.text", is("Топовая вещь!")));
    }
}
