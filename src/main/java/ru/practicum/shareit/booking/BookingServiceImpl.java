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
import ru.practicum.shareit.exception.ForbiddenException;
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
            throw new ValidationException("start and end must be provided");
        }
        if (dto.start() == null || dto.end() == null) {
            throw new ValidationException("start and end must be provided");
        }
        if (!dto.start().isBefore(dto.end())) {
            throw new ValidationException("start must be before end");
        }
        LocalDateTime now = LocalDateTime.now();
        if (dto.start().isBefore(now) || dto.end().isBefore(now)) {
            throw new ValidationException("start/end must be in the future");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + dto.itemId()));

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available for booking");
        }

        Booking booking = Booking.builder()
                .start(dto.start())
                .end(dto.end())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        Long itemOwnerId = booking.getItem().getOwner().getId();
        if (!itemOwnerId.equals(ownerId)) {
            throw new ForbiddenException("Only item owner can approve/reject booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Only WAITING booking can be changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
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

        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        Page<Booking> page;
        switch (state) {
            case CURRENT -> page = bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, pageRequest);
            case PAST -> page = bookingRepository.findByBooker_IdAndEndBefore(userId, now, pageRequest);
            case FUTURE -> page = bookingRepository.findByBooker_IdAndStartAfter(userId, now, pageRequest);
            case WAITING -> page = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, pageRequest);
            case REJECTED -> page = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, pageRequest);
            case ALL -> page = bookingRepository.findByBooker_Id(userId, pageRequest);
            default -> throw new ValidationException("Unknown state: " + state);
        }

        return page.getContent().stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> listForOwner(Long ownerId, BookingState state, int from, int size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));
        validatePage(from, size);

        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        Page<Booking> page;
        switch (state) {
            case CURRENT -> page = bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(ownerId, now, now, pageRequest);
            case PAST -> page = bookingRepository.findByItem_Owner_IdAndEndBefore(ownerId, now, pageRequest);
            case FUTURE -> page = bookingRepository.findByItem_Owner_IdAndStartAfter(ownerId, now, pageRequest);
            case WAITING -> page = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.WAITING, pageRequest);
            case REJECTED -> page = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.REJECTED, pageRequest);
            case ALL -> page = bookingRepository.findByItem_Owner_Id(ownerId, pageRequest);
            default -> throw new ValidationException("Unknown state: " + state);
        }

        return page.getContent().stream()
                .map(BookingMapper::toDto)
                .toList();
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
