package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * Модель вещи.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "items")
public class Item {
    /** уникальный идентификатор вещи */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** краткое название */
    @Column(nullable = false)
    private String name;

    /** развёрнутое описание */
    @Column(nullable = false)
    private String description;

    /** доступна ли вещь для аренды */
    @Column(name = "is_available", nullable = false)
    private Boolean available;

    /** владелец вещи */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** ссылка на запрос, по которому была создана вещь (может отсутствовать) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}
