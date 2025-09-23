package ru.practicum.shareit.item;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long ownerId, ItemDto dto) {
        return post("", ownerId, dto);
    }

    public ResponseEntity<Object> update(long ownerId, long itemId, ItemDto patch) {
        return patch("/" + itemId, ownerId, patch);
    }

    public ResponseEntity<Object> getById(long requesterId, long itemId) {
        return get("/" + itemId, requesterId);
    }

    public ResponseEntity<Object> getByOwner(long ownerId, Integer from, Integer size) {
        Map<String, Object> p = Map.of("from", from, "size", size);
        return get("?from={from}&size={size}", ownerId, p);
    }

    public ResponseEntity<Object> search(String text) {
        Map<String, Object> p = Map.of("text", text);
        return get("/search?text={text}", (Long) null, p);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentCreateDto comment) {
        return post("/" + itemId + "/comment", userId, comment);
    }
}
