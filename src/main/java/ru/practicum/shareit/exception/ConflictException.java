package ru.practicum.shareit.exception;

/** 409 Conflict (например, при дубликате email). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
