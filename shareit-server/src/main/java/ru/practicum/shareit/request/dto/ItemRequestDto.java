package ru.practicum.shareit.request.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemShortDto> items;

    public ItemRequestDto() {
    }

    public ItemRequestDto(Long id, String description, LocalDateTime created, List<ItemShortDto> items) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = items;
    }

    public static class ItemShortDto {
        private Long id;
        private String name;
        private Long ownerId;

        public ItemShortDto() {
        }

        public ItemShortDto(Long id, String name, Long ownerId) {
            this.id = id;
            this.name = name;
            this.ownerId = ownerId;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public List<ItemShortDto> getItems() {
        return items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setItems(List<ItemShortDto> items) {
        this.items = items;
    }
}
