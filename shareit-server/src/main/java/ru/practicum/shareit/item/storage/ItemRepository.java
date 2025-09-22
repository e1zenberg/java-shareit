package ru.practicum.shareit.item.storage;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByOwner_Id(Long ownerId, Pageable pageable);

    @Query("""
        select i from Item i
        where i.available = true and (
            upper(i.name) like upper(concat('%', ?1, '%')) or
            upper(i.description) like upper(concat('%', ?1, '%'))
        )
    """)
    Page<Item> search(String text, Pageable pageable);

    List<Item> findByRequest_Id(Long requestId);

    List<Item> findByRequest_IdIn(List<Long> requestIds);
}
