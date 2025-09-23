package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Test
    void create_ok() throws Exception {
        ItemRequestCreateDto body = new ItemRequestCreateDto("Нужна стремянка");
        ItemRequestDto resp = new ItemRequestDto(1L, "Нужна стремянка", LocalDateTime.now(), List.of());
        Mockito.when(itemRequestService.create(anyLong(), any())).thenReturn(resp);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOwn_ok() throws Exception {
        Mockito.when(itemRequestService.getOwn(anyLong())).thenReturn(List.of());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ok() throws Exception {
        Mockito.when(itemRequestService.getAll(anyLong(), anyInt(), anyInt())).thenReturn(List.of());
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ok() throws Exception {
        ItemRequestDto resp = new ItemRequestDto(5L, "Q", LocalDateTime.now(), List.of());
        Mockito.when(itemRequestService.getById(anyLong(), eq(5L))).thenReturn(resp);

        mvc.perform(get("/requests/5").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
