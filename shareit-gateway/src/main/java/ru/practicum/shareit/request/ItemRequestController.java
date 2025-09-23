package ru.practicum.shareit.request;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Profile("disabled")
@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final RestTemplate restTemplate;

    public ItemRequestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange("/requests", HttpMethod.POST, entity, Object.class);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader("X-Sharer-User-Id") long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("/requests", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String path = "/requests/all?from={from}&size={size}";
        return restTemplate.exchange(path, HttpMethod.GET, entity, Object.class, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable("requestId") long requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("/requests/{requestId}", HttpMethod.GET, entity, Object.class, requestId);
    }
}
