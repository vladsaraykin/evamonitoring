package com.evagroup.evamonitoring.telegram.entity;

import org.springframework.data.annotation.Id;

public record TelegramUser(@Id Long id, Long userId, String username, TelegramStatus status) {

    public TelegramUser(Long userId, String username, TelegramStatus status) {
        this(null, userId, username, status);
    }

    public TelegramUser(Long id, Long userId, String username, TelegramStatus status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.status = status;
    }
}
