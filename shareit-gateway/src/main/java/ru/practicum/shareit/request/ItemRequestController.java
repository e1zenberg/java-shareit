package ru.practicum.shareit.request;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final RestTemplate restTemplate;

    public ItemRequestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange("/requests", HttpMethod.POST, entity, Object.class);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("/requests", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") int from,
                                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String path = "/requests/all?from={from}&size={size}";
        return restTemplate.exchange(path, HttpMethod.GET, entity, Object.class, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable("requestId") long requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("/requests/{requestId}", HttpMethod.GET, entity, Object.class, requestId);
    }
}
