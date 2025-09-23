package ru.practicum.shareit.booking;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final RestTemplate restTemplate;

    public BookingController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange("/bookings", HttpMethod.POST, entity, Object.class);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable("bookingId") long bookingId,
                                                 @RequestParam(name = "approved") boolean approved) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String path = "/bookings/{bookingId}?approved={approved}";
        return restTemplate.exchange(path, HttpMethod.PATCH, entity, Object.class, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable("bookingId") long bookingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("/bookings/{bookingId}", HttpMethod.GET, entity, Object.class, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestParam(name = "from", defaultValue = "0") int from,
                                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String path = "/bookings?state={state}&from={from}&size={size}";
        return restTemplate.exchange(path, HttpMethod.GET, entity, Object.class, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestParam(name = "from", defaultValue = "0") int from,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String path = "/bookings/owner?state={state}&from={from}&size={size}";
        return restTemplate.exchange(path, HttpMethod.GET, entity, Object.class, state, from, size);
    }
}
