package ru.practicum.shareit.request;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long requestorId);

    Page<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long userId, Pageable pageable);
}
