package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Смоук-тесты REST по вещам под требования ТЗ.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private void createOwner(long id) throws Exception {
        // просто создаём пользователя (вернётся id=1)
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UserDto.builder().name("Owner").email("owner@ex.com").build())))
                .andExpect(status().isOk());
    }

    @Test
    void createGetUpdateAndListByOwner() throws Exception {
        createOwner(1L);

        // create item
        ItemDto dto = ItemDto.builder()
                .name("Дрель Салют")
                .description("Ударный режим, 600Вт")
                .available(true)
                .build();

        String resp = mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andReturn().getResponse().getContentAsString();

        ItemDto created = objectMapper.readValue(resp, ItemDto.class);

        // get by id
        mockMvc.perform(get("/items/{id}", created.getId()).header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", containsString("Дрель")));

        // patch: change availability
        mockMvc.perform(patch("/items/{id}", created.getId())
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));

        // owner list
        String listJson = mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        List<ItemDto> list = objectMapper.readValue(listJson, new TypeReference<>() {});
        assert list.size() == 1;
    }

    @Test
    void searchReturnsOnlyAvailableAndIsCaseInsensitive() throws Exception {
        createOwner(1L);

        // available item
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ItemDto.builder().name("Перфоратор").description("бетОн").available(true).build())))
                .andExpect(status().isOk());

        // unavailable item
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ItemDto.builder().name("Сверло").description("для бетона").available(false).build())))
                .andExpect(status().isOk());

        // search "БЕТОН" (разный регистр)
        mockMvc.perform(get("/items/search").param("text", "БЕТОН"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Перфоратор")));

        // empty text -> empty list
        mockMvc.perform(get("/items/search").param("text", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
