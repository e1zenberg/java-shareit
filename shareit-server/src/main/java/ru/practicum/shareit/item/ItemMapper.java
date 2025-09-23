package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

public final class ItemMapper {

    private ItemMapper() {
    }

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(requestId);
        return dto;
    }

    public static ItemDetailsDto toItemDetailsDto(Item item, List<CommentDto> comments) {
        if (item == null) return null;
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        return new ItemDetailsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                ownerId,
                requestId,
                safe(comments)
        );
    }

    public static ItemWithBookingsDto toItemWithBookings(
            Item item,
            BookingShortDto lastBooking,
            BookingShortDto nextBooking,
            List<CommentDto> comments
    ) {
        if (item == null) return null;
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                ownerId,
                requestId,
                lastBooking,
                nextBooking,
                safe(comments)
        );
    }

    private static <T> List<T> safe(List<T> src) {
        return src == null ? new ArrayList<>() : src;
    }
}
