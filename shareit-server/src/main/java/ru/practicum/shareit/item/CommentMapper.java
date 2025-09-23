package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public final class CommentMapper {
    private CommentMapper() {

    }

    public static CommentDto toDto(Comment c) {
        return new CommentDto(
                c.getId(),
                c.getText(),
                c.getAuthor() != null ? c.getAuthor().getName() : null,
                c.getCreated()
        );
    }
}
