package ru.practicum.shareit.request;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        if (dto == null) {
            throw new ValidationException("Request payload must not be null");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("description must not be blank");
        }
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest entity = ItemRequestMapper.toEntity(dto, requestor);
        entity = itemRequestRepository.save(entity);
        return ItemRequestMapper.toDto(entity, new ArrayList<>());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        ensureUserExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        Map<Long, List<Item>> itemsByRequest = loadItemsForRequests(requests);
        return requests.stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(r -> ItemRequestMapper.toDto(r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, Integer from, Integer size) {
        ensureUserExists(userId);
        int page = from != null && size != null && size > 0 ? from / size : 0;
        int pageSize = size != null && size > 0 ? size : 10;
        List<ItemRequest> requests = itemRequestRepository
                .findByRequestor_IdNotOrderByCreatedDesc(userId, PageRequest.of(page, pageSize))
                .getContent();
        Map<Long, List<Item>> itemsByRequest = loadItemsForRequests(requests);
        return requests.stream()
                .map(r -> ItemRequestMapper.toDto(r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        ensureUserExists(userId);
        ItemRequest r = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        List<Item> items = itemRepository.findByRequest_Id(requestId);
        return ItemRequestMapper.toDto(r, items);
    }

    private void ensureUserExists(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private Map<Long, List<Item>> loadItemsForRequests(List<ItemRequest> requests) {
        List<Long> ids = requests.stream().map(ItemRequest::getId).toList();
        List<Item> items = ids.isEmpty() ? List.of() : itemRepository.findByRequest_IdIn(ids);
        return items.stream().collect(Collectors.groupingBy(i -> i.getRequest() != null ? i.getRequest().getId() : null));
    }
}
