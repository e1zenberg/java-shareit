package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Booking findFirstByItem_IdAndStartBeforeOrderByEndDesc(Long itemId, LocalDateTime now);
    Booking findFirstByItem_IdAndStartBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime now, Booking.BookingStatus status);
    Booking findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, Booking.BookingStatus status);

    boolean existsByBooker_IdAndItem_IdAndStatusAndEndBefore(Long bookerId, Long itemId,
                                                             Booking.BookingStatus status, LocalDateTime endBefore);

    Page<Booking> findByBooker_Id(Long bookerId, Pageable p);
    Page<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable p);
    Page<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime now, Pageable p);
    Page<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime now, Pageable p);
    Page<Booking> findByBooker_IdAndStatus(Long bookerId, Booking.BookingStatus status, Pageable p);

    Page<Booking> findByItem_Owner_Id(Long ownerId, Pageable p);
    Page<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime now1, LocalDateTime now2, Pageable p);
    Page<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime now, Pageable p);
    Page<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime now, Pageable p);
    Page<Booking> findByItem_Owner_IdAndStatus(Long ownerId, Booking.BookingStatus status, Pageable p);
}
