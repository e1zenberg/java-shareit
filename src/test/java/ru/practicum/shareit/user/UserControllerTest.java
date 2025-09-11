package ru.practicum.shareit.user;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Web-slice тест контроллера пользователей.
 * Контекст поднимаем без БД, сервис замокаем.
 */
@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /users — создаёт пользователя и отдаёт DTO")
    void create_shouldReturnDto() throws Exception {
        User input = User.builder().name("Alice").email("a@ex.io").build();
        User saved = User.builder().id(1L).name("Alice").email("a@ex.io").build();
        Mockito.when(userService.create(any(User.class))).thenReturn(saved);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserMapper.toDto(input))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("a@ex.io")));
    }

    @Test
    @DisplayName("PATCH /users/{id} — обновляет частично и отдаёт DTO")
    void update_shouldReturnDto() throws Exception {
        Long id = 10L;
        User patch = User.builder().name("Bob").build();
        User updated = User.builder().id(id).name("Bob").email("b@ex.io").build();
        Mockito.when(userService.update(eq(id), any(User.class))).thenReturn(updated);

        mvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserMapper.toDto(patch))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Bob")));
    }

    @Test
    @DisplayName("GET /users/{id} — возвращает DTO пользователя")
    void getById_shouldReturnDto() throws Exception {
        Long id = 5L;
        User user = User.builder().id(id).name("Carol").email("c@ex.io").build();
        Mockito.when(userService.getById(id)).thenReturn(user);

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.email", is("c@ex.io")));
    }

    @Test
    @DisplayName("GET /users — возвращает список DTO")
    void getAll_shouldReturnList() throws Exception {
        List<User> list = List.of(
                User.builder().id(1L).name("A").email("a@ex.io").build(),
                User.builder().id(2L).name("B").email("b@ex.io").build()
        );
        Mockito.when(userService.getAll()).thenReturn(list);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].email", is("b@ex.io")));
    }

    @Test
    @DisplayName("DELETE /users/{id} — делегирует удаление в сервис")
    void delete_shouldDelegateToService() throws Exception {
        Long id = 9L;

        mvc.perform(delete("/users/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(userService).deleteById(id);
    }
}
