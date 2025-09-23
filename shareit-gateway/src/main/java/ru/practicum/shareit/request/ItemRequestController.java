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

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String REQUESTS_PATH = "/requests";

    private final RestTemplate restTemplate;

    public ItemRequestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                                @RequestBody Map<String, Object> body) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersWithUserId(userId));
        return restTemplate.exchange(REQUESTS_PATH, HttpMethod.POST, entity, Object.class);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID_HEADER) long userId) {
        HttpEntity<Void> entity = new HttpEntity<>(headersWithUserId(userId));
        return restTemplate.exchange(REQUESTS_PATH, HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) long userId,
                                         @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        HttpEntity<Void> entity = new HttpEntity<>(headersWithUserId(userId));
        String path = REQUESTS_PATH + "/all?from={from}&size={size}";
        return restTemplate.exchange(path, HttpMethod.GET, entity, Object.class, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) long userId,
                                          @PathVariable("requestId") long requestId) {
        HttpEntity<Void> entity = new HttpEntity<>(headersWithUserId(userId));
        return restTemplate.exchange(REQUESTS_PATH + "/{requestId}", HttpMethod.GET, entity, Object.class, requestId);
    }

    private HttpHeaders headersWithUserId(long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(USER_ID_HEADER, Long.toString(userId));
        return headers;
    }
}
