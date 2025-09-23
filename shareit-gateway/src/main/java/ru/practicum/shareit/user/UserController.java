package ru.practicum.shareit.user;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final RestTemplate restTemplate;

    public UserController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody Map<String, Object> requestBody) {
        return restTemplate.postForEntity("/users", requestBody, Object.class);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable("userId") long userId,
                                             @RequestBody Map<String, Object> requestBody) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);
        return restTemplate.exchange("/users/{userId}", HttpMethod.PATCH, entity, Object.class, userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable("userId") long userId) {
        return restTemplate.getForEntity("/users/{userId}", Object.class, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable("userId") long userId) {
        ResponseEntity<Void> response = restTemplate.exchange("/users/{userId}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, userId);
        return ResponseEntity.status(response.getStatusCode()).build();
    }
}
