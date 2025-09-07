package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

/**
 * Сервис вещей с поддержкой комментариев и расчётом last/next бронирований.
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(final Long ownerId, final ItemDto dto) {
        validateNew(dto);
        final User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        final Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(final Long ownerId, final Long itemId, final ItemDto patch) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Only owner can edit the item");
        }
        if (StringUtils.hasText(patch.getName())) {
            item.setName(patch.getName());
        }
        if (StringUtils.hasText(patch.getDescription())) {
            item.setDescription(patch.getDescription());
        }
        if (patch.getAvailable() != null) {
            item.setAvailable(patch.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDetailsDto getById(final Long requesterId, final Long itemId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        final List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        // Для GET /items/{id} возвращаем детали и комментарии (last/next — в списке владельца).
        return ItemMapper.toItemDetailsDto(item, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getByOwner(final Long ownerId) {
        final List<Item> items = itemRepository
                .findByOwner_Id(ownerId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (items.isEmpty()) {
            return List.of();
        }

        final Map<Long, List<CommentDto>> commentsByItem = commentRepository
                .findByItem_IdIn(items.stream().map(Item::getId).toList()).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())));

        final LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(i -> {
                    final var last = BookingMapper.toShort(
                            bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(i.getId(), now));
                    final var next = BookingMapper.toShort(
                            bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                                    i.getId(), now, Booking.BookingStatus.APPROVED));
                    final var comments = commentsByItem.getOrDefault(i.getId(), List.of());
                    return ItemMapper.toItemWithBookingsDto(i, last, next, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(final String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return itemRepository.search(text, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(final Long userId, final Long itemId, final CommentCreateDto comment) {
        if (!StringUtils.hasText(comment.text())) {
            throw new ValidationException("comment text must not be blank");
        }
        final boolean mayComment = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, Booking.BookingStatus.APPROVED, LocalDateTime.now());
        if (!mayComment) {
            throw new ValidationException("Only users with finished approved booking can comment this item");
        }

        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        final User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        final Comment entity = Comment.builder()
                .text(comment.text())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return CommentMapper.toDto(commentRepository.save(entity));
    }

    private void validateNew(final ItemDto dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("description must not be blank");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("available must not be null");
        }
    }
}
