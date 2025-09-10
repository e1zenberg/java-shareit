package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(final Long ownerId, final ItemDto dto) {
        validateNew(dto);

        final User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        final Item entity = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();

        final Item saved = itemRepository.save(entity);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(final Long ownerId, final Long itemId, final ItemDto patch) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Only owner can edit item");
        }

        if (patch != null) {
            if (StringUtils.hasText(patch.getName())) {
                item.setName(patch.getName());
            }
            if (patch.getDescription() != null) {
                if (!StringUtils.hasText(patch.getDescription())) {
                    throw new ValidationException("description must not be blank");
                }
                item.setDescription(patch.getDescription());
            }
            if (patch.getAvailable() != null) {
                item.setAvailable(patch.getAvailable());
            }
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDetailsDto getById(final Long requesterId, final Long itemId) {
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        final List<Comment> comments = commentRepository.findByItem_Id(itemId);

        // last/next — только для владельца в списке вещей; на карточке можно вернуть без них
        return ItemMapper.toItemDetailsDto(item, comments.stream().map(CommentMapper::toDto).toList());
    }

    @Override
    public List<ItemWithBookingsDto> getByOwner(final Long ownerId) {
        final List<Item> items = itemRepository
                .findByOwner_Id(ownerId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (items.isEmpty()) {
            return List.of();
        }

        final List<Long> itemIds = items.stream().map(Item::getId).toList();
        final Map<Long, List<CommentDto>> commentsByItem = commentRepository.findByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())));

        final LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(it -> {
                    final Booking last = bookingRepository
                            .findFirstByItem_IdAndStartBeforeOrderByEndDesc(it.getId(), now);
                    final Booking next = bookingRepository
                            .findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(it.getId(), now, Booking.BookingStatus.APPROVED);
                    return ItemMapper.toItemWithBookings(
                            it,
                            BookingMapper.toShort(last),
                            BookingMapper.toShort(next),
                            commentsByItem.getOrDefault(it.getId(), List.of())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(final String text) {
        if (!StringUtils.hasText(text)) {
            // тест "Item search empty" ожидает пустой список
            return List.of(); // 200 OK
        }
        return itemRepository.search(text, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(final Long userId, final Long itemId, final CommentCreateDto comment) {
        if (comment == null || !StringUtils.hasText(comment.text())) {
            throw new ValidationException("comment text must not be blank");
        }

        final boolean canComment = bookingRepository
                .existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                        userId, itemId, Booking.BookingStatus.APPROVED, LocalDateTime.now());

        if (!canComment) {
            // тест «Comment approved booking» ждёт 400, если бронь в будущем/не закончилась
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
        if (dto == null) {
            throw new ValidationException("Item payload must not be null");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("description must not be blank");
        }
        if (dto.getAvailable() == null) {
            // тест «Item create without available field» ожидает 400
            throw new ValidationException("available must not be null");
        }
    }
}
