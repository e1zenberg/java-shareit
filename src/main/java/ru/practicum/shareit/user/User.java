package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Простейшая модель пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** отображаемое имя/логин */
    @Column(nullable = false)
    private String name;
    /** адрес электронной почты (уникальный в системе) */
    @Column(nullable = false, length = 512, unique = true)
    private String email;
}
