package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookingCreateDto dto) {
        if (dto == null) {
            throw new ValidationException("Booking payload must not be null");
        }
        if (dto.start() == null || dto.end() == null) {
            throw new ValidationException("start and end must be provided");
        }
        if (!dto.start().isBefore(dto.end())) {
            throw new ValidationException("start must be before end");
        }
        final LocalDateTime now = LocalDateTime.now();
        if (dto.start().isBefore(now) || dto.end().isBefore(now)) {
            throw new ValidationException("start/end must be in the future");
        }

        final User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        final Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + dto.itemId()));

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)) {
            // по ТЗ бронировать свою вещь нельзя
            throw new NotFoundException("Owner cannot book own item");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available for booking");
        }

        final Booking entity = Booking.builder()
                .start(dto.start())
                .end(dto.end())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(entity));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        final Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        final Long itemOwnerId = booking.getItem().getOwner().getId();
        if (!itemOwnerId.equals(ownerId)) {
            throw new NotFoundException("Only item owner can approve/reject booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Only WAITING booking can be changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto get(Long userId, Long bookingId) {
        final Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        final Long ownerId = booking.getItem().getOwner().getId();
        final Long bookerId = booking.getBooker().getId();
        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new NotFoundException("Booking accessible only to booker or item owner");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> listForUser(Long userId, BookingState state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        validatePage(from, size);

        final LocalDateTime now = LocalDateTime.now();
        final PageRequest pageRequest = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        final Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, pageRequest);
            case CURRENT -> bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, pageRequest);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, pageRequest);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, pageRequest);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, pageRequest);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, pageRequest);
        };
        return page.getContent().stream().map(BookingMapper::toDto).toList();
    }

    @Override
    public List<BookingDto> listForOwner(Long ownerId, BookingState state, int from, int size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));
        validatePage(from, size);

        final LocalDateTime now = LocalDateTime.now();
        final PageRequest pageRequest = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        final Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(ownerId, pageRequest);
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(ownerId, now, now, pageRequest);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBefore(ownerId, now, pageRequest);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfter(ownerId, now, pageRequest);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.WAITING, pageRequest);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.REJECTED, pageRequest);
        };
        return page.getContent().stream().map(BookingMapper::toDto).toList();
    }

    private void validatePage(int from, int size) {
        if (from < 0) {
            throw new ValidationException("'from' must be >= 0");
        }
        if (size <= 0) {
            throw new ValidationException("'size' must be > 0");
        }
    }
}
